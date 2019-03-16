package com.hjri.khandeland.messages.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.hjri.khandeland.messages.Configs
import com.hjri.khandeland.messages.networking.UpdateResponseState
import java.sql.Timestamp

class HTTPHelper(val activity: Activity) {

    var lastUpdateEpoch: Long = 0
    get(){
        return activity.getPreferences(Context.MODE_PRIVATE).getLong(Configs.LAST_UPDATE_PREFERENCE_KEY, 0)
    }

    fun refreshLastUpdateEpoch() {
        val newEpoch = Timestamp(System.currentTimeMillis()).time / 1000
        with (activity.getPreferences(Context.MODE_PRIVATE).edit()) {
            putLong(Configs.LAST_UPDATE_PREFERENCE_KEY, newEpoch)
            apply()
        }
    }

    companion object {

        @JvmStatic
        fun openLinkInBrowser(context: Context, downloadLink: String) {

            val uris = Uri.parse(downloadLink)
            val intents = Intent(Intent.ACTION_VIEW, uris)
            context.startActivity(intents)
        }
    }
}