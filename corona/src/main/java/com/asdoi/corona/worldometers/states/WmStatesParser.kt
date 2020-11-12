package com.asdoi.corona.worldometers.states

import com.asdoi.corona.ParseError
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*


object WmStatesParser : LiveTickerParser() {
    private fun getAPIUrl(vararg states: String): String {
        if (states.isEmpty())
            return ""

        val countriesQuery = StringBuilder()
        for (country in states) {
            countriesQuery.append(",").append(country)
        }
        val query = countriesQuery.removePrefix(",")

        return "https://disease.sh/v3/covid-19/states/${query}?yesterday=false&strict=false"
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
                val location = jsonObject.getString("state")

                try {
                    val cases = jsonObject.getInt("cases")
                    val todayCases = jsonObject.getInt("todayCases")
                    val deaths = jsonObject.getInt("deaths")
                    val todayDeaths = jsonObject.getInt("todayDeaths")
                    val recovered = jsonObject.getInt("recovered")
                    val active = jsonObject.getInt("active")
                    val casesPerMillion = jsonObject.getDouble("casesPerOneMillion")
                    val deathsPerMillion = jsonObject.getDouble("deathsPerOneMillion")
                    val tests = jsonObject.getInt("tests")
                    val testsPerMillion = jsonObject.getDouble("testsPerOneMillion")

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
                            WmStatesTicker(
                                    location,
                                    lastUpdate,
                                    cases,
                                    todayCases,
                                    casesPerMillion,
                                    active,
                                    recovered,
                                    deaths,
                                    todayDeaths,
                                    deathsPerMillion,
                                    tests,
                                    testsPerMillion,
                            )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    tickers.add(
                            ParseError(
                                    location,
                                    WmStatesTicker.DATA_SOURCE,
                                    WmStatesTicker.VISIBLE_DATA_SOURCE,
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
    fun downloadDocuments(vararg states: String): Document {
        return Jsoup.connect(getAPIUrl(*states))
            .ignoreHttpErrors(true)
            .ignoreContentType(true)
            .get()
            ?: throw IOException()
    }

    @Throws(IOException::class)
    override fun parse(vararg locations: String): List<LiveTicker> {
        return parse(downloadDocuments(*locations))
    }
}