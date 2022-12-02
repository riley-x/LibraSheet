package com.example.librasheet.screenReader

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class ScreenReader : AccessibilityService() {
    override fun onInterrupt() {}

    override fun onServiceConnected() {
        Log.d("Libra/ScreenReader", "onServiceConnected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        Log.v("Libra/ScreenReader", String.format(
            "onAccessibilityEvent: type = [ %s ], class = [ %s ], package = [ %s ], time = [ %s ], text = [ %s ]",
            event.eventType, event.className, event.packageName,
            event.eventTime, event.text));
    }
}
