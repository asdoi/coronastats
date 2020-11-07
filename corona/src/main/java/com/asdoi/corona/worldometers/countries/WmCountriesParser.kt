package com.asdoi.corona.worldometers.countries

import com.asdoi.corona.ParseError
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*


object WmCountriesParser : LiveTickerParser() {
    private fun getAPIUrl(vararg countries: String): String {
        if (countries.isEmpty())
            return ""

        val countriesQuery = StringBuilder()
        for (country in countries) {
            countriesQuery.append(",").append(country)
        }
        val query = countriesQuery.removePrefix(",")

        return "https://disease.sh/v3/covid-19/countries/${query}?yesterday=false&twoDaysAgo=false&strict=true"
    }

    private fun parseLiveTickers(vararg documents: Document): List<LiveTicker> {
        val tickers: MutableList<LiveTicker> = mutableListOf()
        val jsonObjects: MutableList<JSONObject> = mutableListOf()

        for (document in documents) {
            try {
                val jsonObject = JSONObject(document.text())
                jsonObjects.add(jsonObject)
            } catch (ignore: Exception) {
                try {
                    val jsonArray = JSONArray(document.text())
                    for (jsonArrayIndex in 0 until jsonArray.length()) {
                        jsonObjects.add(jsonArray.getJSONObject(jsonArrayIndex))
                    }
                } catch (ignore: Exception) {
                }
            }
        }

        for (jsonObject in jsonObjects) {
            try {
                val location = jsonObject.getString("country")

                try {
                    val cases = jsonObject.getInt("cases")
                    val todayCases = jsonObject.getInt("todayCases")
                    val deaths = jsonObject.getInt("deaths")
                    val todayDeaths = jsonObject.getInt("todayDeaths")
                    val recovered = jsonObject.getInt("recovered")
                    val todayRecovered = jsonObject.getInt("todayRecovered")
                    val active = jsonObject.getInt("active")
                    val critical = jsonObject.getInt("critical")
                    val casesPerMillion = jsonObject.getDouble("casesPerOneMillion")
                    val deathsPerMillion = jsonObject.getDouble("deathsPerOneMillion")
                    val tests = jsonObject.getInt("tests")
                    val testsPerMillion = jsonObject.getDouble("testsPerOneMillion")
                    val casePerPeople = jsonObject.getInt("oneCasePerPeople")
                    val deathPerPeople = jsonObject.getInt("oneDeathPerPeople")
                    val testPerPeople = jsonObject.getInt("oneTestPerPeople")
                    val activePerMillion = jsonObject.getDouble("activePerOneMillion")
                    val recoveredPerMillion = jsonObject.getDouble("recoveredPerOneMillion")
                    val criticalPerMillion = jsonObject.getDouble("criticalPerOneMillion")

                    val lastUpdateMillis = jsonObject.getString("updated")
                    val lastUpdate: Calendar =
                            try {
                                val calendar = Calendar.getInstance()
                                calendar.timeInMillis = lastUpdateMillis.toLong()
                                calendar
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Calendar.getInstance()
                            }

                    tickers.add(
                            WmCountriesTicker(
                                    location,
                                    lastUpdate,
                                    cases,
                                    todayCases,
                                    casesPerMillion,
                                    casePerPeople,
                                    active,
                                    activePerMillion,
                                    critical,
                                    criticalPerMillion,
                                    recovered,
                                    todayRecovered,
                                    recoveredPerMillion,
                                    deaths,
                                    todayDeaths,
                                    deathsPerMillion,
                                    deathPerPeople,
                                    tests,
                                    testsPerMillion,
                                    testPerPeople
                            )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    tickers.add(
                            ParseError(
                                    location,
                                    WmCountriesTicker.DATA_SOURCE,
                                    WmCountriesTicker.VISIBLE_DATA_SOURCE,
                                    e
                            )
                    )
                }
            } catch (ignore: Exception) {
            }
        }


        return tickers
    }

    fun parse(vararg documents: Document): List<LiveTicker> {
        return parseLiveTickers(*documents)
    }

    fun parseNoErrors(vararg documents: Document) =
            parse(*documents).filter { !it.isError() }

    fun parseNoInternalErrors(vararg documents: Document) =
            parse(*documents).filter {
                if (it.isError()) {
                    !(it as ParseError).isInternalError()
                } else
                    true
            }

    @Throws(IOException::class)
    fun downloadDocuments(vararg countries: String): Document {
        return Jsoup.connect(getAPIUrl(*countries))
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .get()
                ?: throw IOException()
    }


    override fun parse(vararg countries: String): List<LiveTicker> {
        return parse(downloadDocuments(*countries))
    }
}