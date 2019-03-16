package com.hjri.khandeland.messages.helper

import android.util.Log
import com.hjri.khandeland.messages.Configs

class Helper {

    companion object {

        fun writeDebugLog(message: String?) {
            Log.d(Configs.DEBUG_LOG_KEY, message)
        }
    }
}