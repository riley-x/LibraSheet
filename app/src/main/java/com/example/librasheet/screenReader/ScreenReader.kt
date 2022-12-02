package com.example.librasheet.screenReader

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo




class ScreenReader : AccessibilityService() {

    val cache = mutableMapOf<String, Long>()


    override fun onInterrupt() {}

    override fun onServiceConnected() {
        Log.d("Libra/ScreenReader", "onServiceConnected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.apply {
            Log.v(
                "Libra/ScreenReader/onAccessibilityEvent",
                "type = ${eventType}, class = ${className}, " +
                        "package = ${packageName}, time = ${eventTime}, action = ${action}, " +
                        "sourcePresent = ${source != null} recordCount = ${recordCount}"
            )
        }

        if (event?.packageName != "com.infonow.bofa") return

        /**
         * Specifically for BofA,
         * when loading an account detail page, the typeWindowStateChanged=32 event is triggered
         * but it has null source, probably because the data is still loading. Scrolling even after
         * data is loaded only triggers typeViewScrolled=4096 with null source. But if you turn the
         * phone screen off and on again, these all now have valid source, and additionally events
         * typeWindowContentChanged=2048 are also triggered when scrolling.
         *
         * Somehow, if you access this property, after the screen is done loading, the
         * sources appear and the 2048 events are triggered too.
         *
         * But in the see all transactions screen, we get everything loaded without gimmicks.
         */
//        val node = rootInActiveWindow

        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val node = event.source ?: return

        if (node.viewIdResourceName != "com.infonow.bofa:id/recent_transactions") return
        Log.v("Libra/ScreenReader/parseBofA", "childCount = ${node.childCount}")

        for (j in 0 until node.childCount) {
            val t = node.getChild(j)
            if (t.viewIdResourceName == "com.infonow.bofa:id/transaction_row_layout") {
                parseBofATransaction(t)
            }
        }



//        Log.v("Libra/ScreenReader", "${node == null}")
//        node?.let { printAllViews(it, maxDepth = 1) }
//        printAllViews(event.source, maxDepth = 1)


//                parseBofA(event.source)
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

    private fun parseBofA(nodeInfo: AccessibilityNodeInfo?) {
        if (nodeInfo == null) return
        if (nodeInfo.childCount < 1) return

        val scrollContainer = nodeInfo.getChild(0)
        Log.v("Libra/ScreenReader/parseBofA", "${System.identityHashCode(scrollContainer)}")
        for (i in 0 until scrollContainer.childCount) {
            val transactionList = scrollContainer.getChild(i)
            if (transactionList.viewIdResourceName != "com.infonow.bofa:id/recent_transactions") continue
            Log.v("Libra/ScreenReader/parseBofA", "childCount = ${transactionList.childCount}")

            for (j in 0 until transactionList.childCount) {
                val t = transactionList.getChild(j)
                if (t.viewIdResourceName == "com.infonow.bofa:id/transaction_row_layout") {
                    parseBofATransaction(t)
                }
            }
            return
        }
    }

    private fun parseBofATransaction(row: AccessibilityNodeInfo) {
        if (row.childCount < 2) return

        val leftInfo = row.getChild(0)
        if (leftInfo.childCount < 2) return
        val date = leftInfo.getChild(0).text
        val name = leftInfo.getChild(1).text

        val rightInfo = row.getChild(1)
        if (rightInfo.childCount < 1) return
        val value = rightInfo.getChild(0).text

        Log.d("Libra/ScreenReader/parseBofA", "date=$date name=$name value=$value")
    }
}




