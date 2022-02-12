package com.asdoi.corona.worldometers.continents

import com.asdoi.corona.ParseError
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*


object WmContinentsParser : LiveTickerParser() {
    private fun getAPIUrl(continent: String): String {
        return "https://disease.sh/v3/covid-19/continents/${continent}?yesterday=false&twoDaysAgo=false&strict=false"
    }

    private fun parseLiveTickers(vararg documents: Document): List<LiveTicker> {
        val tickers: MutableList<LiveTicker> = mutableListOf()

        for (document in documents) {
            try {
                val jsonObject = JSONObject(document.text())
                val location = jsonObject.getString("continent")

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
                    val activePerMillion = jsonObject.getDouble("activePerOneMillion")
                    val recoveredPerMillion = jsonObject.getDouble("recoveredPerOneMillion")
                    val criticalPerMillion = jsonObject.getDouble("criticalPerOneMillion")

                    val lastUpdateMillis = jsonObject.get("updated").toString()
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
                        WmContinentsTicker(
                            location,
                            lastUpdate,
                            cases,
                            todayCases,
                            casesPerMillion,
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
                            tests,
                            testsPerMillion
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    tickers.add(
                        ParseError(
                            location,
                            WmContinentsTicker.DATA_SOURCE,
                            WmContinentsTicker.VISIBLE_DATA_SOURCE,
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
    fun downloadDocuments(vararg continents: String): List<Document> {
        val documents: MutableList<Document> = mutableListOf()
        for (continent in continents) {
            val document = Jsoup.connect(getAPIUrl(continent))
                .ignoreHttpErrors(true)
                .ignoreContentType(true)
                .get()

            if (document != null)
                documents.add(document)
            else
                throw IOException()
        }
        return documents
    }

    @Throws(IOException::class)
    override fun parse(vararg locations: String): List<LiveTicker> {
        return parse(*downloadDocuments(*locations).toTypedArray())
    }

    override fun getSuggestions(): List<String> {
        return listOf(
            "Europe",
            "North America",
            "South America",
            "Asia",
            "Africa",
            "Australia/Oceania"
        )
    }
}