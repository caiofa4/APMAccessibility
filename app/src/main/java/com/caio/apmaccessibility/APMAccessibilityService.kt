package com.caio.apmaccessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.caio.apmaccessibility.AppPackageNames.settings
import com.caio.apmaccessibility.SharedState.shouldToggle
import com.caio.apmaccessibility.ViewIds.apmSwitch

class APMAccessibilityService : AccessibilityService() {
    private val tag = "APMAccessibility"

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(tag, "onAccessibilityEvent")
        val rootNode = rootInActiveWindow

        val isAPMOn = isAirplaneModeOn()
        Log.d(tag, "isAPMOn: $isAPMOn")
        if (event.packageName == settings && shouldToggle) {
            rootNode?.let { node ->
                Log.d(tag, "toggleAPM")
                toggleAPM(node)
            }
        }
    }

    override fun onInterrupt() {
        Log.d(tag, "Service interrupted")
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            packageNames = arrayOf(settings)
        }
        serviceInfo = info
        Log.d(tag, "Service connected")
    }

    private fun isAirplaneModeOn(): Boolean {
        val context = MyApplication.instance
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
    }

    private fun toggleAPM(node: AccessibilityNodeInfo) {
        try {
            Thread.sleep(500)
            pressButton(node, apmSwitch)
            Log.d(tag, "tracker start")
            shouldToggle = false
            Thread.sleep(500)
            launchPoCApp()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun getValidNode(node: AccessibilityNodeInfo, id: String): AccessibilityNodeInfo? {
        Log.d(tag, "getValidNode")
        val nodeList = node.findAccessibilityNodeInfosByViewId(id)
        if (nodeList.isNotEmpty()) {
            return nodeList.first()
        }
        return null
    }

    private fun pressButton(node: AccessibilityNodeInfo, id: String) {
        Log.d(tag, "pressButton")
        val buttonNode = getValidNode(node, id)
        buttonNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: kotlin.run {
            Log.d(tag, "Button not found")
            throw Exception("Button not found")
        }
    }

    private fun launchPoCApp() {
        Log.d(tag, "launchPoCApp")
        val launchIntent = packageManager.getLaunchIntentForPackage("com.caio.apmaccessibility")
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launchIntent)
    }
}