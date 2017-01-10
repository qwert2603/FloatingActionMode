package com.qwert2603.floating_action_mode

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.annotation.FloatRange
import android.support.annotation.LayoutRes
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.qwert2603.floating_action_mode.Utils.centerX
import com.qwert2603.floating_action_mode.Utils.parentHeight
import com.qwert2603.floating_action_mode.Utils.setEnabledWithDescendants
import kotlinx.android.synthetic.main.floating_action_mode.view.*

/**
 * Floating action mode that shows layout given to it.
 * Can be dragged over screen and swiped-to-dismiss.
 */
@CoordinatorLayout.DefaultBehavior(FloatingActionMode.FloatingActionModeBehavior::class)
open class FloatingActionMode @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : RelativeLayout(context, attrs, defStyleAttr) {

    var opened: Boolean = false

    /**
     * Top offset of actionMode calculated by [FloatingActionModeBehavior] as [AppBarLayout.getHeight].
     */
    var topOffset: Int = 0
        set(value) {
            if (value != field) {
                field = value
                if (isInEditMode) {
                    offsetTopAndBottom(calculateArrangeTranslationY().toInt())
                    return
                }
                arrangeY()
            }
        }

    /**
     * Bottom offset of actionMode calculated by [FloatingActionModeBehavior] as [Snackbar.SnackbarLayout.getHeight] - [Snackbar.SnackbarLayout.getTranslationY].
     */
    var bottomOffset: Int = 0
        set(value) {
            if (value != field) {
                field = value
                if (isInEditMode) {
                    offsetTopAndBottom(calculateArrangeTranslationY().toInt())
                    return
                }
                arrangeY()
            }
        }

    @LayoutRes
    open var contentRes: Int = 0
        set(value) {
            field = value

            if (childCount > 2) {
                removeViewAt(2)
            }

            if (field != 0) {
                LayoutInflater.from(context).inflate(field, this, true)
            }
        }

    open var canClose: Boolean = true
        set(value) {
            field = value
            close_button.visibility = if (field) View.VISIBLE else View.GONE
        }

    @DrawableRes
    open var closeIconRes: Int = R.drawable.ic_close_white_24dp
        set(value) {
            field = value
            if (field != 0) {
                close_button.setImageResource(field)
            } else {
                close_button.setImageDrawable(null)
            }
        }

    var maximized: Boolean = true

    var maximizeTranslationY: Float = 0f
        set(value) {
            if (value != field) {
                field = value
                arrangeY()
            }
        }

    open var canDrag: Boolean = true
        set(value) {
            field = value
            drag_button.visibility = if (field) View.VISIBLE else View.GONE
        }

    @DrawableRes
    open var dragIconRes: Int = R.drawable.ic_drag_white_24dp
        set(value) {
            field = value
            if (field != 0) {
                drag_button.setImageResource(field)
            } else {
                drag_button.setImageDrawable(null)
            }
        }

    var canDismiss: Boolean = true

    /**
     * Set threshold for actionMode dismissal.
     * If ([getTranslationX]/[getWidth]) > [dismissThreshold] actionMode will be dismissed after user stops touching [drag_button].
     */
    @FloatRange(from = 0.0, to = 1.0)
    var dismissThreshold: Float = 0.4f

    interface OnCloseListener {
        fun onClose()
    }

    var onCloseListener: OnCloseListener? = null

    enum class MinimizeDirection {
        NONE,
        TOP,
        BOTTOM,
        NEAREST
    }

    var minimizeDirection = MinimizeDirection.NEAREST

    var animationDuration: Long = 400L

    init {
        if (!isInEditMode) {
            visibility = View.INVISIBLE
        }

        LayoutInflater.from(context).inflate(R.layout.floating_action_mode, this, true)

        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionMode)
            try {
                opened = typedArray.getBoolean(R.styleable.FloatingActionMode_opened, false)
                contentRes = typedArray.getResourceId(R.styleable.FloatingActionMode_content_res, 0)
                canClose = typedArray.getBoolean(R.styleable.FloatingActionMode_can_close, true)
                closeIconRes = typedArray.getResourceId(R.styleable.FloatingActionMode_close_icon, R.drawable.ic_close_white_24dp)
                canDrag = typedArray.getBoolean(R.styleable.FloatingActionMode_can_drag, true)
                dragIconRes = typedArray.getResourceId(R.styleable.FloatingActionMode_drag_icon, R.drawable.ic_drag_white_24dp)
                canDismiss = typedArray.getBoolean(R.styleable.FloatingActionMode_can_dismiss, true)
                dismissThreshold = typedArray.getFloat(R.styleable.FloatingActionMode_dismiss_threshold, 0.4f)
                val md = typedArray.getInteger(R.styleable.FloatingActionMode_minimize_direction, -1)
                if (md > 0) minimizeDirection = MinimizeDirection.values()[md]
                animationDuration = typedArray.getInteger(R.styleable.FloatingActionMode_animation_duration, 400).toLong()
            } finally {
                typedArray.recycle()
            }
        }

        close_button.setOnClickListener { close() }

        drag_button.setOnTouchListener(object : OnTouchListener {
            var prevTransitionY = 0f
            var startRawX = 0f
            var startRawY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (!this@FloatingActionMode.canDrag) {
                    return false
                }

                val fractionX = Math.abs(event.rawX - startRawX) / this@FloatingActionMode.width

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        this@FloatingActionMode.drag_button.isPressed = true
                        startRawX = event.rawX
                        startRawY = event.rawY
                        prevTransitionY = this@FloatingActionMode.translationY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        this@FloatingActionMode.maximizeTranslationY = prevTransitionY + event.rawY - startRawY
                        translationX = event.rawX - startRawX
                        if (canDismiss) {
                            val alpha = if (fractionX < dismissThreshold) 1.0f else Math.pow(1.0 - (fractionX - dismissThreshold) / (1 - dismissThreshold), 4.0).toFloat()
                            this@FloatingActionMode.alpha = alpha
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        drag_button.isPressed = false
                        this@FloatingActionMode.animate().translationX(0f).duration = animationDuration
                        if (canDismiss && fractionX > dismissThreshold) {
                            this@FloatingActionMode.close()
                        }
                    }
                }
                return true
            }
        })

        if (opened) {
            openWithoutAnimation()
        }
    }

    private fun openWithoutAnimation() {
        visibility = View.VISIBLE
        maximize(false)
    }

    private fun arrangeY() {
        if (!maximized) {
            animate().translationY(calculateMinimizeTranslationY()).duration = animationDuration
            return
        }
        translationY = calculateArrangeTranslationY()
    }

    private fun calculateArrangeTranslationY(): Float {
        if (!maximized) {
            return calculateMinimizeTranslationY()
        }
        var tY = maximizeTranslationY
        var topMargin = 0
        var bottomMargin = 0
        val lp = layoutParams
        if (lp is MarginLayoutParams) {
            topMargin = lp.topMargin
            bottomMargin = lp.bottomMargin
        }
        val topOver = (topOffset + topMargin) - (top + tY)
        if (topOver > 0) {
            tY += topOver
        }
        val bottomOver = (bottom + tY) - (parentHeight() - bottomOffset - bottomMargin)
        if (bottomOver > 0) {
            tY -= bottomOver
        }
        return tY
    }

    open fun open() {
        if (opened) {
            return
        }
        opened = true
        visibility = View.VISIBLE
        maximizeTranslationY = 0f
        translationY = 0f
        minimize(false)
        maximize(true)
    }

    open fun close() {
        onCloseListener?.onClose()
        minimize(true)
        Utils.runOnUI(animationDuration * 2) {
            opened = false
            visibility = View.INVISIBLE
        }
    }

    open fun maximize(animate: Boolean) {
        if (!opened) {
            return
        }
        maximized = true
        setEnabledWithDescendants(true)
        val function = {
            scaleY = 1f
            scaleX = 1f
            translationY = calculateArrangeTranslationY()
            alpha = 1f
        }
        if (animate) {
            animate().scaleY(1f).scaleX(1f).translationY(calculateArrangeTranslationY()).alpha(1f)
                    .duration = animationDuration
        } else {
            function()
        }
    }

    open fun minimize(animate: Boolean) {
        if (!opened) {
            return
        }
        maximized = false
        setEnabledWithDescendants(false)
        val function = {
            scaleY = 0.5f
            scaleX = 0.5f
            translationY = calculateMinimizeTranslationY()
            alpha = 0.5f
        }
        if (animate) {
            animate().scaleY(0.5f).scaleX(0.5f).translationY(calculateMinimizeTranslationY()).alpha(0.5f)
                    .duration = animationDuration
        } else {
            function()
        }
    }

    private fun calculateMinimizeTranslationY() = when (minimizeDirection) {
        MinimizeDirection.TOP -> calculateMinimizeTranslationYTop()
        MinimizeDirection.BOTTOM -> calculateMinimizeTranslationYBottom()
        MinimizeDirection.NEAREST -> if (isInTopHalfOfParent()) calculateMinimizeTranslationYTop() else calculateMinimizeTranslationYBottom()
        MinimizeDirection.NONE -> 0f
    }

    private fun calculateMinimizeTranslationYTop() = (-top + topOffset).toFloat() - height * 0.25f

    private fun calculateMinimizeTranslationYBottom() = parentHeight() - bottomOffset - bottom + height * 0.25f

    private fun isInTopHalfOfParent() = (centerX() + translationY < parentHeight() / 2)

    open class FloatingActionModeBehavior @JvmOverloads constructor(context: Context? = null, attrs: AttributeSet? = null) : CoordinatorLayout.Behavior<FloatingActionMode>(context, attrs) {

        override fun layoutDependsOn(parent: CoordinatorLayout?, child: FloatingActionMode?, dependency: View?): Boolean {
            return dependency is AppBarLayout || dependency is Snackbar.SnackbarLayout
        }

        override fun onDependentViewChanged(parent: CoordinatorLayout, child: FloatingActionMode, dependency: View): Boolean {
            when (dependency) {
                is AppBarLayout -> child.topOffset = dependency.bottom
                is Snackbar.SnackbarLayout -> child.bottomOffset = dependency.height - dependency.translationY.toInt()
            }
            return false
        }

        override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout?, child: FloatingActionMode?, directTargetChild: View?, target: View?, nestedScrollAxes: Int): Boolean {
            return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
        }

        override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionMode, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)

            // FAM should not react to scroll its children.
            var parent = target.parent
            while (parent != coordinatorLayout) {
                if (parent == child) {
                    return
                }
                parent = target.parent
            }

            if (dyConsumed > 0) {
                child.minimize(true)
            } else if (dyConsumed < 0) {
                child.maximize(true)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())

        bundle.putBoolean(OPENED_KEY, opened)
        bundle.putInt(CONTENT_RES_KEY, contentRes)
        bundle.putBoolean(CAN_CLOSE_KEY, canClose)
        bundle.putInt(CLOSE_ICON_RES_KEY, closeIconRes)
        bundle.putBoolean(CAN_DRAG_KEY, canDrag)
        bundle.putInt(DRAG_ICON_RES_KEY, dragIconRes)
        bundle.putBoolean(CAN_DISMISS_KEY, canDismiss)
        bundle.putFloat(DISMISS_THRESHOLD_KEY, dismissThreshold)
        bundle.putInt(MINIMIZE_DIRECTION_KEY, minimizeDirection.ordinal)
        bundle.putLong(ANIMATION_DURATION_KEY, animationDuration)

        return bundle
    }

    override fun onRestoreInstanceState(parcelable: Parcelable) {
        if (parcelable is Bundle) {
            opened = parcelable.getBoolean(OPENED_KEY)
            contentRes = parcelable.getInt(CONTENT_RES_KEY)
            canClose = parcelable.getBoolean(CAN_CLOSE_KEY)
            closeIconRes = parcelable.getInt(CLOSE_ICON_RES_KEY)
            canDrag = parcelable.getBoolean(CAN_DRAG_KEY)
            dragIconRes = parcelable.getInt(DRAG_ICON_RES_KEY)
            canDismiss = parcelable.getBoolean(CAN_DISMISS_KEY)
            dismissThreshold = parcelable.getFloat(DISMISS_THRESHOLD_KEY)
            minimizeDirection = MinimizeDirection.values()[parcelable.getInt(MINIMIZE_DIRECTION_KEY)]
            animationDuration = parcelable.getLong(ANIMATION_DURATION_KEY)

            val state = parcelable.getParcelable<Parcelable>(SUPER_STATE_KEY)
            super.onRestoreInstanceState(state)
        } else {
            super.onRestoreInstanceState(parcelable)
        }

        if (opened) {
            openWithoutAnimation()
        }
    }

    companion object {
        private val SUPER_STATE_KEY = "com.qwert2603.floating_action_mode.SUPER_STATE_KEY"

        private val OPENED_KEY = "com.qwert2603.floating_action_mode.OPENED_KEY"
        private val CONTENT_RES_KEY = "com.qwert2603.floating_action_mode.CONTENT_RES_KEY"
        private val CAN_CLOSE_KEY = "com.qwert2603.floating_action_mode.CAN_CLOSE_KEY"
        private val CLOSE_ICON_RES_KEY = "com.qwert2603.floating_action_mode.CLOSE_ICON_RES_KEY"
        private val CAN_DRAG_KEY = "com.qwert2603.floating_action_mode.CAN_DRAG_KEY"
        private val DRAG_ICON_RES_KEY = "com.qwert2603.floating_action_mode.DRAG_ICON_RES_KEY"
        private val CAN_DISMISS_KEY = "com.qwert2603.floating_action_mode.CAN_DISMISS_KEY"
        private val DISMISS_THRESHOLD_KEY = "com.qwert2603.floating_action_mode.DISMISS_THRESHOLD_KEY"
        private val MINIMIZE_DIRECTION_KEY = "com.qwert2603.floating_action_mode.MINIMIZE_DIRECTION_KEY"
        private val ANIMATION_DURATION_KEY = "com.qwert2603.floating_action_mode.ANIMATION_DURATION_KEY"

    }
}
