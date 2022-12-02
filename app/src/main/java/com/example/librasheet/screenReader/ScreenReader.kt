package com.example.librasheet.screenReader

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo


class ScreenReader : AccessibilityService() {


    override fun onInterrupt() {}

    override fun onServiceConnected() {
        Log.d("Libra/ScreenReader", "onServiceConnected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        event.apply {
            Log.v(
                "Libra/ScreenReader/onAccessibilityEvent",
                "type = ${eventType}, class = ${className}, " +
                        "package = ${packageName}, time = ${eventTime}, action = ${action}, " +
                        "sourcePresent = ${source != null} recordCount = ${recordCount}"
            )
        }

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        when(event.packageName) {
            "com.infonow.bofa" -> BofaReader.parse(event)
        }

//        val node = rootInActiveWindow
//        Log.v("Libra/ScreenReader", "${node == null}")
//        node?.let { printAllViews(it, maxDepth = 1) }
//        printAllViews(event.source, maxDepth = 1)
    }


    private fun printAllViews(nodeInfo: AccessibilityNodeInfo?, depth: Int = 0, maxDepth: Int = -1) {
        if (nodeInfo == null) return
        if (maxDepth >= 0 && depth > maxDepth) return

        var log = ".".repeat(depth)
        log += "[${nodeInfo.text}] [${nodeInfo.contentDescription}] <-- ${nodeInfo.viewIdResourceName}"
        Log.v("Libra/ScreenReader/printAllViews", log)

        for (i in 0 until nodeInfo.childCount) {
            printAllViews(nodeInfo.getChild(i), depth + 1, maxDepth)
        }
    }


}




