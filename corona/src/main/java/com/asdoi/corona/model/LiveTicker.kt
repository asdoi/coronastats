package com.asdoi.corona.model

import android.content.Context
import com.asdoi.corona.ParseError
import com.asdoi.corona.R
import java.text.SimpleDateFormat
import java.util.*

abstract class LiveTicker(
    val priority: Priority,
    val location: String,
    val lastUpdate: Calendar,
    val dataSource: String,
    val linkToVisibleData: String,
    val cases: Int,
    val deaths: Int,
    val lightColor: LightColor
) {

    abstract fun summary(context: Context): String
    abstract fun details(context: Context): String

    fun metaInformation(context: Context): String {
        val lastUpdateString =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT)
                .format(lastUpdate.time)

        return StringBuilder()
            .append(
                context.getString(
                    R.string.data_source,
                    dataSource
                )
            ).append("\n")
            .append(
                context.getString(
                    R.string.last_update,
                    lastUpdateString
                )
            )
            .toString()
    }

    fun getColor(context: Context): Int {
        val colorResource: Int =
            when (lightColor) {
                LightColor.GREEN -> R.color.light_green
                LightColor.YELLOW -> R.color.light_yellow
                LightColor.RED -> R.color.light_red
                LightColor.DEEP_RED -> R.color.light_deep_red
                LightColor.PURPLE -> R.color.light_purple
                LightColor.ERROR -> R.color.error_light
                LightColor.NO_COLOR -> R.color.no_color
            }
        return context.resources.getColor(colorResource)
    }

    fun isError() = this is ParseError
}