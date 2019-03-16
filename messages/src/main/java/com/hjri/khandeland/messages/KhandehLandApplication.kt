package com.hjri.khandeland.messages

import android.app.Application
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import com.hjri.khandeland.messages.helper.DatabaseMigration
import com.hjri.khandeland.messages.helper.Helper
import io.realm.Realm
import io.realm.RealmConfiguration
import ir.tapsell.sdk.Tapsell

class KhandehLandApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        Tapsell.initialize(this, Configs.tapsellAdsKey)

        initRealm()
    }

    private fun initRealm() {
        Realm.init(this)

        val config = RealmConfiguration.Builder()
                .schemaVersion(1) // Must be bumped when the schema changes
                .migration(DatabaseMigration()) // Migration to run instead of throwing an exception
                .build()

        Realm.setDefaultConfiguration(config)
    }
}