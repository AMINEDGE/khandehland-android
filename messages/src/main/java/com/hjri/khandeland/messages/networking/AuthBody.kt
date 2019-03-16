package com.hjri.khandeland.messages.networking

import android.util.Log
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.hjri.khandeland.messages.BuildConfig
import com.hjri.khandeland.messages.Configs
import com.hjri.khandeland.messages.helper.Helper
import com.hjri.khandeland.messages.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


// Registration

class RegisterBody (

        @Expose
        @SerializedName("username")
        var username: String,

        @Expose
        @SerializedName("password")
        var password: String,

        @Expose
        @SerializedName("email")
        var email: String? = null,

        @Expose
        @SerializedName("version")
        var version: String = BuildConfig.VERSION_NAME
)

class RegisterMessageBody (

        @Expose
        @SerializedName("token")
        var token: String
)

class RegisterResponseBody (

        @Expose
        @SerializedName("message")
        var message: RegisterMessageBody
)


// Login

class LoginBody (

        @Expose
        @SerializedName("username")
        var username: String,

        @Expose
        @SerializedName("password")
        var password: String,

        @Expose
        @SerializedName("client_id")
        var clientId: Int,

        @Expose
        @SerializedName("client_secret")
        var clientSecret: String,

        @Expose
        @SerializedName("grant_type")
        var grantType: String = "password",

        @Expose
        @SerializedName("version")
        var version: String = BuildConfig.VERSION_NAME
)

class LoginResponseBody (

        @Expose
        @SerializedName("access_token")
        var accessToken: String,

        @Expose
        @SerializedName("refresh_token")
        var refreshToken: String
)

class AuthHTTPHelper {
    companion object {

        fun registerAndUpdateUser(username: String, password: String, email: String? = null, completion: (success: Boolean) -> Unit) {

            Configs.retrofitInstance
                    .create(Routes.Auth::class.java)
                    .register(RegisterBody(username, password, email))
                    .enqueue(object: Callback<RegisterResponseBody> {

                        override fun onResponse(call: Call<RegisterResponseBody>, response: Response<RegisterResponseBody>) {
                            try {
                                if (response.isSuccessful) {
                                    val token = response.body()?.message?.token ?: throw NullPointerException("TOKEN IS NULL")
                                    User.make(username, email, password, token)
                                    completion(true)
                                } else {
                                    Helper.writeDebugLog("Registration Failed: ${"${response.code()} - ${response.message()}"}")
                                    completion(false)
                                }
                            } catch (e: Exception) {
                                Helper.writeDebugLog("REGISTRATION EXCEPTION: " + e.message)
                                completion(false)
                            }
                        }

                        override fun onFailure(call: Call<RegisterResponseBody>, t: Throwable) {
                            Helper.writeDebugLog("REGISTRATION CONNECTION FAILED: " + t.message)
                            completion(false)
                        }

                    })
        }

        fun loginAndUpdateUser(username: String, password: String, completion: (success: Boolean) -> Unit) {
            Configs.retrofitInstanceWithoutAPIRoute.create(Routes.Auth::class.java)
                    .login(LoginBody(username, password, Configs.clientId, Configs.clientSecret))
                    .enqueue(object: Callback<LoginResponseBody> {

                        override fun onResponse(call: Call<LoginResponseBody>, response: Response<LoginResponseBody>) {
                            if (response.isSuccessful) {
                                User.make(username, null, password)

                                updateUserToken(response.body())

                                completion(true)
                            } else {
                                Helper.writeDebugLog("Login Failed")
                                completion(false)
                            }
                        }

                        override fun onFailure(call: Call<LoginResponseBody>, t: Throwable) {
                            Helper.writeDebugLog("Login Connection Failure: " + t.message)
                            completion(false)
                        }

                    })
        }

        private fun updateUserToken(response: LoginResponseBody?) {

            var accessToken: String? = null
            var refreshToken: String? = null

            try {
                accessToken = response?.accessToken ?: throw NullPointerException("Access Token is null")
                refreshToken = response?.refreshToken ?: throw NullPointerException("Refresh Token is null")
            } catch (e: Exception) {
                Helper.writeDebugLog("Update User Token Failed: " + e.message)
            }

            User.update(accessToken, refreshToken)
        }
    }
}