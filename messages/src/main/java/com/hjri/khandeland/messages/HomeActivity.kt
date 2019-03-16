package com.hjri.khandeland.messages

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.hjri.khandeland.messages.helper.*
import com.hjri.khandeland.messages.model.Message
import com.hjri.khandeland.messages.networking.UpdateResponseState
import com.hjri.khandeland.messages.view.MessageViewAdapter
import io.realm.RealmResults

import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import kotlinx.coroutines.experimental.*

class HomeActivity : AppCompatActivity() {

    private var showingFavoriteList: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initAddFab()

        initFavoriteListButton()

        initMessageRecyclerView()

        initHome()

        launch(CommonPool) {

            PremiumHelper(this@HomeActivity).run()
        }


    }

    override fun onResume() {
        super.onResume()

        // Showing Pending Message Alert
        if (Configs.pendingMessage) {
            AlertHelper.showPendingMessage(this)
            Configs.pendingMessage = false

            if (Configs.favoriteShouldSync) {
                updateView()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!InAppBillingHelper(this).handledActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
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

    private fun initHome() {

        loadMessagesIntoRecyclerView(false)
    }

    fun updateView() {

        beginLoadingView()

        RefreshHelper(this) {

            loadMessagesIntoRecyclerView()
            stopLoadingView()

        }.run()
        loadMessagesIntoRecyclerView()

    }

    private fun initAddFab() {

        fab.setOnClickListener { view ->

            if (!Configs(this).isOnline) {
                AlertHelper.showNoConnectionError(this, false)
                return@setOnClickListener
            }

            if (!Configs.updateSupported) {
                AlertHelper.showUpdateAvailable(this, Configs(this).updateDownloadLink, UpdateResponseState.NECESSARY)
                return@setOnClickListener
            }

            presentLoginActivity(view)
        }
    }

    private fun presentLoginActivity(view: View) {
        val options = ActivityOptionsCompat.
        makeSceneTransitionAnimation(this, view, "transition")
        val revealX: Int = (view.x + view.width / 2).toInt()
        val revealY: Int = (view.y + view.height / 2).toInt()

        val intent = Intent(this, FormActivity::class.java)
        intent.putExtra(FormActivity.EXTRA_CIRCULAR_REVEAL_X, revealX)
        intent.putExtra(FormActivity.EXTRA_CIRCULAR_REVEAL_Y, revealY)

        ActivityCompat.startActivity(this, intent, options.toBundle())
    }

    private fun initFavoriteListButton() {
        val favoriteListButton: ImageButton = findViewById(R.id.nav_favorite_button)

        favoriteListButton.setOnClickListener { view ->

            if (Configs(this).isPremium || !Configs.shouldShowIABAndAds) {


                if (view !is ImageButton) {
                    return@setOnClickListener
                }

                showingFavoriteList = !showingFavoriteList
                view.setImageResource(if (showingFavoriteList) R.drawable.ic_favorite_nav_red else R.drawable.ic_favorite_nav_gray)

                loadMessagesIntoRecyclerView()
                initMessageRecyclerView()
            } else {
                showUpgradeOrInstallMarketAlert()
            }
        }
    }

    fun showUpgradeOrInstallMarketAlert() {
        if (Configs.isMarketInstalled) {

            AlertHelper.showAskForPremium(this) { dialog, view ->

                // For avoiding async overflow issues, we delete the old instances and re create a new instance
                PremiumHelper.iabHelper.destroy()
                PremiumHelper(this).init {
                    PremiumHelper.iabHelper.performPurchase(this)
                }
                dialog.dismiss()
            }
        } else {
            AlertHelper.showMarketIsNotInstalled(this)
        }
    }

    private fun initMessageRecyclerView() {

        swipe_refresh_layout.setOnRefreshListener {
            updateView()
        }

        // Responsive Calculation

        val size = Point()
        windowManager.defaultDisplay.getSize(size)

        val messageViewWidth = 600

        val columnCount: Int = size.x / messageViewWidth

        val messageLayoutManager =
                if (columnCount <= 1) LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                // Cast is needed! otherwise error will be thrown
                else StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL) as RecyclerView.LayoutManager

        // Final Initializations

        val messageAdapter = MessageViewAdapter(this, MessageHelper.messages, false)

        message_recycler_view.layoutManager = messageLayoutManager
        message_recycler_view.adapter = messageAdapter
    }



    private fun loadMessagesIntoRecyclerView(readFromDB: Boolean = true) {

        val emptyListPlaceHolder: TextView = findViewById(R.id.empty_list_placeholder)
        emptyListPlaceHolder.visibility = View.GONE

        if (readFromDB) MessageHelper(this).load(showingFavoriteList)

        container_scroll_view.smoothScrollTo(0, 0)

        if (MessageHelper.messages.isEmpty()) {
            emptyListPlaceHolder.visibility = View.VISIBLE
        } else {
            Log.d(Configs.DEBUG_LOG_KEY,"RECYCLER_VIEW: First Message Content: " + MessageHelper.messages[0]?.content)
        }

        message_recycler_view.adapter?.notifyDataSetChanged()
    }

    private fun beginLoadingView() {
        swipe_refresh_layout.isRefreshing = true
    }

    private fun stopLoadingView() {
        Helper.writeDebugLog("STOPPING LOADING")
        swipe_refresh_layout.isRefreshing = false
    }
}
