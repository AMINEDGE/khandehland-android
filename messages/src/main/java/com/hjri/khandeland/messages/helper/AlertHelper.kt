package com.hjri.khandeland.messages.helper

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.view.View
import android.widget.Button
import com.hjri.khandeland.messages.Configs
import com.hjri.khandeland.messages.R
import com.hjri.khandeland.messages.networking.UpdateResponseState
import com.hjri.khandeland.messages.view.AlertViewStyle
import com.hjri.khandeland.messages.view.CustomAlertDialog

class AlertHelper {
    companion object {

        fun showSimpleWithCancel(style: AlertViewStyle, title: String, text: String, context: Context, cancelTitle: String? = null, onCancelTouched: (() -> Unit)? = null) {
            val alert = CustomAlertDialog(style, title, text, context)
            alert.show()


            //IMPORTANT NOTE: methods of alertdialog only works after calling its show() method, otherwise they won't work
            alert.enableCancelButton(cancelTitle, onCancelTouched)
        }

        fun showNoConnectionError(context: Context, serverError: Boolean = false, onCancelTouched: (() -> Unit)? = null) {

            val title = context.getString(if (serverError) R.string.server_error_title else R.string.no_internet_connection_title)
            val text = context.getString(if (serverError) R.string.server_error_text else R.string.no_internet_connection_text)

            val cancelTitle = context.getString(R.string.use_app_in_offline_mode)

            showSimpleWithCancel(AlertViewStyle.ERROR, title, text, context, cancelTitle, onCancelTouched)
        }

        fun showUpdateAvailable(context: Context, downloadLink: String, state: UpdateResponseState, next: () -> Unit = {}) {

            val title = context.getString(R.string.update_available)

            val alertDialog = if (state == UpdateResponseState.NECESSARY) {
                val text = context.getString(R.string.necessary_update_text)
                CustomAlertDialog(AlertViewStyle.WARNING, title, text, context)
            } else {
                val text = context.getString(R.string.optional_update_text)
                CustomAlertDialog(AlertViewStyle.INFO, title, text, context)
            }

            alertDialog.show()

            alertDialog.enableActionButton(context.getString(R.string.get_update_title))
            alertDialog.actionButton?.setOnClickListener {
                HTTPHelper.openLinkInBrowser(context, downloadLink)
                alertDialog.dismissButtonClicked = true
                (context as? Activity)?.finish()
            }

            alertDialog.enableCancelButton(context.getString(if (state == UpdateResponseState.NECESSARY) R.string.use_app_in_offline_mode else R.string.later)) {
                next()
            }

        }

        fun showPendingMessage(context: Context) {
            val title = context.getString(R.string.message_pending_title)
            val text = context.getString(R.string.message_pending_text)

            showSimpleWithCancel(AlertViewStyle.SUCCESS, title, text, context)
        }

        fun showAskForPremium(activity: Activity, onBuyClicked: (dialog: CustomAlertDialog, view: View) -> Unit) {
            val title = activity.getString(R.string.premium_title)
            val text = activity.getString(R.string.premium_features) + if (Configs(activity).premiumPrice != "") String.format(activity.getString(R.string.premium_price), Configs(activity).premiumPrice) else ""

            val alertDialog = CustomAlertDialog(AlertViewStyle.INFO, title, text, activity)
            alertDialog.show()

            alertDialog.enableActionButton(activity.getString(R.string.premium_buy_and_activate))
            alertDialog.actionButton!!.setOnClickListener { view ->
                onBuyClicked(alertDialog, view)
            }
            alertDialog.enableCancelButton(activity.getString(R.string.premium_buy_later))
        }

        fun showMarketIsNotInstalled(context: Context) {
            val title = context.getString(R.string.market_not_installed_title)
            val text = context.getString(R.string.market_not_installed_text)

            showSimpleWithCancel(AlertViewStyle.ERROR, title, text, context)
        }

        fun showPurchaseDone(context: Context) {
            val title = context.getString(R.string.purchase_done_title)
            val text = context.getString(R.string.purchase_done_text)

            val cancelTitle = context.getString(R.string.purchase_no_problem)

            showSimpleWithCancel(AlertViewStyle.SUCCESS, title, text, context, cancelTitle)
        }
    }
}