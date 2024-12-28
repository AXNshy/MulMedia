package com.luffy.mulmedia

import android.app.Application
import android.content.Context
import com.luffyxu.base.utils.ContextUtils

class MyApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        ContextUtils.context = base
    }
}