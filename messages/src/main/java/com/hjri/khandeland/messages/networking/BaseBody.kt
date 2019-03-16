package com.hjri.khandeland.messages.networking

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.hjri.khandeland.messages.BuildConfig
import com.hjri.khandeland.messages.model.Message

open class SingleVersionBody(

        @Expose
        @SerializedName("version")
        val version: String = BuildConfig.VERSION_NAME
)

open class ResponseBody (

        @Expose
        @SerializedName("success")
        var success: Boolean,

        @Expose
        @SerializedName("message")
        var message: String
)

