package com.hjri.khandeland.messages.model

import com.hjri.khandeland.messages.helper.Helper
import io.realm.Realm
import io.realm.RealmObject

open class User: RealmObject() {

    var username: String? = ""
    var email: String? = ""
    var password: String? = ""
    var accessToken: String? = ""
    var refreshToken: String? = ""
    var isPremium: Boolean = false

    companion object {

        // Static Functions

        @JvmStatic
        fun update(accessToken: String? = null, refreshToken: String? = null, isPremium: Boolean? = null) {

            val realm = Realm.getDefaultInstance()
            val user = current

            if (user == null) {
                Helper.writeDebugLog("Update Token Failed: Current User Doesn't exist")
                return
            }

            realm.beginTransaction()

            user.accessToken = accessToken ?: user.accessToken
            user.refreshToken = refreshToken ?: user.refreshToken
            user.isPremium = isPremium ?: user.isPremium

            realm.commitTransaction()

            realm.close()
        }

        @JvmStatic
        fun make(username: String, email: String?, password: String, accessToken: String? = null) {

            val realm = Realm.getDefaultInstance()

            // Query and delete if any other row exists

            val oldUsers = realm.where(User::class.java).findAll()

            if (oldUsers.isNotEmpty()) {
                for (oldUser in oldUsers) {
                    realm.beginTransaction()

                    oldUser.deleteFromRealm()

                    realm.commitTransaction()
                }
            }

            // Add New User

            realm.beginTransaction()

            val newUser = realm.createObject(User::class.java)
            newUser.username = username
            newUser.password = password
            newUser.email = email
            newUser.accessToken = accessToken

            realm.commitTransaction()

            realm.close()

        }

        // Static Properties

        @JvmStatic
        private var instance: User? = null

        @JvmStatic
        val current: User?
            get() {

                if (instance == null) {

                    val realm = Realm.getDefaultInstance()

                    instance = realm.where(User::class.java).findFirst()
                }

                return instance
            }

        @JvmStatic
        val tokenAuthorizationHeader: String
        get() {
            val user = User.current
            return "Bearer " + user?.accessToken
        }
    }
}

