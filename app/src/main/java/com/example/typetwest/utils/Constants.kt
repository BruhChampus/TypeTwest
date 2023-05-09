package com.example.typetwest.utils

import android.app.Activity
import android.net.Uri
import android.webkit.MimeTypeMap

object Constants {
    const val BASE_URL = "https://baconipsum.com"
    const val SIGN_IN_FRAGMENT = "sign_in_fragment"
    const val SECONDS = "seconds"
    const val MISTAKES = "mistakes"
    const val CORRECT = "correct"
    const val IMAGE = "image"
    const val AVG_WPM = "avgWpm"
    const val ACCURACY = "accuracy"

    const val READ_STORAGE_PERMISSION_CODE = 1


    const val USERS = "Users"

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

}