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
import android.view.ViewGroup
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.floating_action_mode.view.*

/**
 * Floating action mode that shows layout given to it.
 * Can be dragged over screen and swiped-to-dismiss.
 */
@CoordinatorLayout.DefaultBehavior(FloatingActionMode.FloatingActionModeBehavior::class)
class FloatingActionMode : LinearLayout {

    enum class HideDirection {
        NONE,
        TOP,
        BOTTOM,
        NEAREST
    }

    @DrawableRes
    private var dragIconRes = R.drawable.ic_drag_white_24dp
        set(dragIcon) {
            this.dragIconRes = dragIcon
            drag_button.setImageResource(this.dragIconRes)
        }

    interface OnActionModeDismissListener {
        fun onActionModeDismiss()
    }

    var onActionModeDismissListener: OnActionModeDismissListener? = null

    var canDismiss = true
    var canDrag = true

    /**
     * Set threshold from than actionMode will be dismissed.
     * If ([getTranslationX]/[getWidth]) > [dismissThreshold] actionMode will be dismissed after user stops touching [drag_button].
     */
    @FloatRange(from = 0.0, to = 1.0)
    var dismissThreshold = 0.4f

    @LayoutRes
    private var mContentRes = 0
        set(actionModeContentRes) {
            mContentRes = actionModeContentRes

            if (childCount > 1) {
                removeViewAt(1)
            }

            LayoutInflater.from(context).inflate(mContentRes, this, true)
        }

    var animationDuration = 400L

    var hideDirection = HideDirection.NEAREST

    /**
     * Top offset of actionMode calculated by [FloatingActionModeBehavior] as [AppBarLayout.getHeight].
     */
    private var topOffset: Int = 0

    private var dragTranslationY = 0f

    private var opened = false

    constructor(context: Context) : super(context) {
        LogUtils.d("public FloatingActionMode(Context $context) {")
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        if (!isInEditMode) {
            LogUtils.d("public FloatingActionMode(Context $context, @Nullable AttributeSet $attrs) {")
        }

        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FloatingActionMode)
            canDismiss = typedArray.getBoolean(R.styleable.FloatingActionMode_can_dismiss, true)
            dismissThreshold = typedArray.getFloat(R.styleable.FloatingActionMode_dismiss_threshold, 0.4f)
            canDrag = typedArray.getBoolean(R.styleable.FloatingActionMode_can_drag, true)
            dragIconRes = typedArray.getResourceId(R.styleable.FloatingActionMode_drag_icon, R.drawable.ic_drag_white_24dp)
            mContentRes = typedArray.getResourceId(R.styleable.FloatingActionMode_content_res, 0)
            animationDuration = typedArray.getInteger(R.styleable.FloatingActionMode_animation_duration, 400).toLong()
            val hd = typedArray.getInteger(R.styleable.FloatingActionMode_hide_direction, -1)
            if (hd > 0) hideDirection = HideDirection.values()[hd]
            typedArray.recycle()
        }

        init(context)
    }

    private fun init(context: Context) {
        if (!isInEditMode) {
            visibility = View.INVISIBLE
        }

        LayoutInflater.from(context).inflate(R.layout.floating_action_mode, this, true)
        orientation = LinearLayout.HORIZONTAL
//        gravity = Gravity.CENTER_VERTICAL todo ?????????

        drag_button.setOnTouchListener(object : View.OnTouchListener {
            internal var prevTransitionY = 0f
            internal var startRawX = 0f
            internal var startRawY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (!canDrag) {
                    return false
                }

                val fractionX = Math.abs(event.rawX - startRawX) / this@FloatingActionMode.width
                LogUtils.d("fractionX == " + fractionX)

                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        drag_button.isPressed = true
                        startRawX = event.rawX
                        startRawY = event.rawY
                        prevTransitionY = this@FloatingActionMode.translationY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        dragTranslationY = prevTransitionY + event.rawY - startRawY
                        translationY = dragTranslationY
                        translationX = event.rawX - startRawX
                        if (canDismiss) {
                            val alpha = if (fractionX < dismissThreshold) 1.0f else 1 - (fractionX - dismissThreshold) / (1 - dismissThreshold)
                            setAlpha(alpha)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        drag_button.isPressed = false
                        this@FloatingActionMode.animate().translationX(0f)
                        if (canDismiss && fractionX > dismissThreshold) {
                            onActionModeDismissListener?.onActionModeDismiss()
                        }
                    }
                }
                return true
            }
        })

        drag_button.visibility = if (canDrag) VISIBLE else INVISIBLE
        drag_button.setImageResource(dragIconRes)

        if (opened) {
            open(false)
        }
    }

    private fun open(animate: Boolean) {
        opened = true
        if (animate) {
            minimize(false)
            maximize(animate)
        } else {
            visibility = VISIBLE
        }
    }

    private fun close(animate: Boolean) {
        opened = false
        if (animate) {
            minimize(true)
            AndroidUtils.runOnUI(Runnable { visibility = View.GONE }, animationDuration)
        } else {
            visibility = GONE
        }
    }

    private fun maximize(animate: Boolean) {
        AndroidUtils.setViewEnabled(this@FloatingActionMode, true)
        visibility = VISIBLE
        animate().scaleY(1f).scaleX(1f).translationY(dragTranslationY).alpha(1f).duration = (if (animate) animationDuration else 10/*todo: is THAT works?*/)
    }

    private fun minimize(animate: Boolean) {
        AndroidUtils.setViewEnabled(this@FloatingActionMode, false)
        visibility = VISIBLE
        pivotY = 0f
        pivotX = (width / 2).toFloat()
        animate().scaleY(0.5f).scaleX(0.5f).translationY(calculateMinimizeTranslationY()).alpha(0.5f).duration = (if (animate) animationDuration else 10)
    }

    private fun calculateMinimizeTranslationY(): Float {
        val parent = parent as ViewGroup

        LogUtils.d("getHideTranslationY " + topOffset)

        return when (hideDirection) {
            FloatingActionMode.HideDirection.TOP -> (topOffset - top).toFloat()
            FloatingActionMode.HideDirection.BOTTOM -> parent.height - bottom + height * 0.5f
            FloatingActionMode.HideDirection.NEAREST -> if (top.toFloat() + (height / 2).toFloat() + dragTranslationY < parent.height / 2) {
                (topOffset - top).toFloat()
            } else {
                parent.height - bottom + height * 0.5f
            }
            HideDirection.NONE -> 0f
        }
    }

    class FloatingActionModeBehavior : CoordinatorLayout.Behavior<FloatingActionMode> {

        constructor()

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

        override fun layoutDependsOn(parent: CoordinatorLayout?, child: FloatingActionMode?, dependency: View?): Boolean {
            return dependency is AppBarLayout || dependency is Snackbar.SnackbarLayout
        }

        override fun onDependentViewChanged(parent: CoordinatorLayout, child: FloatingActionMode, dependency: View): Boolean {
            if (dependency is AppBarLayout) {
                val d = dependency.height - child.topOffset
                child.topOffset = dependency.height
                child.top = child.top + d
                child.bottom = child.bottom + d
            } else if (dependency is Snackbar.SnackbarLayout) {
                // todo
            }
            return false
        }

        override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout?, child: FloatingActionMode?, directTargetChild: View?, target: View?, nestedScrollAxes: Int): Boolean {
            return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
        }

        override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionMode, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)

            if (dyConsumed > 0) {
                child.minimize(true)
            } else if (dyConsumed < 0) {
                child.maximize(true)
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        LogUtils.d("protected Parcelable onSaveInstanceState() {")
        val bundle = Bundle()
        bundle.putParcelable(SUPER_STATE_KEY, super.onSaveInstanceState())

        bundle.putInt(DRAG_ICON_KEY, dragIconRes)
        bundle.putBoolean(CAN_DISMISS_KEY, canDismiss)
        bundle.putFloat(DISMISS_THRESHOLD_KEY, dismissThreshold)
        bundle.putBoolean(CAN_DRAG_KEY, canDrag)
        bundle.putInt(ANIMATION_CONTENT_RES_KEY, mContentRes)
        bundle.putLong(ANIMATION_DURATION_KEY, animationDuration)
        bundle.putInt(HIDE_DIRECTION_KEY, hideDirection.ordinal)

        return bundle
    }

    override fun onRestoreInstanceState(parcelable: Parcelable) {
        LogUtils.d("protected void onRestoreInstanceState(Parcelable parcelable) {")
        if (parcelable is Bundle) {
            dragIconRes = parcelable.getInt(DRAG_ICON_KEY)
            canDismiss = parcelable.getBoolean(CAN_DISMISS_KEY)
            dismissThreshold = parcelable.getFloat(DISMISS_THRESHOLD_KEY)
            canDrag = parcelable.getBoolean(CAN_DRAG_KEY)
            mContentRes = parcelable.getInt(ANIMATION_CONTENT_RES_KEY)
            animationDuration = parcelable.getLong(ANIMATION_DURATION_KEY)
            hideDirection = HideDirection.values()[parcelable.getInt(HIDE_DIRECTION_KEY)]

            val state = parcelable.getParcelable<Parcelable>(SUPER_STATE_KEY)
            super.onRestoreInstanceState(state)
        } else {
            super.onRestoreInstanceState(parcelable)
        }

        init(context)
    }

    companion object {
        private val SUPER_STATE_KEY = "com.qwert2603.floating_action_mode.SUPER_STATE_KEY"
        private val DRAG_ICON_KEY = "com.qwert2603.floating_action_mode.DRAG_ICON_KEY"
        private val CAN_DISMISS_KEY = "com.qwert2603.floating_action_mode.CAN_DISMISS_KEY"
        private val DISMISS_THRESHOLD_KEY = "com.qwert2603.floating_action_mode.DISMISS_THRESHOLD_KEY"
        private val CAN_DRAG_KEY = "com.qwert2603.floating_action_mode.CAN_DRAG_KEY"
        private val ANIMATION_CONTENT_RES_KEY = "com.qwert2603.floating_action_mode.ANIMATION_CONTENT_RES_KEY"
        private val ANIMATION_DURATION_KEY = "com.qwert2603.floating_action_mode.ANIMATION_DURATION_KEY"
        private val HIDE_DIRECTION_KEY = "com.qwert2603.floating_action_mode.HIDE_DIRECTION_KEY"
    }
}
