package com.hjri.khandeland.messages

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.util.Log
import com.hjri.khandeland.messages.helper.AlertHelper
import com.hjri.khandeland.messages.helper.CryptographyHelper
import com.hjri.khandeland.messages.helper.Helper
import com.hjri.khandeland.messages.helper.MessageHelper
import com.hjri.khandeland.messages.iab.Base64
import com.hjri.khandeland.messages.model.User
import com.hjri.khandeland.messages.networking.ConfigResponseBody
import com.hjri.khandeland.messages.networking.Routes
import com.hjri.khandeland.messages.networking.SingleVersionBody
import com.hjri.khandeland.messages.networking.UpdateData
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress

class Configs(private val activity: Activity) {

    // Context Dependent Properties (Cannot be static as a property)
    private var lastUpdateEpochCache: Double? = null
    var lastUpdateEpoch: Double
        get() {
            return lastUpdateEpochCache ?: activity.getPreferences(Context.MODE_PRIVATE).getFloat(LAST_UPDATE_EPOCH, 0.0f).toDouble()
        }
        set(value){
            lastUpdateEpochCache = value
            with (activity.getPreferences(Context.MODE_PRIVATE).edit()) {
                putFloat(LAST_UPDATE_EPOCH, value.toFloat())
                apply()
            }
        }


    private var updateDownloadLinkCache: String? = null
    var updateDownloadLink: String
        get() {
            return updateDownloadLinkCache ?: activity.getPreferences(Context.MODE_PRIVATE).getString(UPDATE_DOWNLOAD_LINK_KEY, "https://khandehland.hjri.ir")
        }
        set(value) {
            updateDownloadLinkCache = value
            with(activity.getPreferences(Context.MODE_PRIVATE).edit()) {
                putString(UPDATE_DOWNLOAD_LINK_KEY, value)
                apply()
            }
        }

    val isOnline: Boolean
        get() {

            val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return  networkInfo != null && networkInfo.isConnected
        }

    private var isPremiumCache: Boolean? = null
    var isPremium: Boolean
        get() {
            return isPremiumCache ?: activity.getPreferences(Context.MODE_PRIVATE).getBoolean(IS_PREMIUM_KEY, false)
        }
        set(value) {
            isPremiumCache = value
            with (activity.getPreferences(Context.MODE_PRIVATE).edit()) {
                putBoolean(IS_PREMIUM_KEY, value)
                apply()
            }
        }

    private var premiumPriceCache: String? = null
    var premiumPrice: String
        get() {
            return premiumPriceCache ?: activity.getPreferences(Context.MODE_PRIVATE).getString(PREMIUM_PRICE_KEY, "")
        }
        set(value) {
            premiumPriceCache = value
            with (activity.getPreferences(Context.MODE_PRIVATE).edit()) {
                putString(PREMIUM_PRICE_KEY, value)
                apply()
            }
        }

    companion object {

        var isMarketInstalled: Boolean = false
        var shouldShowIABAndAds: Boolean = false

        // Computed Properties

        val retrofitInstance: Retrofit
            get() {
                return Retrofit.Builder().baseUrl(Routes.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
            }

        val retrofitInstanceWithoutAPIRoute: Retrofit
            get() {
                return Retrofit.Builder().baseUrl(Routes.MAIN_URL).addConverterFactory(GsonConverterFactory.create()).build()
            }

        val isLogin: Boolean
            get() {
                return User.current != null
            }

        // Properties

        var showMessageFavoriteCount: Boolean = false

        var updateSupported: Boolean = true

        var isFirstTimeShowingAlert: Boolean = true

        var pendingMessage: Boolean = false

        val clientId: Int = if (BuildConfig.DEBUG) 4 else 2
        val clientSecret: String = if (BuildConfig.DEBUG) "M0E9PcR2Iwq0VFVUTpRemQyd0gsSB3JKhp5ZEmMG" else "NT9cJ7RL9bRTyRRtAMvH8eDPtHxVPZEgxq4tRmFp"

        var favoriteShouldSync: Boolean = false

        var shouldShowPurchaseFinishedAlertAndRefreshLayout: Boolean = false

        // Constant Variables

        private const val IAB_RSA_TEXT: String = "eMe7uDhjW3Rob2iiBD5cHhshlZtRndhhFLwNvMPQ3ms30cmOns6lAFnboIqtaRAoD1ABJhJcx0sZkTMDLDtrVdlOuST50mdyZHxZ3N20PQ9KSQ99cSPe9XLYxK+tly1E8HowBKXgjpewj95RZDNrDnT2BAfDrq5ep4Utq8A4sBcGG7cXxfP9VtBgpJS1Iz86HF8MLwMlBmxxRjNt0lxtjgGY1qW72RuuU576ds1yvyHzMyziVfO2ocDeZVr+wkFQlVW95KA5kgmiYWPJnhyl5dPIt/Vwvi7+5L6S/2YNKdr+r8iQukeKavwnxMzWskn2JP1SHdDmxv5mVyHimot7q6D5hC/i/wIEmXXFXPYr5abTSLRBEnSMeCHtmOKncBtE"
        private const val IAB_RSA_IV: String = "CDvVTo0rRRiJieKGI73sAQ=="


        // Should be defined after TEXT and IV
        val iabRSA: String = CryptographyHelper
                .decrypt(Base64.decode(IAB_RSA_TEXT),
                        Base64.decode(IAB_RSA_IV),
                        CryptographyHelper.getSecretKey())

        const val TAPSELL_REWARD_ZONE_ID: String = "5b883a6d37a1ee0001011ed4"
        const val TAPSELL_INTERSTITIAL_VIDEO_ZONE_ID: String = "5b9509b7791fec0001655981"
        const val TAPSELL_INTERSTITIAL_BANNER_ZONE_ID: String = "5b950b13f70bec0001082c42"
        const val TAPSELL_BANNER_ZONE_ID: String = "5b950752791fec0001655980"

        private const val TAPSELL_ADS_TEXT: String = "YiHgNWrYpzZNUYw4rgbnIsHBHvphYw5HfGbQqoPWhN49TPcQiDl/kyyu4xQAzcR/usWy9ojJPP75yz0Yn2DnTycfX0Vvh90U3/pGsD1SEXY="
        private const val TAPSELL_ADS_IV: String = "L1PFrBLeeaFgc71mBlvHDg=="

        val tapsellAdsKey = CryptographyHelper
                .decrypt(Base64.decode(TAPSELL_ADS_TEXT),
                        Base64.decode(TAPSELL_ADS_IV),
                        CryptographyHelper.getSecretKey())

        // Keys

        const val UPDATE_DOWNLOAD_LINK_KEY = "link"

        const val LAST_UPDATE_PREFERENCE_KEY = "last_update_preference_key"

        const val DEBUG_LOG_KEY = "DEBUG LOG"

        const val FAVORITE_SHOULD_SYNC = "favorite_should_sync"

        const val LAST_UPDATE_EPOCH = "last_update_epoch"

        const val SHOULD_SHOW_IAB_KEY = "should_show_iab"

        const val IS_PREMIUM_KEY = "is_premium"

        const val PREMIUM_PRICE_KEY = "premium_price"
    }
}