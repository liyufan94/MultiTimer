package com.example.multitimer

import android.os.CountDownTimer

class TimerItem(
    val name: String,
    var remainingMillis: Long,
    val totalMillis: Long,
    var endTime: Long = 0L
) {
    var timer: CountDownTimer? = null

    fun formatTime(): String {
        val totalSec = remainingMillis / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%02d:%02d".format(min, sec)
    }

}