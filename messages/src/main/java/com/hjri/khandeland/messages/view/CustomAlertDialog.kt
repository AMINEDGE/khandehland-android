package com.hjri.khandeland.messages.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.hjri.khandeland.messages.R

enum class AlertViewStyle {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

class CustomAlertDialog(val style: AlertViewStyle, val title: String, val text: String, context: Context): AlertDialog(context) {

    private var onDismissed: () -> Unit = {}
    var dismissButtonClicked: Boolean = false

    private var infoIconDrawable: Drawable? = null
    private var infoColor: Int = 0

    private var successIconDrawable: Drawable? = null
    private var successColor: Int = 0

    private var warningIconDrawable: Drawable? = null
    private var warningColor: Int = 0

    private var errorIconDrawable: Drawable? = null
    private var errorColor: Int = 0

    private var titleTextView: TextView? = null
    private var contentTextView: TextView? = null

    var actionButton: Button? = null
    var cancelButton: Button? = null

    private var titleContainer: LinearLayout? = null
    private var buttonContainer: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.custom_alert_dialog)

        infoIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_dialog_info_fill)
        infoColor = ContextCompat.getColor(context, R.color.colorInfo)

        successIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_dialog_success_fill)
        successColor = ContextCompat.getColor(context, R.color.colorSuccess)

        warningIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_dialog_warning)
        warningColor = ContextCompat.getColor(context, R.color.colorWarning)

        errorIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_dialog_error)
        errorColor = ContextCompat.getColor(context, R.color.colorError)

        titleTextView = findViewById(R.id.title)
        contentTextView = findViewById(R.id.text)

        actionButton = findViewById(R.id.action_button)
        cancelButton = findViewById(R.id.cancel_button)

        titleContainer = findViewById(R.id.title_container)
        buttonContainer = findViewById(R.id.button_container)

        titleTextView?.text = title
        contentTextView?.text = text

        applyTheme()
    }

    fun enableActionButton(text: String) {

        actionButton?.visibility = View.VISIBLE
        actionButton?.text = text
    }

    fun enableCancelButton(text: String?, onClick: (() -> Unit)? = null) {

        onDismissed = onClick ?: {}

        cancelButton?.visibility = View.VISIBLE
        cancelButton?.text = text ?: context.getString(R.string.ok)

        cancelButton?.setOnClickListener { view ->
            if (onClick != null) {
                onDismissed()
            }
            dismissButtonClicked = true
            dismiss()
        }
    }

    private fun applyTheme() {

        when (style) {
            AlertViewStyle.INFO -> {
                setTitleDrawable(infoIconDrawable)
                setElementColors(infoColor)
            }
            AlertViewStyle.SUCCESS -> {
                setTitleDrawable(successIconDrawable)
                setElementColors(successColor)
            }
            AlertViewStyle.WARNING -> {
                setTitleDrawable(warningIconDrawable)
                setElementColors(warningColor)
            }
            AlertViewStyle.ERROR -> {
                setTitleDrawable(errorIconDrawable)
                setElementColors(errorColor)
            }
        }
    }

    private fun setTitleDrawable(drawable: Drawable?) {
        titleTextView?.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    }

    private fun setElementColors(color: Int) {
        titleContainer?.setBackgroundColor(color)
        actionButton?.setTextColor(color)
        cancelButton?.setTextColor(color)
    }

    override fun onDetachedFromWindow() {
        if (!dismissButtonClicked) onDismissed()
        super.onDetachedFromWindow()
    }
}