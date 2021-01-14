package com.asdoi.corona.rki.counties

import android.annotation.SuppressLint
import android.content.Context
import com.asdoi.corona.R
import com.asdoi.corona.model.LightColor
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.Priority
import java.util.*
import kotlin.math.roundToInt

class RKICountyTicker(
    location: String,
    lastUpdate: Calendar,
    cases: Int,
    deaths: Int,
    val casesPerOneHundredThousands: Double,
    val sevenDayIncidencePerOneHundredThousands: Double,
    val deathRate: Double,
    val casesPerPopulation: Double
) : LiveTicker(
    Priority.NATIONAL,
    location,
    lastUpdate,
    DATA_SOURCE,
    VISIBLE_DATA_SOURCE,
    cases,
    deaths,
    calculateLightColor(sevenDayIncidencePerOneHundredThousands.roundToInt())
) {


    @SuppressLint("StringFormatInvalid")
    override fun summary(context: Context): String {
        return StringBuilder()
            .append(
                context.getString(
                    R.string.infections_per_population,
                    casesPerPopulation
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

    @SuppressLint("StringFormatInvalid")
    override fun details(context: Context): String {
        return StringBuilder()
            .append(
                context.getString(
                    R.string.total_infections,
                    cases
                )
            ).append("\n").append("\t\t")
            .append(
                context.getString(
                    R.string.infections_per_population,
                    casesPerPopulation
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
            ).append("\n").append("\t\t")
            .append(
                context.getString(R.string.death_rate, deathRate)
            )
            .toString()
    }

    companion object {
        const val DATA_SOURCE = "www.rki.de"
        const val VISIBLE_DATA_SOURCE =
            "https://experience.arcgis.com/experience/478220a4c454480e823b17327b2bf1d4"

        private fun calculateLightColor(value: Int) = when (value) {
            in 0 until 25 -> LightColor.GREEN
            in 25 until 50 -> LightColor.YELLOW
            in 50 until 100 -> LightColor.RED
            in 100 until 200 -> LightColor.DEEP_RED
            else -> LightColor.PURPLE
        }
    }
}