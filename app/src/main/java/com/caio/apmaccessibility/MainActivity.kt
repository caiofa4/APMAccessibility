package com.caio.apmaccessibility

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.caio.apmaccessibility.SharedState.cellularReconnectionTime
import com.caio.apmaccessibility.SharedState.dataReconnectionTime
import com.caio.apmaccessibility.SharedState.shouldToggle
import com.caio.apmaccessibility.ui.theme.APMAccessibilityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            APMAccessibilityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Enable Accessibility Permission")
        }

        Button(
            onClick = {
                openAPMSettings()
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Toggle APM")
        }

        if (cellularReconnectionTime.longValue > 0) {
            Text("Cellular reconnected in ${cellularReconnectionTime.longValue} ms")
        }

        if (dataReconnectionTime.longValue > 0) {
            Text("Mobile data reconnected in ${dataReconnectionTime.longValue} ms")
        }
    }
}

fun initTracker(): AirplaneReconnectTracker {
    return AirplaneReconnectTracker(MyApplication.instance, object : AirplaneReconnectTracker.ReconnectCallback {
        override fun onAirplaneModeDisabled() {
            Log.d("TESTTAG", "Airplane mode disabled, monitoring...")
        }

        override fun onCellularReconnected(elapsedMillis: Long) {
            Log.d("TESTTAG", "Cellular reconnected in $elapsedMillis ms")
            cellularReconnectionTime.longValue = elapsedMillis
        }

        override fun onMobileDataReconnected(elapsedMillis: Long) {
            Log.d("TESTTAG", "Mobile data reconnected in $elapsedMillis ms")
            dataReconnectionTime.longValue = elapsedMillis
        }
    })
}

fun openAPMSettings() {
    val tracker = initTracker()
    tracker.start()
    shouldToggle = true
    val context = MyApplication.instance
    val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    APMAccessibilityTheme {
        MainScreen()
    }
}