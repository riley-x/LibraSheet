package com.example.librasheet.screenReader

import android.annotation.SuppressLint
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.librasheet.data.*
import com.example.librasheet.ui.components.parseOrNull
import java.text.SimpleDateFormat


object VenmoReader {
    const val venmoName = "Venmo"

    /**
     * Filter transactions page (select both payment received and payment sent), 2048 event.source:
     *
     *      [null] [null] <-- com.venmo:id/container
     *      .{{ }}
     *      .[null] [null] <-- com.venmo:id/transaction_history_list_view
     *
     *      ..[null] [Abbey Li, $13.50 debited, December 6th, 2022, Audience - Private, "Joju"] <-- null
     *      ...[Abbey Li] [null] <-- com.venmo:id/transaction_display_name
     *      ...[- $13.50] [null] <-- com.venmo:id/transaction_amount
     *      ...[Dec 6 ·] [null] <-- com.venmo:id/transaction_running_balance_time_since
     *      ...["Joju"] [null] <-- com.venmo:id/transaction_description
     *
     *      ..[null] [Sunny Gakhar, $31.25 credited, December 5th, 2022, Audience - Private, "Sake tini + gyudon + (takoyaki + karaage) / 3<\n><\n>Abbey will cover the desserts LOL ?"] <-- null
     *      ...[Sunny Gakhar] [null] <-- com.venmo:id/transaction_display_name
     *      ...[+ $31.25] [null] <-- com.venmo:id/transaction_amount
     *      ...[Dec 5 ·] [null] <-- com.venmo:id/transaction_running_balance_time_since
     *      ...["Sake tini + gyudon + (takoyaki + karaage) ..."] [null] <-- com.venmo:id/transaction_description
     */
    fun parse(reader: ScreenReader, event: AccessibilityEvent): Pair<String, List<ParsedTransaction>>? {

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return null
        val node = event.source ?: return null
        if (node.viewIdResourceName != "com.venmo:id/container") return null

        val lastDate = reader.getLatestDate(venmoName)
        var transactions: List<ParsedTransaction>? = null

        for (i in 0 until node.childCount) {
            val historyList = node.getChild(i) ?: continue
            if (historyList.viewIdResourceName == "com.venmo:id/transaction_history_list_view") {
                transactions = parseTransactionHistoryList(historyList, lastDate)
                break
            }
        }

        if (transactions == null) return null
        return venmoName to transactions
    }


    /**
     *      .[null] [null] <-- com.venmo:id/transaction_history_list_view
     *      ..[null] [Abbey Li, $13.50 debited, December 6th, 2022, Audience - Private, "Joju"] <-- null
     *      ..{{ }}
     */
    private fun parseTransactionHistoryList(historyList: AccessibilityNodeInfo, lastDate: Int): List<ParsedTransaction> {
        Log.v("Libra/VenmoReader/parseTransactionHistoryList", "childCount=${historyList.childCount}")
        val out = mutableListOf<ParsedTransaction>()
        for (i in 0 until historyList.childCount) {
            val node = historyList.getChild(i) ?: continue
            val t = parseTransaction(node, lastDate) ?: continue
            out.add(t)
        }
        return out
    }

    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("MMM d").apply { isLenient = false }

    /**
     *      ..[null] [Sunny Gakhar, $31.25 credited, December 5th, 2022, Audience - Private, "Sake tini + gyudon + (takoyaki + karaage) / 3<\n><\n>Abbey will cover the desserts LOL ?"] <-- null
     *      ...[Sunny Gakhar] [null] <-- com.venmo:id/transaction_display_name
     *      ...[+ $31.25] [null] <-- com.venmo:id/transaction_amount
     *      ...[Dec 5 ·] [null] <-- com.venmo:id/transaction_running_balance_time_since
     *      ...["Sake tini + gyudon + (takoyaki + karaage) ..."] [null] <-- com.venmo:id/transaction_description
     */
    private fun parseTransaction(node: AccessibilityNodeInfo, lastDate: Int): ParsedTransaction? {
        if (node.childCount < 4) return null
        val contentDescription = node.contentDescription.toString()

        val dateText = node.getChild(2)?.text?.toString() ?: return null
        val monthDate = formatter.parseOrNull(dateText)?.toIntDate() ?: return null
        val year = contentDescription
            .substringBefore(", Audience - ")
            .takeLast(4)
            .toIntOrNull() ?: return null
        val date = getIntDate(year, getMonth(monthDate), getDay(monthDate))
        if (date < lastDate) return null

        val valueText = node.getChild(1)?.text ?: return null
        val value = valueText.toString()
            .filterNot { ",+ $".contains(it) }
            .toDoubleOrNull()
            ?.toLongDollar() ?: return null

        val person = node.getChild(0)?.text ?: return null
        val description = contentDescription
            .substringAfter("Audience - ")
            .substringAfter(", ")

        Log.v("Libra/VenmoReader/parseTransaction", "date=$date value=$value person=$person description=$description")
        return ParsedTransaction(
            date = date,
            name = "$person: $description",
            value = value,
        )
    }

    /**
     * "Me" page, 2048 event.source: this lacks the transaction descriptions...
     *      [null] [null] <-- null
     *      .{{ other tabs seem to be loaded here too }}
     *
     *      .[null] [null] <-- com.venmo:id/your_money_container
     *      ..{{ }}
     *      ..[null] [null] <-- com.venmo:id/view_pager
     *      ...[null] [null] <-- com.venmo:id/transaction_history_list_view
     *
     *      ....[null] [null] <-- null
     *      .....[Completed] [null] <-- com.venmo:id/transaction_history_header_status
     *      .....[2022] [null] <-- com.venmo:id/transaction_history_header_year
     *      .....[null] [Search Transactions] <-- com.venmo:id/search_glass
     *
     *      ....[null] [Abbey Li, $13.50 debited, December 6th, 2022, Audience - Private] <-- null
     *      .....[Abbey Li] [null] <-- com.venmo:id/transaction_display_name
     *      .....[- $13.50] [null] <-- com.venmo:id/transaction_amount
     *      .....[Dec 6 ·] [null] <-- com.venmo:id/transaction_running_balance_time_since
     *
     *      ....[null] [Sunny Gakhar, $31.25 credited, December 5th, 2022, Audience - Private] <-- null
     *      .....[Sunny Gakhar] [null] <-- com.venmo:id/transaction_display_name
     *      .....[+ $31.25] [null] <-- com.venmo:id/transaction_amount
     *      .....[Dec 5 ·] [null] <-- com.venmo:id/transaction_running_balance_time_since
     */
}