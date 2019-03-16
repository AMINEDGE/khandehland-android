package com.hjri.khandeland.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.hjri.khandeland.messages.helper.*
import com.hjri.khandeland.messages.model.Message
import kotlinx.android.synthetic.main.activity_load.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

class LoadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load)

        version_textview.text = "Version ${BuildConfig.VERSION_NAME}"

        launch (CommonPool) {
            Message.makeOld()
            initLoading()
        }
    }

    override fun onDestroy() {
        try {

            PremiumHelper.iabHelper.destroy()
        } catch (e: Exception) {
            Helper.writeDebugLog("IAB Destroy Error: ${e.localizedMessage}")
        }
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!InAppBillingHelper(this).handledActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun initLoading() {
        RefreshHelper(this) {
            launch { next() }
        }.run()
    }

    private fun next() {

        startActivity(Intent(this@LoadActivity, HomeActivity::class.java))
        finish()
    }
}
