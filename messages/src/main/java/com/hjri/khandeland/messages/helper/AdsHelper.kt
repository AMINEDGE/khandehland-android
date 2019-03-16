package com.hjri.khandeland.messages.helper

import android.app.Activity
import android.view.View
import com.hjri.khandeland.messages.Configs
import com.hjri.khandeland.messages.HomeActivity
import ir.tapsell.sdk.*
import ir.tapsell.sdk.bannerads.TapsellBannerType
import ir.tapsell.sdk.bannerads.TapsellBannerViewEventListener
import kotlinx.android.synthetic.main.activity_home.*

// if we put activity as a parameter, this would be a memory leak, so we pass it only to constructor
class AdsHelper(activity: Activity) {

    private var tapsellAds: TapsellAd? = null

    // if adsCounter was equal or bigger than adsMaximumAttempt, we'll show ads
    // and each time we show ads, we increase the adsMaximumAttempt so the user won't be bothered so much
    var adsCounter: Int = 0
    var adsMaximumAttempt: Int = 5

    var onAdsClosed: () -> Unit = {}

    init {

        // Tapsell
        prepareAds(activity)
    }

    fun prepareAds(activity: Activity) {


        initTapsellAd(activity,
// Not available yet
//                if (adsMaximumAttempt % 15 == 0)
//                    Configs.TAPSELL_INTERSTITIAL_VIDEO_ZONE_ID
//                else
                    Configs.TAPSELL_INTERSTITIAL_BANNER_ZONE_ID
        )

        if (activity is HomeActivity) initTapsellBannerAds(activity)
    }

    private fun initTapsellAd(activity: Activity, zoneId: String) {
        Tapsell.requestAd(activity,
                        zoneId,
                        TapsellAdRequestOptions(TapsellAdRequestOptions.CACHE_TYPE_CACHED),
                        object: TapsellAdRequestListener {
                            override fun onAdAvailable(p0: TapsellAd?) {
                                tapsellAds = p0
                                Helper.writeDebugLog("Ads available")
                            }

                            override fun onExpiring(p0: TapsellAd?) {
                                if (tapsellAds === p0) {
                                    tapsellAds = null
                                    prepareAds(activity)
                                }
                                Helper.writeDebugLog("Video Ads expiring")
                            }

                            override fun onNoAdAvailable() {
                                Helper.writeDebugLog("Video Ads not available")
                            }

                            override fun onError(p0: String?) {
                                Helper.writeDebugLog("Request Ad Error: $p0")
                            }

                            override fun onNoNetwork() {
                                Helper.writeDebugLog("No network to get video ads")
                            }

                        })
        Tapsell.setRewardListener { ads, completed ->

            Helper.writeDebugLog("Tapsell Reward Ads ${if (completed) "Opened" else "Canceled"}")

//            prepareAds(activity)
//            onAdsClosed()

        }
    }

    fun showPreparedAds(activity: Activity) {

        Helper.writeDebugLog("Trying to show ads")

        if (tapsellAds == null) return

        tapsellAds!!.show(activity, tapsellAdsShowOptions, object: TapsellAdShowListener {
            override fun onOpened(p0: TapsellAd?) {
                Helper.writeDebugLog("Ads Opened")
            }

            override fun onClosed(p0: TapsellAd?) {
                Helper.writeDebugLog("Ads Closed")
                prepareAds(activity)
                onAdsClosed()
            }

        })

        adsMaximumAttempt += adsMaximumAttempt
    }

    fun initTapsellBannerAds(activity: HomeActivity) {
        val adContainer = activity.tapsell_banner_ads_view

        activity.runOnUiThread {
            adContainer.loadAd(activity, Configs.TAPSELL_BANNER_ZONE_ID, TapsellBannerType.BANNER_320x50)
        }
        adContainer.setEventListener(object: TapsellBannerViewEventListener {
            override fun onNoAdAvailable() {
                Helper.writeDebugLog("No Banner Ads Available")
            }

            override fun onRequestFilled() {
                Helper.writeDebugLog("Banner Ads Request Filled")
            }

            override fun onHideBannerView() {
                Helper.writeDebugLog("Hided Banner View")
            }

            override fun onError(p0: String?) {
                Helper.writeDebugLog("Banner Ads Error: $p0")
            }

            override fun onNoNetwork() {
                Helper.writeDebugLog("No Network to show Banner Ads")
            }

        })
    }

    private fun initVideoAds(activity: Activity) {
        initTapsellAd(activity, Configs.TAPSELL_INTERSTITIAL_VIDEO_ZONE_ID)
    }

    private fun initPictureAds(activity: Activity) {
        initTapsellAd(activity, Configs.TAPSELL_INTERSTITIAL_BANNER_ZONE_ID)
    }

    private var tapsellAdsShowOptions: TapsellShowOptions? = null
        get() {
            val showOptions = TapsellShowOptions()
            showOptions.isBackDisabled = true
            showOptions.isImmersiveMode = true
            showOptions.rotationMode = TapsellShowOptions.ROTATION_LOCKED_PORTRAIT

            return showOptions
        }

    companion object {

        fun enableBannerAds(activity: HomeActivity) {
            activity.tapsell_banner_ads_container.visibility = View.VISIBLE
            activity.tapsell_banner_ads_view.showBannerView()
        }

        fun disableBannerAds(activity: HomeActivity) {
            activity.tapsell_banner_ads_container.visibility = View.GONE
            activity.tapsell_banner_ads_view.hideBannerView()
        }
    }
}