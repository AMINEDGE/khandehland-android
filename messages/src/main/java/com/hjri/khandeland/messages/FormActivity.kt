package com.hjri.khandeland.messages

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.support.v7.app.AppCompatActivity
import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.widget.RelativeLayout

import kotlinx.android.synthetic.main.activity_form.*
import android.os.Handler
import android.text.InputType
import android.util.Log
import android.view.inputmethod.EditorInfo
import com.hjri.khandeland.messages.helper.FormInputType
import com.hjri.khandeland.messages.helper.FormValidationHelper
import com.hjri.khandeland.messages.helper.Helper
import com.hjri.khandeland.messages.model.User
import com.hjri.khandeland.messages.networking.AuthHTTPHelper
import com.hjri.khandeland.messages.networking.MessageBody
import com.hjri.khandeland.messages.networking.Routes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FormActivity : AppCompatActivity() {

    private lateinit var rootLayout: RelativeLayout

    private var revealX: Int = 0
    private var revealY: Int = 0

    private var isShowingLoginForm = false
    private var submitAttempted = false

    private var loginFormVisibility: Boolean
    get() {
        return login_form_container_linear_layout.visibility == View.VISIBLE
    }
    set(visible) {
        login_form_container_linear_layout.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        // Layout Initializations
        rootLayout = container_relative_layout
        nav_title_text_view.text = getString(if (Configs.isLogin) R.string.submit_message_title else R.string.login_and_submit_message_title)
        nav_close_button.setOnClickListener { onBackPressed() }

        login_form_container_linear_layout.visibility = if (Configs.isLogin) View.GONE else View.VISIBLE

        // Inputs
        initInputs()

        doAnimation(savedInstanceState)

    }

    override fun onBackPressed() {
        closeForm()
    }

    // Private Functions

    private fun doFullScreen() {
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
    }

    // Activity Animations

    private fun doAnimation(savedInstanceState: Bundle?) {

        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.visibility = View.INVISIBLE

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0)
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0)


            val viewTreeObserver = rootLayout.viewTreeObserver
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        revealActivity(revealX, revealY)
                        rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        } else {
            rootLayout.visibility = View.VISIBLE
        }
    }


    private fun revealActivity(x: Int, y: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val finalRadius = (Math.max(rootLayout.width, rootLayout.height) * 1.1).toFloat()

                // create the animator for this view (the start radius is zero)
                val circularReveal: Animator = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0f, finalRadius)
                circularReveal.duration = 300
                circularReveal.interpolator = AccelerateInterpolator() as TimeInterpolator?

                // make the view visible and start the animation
                rootLayout.visibility = View.VISIBLE
                circularReveal.start()
            } else {
                finish()
            }
    }

    private fun unRevealActivity() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                finish()
            } else {
                val finalRadius = (Math.max(rootLayout.width, rootLayout.height) * 1.1).toFloat()
                val circularReveal: Animator = ViewAnimationUtils.createCircularReveal(
                        rootLayout, revealX, revealY, finalRadius, 0f)

                circularReveal.duration = 300
                circularReveal.addListener(object: AnimatorListenerAdapter() {
                    @Override
                    override fun onAnimationEnd(animation: Animator) {
                        rootLayout.visibility = View.INVISIBLE
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                })


                circularReveal.start()
            }
    }

    // A small helper just to remember the name easier
    private fun closeForm() {
        unRevealActivity()
    }

    private fun performSubmit() {

        submitAttempted = true

        if (!isFormValid(false)) {
            return
        }

        beginLoading()

        if (Configs.isLogin) {

            Helper.writeDebugLog("Gonna Submit Text")

            val messageText = message_edit_text.text.toString().trim()

            Configs.retrofitInstance.create(Routes.Message::class.java).submitMessage(User.tokenAuthorizationHeader, MessageBody(messageText))
                    .enqueue(object: Callback<Void> {

                        override fun onResponse(call: Call<Void>, response: Response<Void>) {

                            Helper.writeDebugLog("Submit Text was ${if (response.isSuccessful) "successful" else "not successful"}")

                            if (response.isSuccessful) {
                                Configs.pendingMessage = true
                                closeForm()
                            } else {
                                Log.d(Configs.DEBUG_LOG_KEY, "Submit Message Failed: " + response.code() + ", " + response.message())
                            }
                            endLoading()
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Log.d(Configs.DEBUG_LOG_KEY, "Submit Message Connection Failure")
                        }

                    })
        } else {

            val username = username_edit_text.text.toString()
            val password = password_edit_text.text.toString()

            if (isShowingLoginForm) {

                AuthHTTPHelper.loginAndUpdateUser(username, password) { success ->
                    if (success) {

                        Configs.favoriteShouldSync = true
                        performSubmit()
                    } else {
                        endLoading()
                    }
                }

            } else {
                val email = email_edit_text.text.toString()

                Helper.writeDebugLog("Gonna Register")

                AuthHTTPHelper.registerAndUpdateUser(username, password, email) { success ->

                    Helper.writeDebugLog("Registration ${if(success) "succeed" else "not succeed"}")
                    if (success) {
                        performSubmit()
                    } else {
                        endLoading()
                    }
                }
            }
        }
    }

    // Inputs

    private fun initInputs() {

        // Keyboard Focus on the first input (username)
        Handler().postDelayed({

            email_edit_text.clearFocus()

            if (Configs.isLogin) {
                message_edit_text.requestFocus()
            } else {
                username_edit_text.requestFocus()
            }
        }, 100)

        initLoginButton()
        initUsername()
        initEmail()
        initPassword()
        initMessage()
        initSubmitButton()
    }

    private fun initLoginButton() {
        login_ask_button.setOnClickListener { view ->
            isShowingLoginForm = !isShowingLoginForm
            refreshLoginLayout()
        }
    }

    private fun initUsername() {
        // Nothing so far
    }

    private fun initEmail() {
        // Nothing so far
    }

    private fun initPassword() {

        password_edit_text.transformationMethod = PasswordTransformationMethod()
    }

    private fun initMessage() {
        message_edit_text.imeOptions = EditorInfo.IME_ACTION_DONE
        message_edit_text.setRawInputType(InputType.TYPE_CLASS_TEXT)

        message_edit_text.setOnEditorActionListener{ view, actionId, event ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSubmit()
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }
    }

    private fun initSubmitButton() {
        submit_button.setOnClickListener {
            performSubmit()
        }
    }

    private fun resetForm() {

        loginFormVisibility = !Configs.isLogin

        username_edit_text.setText("")
        email_edit_text.setText("")
        password_edit_text.setText("")
        message_edit_text.setText("")
    }

    private fun isFormValid(avoidWhenFocused: Boolean = true): Boolean {
            return if (!Configs.isLogin) {
                handleValidationMessage(username_edit_text.text.toString().trim(), FormInputType.Username, avoidWhenFocused)
                        .and(handleValidationMessage(email_edit_text.text.toString(), FormInputType.Email, avoidWhenFocused))
                        .and(handleValidationMessage(password_edit_text.text.toString(), FormInputType.Password, avoidWhenFocused))
                        .and(handleValidationMessage(message_edit_text.text.toString(), FormInputType.Message, avoidWhenFocused))
            } else handleValidationMessage(message_edit_text.text.toString().trim(), FormInputType.Message, avoidWhenFocused)

    }

    private fun handleValidationMessage(text: String, type: FormInputType, avoidWhenFocused: Boolean = true): Boolean {

        return when (type) {
            FormInputType.Username -> {
                val isValid = FormValidationHelper.validateUsername(text)

                val hideError = isValid || !submitAttempted || (if (avoidWhenFocused) username_edit_text.isFocused else false)
                username_validation_placeholder.visibility = if (hideError) View.GONE else View.VISIBLE

                isValid
            }

            FormInputType.Email -> {

                val isValid = FormValidationHelper.validateEmail(text)

                val hideError = isValid || !submitAttempted || (if (avoidWhenFocused) email_edit_text.isFocused else false)
                email_validation_placeholder.visibility = if (hideError) View.GONE else View.VISIBLE

                isValid
            }

            FormInputType.Password -> {

                val isValid = FormValidationHelper.validatePassword(text)

                val hideError = isValid || !submitAttempted || (if (avoidWhenFocused) password_edit_text.isFocused else false)
                password_validation_placeholder.visibility = if (hideError) View.GONE else View.VISIBLE

                isValid
            }

            FormInputType.Message -> {


                val isValid = FormValidationHelper.validateMessage(text)

                val hideError = isValid || !submitAttempted || (if (avoidWhenFocused) message_edit_text.isFocused else false)
                message_validation_placeholder.visibility = if (hideError) View.GONE else View.VISIBLE

                isValid
            }
        }
    }

    // View

    private fun refreshLoginLayout() {

        if (Configs.isLogin) return

        login_ask_text_view.text = getString(if (isShowingLoginForm) R.string.register_ask else R.string.login_ask)
        login_ask_button.text = getString(if (isShowingLoginForm) R.string.register_ask_button else R.string.login_ask_button)

        email_container_text_input.visibility = if (isShowingLoginForm) View.GONE else View.VISIBLE

    }

    private fun setLoadingState(enabled: Boolean) {

        username_edit_text.isEnabled = !enabled
        email_edit_text.isEnabled = !enabled
        password_edit_text.isEnabled = !enabled
        message_edit_text.isEnabled = !enabled

        login_ask_button.isEnabled = !enabled

        loading_progress_bar.visibility = if (enabled) View.VISIBLE else View.GONE

        submit_button.visibility = if (enabled) View.GONE else View.VISIBLE
    }

    private fun beginLoading() {
        setLoadingState(true)
    }

    private fun endLoading() {
        setLoadingState(false)
    }

    companion object {
        const val EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X"
        const val EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y"
    }
}
