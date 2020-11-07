package com.asdoi.corona.rki.states

import android.content.Context
import com.asdoi.corona.R
import com.asdoi.corona.model.LightColor
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.Priority
import java.util.*
import kotlin.math.roundToInt

class RKIStateTicker(
        location: String,
        lastUpdate: Calendar,
        cases: Int,
        deaths: Int,
        val casesPerOneHundredThousands: Double,
        val sevenDayIncidencePerOneHundredThousands: Double,
) : LiveTicker(
        Priority.NATIONAL,
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
                                R.string.total_infections_per_100_000,
                                casesPerOneHundredThousands
                        )
                ).append("\n")
                .append(
                        context.getString(
                                R.string.seven_day_incidence_per_100_000,
                                sevenDayIncidencePerOneHundredThousands.roundToInt().toFloat()
                        )
                                .removeSuffix(".0").removeSuffix(",0")
                )
                .toString()
    }

    override fun details(context: Context): String {
        return StringBuilder()
                .append(
                        context.getString(
                                R.string.total_infections,
                                cases
                        )
                ).append("\n")
                .append(
                        context.getString(
                                R.string.total_infections_per_100_000,
                                casesPerOneHundredThousands
                        )
                ).append("\n")
                .append(
                        context.getString(
                                R.string.seven_day_incidence_per_100_000,
                                sevenDayIncidencePerOneHundredThousands
                        )
                ).append("\n")
                .append(
                        context.getString(R.string.deaths, deaths)
                )
                .toString()
    }

    companion object {
        const val DATA_SOURCE = "www.rki.de"
        const val VISIBLE_DATA_SOURCE =
                "https://experience.arcgis.com/experience/478220a4c454480e823b17327b2bf1d4"
    }
}