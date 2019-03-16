package com.hjri.khandeland.messages.helper

import android.text.TextUtils

enum class FormInputType {
    Username,
    Email,
    Password,
    Message
}

class FormValidationHelper {

    companion object {

        const val USERNAME_MIN_LENGTH = 3
        const val USERNAME_MAX_LENGTH = 20

        const val EMAIL_MIN_LENGTH = 7
        const val EMAIL_MAX_LENGTH = 100

        const val PASSWORD_MIN_LENGTH = 5
        const val PASSWORD_MAX_LENGTH = 30

        const val MESSAGE_MIN_LENGTH = 10
        const val MESSAGE_MAX_LENGTH = 600
        const val MESSAGE_MAX_LINES = 12

        fun validateUsername(username: String): Boolean {
            return isNumberOrLetter(username) && isStringLengthValid(username, FormInputType.Username)
        }

        fun validateEmail(email: String): Boolean {
            return TextUtils.isEmpty(email) || (isEmail(email) && isStringLengthValid(email, FormInputType.Email))
        }

        fun validatePassword(password: String): Boolean {
            return isStringLengthValid(password, FormInputType.Password)
        }

        fun validateMessage(message: String): Boolean {
            val isLengthValid = isStringLengthValid(message, FormInputType.Message)

            var returnCounter = 0

            for (character in message) {
                if (character == '\n') returnCounter++
            }

            val areLinesValid = returnCounter <= MESSAGE_MAX_LINES

            return isLengthValid && areLinesValid
        }

        private fun isStringLengthValid(str: String, type: FormInputType): Boolean {
            return when(type) {

                FormInputType.Username -> str.length in USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH
                FormInputType.Email -> str.length in EMAIL_MIN_LENGTH..EMAIL_MAX_LENGTH
                FormInputType.Password -> str.length in PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH
                FormInputType.Message -> str.length in MESSAGE_MIN_LENGTH..MESSAGE_MAX_LENGTH
            }
        }


        fun isEmail(str: String): Boolean {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(str).matches()
        }

        fun isNumber(str: String): Boolean {
            return TextUtils.isDigitsOnly(str)
        }

        fun isNumberOrLetter(str: String): Boolean {
            for (character in str) {
                if (!Character.isLetterOrDigit(character)) return false
            }
            return true
        }

        fun isLengthBetween(str: String, min: Int, max: Int): Boolean {
            return str.length in min..max
        }
    }
}