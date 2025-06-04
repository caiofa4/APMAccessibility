package com.caio.apmaccessibility

import android.content.*
import android.net.*
import android.telephony.*

class AirplaneReconnectTracker(
    private val context: Context,
    private val callback: ReconnectCallback
) {
    interface ReconnectCallback {
        fun onAirplaneModeDisabled()
        fun onCellularReconnected(elapsedMillis: Long)
        fun onMobileDataReconnected(elapsedMillis: Long)
    }

    private var startTime: Long = 0L
    private var isListening = false

    private val airplaneModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
                val isAirplaneModeOn = intent.getBooleanExtra("state", false)
                if (!isAirplaneModeOn) {
                    startTime = System.currentTimeMillis()
                    callback.onAirplaneModeDisabled()
                    startMonitoring()
                }
            }
        }
    }

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onServiceStateChanged(serviceState: ServiceState?) {
            super.onServiceStateChanged(serviceState)
            if (serviceState?.state == ServiceState.STATE_IN_SERVICE) {
                val elapsed = System.currentTimeMillis() - startTime
                callback.onCellularReconnected(elapsed)
                stopTelephonyListener()
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            val elapsed = System.currentTimeMillis() - startTime
            callback.onMobileDataReconnected(elapsed)
            stopNetworkCallback()
        }
    }

    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun start() {
        if (!isListening) {
            context.registerReceiver(
                airplaneModeReceiver,
                IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            )
            isListening = true
        }
    }

    fun stop() {
        if (isListening) {
            context.unregisterReceiver(airplaneModeReceiver)
            stopTelephonyListener()
            stopNetworkCallback()
            isListening = false
        }
    }

    private fun startMonitoring() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE)

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    private fun stopTelephonyListener() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
    }

    private fun stopNetworkCallback() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
        }
    }
}