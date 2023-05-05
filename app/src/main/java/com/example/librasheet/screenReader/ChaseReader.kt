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
     * Credit card all transactions screen, 2048 event.source, 2023-05-04 version 4.411
     *
     * Warning! When scrolling fast, the date headers / transactions may come out-of-sync.
     *
     * type = 2048, class = android.view.ViewGroup, package = com.chase.sig.android
     *
     *      0 [null] [null] <-- null
     *
     *      ~~~ Header ~~~
     *      1   [null] [null] <-- com.chase.sig.android:id/
     *      2     [null] [Navigate up] <-- null
     *      2     [CREDIT CARD (...5940)] [null] <-- null
     *      2     [null] [Profile & Settings icon, new alerts available] <-- null
     *
     *      ~~~ Content ~~~
     *      1   [null] [null] <-- null
     *      2     [null] [null] <-- com.chase.sig.android:id/
     *
     *      ~~~ Search ~~~
     *      3       [null] [null] <-- com.chase.sig.android:id/
     *      4         [null] [null] <-- com.chase.sig.android:id/
     *      5           [null] [Search button] <-- com.chase.sig.android:id/
     *      5           [Search or filter] [null] <-- com.chase.sig.android:id/
     *
     *      ~~~ Transactions ~~~
     *      3       [null] [null] <-- com.chase.sig.android:id/
     *      4         [null] [null] <-- null
     *
     *      Types of entries at level 5:
     *          1. Pending header with total amount pending
     *          2. Period summary drop down header
     *          3. Period summary drop down data (user must click to open to load data)
     *          4. Date header
     *          5. Transaction
     *
     *      ~~~ Pending ~~~
     *      5           [null] [null] <-- null
     *      6             [Pending (2)] [PendingHeader2] <-- com.chase.sig.android:id/
     *      6             [null] [Info] <-- com.chase.sig.android:id/
     *      6             [$22.73] [null] <-- com.chase.sig.android:id/
     *
     *      ~~~ Pending transaction ~~~
     *      5           [null] [null] <-- null
     *      6             [null] [null] <-- null
     *      7               [Split with Zelle®] [null] <-- null
     *      6             [null] [null] <-- null
     *      7               [SWEETGREEN RITTENHOU] [null] <-- com.chase.sig.android:id/merchant_name
     *      7               [May 04, 2023] [null] <-- com.chase.sig.android:id/
     *      7               [$13.45] [$13.45] <-- com.chase.sig.android:id/trans_amount
     *
     *      ~~~ Activity header ~~~
     *      5           [null] [null] <-- null
     *      6             [null] [null] <-- com.chase.sig.android:id/
     *      7               [null] [null] <-- com.chase.sig.android:id/
     *      8                 [Activity since April 12, 2023] [Activity since April 12, 2023 shows content below  Button] <-- com.chase.sig.android:id/
     *
     *      ~~~ Date header ~~~
     *      5           [null] [null] <-- null
     *      6             [May 01, 2023] [May 01, 2023Header] <-- com.chase.sig.android:id/
     *
     *      ~~~ Transaction ~~~
     *      5           [null] [null] <-- null
     *      6             [null] [null] <-- null
     *      7               [Split with Zelle®] [null] <-- null
     *      6             [null] [null] <-- null
     *      7               [PLAYA BOWLS - PHILLY] [null] <-- com.chase.sig.android:id/merchant_name
     *      7               [Food & drink] [null] <-- com.chase.sig.android:id/
     *      7               [$12.85] [$12.85] <-- com.chase.sig.android:id/trans_amount
     */
    fun parse(reader: ScreenReader, event: AccessibilityEvent): Pair<String, List<ParsedTransaction>>? {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.className == "android.view.ViewGroup"
        ) {
            val node = event.source ?: return null
//            printAllViews(node)

            val header = node.child(0) ?: return null
            val content = node.child(1) ?: return null
            val transactionsNode = content.child(0)?.child(1)?.child(0) ?: return null

            val account = parseAccountName(header) ?: ScreenReader.unknownAccountName
            Log.d("LibraSheet/ChaseReader/parse", "account: $account")

            val lastDate = reader.getLatestDate(account)
            val transactions = parseTransactions(transactionsNode, lastDate)
            Log.d("LibraSheet/ChaseReader/parse", "transactions: ${transactions.size}")

            if (transactions.isEmpty()) return null
            return Pair(account, transactions)
        }
        return null
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
        printAllViews(transactions)

        var date: Int? = null
        for (i in 0 until transactions.childCount) {
            /** Iterate through the depth-5 nodes until we find a date header **/
            val node = transactions.getChild(i) ?: continue

            val newDate = parseDateHeader(node)
            if (newDate != null) {
                Log.v("LibraSheet/ChaseReader/parseTransactions", "i=$i date=$newDate")
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