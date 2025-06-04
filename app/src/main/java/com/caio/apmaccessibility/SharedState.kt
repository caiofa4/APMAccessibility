package com.caio.apmaccessibility

import androidx.compose.runtime.mutableLongStateOf

object SharedState {
    var shouldToggle = false
    var cellularReconnectionTime = mutableLongStateOf(0L)
    var dataReconnectionTime = mutableLongStateOf(0L)
}