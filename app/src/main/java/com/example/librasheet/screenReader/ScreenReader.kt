package com.example.librasheet.screenReader

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf


class ScreenReader : AccessibilityService() {

    companion object {
        val cache = mutableMapOf<String, MutableList<ParsedTransaction>>()
        val nItems = mutableStateOf(0)

        fun add(account: String, t: ParsedTransaction) {
            val x = cache.getOrPut(account) { mutableListOf() }
            if (x.add(t)) {
                x.add(t)
                nItems.value += 1
            }
        }

        fun add(account: String, transactions: List<ParsedTransaction>) {
            val x = cache.getOrPut(account) { mutableListOf() }
            transactions.forEach {
                if (x.add(it)) {
                    x.add(it)
                    nItems.value += 1
                }
            }
        }
    }

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
            "com.infonow.bofa" -> BofaReader.parse(this, event)
        }

//        val node = rootInActiveWindow
//        Log.v("Libra/ScreenReader", "${node == null}")
//        node?.let { printAllViews(it, maxDepth = 1) }
//        printAllViews(event.source, maxDepth = 1)
    }

}

private val nonPrintableRegex = "[^\\p{Print}]".toRegex()
private fun replaceNonPrintableCharacters(s: String): String {
    return s.replace("\r", """<\r>""")
        .replace("\n", """<\n>""")
        .replace("\t", """<\t>""")
        .replace(nonPrintableRegex, "?")
}

internal fun printAllViews(nodeInfo: AccessibilityNodeInfo?, depth: Int = 0, maxDepth: Int = -1) {
    if (nodeInfo == null) return
    if (maxDepth >= 0 && depth > maxDepth) return

    var log = ".".repeat(depth)
    log += "[${nodeInfo.text}] [${nodeInfo.contentDescription}] <-- ${nodeInfo.viewIdResourceName}"
    Log.v("Libra/ScreenReader/printAllViews", replaceNonPrintableCharacters(log))

    for (i in 0 until nodeInfo.childCount) {
        printAllViews(nodeInfo.getChild(i), depth + 1, maxDepth)
    }
}

