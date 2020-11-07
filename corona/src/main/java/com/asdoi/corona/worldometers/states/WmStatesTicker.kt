package com.asdoi.corona.worldometers.states

import android.annotation.SuppressLint
import android.content.Context
import com.asdoi.corona.R
import com.asdoi.corona.model.LightColor
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.Priority
import java.util.*

class WmStatesTicker(
        location: String,
        lastUpdate: Calendar,
        cases: Int,
        val casesToday: Int,
        val casesPerMillion: Double,
        val active: Int,
        val recovered: Int,
        deaths: Int,
        val deathsToday: Int,
        val deathsPerOneMillion: Double,
        val tests: Int,
        val testsPerOneMillion: Double,
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
        val infectionsYesterdayToday =
                (if (casesToday > 0) "+" else "") + casesToday


        return StringBuilder()
                .append(context.getString(R.string.change_from_previous_day, infectionsYesterdayToday))
                .append("\n")
                .append(context.getString(R.string.active_cases, active))
                .toString()
    }

    @SuppressLint("StringFormatInvalid")
    override fun details(context: Context): String {
        val changeCases =
                (if (casesToday > 0) "+" else "") + casesToday
        val changeDeaths =
                (if (deathsToday > 0) "+" else "") + deathsToday

        return StringBuilder()
                .append(context.getString(R.string.total_infections, cases)).append("\n")
                .append("\t\t")
                .append(context.getString(R.string.change_from_previous_day, changeCases)).append("\n")
                .append(context.getString(R.string.infections_per_one_million, casesPerMillion))
                .append("\n").append("\n")
                .append(context.getString(R.string.active_cases, active)).append("\n")
                .append(context.getString(R.string.recovered_infections, recovered)).append("\n")
                .append("\n")
                .append(context.getString(R.string.deaths, deaths)).append("\n")
                .append("\t\t")
                .append(context.getString(R.string.change_from_previous_day, changeDeaths)).append("\n")
                .append(context.getString(R.string.deaths_per_one_million, deathsPerOneMillion))
                .append("\n").append("\n")
                .append(context.getString(R.string.total_tests, tests)).append("\n")
                .append(context.getString(R.string.tests_per_one_million, testsPerOneMillion))
                .toString()
    }

    companion object {
        const val DATA_SOURCE = "www.worldometers.info"
        const val VISIBLE_DATA_SOURCE = "https://www.worldometers.info/coronavirus/"
    }
}