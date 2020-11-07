package com.asdoi.corona.johnshopkins

import android.content.Context
import com.asdoi.corona.R
import com.asdoi.corona.model.LightColor
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.Priority
import java.util.*

class JHUTicker(
        location: String,
        lastUpdate: Calendar,
        cases: Int,
        deaths: Int,
        val recovered: Int,
) : LiveTicker(
        Priority.GLOBAL,
        location,
        lastUpdate,
        DATA_SOURCE,
        VISIBLE_DATA_SOURCE,
        cases,
        deaths,
        LightColor.NO_COLOR
) {

    override fun summary(context: Context): String {
        return StringBuilder()
                .append(
                        context.getString(
                                R.string.total_infections,
                                cases
                        )
                ).append("\n")
                .append(context.getString(R.string.recovered_infections, recovered)).append("\n")
                .append(context.getString(R.string.deaths, deaths))
                .toString()
    }

    override fun details(context: Context): String {
        return summary(context)
    }

    companion object {
        const val DATA_SOURCE = "www.jhu.edu"
        const val VISIBLE_DATA_SOURCE = "https://coronavirus.jhu.edu/region"
    }
}