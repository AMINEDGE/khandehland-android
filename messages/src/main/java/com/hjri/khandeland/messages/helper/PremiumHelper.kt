package com.hjri.khandeland.messages.helper

import android.app.Activity
import com.hjri.khandeland.messages.Configs
import com.hjri.khandeland.messages.HomeActivity
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

class PremiumHelper(val activity: Activity) {

    companion object {

        lateinit var adsHelper: AdsHelper
        lateinit var iabHelper: InAppBillingHelper
    }


    fun run(premium: Boolean = Configs(activity).isPremium) {

        if (premium || !Configs.shouldShowIABAndAds) {
            (activity as? HomeActivity)?.runOnUiThread { AdsHelper.disableBannerAds(activity) }
        } else {
            adsHelper = AdsHelper(activity)
            if (activity is HomeActivity) {
                adsHelper.onAdsClosed = { activity.showUpgradeOrInstallMarketAlert() }
                adsHelper.initTapsellBannerAds(activity)
                activity.runOnUiThread { AdsHelper.enableBannerAds(activity) }
            }
        }
    }

    fun init(completion: () -> Unit = {}) {

        iabHelper = InAppBillingHelper(activity) { success ->
            this.run(success)
            completion()
        }
    }
}