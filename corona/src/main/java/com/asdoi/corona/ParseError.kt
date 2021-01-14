package com.asdoi.corona

import android.content.Context
import com.asdoi.corona.model.LightColor
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.Priority
import java.util.*


class ParseError(
    location: String,
    dataSource: String,
    linkToVisibleData: String,
    val exception: Exception
) : LiveTicker(
    Priority.NONE,
    location,
    Calendar.getInstance(),
    dataSource,
    linkToVisibleData,
    -1, -1,
    LightColor.ERROR
) {
    constructor(dataSource: String, linkToVisibleData: String, exception: Exception) : this(
        INTERNAL_ERROR,
        dataSource,
        linkToVisibleData,
        exception
    )

    override fun summary(context: Context): String {
        return if (isInternalError())
            exception.toString()
        else
            context.getString(R.string.parsing_error)
    }

    override fun details(context: Context): String {
        return exception.stackTraceToString()
    }

    fun isInternalError(): Boolean = location == INTERNAL_ERROR

    companion object {
        const val INTERNAL_ERROR = "Internal Error"
    }
}