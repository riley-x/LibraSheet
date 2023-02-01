package com.example.librasheet.screenReader

import android.annotation.SuppressLint
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.librasheet.data.toIntDate
import com.example.librasheet.data.toLongDollar
import com.example.librasheet.ui.components.parseOrNull
import java.text.SimpleDateFormat


object BofaReader {

    /**
     * When loading an account detail page, the typeWindowStateChanged=32 event is triggered
     * but it has null source, probably because the data is still loading. Scrolling even after
     * data is loaded only triggers typeViewScrolled=4096 with null source. But if you turn the
     * phone screen off and on again, these all now have valid source, and additionally events
     * typeWindowContentChanged=2048 are also triggered when scrolling.
     *
     * Alternatively, merely accessing this.rootInActiveWindow, the sources appear and the 2048
     * events are triggered too. But make sure this happens before catching event.eventType.
     * Probably triggering some side effect in the initial 4096 event after switching to the
     * account detail screen.
     *
     * But in the see all transactions screen, we get everything loaded without gimmicks. The
     * top level node in event.source is the list of visible transactions, with class
     * androidx.recyclerview.widget.RecyclerView
     *
     * reader.rootInActiveWindow (credit card screen transaction details screen):
     *
     *      [null] [null] <-- null
     *
     *  {1} .[null] [SpicaHeader] <-- com.infonow.bofa:id/header_container
     *      ..[null] [Go back to previous screen] <-- com.infonow.bofa:id/__boa_header_left_button_click_area
     *      ..[null] [Products cart with 0 items. Button] <-- com.infonow.bofa:id/rl_shopping_cart
     *      ..[null] [null] <-- com.infonow.bofa:id/__boa_header_help_image_button_click_area
     *      ...[null] [Erica. There are 4 notifications.] <-- com.infonow.bofa:id/button_erica_icon_container
     *
     *  {2} .[null] [null] <-- com.infonow.bofa:id/recent_transactions
     *      ..[null] [null] <-- com.infonow.bofa:id/transaction_row_layout
     *      ...[null] [<info with lots of whitespace>] <-- com.infonow.bofa:id/recent_transaction_left_text_nocheck
     *      ....[Pending or Dec 1, 2022] [null] <-- null
     *      ....[PAYPAL *UBER] [null] <-- null
     *      ...[null] [<info with lots of whitespace>] <-- com.infonow.bofa:id/recent_transaction_right_text_nocheck
     *      ....[$38.95] [null] <-- null (amount)
     *      ....[$360.90] [null] <-- null (balance)
     *
     *  {1} parseAccountName
     *  {2} parseBofaRecentTransactions, event.source in 2048 events
     */
    fun parse(reader: ScreenReader, event: AccessibilityEvent): Pair<String, List<ParsedTransaction>>? {
        val root = reader.rootInActiveWindow // this has to be accessed here for the source to be loaded, see above
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return null
        return when (event.className) {
            "androidx.recyclerview.widget.RecyclerView" -> parseTransactionDetailsScreen(reader, event)
            "android.widget.ScrollView" -> parseAccountDetailsScreen(reader, event)
            else -> null
        }
    }

    private fun parseTransactionDetailsScreen(reader: ScreenReader, event: AccessibilityEvent): Pair<String, MutableList<ParsedTransaction>>? {
        val node = event.source ?: return null
        if (node.viewIdResourceName == "com.infonow.bofa:id/recent_transactions") {
            val account = parseTransactionDetailsAccountName(reader.rootInActiveWindow) ?: ScreenReader.unknownAccountName
            val lastDate = reader.getLatestDate(account)
            val transactions = parseRecentTransactions(node, lastDate)
            return Pair(account, transactions)
        }
        return null
    }


    /** This is for the event.source when scrolling the account detail screen. But this page is
     * gimmicky as noted above.
     */
    private fun parseAccountDetailsScreen(reader: ScreenReader, event: AccessibilityEvent): Pair<String, MutableList<ParsedTransaction>>? {
        val nodeInfo = event.source ?: return null
        if (nodeInfo.childCount < 1) return null

        val scrollContainer = nodeInfo.getChild(0)
        for (i in 0 until scrollContainer.childCount) {
            val c = scrollContainer.getChild(i)
            if (c.viewIdResourceName == "com.infonow.bofa:id/recent_transactions") {
                val account = parseAccountDetailsAccountName(reader.rootInActiveWindow) ?: ScreenReader.unknownAccountName
                val lastDate = reader.getLatestDate(account)
                val transactions = parseRecentTransactions(c, lastDate)
                return Pair(account, transactions)
            }
        }
        return null
    }

    /**
     *
     */
    private fun parseTransactionDetailsAccountName(root: AccessibilityNodeInfo?): String? {
        val accountName = root?.child(0)?.child(1) ?: return null
        if (accountName.viewIdResourceName != "com.infonow.bofa:id/__boa_header_tv_headerText") {
            Log.d("Libra/BofaReader/parseTransactionDetailsAccountName", "Mismatch resource name: ${accountName.viewIdResourceName}")
            return null
        }
        Log.d("Libra/BofaReader/parseTransactionDetailsAccountName", "${accountName.text}")
        return accountName.text?.toString()
    }

    /**
     * Bank accounts:
     *     0 [null] [null] <-- null
     *     1   [null] [Header] <-- com.infonow.bofa:id/header_container
     *     2     ...
     *     1   [null] [How can we help?] <-- com.infonow.bofa:id/search_bar_clickable_area
     *     1   [null] [Erica. There are 4 notifications.] <-- com.infonow.bofa:id/button_erica_icon_container
     *     1   [null] [null] <-- null
     *     2     [null] [null] <-- com.infonow.bofa:id/debit_nestedscroll_container
     *     3       [null] [Virgo, ] <-- com.infonow.bofa:id/account_name
     *     4         [Virgo] [null] <-- null
     *
     * Credit card:
     *     0 [null] [null] <-- null
     *     1   [null] [Header] <-- com.infonow.bofa:id/header_container
     *     2     ...
     *     1   [null] [How can we help?] <-- com.infonow.bofa:id/search_bar_clickable_area
     *     1   [null] [Erica. There are 4 notifications.] <-- com.infonow.bofa:id/button_erica_icon_container
     *     1   [null] [null] <-- null
     *     2     [null] [null] <-- com.infonow.bofa:id/credit_account_nestedscroll_view
     *     3       [Spica] [null] <-- null
     */
    private fun parseAccountDetailsAccountName(root: AccessibilityNodeInfo?): String? {
        val level3Node = root?.child(3)?.child(0)?.child(0) ?: return null
        val accountNameNode =
            if (level3Node.viewIdResourceName == "com.infonow.bofa:id/account_name") level3Node.child(0)
            else level3Node
        return accountNameNode?.text?.toString()
    }

    /**
     *      [null] [null] <-- com.infonow.bofa:id/recent_transactions
     *      .[null] [null] <-- com.infonow.bofa:id/transaction_row_layout
     *      .[null] [null] <-- com.infonow.bofa:id/transaction_row_layout
     *      .(etc)
     */
    private fun parseRecentTransactions(list: AccessibilityNodeInfo, latestDate: Int): MutableList<ParsedTransaction> {
        Log.v("Libra/ScreenReader/parseBofA", "childCount = ${list.childCount}")

        val out = mutableListOf<ParsedTransaction>()
        for (j in 0 until list.childCount) {
            val t = list.getChild(j) ?: continue
            if (t.viewIdResourceName == "com.infonow.bofa:id/transaction_row_layout") {
                parseTransactionRowLayout(t, latestDate)?.let { out.add(it) }
            }
        }
        return out
    }

    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("MMM dd, yyyy").apply { isLenient = false }

    /**
     *      [null] [null] <-- com.infonow.bofa:id/transaction_row_layout
     *      .[null] [<info with lots of whitespace>] <-- com.infonow.bofa:id/recent_transaction_left_text_nocheck
     *      ..[Pending or Dec 1, 2022] [null] <-- null
     *      ..[PAYPAL *UBER] [null] <-- null
     *      .[null] [<info with lots of whitespace>] <-- com.infonow.bofa:id/recent_transaction_right_text_nocheck
     *      ..[$38.95] [null] <-- null (amount)
     *      ..[$360.90] [null] <-- null (balance)
     */
    private fun parseTransactionRowLayout(row: AccessibilityNodeInfo, latestDate: Int): ParsedTransaction? {
        if (row.childCount < 2) return null

        val leftInfo = row.getChild(0) ?: return null
        if (leftInfo.childCount < 2) return null
        val date = leftInfo.getChild(0)?.text ?: return null
        val name = leftInfo.getChild(1)?.text ?: return null

        val rightInfo = row.getChild(1) ?: return null
        if (rightInfo.childCount < 1) return null
        val value = rightInfo.getChild(0)?.text ?: return null

        Log.v("Libra/BofaReader/parseBofaTransactionRowLayout", "date=$date name=$name value=$value")

        if (date == "Pending") return null
        val dateInt = formatter.parseOrNull(date.toString())?.toIntDate() ?: return null
        if (dateInt < latestDate) return null

        val valueDouble = value.toString()
            .filter { " ,$".indexOf(it) == -1 }
            .toDoubleOrNull()?.toLongDollar() ?: return null

        return ParsedTransaction(
            date = dateInt,
            name = name.toString(),
            value = valueDouble,
        )
    }
}