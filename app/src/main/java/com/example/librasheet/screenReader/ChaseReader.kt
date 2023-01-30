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
     * Credit card all transactions screen, 2048 event.source, 2023-01-27 version 4.380
     *
     * Warning! When scrolling fast, the date headers / transactions may come out-of-sync.
     *
     *      0 [null] [null] <-- null
     *
     *      ~~~ Header ~~~
     *      1   [null] [null] <-- com.chase.sig.android:id/
     *      2     [null] [Navigate up] <-- null
     *      2     [CREDIT CARD (...5940)] [null] <-- null
     *      2     [null] [Profile & Settings icon, new alerts available] <-- null
     *
     *      ~~~ Tab host ~~~
     *      1   [null] [null] <-- null
     *
     *      ~~~ Tab selector ~~~
     *      2     [null] [null] <-- com.chase.sig.android:id/  
     *      3       [null] [All transactions] <-- null         
     *      4         [All transactions] [null] <-- null       
     *      3       [null] [Spending summary] <-- null         
     *      4         [Spending summary] [null] <-- null       
     *
     *      ~~~ Tab content ~~~
     *      2     [null] [null] <-- com.chase.sig.android:id/
     *      3       [null] [null] <-- null  # for some reason this doesn't always appear, in which
     *                                      # case the level of everything below is shifted one less
     *      4         [null] [null] <-- com.chase.sig.android:id/
     *
     *      ~~~ Search ~~~
     *      5           [null] [null] <-- com.chase.sig.android:id/
     *      6             [null] [null] <-- com.chase.sig.android:id/
     *      7               [null] [Search button] <-- com.chase.sig.android:id/
     *      7               [Search or filter] [null] <-- com.chase.sig.android:id/
     *
     *      ~~~ Transactions ~~~
     *      5           [null] [null] <-- com.chase.sig.android:id/
     *      6             [null] [null] <-- null
     *
     *      Types of entries at level 7:
     *          1. Pending header with total amount pending
     *          2. Period summary drop down header + data (user must click to open to load data)
     *          3. Date header
     *          4. Transaction
     *
     *      ~~~ Pending ~~~
     *      7               [null] [null] <-- null
     *      8                 [Pending (1)] [PendingHeader1] <-- com.chase.sig.android:id/
     *      8                 [null] [Info] <-- com.chase.sig.android:id/
     *      8                 [$12.96] [null] <-- com.chase.sig.android:id/
     *
     *      ~~~ Pending transaction ~~~
     *      7               [null] [null] <-- null
     *      8                 [null] [null] <-- null
     *      9                   [Split with Zelle®] [null] <-- null
     *      8                 [null] [null] <-- null
     *      9                   [TST* 20th Street Piz] [null] <-- com.chase.sig.android:id/merchant_name
     *      9                   [Jan 26, 2023] [null] <-- com.chase.sig.android:id/
     *      9                   [$12.96] [$12.96] <-- com.chase.sig.android:id/trans_amount
     *
     *      ~~~ Activity header ~~~
     *      7               [null] [null] <-- null
     *      8                 [null] [null] <-- com.chase.sig.android:id/
     *      9                   [null] [null] <-- com.chase.sig.android:id/
     *      10                    [Activity since January 12, 2023] [Activity since January 12, 2023 shows content below  Button] <-- com.chase.sig.android:id/
     *
     *      ~~~ Activity data ~~~
     *      7               [null] [null] <-- null
     *      8                 [null] [null] <-- com.chase.sig.android:id/
     *      9                   [Last statement balance] [null] <-- com.chase.sig.android:id/
     *      9                   [$148.40] [null] <-- com.chase.sig.android:id/
     *      8                 [null] [null] <-- com.chase.sig.android:id/
     *      9                   [Payments] [null] <-- null
     *      9                   [$0.00] [$0.00] <-- com.chase.sig.android:id/
     *      8                 [null] [null] <-- com.chase.sig.android:id/
     *      9                   [Purchases] [null] <-- null
     *      9                   [+$725.15] [$725.15] <-- com.chase.sig.android:id/
     *      8                 [null] [null] <-- com.chase.sig.android:id/
     *      9                   [Fees charged] [null] <-- null
     *      9                   [$0.00] [$0.00] <-- com.chase.sig.android:id/
     *      8                 [null] [null] <-- com.chase.sig.android:id/
     *      9                   [Current balance] [null] <-- com.chase.sig.android:id/
     *      9                   [$873.55] [null] <-- com.chase.sig.android:id/
     *
     *      ~~~ Date header ~~~
     *      7               [null] [null] <-- null
     *      8                 [Jan 25, 2023] [Jan 25, 2023Header] <-- com.chase.sig.android:id/
     *
     *      ~~~ Transaction ~~~
     *      7               [null] [null] <-- null
     *      8                 [null] [null] <-- null
     *      9                   [Split with Zelle®] [null] <-- null
     *      8                 [null] [null] <-- null
     *      9                   [TST* BAO NINE- RITTENH] [null] <-- com.chase.sig.android:id/merchant_name
     *      9                   [Food & drink] [null] <-- com.chase.sig.android:id/
     *      9                   [$10.48] [$10.48] <-- com.chase.sig.android:id/trans_amount
     */
    fun parse(reader: ScreenReader, event: AccessibilityEvent): Pair<String, List<ParsedTransaction>>? {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.className == "android.view.ViewGroup"
        ) {
            val node = event.source ?: return null
            printAllViews(node)

            if (!isAllTransactionsScreen(node)) return null
            val header = node.child(0) ?: return null
            val tabHost = node.child(1) ?: return null
            val tabContent = getTabContentFromHost(tabHost) ?: return null
            val transactionsNode = tabContent.child(1)?.child(0) ?: return null

            val account = parseAccountName(header) ?: ScreenReader.unknownAccountName
            val lastDate = reader.getLatestDate(account)
            val transactions = parseTransactions(transactionsNode, lastDate)
            return Pair(account, transactions)
        }
        return null
    }


    /** Look for the tab selector saying "All transactions"
     *
     *      0 [null] [null] <-- null
     *
     *      ~~~ Header ~~~
     *      1   [null] [null] <-- com.chase.sig.android:id/
     *      2     [null] [Navigate up] <-- null
     *      2     [CREDIT CARD (...5940)] [null] <-- null
     *      2     [null] [Profile & Settings icon, new alerts available] <-- null
     *
     *      ~~~ Tab host ~~~
     *      1   [null] [null] <-- null
     *
     *      ~~~ Tab selector ~~~
     *      2     [null] [null] <-- com.chase.sig.android:id/
     *      3       [null] [All transactions] <-- null
     *      4         [All transactions] [null] <-- null
     *      3       [null] [Spending summary] <-- null
     *      4         [Spending summary] [null] <-- null
     */
    private fun isAllTransactionsScreen(root: AccessibilityNodeInfo?): Boolean {
        if (root == null) return false
        val tabSelector = root.child(1)?.child(0) ?: return false
        val allTransactions = tabSelector.child(0) ?: return false
        return allTransactions.contentDescription == "All transactions"
    }


    /**
     *      ~~~ Header ~~~
     *      1   [null] [null] <-- com.chase.sig.android:id/
     *      2     [null] [Navigate up] <-- null
     *      2     [CREDIT CARD (...5940)] [null] <-- null
     *      2     [null] [Profile & Settings icon, new alerts available] <-- null
     */
    private fun parseAccountName(header: AccessibilityNodeInfo): String? {
        val account = header.child(1) ?: return null
        return account.text.toString()
    }

    /**
     *      ~~~ Tab host ~~~
     *      1   [null] [null] <-- null
     *
     *      ~~~ Tab selector ~~~
     *      2     [null] [null] <-- com.chase.sig.android:id/
     *
     *      ~~~ Tab content ~~~
     *      2     [null] [null] <-- com.chase.sig.android:id/
     *      3       [null] [null] <-- null  # for some reason this doesn't always appear, in which
     *                                      # case the level of everything below is shifted one less
     *      4         [null] [null] <-- com.chase.sig.android:id/
     */
    private fun getTabContentFromHost(tabHost: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val depth3 = tabHost.child(1)?.child(0) ?: return null
        return if (depth3.viewIdResourceName == null) depth3.child(0) else depth3
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
                if (date != null && newDate > date) break
                    // failsafe, sometimes even with debouncing the date header has the wrong date
                    // correctly even though the transactions have been read correctly. But we know
                    // the dates are listed in decreasing order.
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