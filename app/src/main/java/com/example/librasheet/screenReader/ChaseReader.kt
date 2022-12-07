package com.example.librasheet.screenReader

import android.annotation.SuppressLint
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.librasheet.data.toIntDate
import com.example.librasheet.data.toLongDollar
import com.example.librasheet.ui.components.parseOrNull
import java.text.SimpleDateFormat

object ChaseReader {

    /**
     * Credit card detail screen, 2048 event.source
     *      [null] [null] <-- null
     *      .[null] [CREDIT CARD (...5940)] <-- com.chase.sig.android:id/
     *      .[null] [null] <-- null
     *      ..[null] [null] <-- com.chase.sig.android:id/
     *      ...{{{many items}}}
     *      ...[null] [null] <-- null
     *      ....[Recent transactions] [null] <-- com.chase.sig.android:id/
     *      ...[null] [null] <-- null
     *      ....[null] [null] <-- com.chase.sig.android:id/
     *      .....[LANZHOU BEEF NOODLE] [null] <-- com.chase.sig.android:id/merchant_name
     *      .....[Dec 03, 2022] [null] <-- com.chase.sig.android:id/
     *      .....[$36.31] [$36.31] <-- com.chase.sig.android:id/trans_amount
     *      .....[Pending] [null] <-- com.chase.sig.android:id/
     *
     * Credit card all transactions screen, 2048 event.source
     *
     * Warning! When scrolling fast, the date headers / transactions may come out-of-sync.
     *
     *      [null] [null] <-- null
     *
     *      ~~~ Header ~~~
     *      .[null] [null] <-- com.chase.sig.android:id/
     *      ..[null] [Navigate up] <-- null
     *      ..[CREDIT CARD (...5940)] [null] <-- null
     *      ..[null] [Profile & Settings icon, new alerts available] <-- null
     *
     *      ~~~ Tab host ~~~
     *      .[null] [null] <-- null
     *
     *      ~~~ Tab selector ~~~
     *      ..[null] [null] <-- com.chase.sig.android:id/
     *      ...[null] [All transactions] <-- null
     *      ....[All transactions] [null] <-- null
     *      ...[null] [Spending summary] <-- null
     *      ....[Spending summary] [null] <-- null
     *
     *      ~~~ Tab content ~~~
     *      ..[null] [null] <-- com.chase.sig.android:id/
     *      ...[null] [null] <-- null
     *
     *      ~~~ Search header ~~~
     *      ....[null] [null] <-- com.chase.sig.android:id/
     *      .....[null] [Search button] <-- com.chase.sig.android:id/
     *      .....[Search or filter] [null] <-- com.chase.sig.android:id/
     *
     *      ~~~ Transactions ~~~
     *      ....[null] [null] <-- null
     *
     *      ~~~ Pending ~~~
     *      .....[null] [null] <-- null
     *      ......[Pending (2)] [PendingHeader2] <-- com.chase.sig.android:id/
     *      ......[null] [Info] <-- com.chase.sig.android:id/
     *      ......[$39.52] [null] <-- com.chase.sig.android:id/
     *
     *      .....{{{ transactions }}}
     *
     *      ~~~ Statement header ~~~
     *      .....[null] [null] <-- null
     *      ......[null] [null] <-- com.chase.sig.android:id/
     *      .......[null] [null] <-- com.chase.sig.android:id/
     *      ........[Activity since November 12, 2022] [Activity since November 12, 2022 shows content below  Button] <-- com.chase.sig.android:id/
     *
     *      ~~~ Date header ~~~
     *      .....[null] [null] <-- null
     *      ......[Dec 03, 2022] [Dec 03, 2022Header] <-- com.chase.sig.android:id/
     *
     *      ~~~ Transaction ~~~
     *      .....[null] [null] <-- null
     *      ......[null] [null] <-- null
     *      .......[Split with Zelle®] [null] <-- null
     *      ......[null] [null] <-- null
     *      .......[MTA*NYCT PAYGO] [null] <-- com.chase.sig.android:id/merchant_name
     *      .......[Travel] [null] <-- com.chase.sig.android:id/
     *      .......[$2.75] [$2.75] <-- com.chase.sig.android:id/trans_amount
     *
     *      .....{{{ transactions and headers }}}
     *
     *      .....[null] [null] <-- null
     *      ......[null] [null] <-- null
     *      .......[Split with Zelle®] [null] <-- null
     *      ......[null] [null] <-- null
     *      .......[SQ *SAKE BAR DECIBEL] [null] <-- com.chase.sig.android:id/merchant_name
     *      .......[Food & drink] [null] <-- com.chase.sig.android:id/
     *      .......[null] [null] <-- com.chase.sig.android:id/
     *      ........[Pay over time] [null] <-- com.chase.sig.android:id/
     *      .......[$107.92] [$107.92] <-- com.chase.sig.android:id/trans_amount
     */
    fun parse(reader: ScreenReader, event: AccessibilityEvent): Pair<String, List<ParsedTransaction>>? {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.className == "android.view.ViewGroup"
        ) {
            val node = event.source ?: return null

            if (!isAllTransactionsScreen(node)) return null
            val header = node.child(0) ?: return null
            val tabHost = node.child(1) ?: return null
            val tabContent = tabHost.child(1)?.child(0) ?: return null
            val transactionsNode = tabContent.child(1) ?: return null
//            printAllViews(transactionsNode)

            val account = parseAccountName(header) ?: "Unknown"
            val lastDate = reader.getLatestDate(account)
            val transactions = parseTransactions(transactionsNode, lastDate)
            return Pair(account, transactions)
        }
        return null
    }


    /** Look for the tab selector saying "All transactions"
     *
     *      [null] [null] <-- null
     *
     *      ~~~ Tab host ~~~
     *  [1] .[null] [null] <-- null
     *
     *      ~~~ Tab selector ~~~
     *      ..[null] [null] <-- com.chase.sig.android:id/
     *      ...[null] [All transactions] <-- null
     *      ....[All transactions] [null] <-- null
     *      ...[null] [Spending summary] <-- null
     *      ....[Spending summary] [null] <-- null
     */
    private fun isAllTransactionsScreen(root: AccessibilityNodeInfo?): Boolean {
        if (root == null) return false
        if (root.childCount < 2) return false
        val tabSelector = root.child(1)?.child(0) ?: return false
        val allTransactions = tabSelector.child(0) ?: return false
        return allTransactions.contentDescription == "All transactions"
    }


    /**
     *      ~~~ Header ~~~
     *      .[null] [null] <-- com.chase.sig.android:id/
     *      ..[null] [Navigate up] <-- null
     *      ..[CREDIT CARD (...5940)] [null] <-- null
     *      ..[null] [Profile & Settings icon, new alerts available] <-- null
     */
    private fun parseAccountName(header: AccessibilityNodeInfo): String? {
        if (header.childCount < 2) return null
        val account = header.getChild(1) ?: return null
        return account.text.toString()
    }

    /**
     *      ~~~ Transactions ~~~
     *      ....[null] [null] <-- null
     *
     *      ~~~ Pending ~~~
     *      .....[null] [null] <-- null
     *      ......{{ }}
     *
     *      ~~~ Statement header ~~~
     *      .....[null] [null] <-- null
     *      ......{{ }}
     *
     *      ~~~ Date header ~~~
     *      .....[null] [null] <-- null
     *      ......{{ }}
     *
     *      ~~~ Transaction ~~~
     *      .....[null] [null] <-- null
     *      ......{{ }}
     */
    private fun parseTransactions(transactions: AccessibilityNodeInfo, latestDate: Int): MutableList<ParsedTransaction> {
        val out = mutableListOf<ParsedTransaction>()

        var date: Int? = null
        for (i in 0 until transactions.childCount) {
            /** Iterate through the depth-5 nodes until we find a date header **/
            val node = transactions.getChild(i) ?: continue

            val newDate = parseDateHeader(node)
            if (newDate != null) {
                if (newDate < latestDate) break
                date = newDate
                continue
            }
            if (date == null) continue

            val (name, value) = parseTransaction(node) ?: continue
            out.add(ParsedTransaction(date = date, name = name, value = value))
            Log.v("Libra/ChaseReader/parseTransactions", "i=$i date=$date name=$name value=$value")
        }
        return out
    }

    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("MMM dd, yyyy").apply { isLenient = false }

    /**
     *      ~~~ Date header ~~~
     *      .....[null] [null] <-- null
     *      ......[Dec 03, 2022] [Dec 03, 2022Header] <-- com.chase.sig.android:id/
     */
    private fun parseDateHeader(node: AccessibilityNodeInfo?) : Int? {
        val dateView = node?.child(0) ?: return null
        if (dateView.text == null) return null
        return formatter.parseOrNull(dateView.text.toString())?.toIntDate()
    }


    /**
     *      ~~~ Transaction ~~~
     *      [null] [null] <-- null
     *
     *      .[null] [null] <-- null
     *      ..[Split with Zelle®] [null] <-- null                       << this doesn't always appear, so can't use indices >>
     *
     *      .[null] [null] <-- null
     *      ..[SQ *SAKE BAR DECIBEL] [null] <-- com.chase.sig.android:id/merchant_name
     *      ..[Food & drink] [null] <-- com.chase.sig.android:id/
     *      ..[null] [null] <-- com.chase.sig.android:id/               << this doesn't always appear, so can't use indices >>
     *      ...[Pay over time] [null] <-- com.chase.sig.android:id/
     *      ..[$107.92] [$107.92] <-- com.chase.sig.android:id/trans_amount
     */
    private fun parseTransaction(node: AccessibilityNodeInfo?): Pair<String, Long>? {
        if (node == null) return null
        var name: String? = null
        var amount: Long? = null

        for (j in 0 until node.childCount) {
            val dataNode = node.getChild(j) ?: continue
            if (dataNode.childCount < 3) continue

            for (i in 0 until dataNode.childCount) {
                val c = dataNode.getChild(i) ?: return null
                if (c.viewIdResourceName == "com.chase.sig.android:id/merchant_name") {
                    name = c.text.toString()
                } else if (c.viewIdResourceName == "com.chase.sig.android:id/trans_amount") {
                    amount = c.text.toString()
                        .filter { ",$".indexOf(it) == -1 }
                        .toDoubleOrNull()?.toLongDollar() ?: return null
                }
            }
        }

        if (name == null || amount == null) return null
        return name to amount
    }
}