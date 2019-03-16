package com.hjri.khandeland.messages.view

import android.animation.ObjectAnimator
import android.content.Intent
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.hjri.khandeland.messages.Configs
import com.hjri.khandeland.messages.HomeActivity
import com.hjri.khandeland.messages.R
import com.hjri.khandeland.messages.helper.Helper
import com.hjri.khandeland.messages.helper.PremiumHelper
import com.hjri.khandeland.messages.model.Message
import com.hjri.khandeland.messages.model.User
import com.hjri.khandeland.messages.networking.Routes
import com.hjri.khandeland.messages.networking.SingleVersionBody
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var newBadge: TextView = itemView.findViewById(R.id.new_badge)
    var messageTextView: TextView = itemView.findViewById(R.id.message_text_view)
    var shareButton: ImageButton = itemView.findViewById(R.id.share_button)
    var favoriteButton: ImageButton = itemView.findViewById(R.id.favorite_button)
    var favoriteProgressBar: ProgressBar = itemView.findViewById(R.id.favorite_progressbar)
}

class MessageViewAdapter(private val activity: HomeActivity, private var messageList: RealmResults<Message>, autoUpdate: Boolean)
    : RealmRecyclerViewAdapter<Message, MessageViewHolder>(messageList, autoUpdate) {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MessageViewHolder {

        val parentView = MessageViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.message_view, p0, false))

        parentView.messageTextView.typeface = ResourcesCompat.getFont(p0.context, R.font.iransansmobile_medium)
        parentView.newBadge.typeface = ResourcesCompat.getFont(p0.context, R.font.iransansmobile_medium)

        return parentView
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(p0: MessageViewHolder, p1: Int) {

        val viewHolder = p0
        val message = messageList[p1] ?: return

        viewHolder.messageTextView.text = message.content
        viewHolder.newBadge.visibility = if (message.isNew) View.VISIBLE else View.GONE

        initFavoriteButton(viewHolder, message)
        initShareButton(viewHolder, message)
    }

    private fun initFavoriteButton(viewHolder: MessageViewHolder, message: Message) {

        setFavoriteButtonImageSource(viewHolder, message.isFavorite)

        viewHolder.favoriteButton.setOnClickListener{ _ ->

            handleShowingAds()

            val liked = !message.isFavorite

            if (!Configs.isLogin || !Configs(activity).isOnline) {

                message.update(null, liked, false, true, if (liked) Message.FAVORITE_SYNC else Message.NOT_FAVORITE_SYNC)
                setFavoriteButtonImageSource(viewHolder, liked)
                doBounceAnimation(viewHolder)
                return@setOnClickListener
            }


            beginLoading(viewHolder)

            val connectionInstance = Configs.retrofitInstance.create(Routes.Favorite::class.java)
            val connectionService =
                    if (liked) connectionInstance.addToFavorite(User.tokenAuthorizationHeader, message.id, SingleVersionBody())
                    else connectionInstance.removeFromFavorite(User.tokenAuthorizationHeader, message.id, SingleVersionBody())

            connectionService.enqueue(object: Callback<Void>{

                override fun onResponse(call: Call<Void>, response: Response<Void>) {

                    message.update(null,
                            liked,
                            null,
                            null,
                            null,
                            if (liked) message.favoriteCount + 1 else message.favoriteCount - 1)

                    endLoading(viewHolder)
                    setFavoriteButtonImageSource(viewHolder, liked)
                    doBounceAnimation(viewHolder, true)
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    message.update(null, liked, false, true, if (liked) Message.FAVORITE_SYNC else Message.NOT_FAVORITE_SYNC)
                    setFavoriteButtonImageSource(viewHolder, message.isFavorite)
                    doBounceAnimation(viewHolder, true)
                    return
                }

            })


        }
    }

    private fun initShareButton(viewHolder: MessageViewHolder, message: Message) {
        viewHolder.shareButton.setOnClickListener { _ ->

            handleShowingAds()

            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type="text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, message.content + String.format(activity.getString(R.string.share_download_text), Configs(activity).updateDownloadLink))
            activity.startActivity(Intent.createChooser(shareIntent, "send to"))

        }
    }

    private fun handleShowingAds() {

        if (Configs(activity).isPremium || !Configs.shouldShowIABAndAds) return

        val counter = ++PremiumHelper.adsHelper.adsCounter

        if (counter % PremiumHelper.adsHelper.adsMaximumAttempt != 0) {
            Helper.writeDebugLog("Not enough attempt: $counter / ${PremiumHelper.adsHelper.adsMaximumAttempt} =! 0")
            return
        }

        PremiumHelper.adsHelper.showPreparedAds(activity)
    }

    private fun setFavoriteButtonImageSource(viewHolder: MessageViewHolder, state: Boolean) {
        val favoriteButton = viewHolder.favoriteButton
        favoriteButton.setImageResource(if (state) R.drawable.ic_favorite else R.drawable.ic_not_favorite)
    }

    private fun doBounceAnimation(viewHolder: MessageViewHolder, hasDelay: Boolean = false) {

        val targetView = viewHolder.favoriteButton

        val normalValue: Float = 1.0f
        val bounceValue: Float = 1.3f
        val duration: Long = 150
        val delay: Long = 50

        val scaleYAnimator = ObjectAnimator.ofFloat(targetView, "scaleY", normalValue, bounceValue)
        scaleYAnimator.startDelay = if(hasDelay) delay else 0
        scaleYAnimator.duration = duration
        scaleYAnimator.start()
        val scaleXAnimator = ObjectAnimator.ofFloat(targetView, "scaleX", normalValue, bounceValue)
        scaleXAnimator.startDelay =  if(hasDelay) delay else 0
        scaleXAnimator.duration = duration
        scaleXAnimator.start()

        val scaleYRevertAnimator = ObjectAnimator.ofFloat(targetView, "scaleY", bounceValue, normalValue)
        scaleYRevertAnimator.duration = duration
        scaleYRevertAnimator.startDelay = duration + if(hasDelay) delay else 0
        scaleYRevertAnimator.start()

        val scaleXRevertAnimator = ObjectAnimator.ofFloat(targetView, "scaleX", bounceValue, normalValue)
        scaleXRevertAnimator.duration = duration
        scaleXRevertAnimator.startDelay = duration + if(hasDelay) delay else 0
        scaleXRevertAnimator.start()
    }

    // Private Methods

    private fun setLoadingState(messageView: MessageViewHolder, state: Boolean) {

        messageView.favoriteButton.visibility = if (state) View.GONE else View.VISIBLE
        messageView.favoriteProgressBar.visibility = if (state) View.VISIBLE else View.GONE
    }

    // Helper Methods

    private fun beginLoading(messageView: MessageViewHolder) {
        setLoadingState(messageView, true)
    }

    private fun endLoading(messageView: MessageViewHolder) {
        setLoadingState(messageView, false)
    }
}