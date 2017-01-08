package com.qwert2603.floating_action_mode

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup

object AndroidUtils {

    private val sMainLooperHandler: Handler = Handler(Looper.getMainLooper())

    /**
     * Post runnable to execute on main thread with given delay.
     */
    fun runOnUI(delay: Long = 0, func: () -> Unit) {
        sMainLooperHandler.postDelayed({ func() }, delay)
    }

    fun isLollipopOrHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    /**
     * @return is given activity in portrait orientation.
     */
    fun isPortraitOrientation(activity: Activity): Boolean {
        return activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    /**
     * Resolve value of given attribute.

     * @return value of attribute in pixels
     */
    fun resolveAttributeToPixel(activity: Activity, resId: Int): Int {
        val typedValue = TypedValue()
        activity.theme.resolveAttribute(resId, typedValue, true)
        return TypedValue.complexToDimensionPixelSize(typedValue.data, activity.resources.displayMetrics)
    }

    /**
     * Set enabled state to view and all its descendants.
     */
    fun View.setEnabledWithDescendants(enabled: Boolean) {
        isEnabled = enabled
        if (this is ViewGroup) {
            for (i in 0..childCount - 1) {
                getChildAt(i).setEnabledWithDescendants(enabled)
            }
        }
    }

    fun View.centerX() = top + height / 2

    fun View.parentHeight() = (parent as ViewGroup?)?.height ?: 0

}
