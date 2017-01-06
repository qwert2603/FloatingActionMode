package com.qwert2603.floating_action_mode

import android.util.Log

object LogUtils {

    val APP_TAG = "AASSDD"
    val ERROR_MSG = "ERROR!!!"

    fun d(s: String) {
        d(APP_TAG, s)
    }

    fun d(tag: String, s: String) {
        Log.d(tag, s)
    }

    fun e(t: Throwable) {
        Log.e(APP_TAG, ERROR_MSG, t)
    }

    fun e(s: String) {
        Log.e(APP_TAG, s)
    }

    fun printCurrentStack() {
        Log.v(APP_TAG, "Current Stack", Exception("Current Stack"))
    }
}
