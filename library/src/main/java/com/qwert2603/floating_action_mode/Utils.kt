package com.qwert2603.floating_action_mode

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup

internal object Utils {

    private val sMainLooperHandler: Handler = Handler(Looper.getMainLooper())

    /**
     * Post runnable to execute on main thread with given delay.
     */
    fun runOnUI(delay: Long = 0, func: () -> Unit) {
        sMainLooperHandler.postDelayed({ func() }, delay)
    }

    fun isLollipopOrHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

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
