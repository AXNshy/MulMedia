package com.luffyxu.mulmedia.util

import android.util.Log
import java.util.Timer
import kotlin.concurrent.fixedRateTimer

class TimerState(val name: String = "", var canceled: () -> Unit = {}) {
    var remain = 0

    var timer: Timer? = null
    fun start() {
        remain = 3
        if (timer == null) {
            timer = fixedRateTimer(name, false, 0, 1000) {
                Log.d("TimerState-$name", "")
                if (remain <= 0) {
                    timer?.cancel()
                    timer = null
                    canceled()
                }
            }
        }
    }


}