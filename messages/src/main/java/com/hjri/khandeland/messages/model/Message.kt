package com.hjri.khandeland.messages.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.hjri.khandeland.messages.helper.DateHelper
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults

open class Message: RealmObject() {

    var id: Int = 0
    var content: String = ""
    var submitDate: String = ""
    var submitDateEpoch: Double = 0.0
    var favoriteCount: Int = 0
    var isFavorite: Boolean = false
    var needsSync: Boolean = false
    var syncDetails: String = ""
    var isNew: Boolean = true

    fun update(content: String? = null,
               isFavorite: Boolean? = null,
               isNew: Boolean? = null,
               needsSync: Boolean? = null,
               syncDetails: String? = null,
               favoriteCount: Int? = null) {

        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()

        this.content = content ?: this.content
        this.isFavorite = isFavorite ?: this.isFavorite
        this.isNew = isNew ?: this.isNew
        this.needsSync = needsSync ?: this.needsSync
        this.syncDetails = syncDetails ?: this.syncDetails
        this.favoriteCount = favoriteCount ?: this.favoriteCount

        realm.commitTransaction()

        realm.close()
    }

    companion object {
        @JvmStatic
        val FAVORITE_SYNC: String = "favorite"

        @JvmStatic
        val NOT_FAVORITE_SYNC: String = "not_favorite"

        @JvmStatic
        fun deleteArray(arr: RealmResults<Message>){

            val realm = Realm.getDefaultInstance()

            realm.beginTransaction()

            for (item in arr) {
                item.deleteFromRealm()
            }

            realm.commitTransaction()
        }

        @JvmStatic
        fun deleteWithSameIdAndWasItFavorite(id: Int): Boolean {

            val realm = Realm.getDefaultInstance()
            var wasFavorite = false

            val results = realm.where(Message::class.java).equalTo("id", id).findAll()

            if (results.isEmpty()) return false

            for (message in results) {
                if (message.isFavorite) {
                    wasFavorite = true
                    break
                }
            }

            deleteArray(results)

            return wasFavorite
        }

        @JvmStatic
        fun shouldDelete(id: Int) {
            val realm = Realm.getDefaultInstance()

            val results = realm.where(Message::class.java).equalTo("id", id).findAll()

            if (results.isEmpty()) return

            deleteArray(results)
        }

        @JvmStatic
        fun makeOld() {

            val realm = Realm.getDefaultInstance()

            val results = realm.where(Message::class.java).findAll()

            for (message in results) message.update(null, null, false)
        }

        @JvmStatic
        fun makeOldBefore(date: String) {

            val realm = Realm.getDefaultInstance()
            val results = realm.where(Message::class.java).findAll()

            for (message in results) {
                if (DateHelper.isLater(date, message.submitDate)) {
                    message.update(null, null, false)
                }
            }
        }

        @JvmStatic
        fun setFavorite(state: Boolean, idArray: ArrayList<Int>) {

            val realm = Realm.getDefaultInstance()

            val results = realm.where(Message::class.java).findAll()

            for (message in results) {
                if (idArray.contains(message.id)) {
                    message.update(null, state)
                }
            }
        }

        @JvmStatic
        val isEmpty: Boolean
        get() {
            val realm = Realm.getDefaultInstance()

            return realm.where(Message::class.java).findAll().isEmpty()
        }


    }
}