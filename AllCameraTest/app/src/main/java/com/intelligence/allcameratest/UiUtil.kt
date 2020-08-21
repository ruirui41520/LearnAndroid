package com.intelligence.allcameratest

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.WindowManager

object UiUtil {

    fun getMaxWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm.widthPixels
    }

    fun getMaxHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        return dm.heightPixels
    }

    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(resourceId)
    }

    fun getNavigationBarHeight(context: Context): Int {
        val rid = context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return if (rid != 0) {
            val resourceId =
                context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            context.resources.getDimensionPixelSize(resourceId)
        } else 0
    }
}