package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

object InAppReviewManager {
    private const val TAG = "InAppReviewManager"

    /**
     * Finds the Activity from a Context.
     */
    fun findActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

    /**
     * Initiates the official In-App Review Flow.
     * If it fails (e.g., Google Play services is missing, quota-limit reached, or other errors),
     * it gracefully falls back to opening the Google Play Store page of the app.
     */
    fun launchReviewFlow(context: Context) {
        val activity = findActivity(context)
        if (activity == null) {
            Log.e(TAG, "Context is not an Activity, falling back to Play Store page directly.")
            AppUpdateManager.openPlayStore(context, null)
            return
        }

        try {
            val manager = ReviewManagerFactory.create(context)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener { launchTask ->
                        if (launchTask.isSuccessful) {
                            Log.d(TAG, "In-App Review flow finished successfully.")
                        } else {
                            Log.e(TAG, "In-App Review flow failed, falling back.")
                            AppUpdateManager.openPlayStore(context, null)
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to request review flow: ${task.exception?.localizedMessage}. Falling back to Play Store.")
                    AppUpdateManager.openPlayStore(context, null)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in InAppReviewManager: ${e.localizedMessage}", e)
            AppUpdateManager.openPlayStore(context, null)
        }
    }
}
