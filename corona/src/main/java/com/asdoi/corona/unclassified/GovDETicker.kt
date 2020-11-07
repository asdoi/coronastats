package com.asdoi.corona.unclassified

import android.content.Context
import com.asdoi.corona.R
import com.asdoi.corona.model.LightColor
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.Priority
import java.util.*
import kotlin.math.roundToInt

class GovDETicker(
        location: String,
        lastUpdate: Calendar,
        cases: Int,
        val casesDifferenceYesterdayToday: Int,
        val casesPerOneHundredThousands: Double,
        val sevenDayIncidencePerOneHundredThousands: Double,
        deaths: Int
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
                .append(context.getString(R.string.deaths, deaths))
                .toString()
    }

    companion object {
        const val DATA_SOURCE = "www.disease.sh"
        const val VISIBLE_DATA_SOURCE =
                "https://disease.sh/v3/covid-19/gov/Germany"
    }
}