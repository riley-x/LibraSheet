package com.example.librasheet.screenReader

import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo



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
     *      [null] [null] <-- null (root)
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
    fun parse(reader: ScreenReader, event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.className == "androidx.recyclerview.widget.RecyclerView"
        ) {
            val node = event.source
            if (node?.viewIdResourceName == "com.infonow.bofa:id/recent_transactions") {
                val transactions = parseBofaRecentTransactions(node)
                val account = parseAccountName(reader.rootInActiveWindow) ?: "Unknown"
                ScreenReader.add(account, transactions)
            }
//            printAllViews(reader.rootInActiveWindow, maxDepth = 2)
//            printAllViews(node)
        }
    }


    /** This is for the source when scrolling the account detail screen. But this page is gimmicky
     * as noted above.
     */
    private fun parseBofaAccountDetailScreen(nodeInfo: AccessibilityNodeInfo?) {
        if (nodeInfo == null) return
        if (nodeInfo.childCount < 1) return

        val scrollContainer = nodeInfo.getChild(0)
        Log.v("Libra/ScreenReader/parseBofA", "${System.identityHashCode(scrollContainer)}")
        for (i in 0 until scrollContainer.childCount) {
            val c = scrollContainer.getChild(i)
            if (c.viewIdResourceName == "com.infonow.bofa:id/recent_transactions") {
                parseBofaRecentTransactions(c)
                return
            }
        }
    }

    /**
     *  {1} .[null] [SpicaHeader] <-- com.infonow.bofa:id/header_container
     *      ..[null] [Go back to previous screen] <-- com.infonow.bofa:id/__boa_header_left_button_click_area
     *      ..[null] [Products cart with 0 items. Button] <-- com.infonow.bofa:id/rl_shopping_cart
     *      ..[null] [null] <-- com.infonow.bofa:id/__boa_header_help_image_button_click_area
     *      ...[null] [Erica. There are 4 notifications.] <-- com.infonow.bofa:id/button_erica_icon_container
     */
    private fun parseAccountName(root: AccessibilityNodeInfo?): String? {
        if (root == null) return null
        if (root.childCount < 1) return null

        val headerContainer = root.getChild(0)
        if (headerContainer.childCount < 2) return null

        val accountName = headerContainer.getChild(1)
        if (accountName.viewIdResourceName != "com.infonow.bofa:id/__boa_header_tv_headerText") {
            Log.d("Libra/BofaReader/parseAccountName", "Mismatch resource name: ${accountName.viewIdResourceName}")
            return null
        }
        return accountName.text?.toString()
    }

    /**
     *  {2} .[null] [null] <-- com.infonow.bofa:id/recent_transactions
     *      ..[null] [null] <-- com.infonow.bofa:id/transaction_row_layout
     *      ..[null] [null] <-- com.infonow.bofa:id/transaction_row_layout
     *      ..(etc)
     */
    private fun parseBofaRecentTransactions(list: AccessibilityNodeInfo): MutableList<ParsedTransaction> {
        Log.v("Libra/ScreenReader/parseBofA", "childCount = ${list.childCount}")

        val out = mutableListOf<ParsedTransaction>()
        for (j in 0 until list.childCount) {
            val t = list.getChild(j)
            if (t.viewIdResourceName == "com.infonow.bofa:id/transaction_row_layout") {
                parseBofaTransactionRowLayout(t)?.let { out.add(it) }
            }
        }
        return out
    }

    /**
     *      ..[null] [null] <-- com.infonow.bofa:id/transaction_row_layout
     *      ...[null] [<info with lots of whitespace>] <-- com.infonow.bofa:id/recent_transaction_left_text_nocheck
     *      ....[Pending or Dec 1, 2022] [null] <-- null
     *      ....[PAYPAL *UBER] [null] <-- null
     *      ...[null] [<info with lots of whitespace>] <-- com.infonow.bofa:id/recent_transaction_right_text_nocheck
     *      ....[$38.95] [null] <-- null (amount)
     *      ....[$360.90] [null] <-- null (balance)
     */
    private fun parseBofaTransactionRowLayout(row: AccessibilityNodeInfo): ParsedTransaction? {
        if (row.childCount < 2) return null

        val leftInfo = row.getChild(0)
        if (leftInfo.childCount < 2) return null
        val date = leftInfo.getChild(0).text
        val name = leftInfo.getChild(1).text

        val rightInfo = row.getChild(1)
        if (rightInfo.childCount < 1) return null
        val value = rightInfo.getChild(0).text

        Log.v("Libra/BofaReader/parseBofaTransactionRowLayout", "date=$date name=$name value=$value")
        return ParsedTransaction(
            date = date.toString(),
            name = name.toString(),
            value = value.toString(),
        )
    }
}