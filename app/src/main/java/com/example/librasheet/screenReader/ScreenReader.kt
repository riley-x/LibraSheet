package com.example.librasheet.screenReader

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.example.librasheet.data.LibraDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOf
import java.util.*


class ScreenReader : AccessibilityService() {

    companion object {
        val cache = mutableMapOf<String, MutableSet<ParsedTransaction>>()
        val nItems = mutableStateOf(0)
        var lastLoadTime = 0L

        fun add(account: String, t: ParsedTransaction) {
            val x = cache.getOrPut(account) { mutableSetOf() }
            if (x.add(t)) {
                lastLoadTime = Calendar.getInstance().timeInMillis
                nItems.value += 1
            }
        }

        fun add(account: String, transactions: List<ParsedTransaction>) {
            Log.d("Libra/ScreenReader/add", "$account $transactions")
            val x = cache.getOrPut(account) { mutableSetOf() }
            transactions.forEach {
                if (x.add(it)) {
                    lastLoadTime = Calendar.getInstance().timeInMillis
                    nItems.value += 1
                }
            }
        }

        fun reset() {
            cache.clear()
            nItems.value = 0
        }
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job)

    /**
     * A MutableStateFlow<AccessibilityEvent?>(null) sometimes doesn't work, all the fields of the
     * event are sometimes null when it's collected. Also weird error "java.lang.IllegalStateException:
     * Cannot perform this action on a sealed instance." FATAL EXCEPTION: main when trying to print event.source
     */
    private val flow = MutableStateFlow<Pair<String, List<ParsedTransaction>>?>(null)

    val database: LibraDatabase by lazy { LibraDatabase.getDatabase(this) }
    var accountDates = mutableMapOf<String, Int>()

    internal fun getLatestDate(account: String?): Int {
        if (accountDates.isEmpty()) return 0
        var date: Int? = null
        if (account != null) date = accountDates[account]
        if (date == null) date = accountDates.minOf { it.value }
        return date
    }

    override fun onInterrupt() {}

    @OptIn(FlowPreview::class)
    override fun onServiceConnected() {
        scope.launch {
            accountDates = withContext(Dispatchers.IO) {
                database.transactionDao().getLastDates().toMutableMap()
            }
            Log.d("Libra/ScreenReader/onServiceConnected", "$accountDates")

            flow.debounce(200)
                .collect { it?.let { add(it.first, it.second) } }
        }
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

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            && event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        flow.value = when(event.packageName) {
            "com.infonow.bofa" -> BofaReader.parse(this, event)
            "com.chase.sig.android" -> ChaseReader.parse(this, event)
            "com.venmo" -> VenmoReader.parse(this, event)
            else -> null
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

    var log = "$depth".padEnd(2 + depth * 2, ' ')
    log += "[${nodeInfo.text}] [${nodeInfo.contentDescription}] <-- ${nodeInfo.viewIdResourceName}"
    Log.v("Libra/ScreenReader/printAllViews", replaceNonPrintableCharacters(log))

    for (i in 0 until nodeInfo.childCount) {
        printAllViews(nodeInfo.getChild(i), depth + 1, maxDepth)
    }
}


fun AccessibilityNodeInfo.child(i: Int): AccessibilityNodeInfo? {
    if (childCount <= i) return null
    return getChild(i)
}