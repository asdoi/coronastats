package com.asdoi.corona.worldometers.world

import com.asdoi.corona.ParseError
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import com.asdoi.corona.unclassified.GovDEParser
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*


object WmWorldParser : LiveTickerParser() {
    private const val DOCUMENT_URL =
        "https://disease.sh/v3/covid-19/all?yesterday=false&twoDaysAgo=false"
    const val location = "World"

    private fun parseLiveTickers(document: Document): LiveTicker {
        try {
            val json = JSONObject(document.text())

            val cases = json.getInt("cases")
            val casesToday = json.getInt("todayCases")
            val casesPerMillion = json.getDouble("casesPerOneMillion")
            val casesPerPeople = json.getInt("oneCasePerPeople")
            val active = json.getInt("active")
            val activePerOneMillion = json.getDouble("activePerOneMillion")
            val critical = json.getInt("critical")
            val criticalPerOneMillion = json.getDouble("criticalPerOneMillion")
            val recovered = json.getInt("recovered")
            val recoveredToday = json.getInt("todayRecovered")
            val recoveredPerOneMillion = json.getDouble("recoveredPerOneMillion")
            val deaths = json.getInt("deaths")
            val deathsToday = json.getInt("todayDeaths")
            val deathsPerOneMillion = json.getDouble("deathsPerOneMillion")
            val deathsPerPeople = json.getInt("oneDeathPerPeople")
            val tests = json.getInt("tests")
            val testsPerOneMillion = json.getDouble("testsPerOneMillion")
            val testsPerPeople = json.getInt("oneTestPerPeople")
            val affectedCountries = json.getInt("affectedCountries")

            val lastUpdateMillis = json.get("updated").toString()
            val lastUpdate: Calendar =
                try {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = lastUpdateMillis.toLong()
                    calendar
                } catch (e: Exception) {
                    e.printStackTrace()
                    Calendar.getInstance()
                }

            return WmWorldTicker(
                location,
                lastUpdate,
                cases,
                casesToday,
                casesPerMillion,
                casesPerPeople,
                active,
                activePerOneMillion,
                critical,
                criticalPerOneMillion,
                recovered,
                recoveredToday,
                recoveredPerOneMillion,
                deaths,
                deathsToday,
                deathsPerOneMillion,
                deathsPerPeople,
                tests,
                testsPerOneMillion,
                testsPerPeople,
                affectedCountries
            )

        } catch (e: Exception) {
            e.printStackTrace()
            return ParseError(
                location,
                WmWorldTicker.DATA_SOURCE,
                WmWorldTicker.VISIBLE_DATA_SOURCE,
                e
            )
        }
    }

    fun parse(document: Document): List<LiveTicker> {
        return listOf(parseLiveTickers(document))
    }

    fun parseNoErrors(document: Document) =
        GovDEParser.parse(document).filter { !it.isError() }

    fun parseNoInternalErrors(document: Document) =
        GovDEParser.parse(document).filter {
            if (it.isError()) {
                !(it as ParseError).isInternalError()
            } else
                true
        }

    @Throws(IOException::class)
    fun downloadDocument(): Document {
        return Jsoup.connect(DOCUMENT_URL)
            .ignoreHttpErrors(true)
            .ignoreContentType(true)
            .get()
    }

    @Throws(IOException::class)
    override fun parse(vararg locations: String): List<LiveTicker> {
        return if (locations.any { it.toUpperCase() == location.toUpperCase() })
            parse(downloadDocument())
        else
            listOf()
    }

    override fun getSuggestions(): List<String> {
        return listOf(location)
    }
}