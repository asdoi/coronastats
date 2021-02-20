package com.asdoi.corona.lgl

import android.content.Context
import com.asdoi.corona.R
import com.asdoi.corona.model.LightColor
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.Priority
import java.util.*
import kotlin.math.roundToInt

class LGLTicker(
    location: String,
    lastUpdate: Calendar,
    cases: Int,
    val casesDifferenceYesterdayToday: Int,
    val casesPerOneHundredThousands: Double,
    val casesInTheLastSevenDays: Int,
    val sevenDayIncidencePerOneHundredThousands: Double,
    deaths: Int,
    val deathsDifferenceYesterdayToday: Int
) : LiveTicker(
    Priority.COUNTY,
    location,
    lastUpdate,
    DATA_SOURCE,
    VISIBLE_DATA_SOURCE,
    cases,
    deaths,
    calculateLightColor(sevenDayIncidencePerOneHundredThousands.roundToInt())
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

        val deathsYesterdayToday =
            (if (deathsDifferenceYesterdayToday > 0) "+" else "") + deathsDifferenceYesterdayToday

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
            .append("\n")
            .append("\t\t")
            .append(
                context.getString(
                    R.string.change_from_previous_day,
                    deathsYesterdayToday
                )
            ).toString()
    }

    companion object {
        const val DATA_SOURCE = "www.lgl.bayern.de"
        const val VISIBLE_DATA_SOURCE =
            "https://www.lgl.bayern.de/gesundheit/infektionsschutz/infektionskrankheiten_a_z/coronavirus/karte_coronavirus/index.htm"

        private fun calculateLightColor(value: Int) = when (value) {
            in 0 until 25 -> LightColor.GREEN
            in 25 until 35 -> LightColor.YELLOW
            in 35 until 50 -> LightColor.ORANGE
            in 50 until 100 -> LightColor.RED
            in 100 until 200 -> LightColor.DEEP_RED
            else -> LightColor.PURPLE
        }
    }
}