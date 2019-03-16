package com.hjri.khandeland.messages.networking

import android.app.Activity
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.hjri.khandeland.messages.helper.HTTPHelper

class MessageResponseBody(

        @Expose
        @SerializedName("id")
        var id: Int,

        @Expose
        @SerializedName("user_id")
        var userId: Int,

        @Expose
        @SerializedName("content")
        var content: String,

        //NOTE This property is boolean but returned as int
        @Expose
        @SerializedName("accepted")
        var accepted: Int,

        @Expose
        @SerializedName("favorite_count")
        var favoriteCount: Int,

        @Expose
        @SerializedName("created_at")
        var createdAt: String,

        @Expose
        @SerializedName("updated_at")
        var updatedAt: String,

        @Expose
        @SerializedName("created_at_epoch")
        var createdAtEpoch: Double
)

class MessageBody (
        @Expose
        @SerializedName("content")
        var content: String
): SingleVersionBody()

class MessageSyncBody(

        @Expose
        @SerializedName("id")
        var id: Int,

        @Expose
        @SerializedName("syncDetails")
        var syncDetails: String
): SingleVersionBody()

class MessageListBody (
        activity: Activity,

        @Expose
        @SerializedName("last_update_epoch")
        var lastUpdateEpoch: Long = HTTPHelper(activity).lastUpdateEpoch
): SingleVersionBody()

class MessageListResponseBody (

        success: Boolean,
        message: String,

        @Expose
        @SerializedName("data")
        var data: ArrayList<MessageResponseBody>

): ResponseBody(success, message)