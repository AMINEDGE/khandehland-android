package com.hjri.khandeland.messages.helper

import android.app.Activity
import android.util.Log
import com.hjri.khandeland.messages.Configs
import com.hjri.khandeland.messages.model.Message
import com.hjri.khandeland.messages.model.User
import com.hjri.khandeland.messages.networking.*
import io.realm.Realm
import io.realm.RealmQuery
import io.realm.RealmResults
import io.realm.Sort
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MessageHelper (val activity: Activity) {

    fun load(showingFavoriteList: Boolean) {

        activity.runOnUiThread {
            val realm = Realm.getDefaultInstance()

            val messageQuery: RealmQuery<Message> = if (showingFavoriteList) {
                realm.where(Message::class.java).equalTo("isFavorite", true)
            } else {
                realm.where(Message::class.java)
            }

            messages = messageQuery.sort("submitDateEpoch", Sort.DESCENDING).findAll()

            Log.d(Configs.DEBUG_LOG_KEY, String.format("%sMessages Queried, Count: %d", if (showingFavoriteList) "Favorite " else "", messages.count()))
        }

    }

    companion object {

        lateinit var messages: RealmResults<Message>

        fun saveList(activity: Activity, messageJSONResponseBodies: ArrayList<MessageResponseBody>, completion: (success: Boolean) -> Unit) {

            val messagesToSave = ArrayList<Message>()

            for (messageBody in messageJSONResponseBodies) {
                val newMessage = Message()
                newMessage.id = messageBody.id
                newMessage.content = messageBody.content
                newMessage.submitDateEpoch = messageBody.createdAtEpoch
                newMessage.submitDate = messageBody.createdAt
                newMessage.favoriteCount = messageBody.favoriteCount


                val shouldDelete: Boolean = messageBody.accepted == 0

                if (shouldDelete) {
                    Message.shouldDelete(newMessage.id)
                    continue
                }

                newMessage.isNew = !DatabaseHelper.isDuplicate<Message>(newMessage.id)

                if (!newMessage.isNew) {

                    val wasFavorite = Message.deleteWithSameIdAndWasItFavorite(newMessage.id)

                    newMessage.isFavorite = wasFavorite
                }

                messagesToSave.add(newMessage)
            }

            DatabaseHelper.saveArray(messagesToSave, completion)
            MessageHelper(activity).load(showingFavoriteList = false)
        }

        fun sync(completion: () -> Unit) {

            Helper.writeDebugLog("*** Syncing Messages ***")

            val realm = Realm.getDefaultInstance()
            val allMessages = realm.where(Message::class.java).findAll()

            val messageBodiesToSync: ArrayList<MessageSyncBody> = arrayListOf()
            val messagesToSync: ArrayList<Message> = arrayListOf()

            val favoriteShouldSync = Configs.favoriteShouldSync

            for (message in allMessages) {
                if (message.needsSync) {

                    // For updating after response
                    messagesToSync.add(message)

                    // For sending as request body
                    messageBodiesToSync.add(MessageSyncBody(message.id, message.syncDetails))
                }
            }

            realm.close()

            Configs.retrofitInstance.create(Routes.AppStart::class.java)
                    .sync(User.tokenAuthorizationHeader, SyncBody(messageBodiesToSync, favoriteShouldSync))
                    .enqueue(object: Callback<SyncResponseBody> {

                        override fun onResponse(call: Call<SyncResponseBody>, response: Response<SyncResponseBody>) {

                            if (response.isSuccessful) {

                                if (!messagesToSync.isEmpty()) {
                                    for (message in messagesToSync) {
                                        message.update(null, null, null, false, "")
                                    }
                                }

                                if (favoriteShouldSync) {
                                    try {
                                        val responseData = response.body()?.data ?: throw NullPointerException("Response Data is NULL")
                                        val favoriteIds = responseData.favoriteIds ?: throw NullPointerException("Favorite ID is NULL")
                                        val lastSyncAt = responseData.lastSyncAt ?: throw NullPointerException("Last Sync At is NULL")

                                        if (!favoriteIds.isEmpty()) {
                                            Message.setFavorite(true, favoriteIds)
                                        }

                                        if (!lastSyncAt.isEmpty()) {
                                            Message.makeOldBefore(lastSyncAt)
                                        }
                                    } catch (e: Exception) {
                                        Helper.writeDebugLog("JSON ERROR: FavoriteSync JSON Data is not correct: ${e.message}")
                                    }
                                }
                            } else {
                                Helper.writeDebugLog("MESSAGE SYNC FAILURE: " + response.code() + ", " + response.message())
                            }

                            completion()
                        }

                        override fun onFailure(call: Call<SyncResponseBody>, t: Throwable) {
                            Helper.writeDebugLog("Message Sync Failed: ${t.localizedMessage}")
                            completion()
                        }

                    })
        }

        fun fetchAndLoad(activity: Activity, completion: () -> Unit = {}) {

            Helper.writeDebugLog("*** Fetching New Messages ***")

            val retrofit = Retrofit.Builder().baseUrl(Routes.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
            val service = retrofit.create(Routes.Message::class.java)
            service.listMessages(MessageListBody(activity), 0, 0).enqueue(object: retrofit2.Callback<MessageListResponseBody> {


                override fun onResponse(call: Call<MessageListResponseBody>, response: Response<MessageListResponseBody>) {
                    if (response.isSuccessful) {

                        try {

                            HTTPHelper(activity).refreshLastUpdateEpoch()

                            val messageResponseList = response.body()?.data ?: throw NullPointerException("Data is null or empty")

                            if (Configs.favoriteShouldSync) {
                                Configs.favoriteShouldSync = false
                            } else {
                                Message.makeOld()
                            }

                            MessageHelper.saveList(activity, messageResponseList) { success ->
                                completion()
                            }

                            Log.d(Configs.DEBUG_LOG_KEY, "SUCCESS: Fetch Done, First Content: " + messageResponseList[0].content)
                        } catch (e: Exception) {
                            Helper.writeDebugLog("Fetch Message Error: " + e.message)

                            completion()
                        }
                    } else {
                        Log.d(Configs.DEBUG_LOG_KEY, "Fetch Message Failed, CAUSE: " + response.message())

                        completion()
                    }
                }

                override fun onFailure(call: Call<MessageListResponseBody>, t: Throwable) {
                    Log.d(Configs.DEBUG_LOG_KEY, "CANNOT_CONNECT ERROR: Fetch Messages Failed, Cause: " + t.message)
                }
            })
        }

    }
}