package com.example

import android.app.Application
<<<<<<< HEAD
=======
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
>>>>>>> 6e834ed (Update Taqwahub)
import com.example.data.room.TaqwaDatabase
import com.example.util.SchedulerUtil
import com.example.util.FontHelper

class TaqwaApplication : Application() {
    val database: TaqwaDatabase by lazy { TaqwaDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        SchedulerUtil.scheduleDailyReminders(this)
        
<<<<<<< HEAD
=======
        // Initialize Google Mobile Ads SDK asynchronously with strict Families Policy
        try {
            val requestConfiguration = MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .build()
            MobileAds.setRequestConfiguration(requestConfiguration)

            MobileAds.initialize(this) {}
        } catch (e: Exception) {
            e.printStackTrace()
        }

>>>>>>> 6e834ed (Update Taqwahub)
        // Initialize dynamic loading and download of beautiful Quranic fonts (Amiri & Scheherazade New)
        FontHelper.checkAndLoadFonts(this)
    }
}
