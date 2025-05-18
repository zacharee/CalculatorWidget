package com.zacharee1.calculatorwidget

import android.app.Application
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.performance.BugsnagPerformance

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        Bugsnag.start(this)
        BugsnagPerformance.start(this)
    }
}
