package com.example.campushop.data

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.collections.get
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object CloudinaryHelper {

    fun initialize(context: Context) {
        val config = HashMap<String, String>()
        config["cloud_name"] = "dkxfegrt4"      // ← Replace with yours
        config["api_key"] = "244785283188837"            // ← Replace with yours
        config["api_secret"] = "Q4bOVFtA93HC17IoC-SPLpaO5rA"      // ← Replace with yours

        MediaManager.init(context, config)
    }

    suspend fun uploadImage(imageUri: Uri): String = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(imageUri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Upload started
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Progress update
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as String
                    continuation.resume(imageUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }
            })
            .dispatch()
    }
}