package com.asdoi.corona.worldometers.world

import android.annotation.SuppressLint
import android.content.Context
import com.asdoi.corona.R
import com.asdoi.corona.model.LightColor
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.Priority
import java.util.*

class WmWorldTicker(
        location: String,
        lastUpdate: Calendar,
        cases: Int,
        val casesToday: Int,
        val casesPerMillion: Double,
        val oneCasePerPeople: Int,
        val active: Int,
        val activePerOneMillion: Double,
        val critical: Int,
        val criticalPerOneMillion: Double,
        val recovered: Int,
        val recoveredToday: Int,
        val recoveredPerOneMillion: Double,
        deaths: Int,
        val deathsToday: Int,
        val deathsPerOneMillion: Double,
        val oneDeathPerPeople: Int,
        val tests: Int,
        val testsPerOneMillion: Double,
        val oneTestPerPeople: Int,
        val affectedCountries: Int
) : LiveTicker(
        Priority.WORLD,
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
                .append(context.getString(R.string.active_cases, active)).append("\n")
                .append(
                        context.getString(
                                R.string.active_infections_per_one_million,
                                activePerOneMillion
                        )
                )
                .toString()
    }

    @SuppressLint("StringFormatInvalid")
    override fun details(context: Context): String {
        val changeCases =
                (if (casesToday > 0) "+" else "") + casesToday
        val changeRecovered =
                (if (recoveredToday > 0) "+" else "") + recoveredToday
        val changeDeaths =
                (if (deathsToday > 0) "+" else "") + deathsToday

        return StringBuilder()
                .append(context.getString(R.string.total_infections, cases)).append("\n")
                .append("\t\t")
                .append(context.getString(R.string.change_from_previous_day, changeCases)).append("\n")
                .append(context.getString(R.string.infections_per_one_million, casesPerMillion))
                .append("\n")
                .append(context.getString(R.string.one_infection_per_persons, oneCasePerPeople))
                .append("\n")
                .append("\n")
                .append(context.getString(R.string.active_cases, active)).append("\n")
                .append(
                        context.getString(
                                R.string.active_infections_per_one_million,
                                activePerOneMillion
                        )
                ).append("\n")
                .append("\n")
                .append(context.getString(R.string.critical_infections, critical)).append("\n")
                .append(
                        context.getString(
                                R.string.critical_infections_per_one_million,
                                criticalPerOneMillion
                        )
                ).append("\n")
                .append("\n")
                .append(context.getString(R.string.recovered_infections, recovered)).append("\n")
                .append("\t\t")
                .append(context.getString(R.string.change_from_previous_day, changeRecovered))
                .append("\n")
                .append(
                        context.getString(
                                R.string.recovered_infections_per_one_million,
                                recoveredPerOneMillion
                        )
                ).append("\n")
                .append("\n")
                .append(context.getString(R.string.deaths, deaths)).append("\n")
                .append("\t\t")
                .append(context.getString(R.string.change_from_previous_day, changeDeaths)).append("\n")
                .append(context.getString(R.string.deaths_per_one_million, deathsPerOneMillion))
                .append("\n")
                .append(context.getString(R.string.one_death_per_persons, oneDeathPerPeople))
                .append("\n")
                .append("\n")
                .append(context.getString(R.string.total_tests, tests)).append("\n")
                .append(context.getString(R.string.tests_per_one_million, testsPerOneMillion))
                .append("\n")
                .append(context.getString(R.string.one_test_per_persons, oneTestPerPeople)).append("\n")
                .append("\n")
                .append(context.getString(R.string.affected_countries, affectedCountries))
                .toString()
    }

    companion object {
        const val DATA_SOURCE = "www.worldometers.info"
        const val VISIBLE_DATA_SOURCE = "https://www.worldometers.info/coronavirus/"
    }
}