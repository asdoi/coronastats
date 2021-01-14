package com.asdoi.corona.rki.germany

import android.content.Context
import com.asdoi.corona.R
import com.asdoi.corona.model.LightColor
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.Priority
import java.util.*
import kotlin.math.roundToInt

class RKIGermanyTicker(
    location: String,
    lastUpdate: Calendar,
    cases: Int,
    deaths: Int,
    val casesDifferenceYesterdayToday: Int,
    val casesInTheLastSevenDays: Int,
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
        val infectionsYesterdayToday =
            (if (casesDifferenceYesterdayToday > 0) "+" else "") + casesDifferenceYesterdayToday

        return StringBuilder()
            .append(
                context.getString(
                    R.string.change_from_previous_day,
                    infectionsYesterdayToday
                )
            )
            .append("\n")
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
        val infectionsYesterdayToday =
            (if (casesDifferenceYesterdayToday > 0) "+" else "") + casesDifferenceYesterdayToday

        return StringBuilder()
            .append(
                context.getString(
                    R.string.total_infections,
                    cases
                )
            ).append("\n")
            .append("\t\t")
            .append(
                context.getString(
                    R.string.change_from_previous_day,
                    infectionsYesterdayToday
                )
            )
            .append("\n")
            .append(
                context.getString(
                    R.string.infections_in_the_last_seven_days,
                    casesInTheLastSevenDays
                )
            ).append("\n")
            .append(
                context.getString(
                    R.string.seven_day_incidence_per_100_000,
                    sevenDayIncidencePerOneHundredThousands
                )
            ).append("\n")
            .append(context.getString(R.string.deaths, deaths))
            .toString()
    }

    companion object {
        const val DATA_SOURCE = "www.rki.de"
        const val VISIBLE_DATA_SOURCE =
            "https://www.rki.de/DE/Content/InfAZ/N/Neuartiges_Coronavirus/Fallzahlen.html"
    }
}