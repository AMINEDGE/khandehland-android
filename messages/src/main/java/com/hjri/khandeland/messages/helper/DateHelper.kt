package com.hjri.khandeland.messages.helper

import java.text.SimpleDateFormat


class DateHelper {

    companion object {

        @JvmStatic
        val DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"

        @JvmStatic
        fun isLater(firstDate: String, secondDate: String): Boolean {
            val comparator = StringDateComparator()

            return comparator.compare(firstDate, secondDate) > 0
        }
    }
}

internal class StringDateComparator : Comparator<String> {
    var dateFormat = SimpleDateFormat(DateHelper.DATE_FORMAT)

    override fun compare(lhs: String, rhs: String): Int {
        return dateFormat.parse(lhs).compareTo(dateFormat.parse(rhs))
    }
}