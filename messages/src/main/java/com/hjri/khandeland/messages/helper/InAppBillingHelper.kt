package com.hjri.khandeland.messages.helper

import android.app.Activity
import android.content.Intent
import com.hjri.khandeland.messages.Configs
import com.hjri.khandeland.messages.HomeActivity
import com.hjri.khandeland.messages.iab.IabHelper


class InAppBillingHelper(activity: Activity,
                         private var onSetupDone: (success: Boolean) -> Unit = {}) {

    companion object {
        const val PREMIUM_SKU = "khandehland_premium"
        const val PURCHASE_REQUEST_CODE = 127

    }

    private var publicKey: String = Configs.iabRSA
    private var purchaseHelper: IabHelper? = IabHelper(activity, publicKey)

    init {

        Helper.writeDebugLog("Starting setup.")

        try {

            purchaseHelper?.startSetup { result ->
                Helper.writeDebugLog("Setup finished.")

                Configs.isMarketInstalled = true

                if (!result.isSuccess) {
                    // Oh noes, there was a problem.
                    Helper.writeDebugLog("Problem setting up In-app Billing: $result")
                }
                // Hooray, IAB is fully set up!
                purchaseHelper?.queryInventoryAsync(gotInventoryListener)
            }
        } catch(e: Exception) {
            if (e is IllegalStateException) {
                Helper.writeDebugLog("MARKET IS NOT INSTALLED")
                Configs.isMarketInstalled = false
            }
            onSetupDone(false)
        }
    }

    fun performPurchase(activity: Activity) {
        purchaseHelper?.launchPurchaseFlow(activity, PREMIUM_SKU, PURCHASE_REQUEST_CODE) { result, purchase ->

            if (result.isSuccess && purchase.sku == PREMIUM_SKU) {

                Configs(activity).isPremium = true
                Configs.shouldShowPurchaseFinishedAlertAndRefreshLayout = true

            }

            PremiumHelper(activity).run(result.isSuccess)
        }
    }

    private var gotInventoryListener: IabHelper.QueryInventoryFinishedListener = IabHelper.QueryInventoryFinishedListener { result, inventory ->
        Helper.writeDebugLog("Query inventory finished.")
        if (result.isFailure) {
            Helper.writeDebugLog("Failed to query inventory: $result")
            onSetupDone(false)
            return@QueryInventoryFinishedListener
        }

        Helper.writeDebugLog("Query inventory was successful.")
        // does the user have the premium upgrade?
        Configs(activity).isPremium = inventory.hasPurchase(PREMIUM_SKU)

        if (Configs.shouldShowPurchaseFinishedAlertAndRefreshLayout) {
            Configs.shouldShowPurchaseFinishedAlertAndRefreshLayout = false

            if (activity is HomeActivity) {
                activity.runOnUiThread {

                    activity.updateView()

                    AlertHelper.showPurchaseDone(activity)
                }
            }
        }

        // update UI accordingly

        onSetupDone(true)
    }

    fun handledActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Helper.writeDebugLog("onActivityResult($requestCode,$resultCode,$data")

        // Pass on the activity result to the purchaseHelper for handling
        return purchaseHelper?.handleActivityResult(requestCode, resultCode, data) ?: false
    }

    fun destroy() {

        if (purchaseHelper != null) purchaseHelper?.dispose()
        purchaseHelper = null
    }

}