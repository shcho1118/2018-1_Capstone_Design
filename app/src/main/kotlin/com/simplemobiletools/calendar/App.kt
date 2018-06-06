package com.simplemobiletools.calendar

import android.support.multidex.MultiDexApplication
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.stetho.Stetho
import com.simplemobiletools.commons.extensions.checkUseEnglish
import com.squareup.leakcanary.LeakCanary

class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this);
        /* if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                return
            }
            LeakCanary.install(this)
            Stetho.initializeWithDefaults(this)
        } */

        checkUseEnglish()
    }
}
