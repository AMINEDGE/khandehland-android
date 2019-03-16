package com.hjri.khandeland.messages.helper

import android.app.Activity
import android.content.Context
import android.util.Log
import com.hjri.khandeland.messages.Configs
import com.hjri.khandeland.messages.networking.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class RefreshHelper(val activity: Activity, val next: () -> Unit = {}) {

    private var messageFetched = false
    private var premiumDone = false

    fun run(isOnline: Boolean = Configs(activity).isOnline, serverError: Boolean = false, showAlert: Boolean = true) {

        if (isOnline) {
            Configs.retrofitInstance.create(Routes.AppStart::class.java)

                    .getConfigs(SingleVersionBody())

                    .enqueue(object : retrofit2.Callback<ConfigResponseBody> {

                        override fun onResponse(call: Call<ConfigResponseBody>, response: Response<ConfigResponseBody>) {

                            Helper.writeDebugLog("*** Config Response Fetched ***")

                            try {

                                val responseData = response.body()?.data ?: throw NullPointerException("Data is Null")

                                val updateData = responseData.updateData

                                if (response.isSuccessful) {

                                    if (Configs.isLogin) {

                                        MessageHelper.sync {
                                            handleVersionSupported(responseData ,updateData)
                                        }
                                    } else {
                                        handleVersionSupported(responseData ,updateData)
                                    }
                                }
                                // This else block wouldn't usually be reached since if the server error happens
                                // the response body throws exception and the catch block will be executed but that's fine tho
                                else {
                                    run(false, true)
                                }
                            } catch (e: Exception) {

                                e.printStackTrace()

                                if (response.code() == 401) {
                                    try {

                                        // response.errorBody() value became null after I used it in Log.d to see the content
                                        // so I deleted the log part and used it to deserialize and use the value
                                        val errorBody = JSONObject(response.errorBody()?.string())
                                        val updateBody = errorBody.getJSONObject("data").getJSONObject("update_data")
                                        val updateData = UpdateData(updateBody.getString("link"), updateBody.getString("state"))
                                        handleUpdateState(updateData)
                                    } catch (e: Exception) {
                                        Helper.writeDebugLog("Showing Update Alert Failed: ${e.message}")
                                        run(false, true)
                                    }
                                } else {

                                    run(false, true)
                                }
                                return
                            }
                        }

                        override fun onFailure(call: Call<ConfigResponseBody>, t: Throwable) {
                            Log.d(Configs.DEBUG_LOG_KEY, "CONNECTION FAILURE: GetConfig Connection Failed, Message: " + t.message)
                            run(false, true)
                            return
                        }

                    })
        } else {
            Log.d(Configs.DEBUG_LOG_KEY, "App is Running in offline mode")
            MessageHelper(activity).load(false)
            activity.runOnUiThread {

                if (showAlert) {
                    AlertHelper.showNoConnectionError(activity, serverError) {
                        next()
                    }
                } else next()
            }
        }
    }


    private fun handleUpdateState(updateData: UpdateData) {

        try {
            val stateValue = updateData.state ?: throw NullPointerException("State Value is Null")

            val state: UpdateResponseState = UpdateResponseState.from(stateValue)

            val downloadLink = updateData.link ?: throw NullPointerException("Update Download Link is Null")

            Configs(activity).updateDownloadLink = downloadLink

            when (state) {
                UpdateResponseState.UPDATED -> {
                    Log.d(Configs.DEBUG_LOG_KEY, "UPDATE RESPONSE STATE: Allowed")
                    MessageHelper.fetchAndLoad(activity) {
                        handleMessagesFetched()
                    }
                }
                UpdateResponseState.OPTIONAL, UpdateResponseState.NECESSARY -> {
                    handleUpdateAvailable(downloadLink, state) {

                        if (Configs.updateSupported) {
                            MessageHelper.fetchAndLoad(activity) {
                                handleMessagesFetched()
                            }
                        } else {
                            run(false, false, false)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(Configs.DEBUG_LOG_KEY, "UPDATE DATA EXCEPTION: Update Data JSON Dictionary Pattern is not correct: " + e.message)
            handleMessagesFetched()
        }
    }

    private fun handleMessagesFetched() {

        Helper.writeDebugLog("Messages Fetch DONE")

        messageFetched = true
        nextIfAllDone()
    }

    private fun handlePremiumDone() {

        Helper.writeDebugLog("Premium Setup DONE")

        premiumDone = true
        nextIfAllDone()
    }

    private fun handleUpdateAvailable(updateLink: String, state: UpdateResponseState, next: () -> Unit = {}) {
        if (state == UpdateResponseState.NECESSARY || (state == UpdateResponseState.OPTIONAL && Configs.isFirstTimeShowingAlert)) {
            AlertHelper.showUpdateAvailable(activity, updateLink, state, next)
        }

        Configs.updateSupported = state != UpdateResponseState.NECESSARY
        Configs.isFirstTimeShowingAlert = false
    }


    private fun handleVersionSupported(responseData: ConfigBody, updateData: UpdateData) {


        Helper.writeDebugLog("*** Handling version supported ***")

        handleUpdateState(updateData)
        updateConfigs(responseData)
        PremiumHelper(activity).init {
            handlePremiumDone()
        }
    }

    private fun updateConfigs(data: ConfigBody) {

        if (!Configs.updateSupported) {
            return
        }

        Helper.writeDebugLog("Updating Configs")

        try {
            val showMessageFavoriteCount = data.showMessageFavoriteCount
                    ?: throw NullPointerException("showMessageFavoriteCount is null")
            val shouldShowPremiumAndAds = data.shouldShowPremiumAndAds
                    ?: throw NullPointerException("shouldShowPremiumAndAds is null")
            val premiumPrice = data.premiumPrice
                    ?: throw NullPointerException("premiumPrice is null")

            Configs.showMessageFavoriteCount = showMessageFavoriteCount
            Configs.shouldShowIABAndAds = shouldShowPremiumAndAds
            Configs(activity).premiumPrice = premiumPrice
        } catch (e: Exception) {
            Helper.writeDebugLog("UPDATE CONFIG JSON ERROR: " + e.message)
            run(false, true)
        }

    }


    private fun nextIfAllDone() {
        if (premiumDone && messageFetched) next()
    }
}