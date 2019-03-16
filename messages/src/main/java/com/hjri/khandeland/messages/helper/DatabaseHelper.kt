package com.hjri.khandeland.messages.helper

import com.hjri.khandeland.messages.model.Message
import io.realm.*
import io.realm.kotlin.createObject

class DatabaseHelper {

    companion object {

        @JvmStatic
        fun <T: RealmObject>save(model: T) {

            val realm = Realm.getDefaultInstance()

            realm.beginTransaction()

            realm.insert(model)

            realm.commitTransaction()

            realm.close()
        }

        fun <T: RealmObject>saveArray(models: ArrayList<T>, completion: (success: Boolean) -> Unit) {

            val realm = Realm.getDefaultInstance()

            realm.executeTransactionAsync({ inRealm ->

                inRealm.insert(models)
            }, {
                realm.close()
                completion(true)
            }, { throwable ->

                Helper.writeDebugLog("Save Message Array Failed: ${throwable.localizedMessage}")
                realm.close()
                completion(false)
            })
        }

        @JvmStatic
        inline fun <reified T: RealmObject>isDuplicate(id: Int): Boolean {

            val realm = Realm.getDefaultInstance()

            val result = realm.where(T::class.java).equalTo("id", id).findAll().isNotEmpty()

            realm.close()

            return result
        }

        @JvmStatic
        fun isMessageDuplicate(id: Int): Boolean {
            val realm = Realm.getDefaultInstance()

            val result = !realm.where(Message::class.java).equalTo("id", id).findAll().isEmpty()

            realm.close()

            return result
        }
    }
}

class DatabaseMigration: RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {

        val sessionSchema = realm.schema

        if (oldVersion == 0.toLong()) {
            val userSchema = sessionSchema.get("User")
            userSchema!!.addField("isPremium", Boolean::class.java)
            oldVersion.inc()
        }
    }

}