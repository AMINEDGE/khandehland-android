package com.hjri.khandeland.messages.networking

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.hjri.khandeland.messages.model.Message
import java.util.*
import kotlin.collections.ArrayList

// Get Configs

enum class UpdateResponseState(private val state: String) {
    UPDATED("allowed"),
    OPTIONAL("optional"),
    NECESSARY("not_supported");

    companion object {
        fun from(findValue: String): UpdateResponseState = UpdateResponseState.values().first { it.state == findValue }
    }
}

data class UpdateData (

        @Expose
        @SerializedName("link")
        var link: String?,

        @Expose
        @SerializedName("state")
        var state: String?
)

data class ConfigBody (

    @Expose
    @SerializedName("update_data")
    var updateData: UpdateData,

    @Expose
    @SerializedName("show_message_favorite_count")
    var showMessageFavoriteCount: Boolean?,

    @Expose
    @SerializedName("should_show_premium_and_ads")
    var shouldShowPremiumAndAds: Boolean?,

    @Expose
    @SerializedName("premium_price")
    var premiumPrice: String?
): SingleVersionBody()

class ConfigResponseBody (

        success: Boolean,
        message: String,

        @Expose
        @SerializedName("data")
        var data: ConfigBody?
): ResponseBody(success, message)


// Sync

data class SyncBody (

        @Expose
        @SerializedName("messages")
        var messages: ArrayList<MessageSyncBody>? = arrayListOf(),

        @Expose
        @SerializedName("favorite_list")
        var favoriteList: Boolean? = false

): SingleVersionBody()

class SyncResponseData (

        @Expose
        @SerializedName("favorite_ids")
        var favoriteIds: ArrayList<Int>? = arrayListOf(),

        @Expose
        @SerializedName("last_sync_at")
        var lastSyncAt: String?
)

class SyncResponseBody (

      success: Boolean,
      message: String,

      @Expose
      @SerializedName("data")
      var data: SyncResponseData?

): ResponseBody(success, message)