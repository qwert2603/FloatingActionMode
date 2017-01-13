package com.qwert2603.floating_action_mode

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

    fun View.centerX() = top + height / 2

    fun View.parentHeight() = (parent as ViewGroup?)?.height ?: 0

    fun <T : Comparable<T>> T.between(l: T, r: T) = l < this && this < r
}
