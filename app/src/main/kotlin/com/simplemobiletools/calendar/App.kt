package com.simplemobiletools.calendar

import android.support.multidex.MultiDexApplication
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.stetho.Stetho
import com.simplemobiletools.commons.extensions.checkUseEnglish
import com.squareup.leakcanary.LeakCanary
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig
import com.facebook.imagepipeline.core.ImagePipelineConfig



class App : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        val config = ImagePipelineConfig.newBuilder(this)
                .setProgressiveJpegConfig(SimpleProgressiveJpegConfig())
                .setResizeAndRotateEnabledForNetwork(true)
                .setDownsampleEnabled(true)
                .build()
        Fresco.initialize(this, config);
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
