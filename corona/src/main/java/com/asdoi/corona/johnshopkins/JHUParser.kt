package com.asdoi.corona.johnshopkins

import com.asdoi.corona.ParseError
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


object JHUParser : LiveTickerParser() {
    private const val DOCUMENT_URL = "https://disease.sh/v3/covid-19/jhucsse"

    private fun parseLiveTickers(document: Document, vararg locations: String): List<LiveTicker> {
        val locationsList: MutableList<String> = mutableListOf()
        for (location in locations) {
            locationsList.add(location.toUpperCase())
        }

        val tickers: MutableList<LiveTicker> = mutableListOf()

        try {
            val json = JSONArray(document.text())

            for (jsonIndex in 0 until json.length()) {
                val jsonObject: JSONObject = json.getJSONObject(jsonIndex)
                val country = jsonObject.getString("country")
                val province = jsonObject.getString("province")
                val location = if (province == "null") country else province
                if (locationsList.contains(location.toUpperCase())) {
                    try {
                        val stats = jsonObject.getJSONObject("stats")
                        val confirmed = stats.getInt("confirmed")
                        val deaths = stats.getInt("deaths")
                        val recovered = stats.getInt("recovered")
                        val updatedAt = jsonObject.getString("updatedAt")
                        val lastUpdate: Calendar =
                                try {
                                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

                                    val calendar = Calendar.getInstance()
                                    calendar.time = dateFormat.parse(updatedAt)!!
                                    calendar
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Calendar.getInstance()
                                }

                        tickers.add(
                                JHUTicker(
                                        location,
                                        lastUpdate,
                                        confirmed,
                                        deaths,
                                        recovered
                                )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        tickers.add(
                                ParseError(
                                        location,
                                        JHUTicker.DATA_SOURCE,
                                        JHUTicker.VISIBLE_DATA_SOURCE,
                                        e
                                )
                        )
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            tickers.add(
                    ParseError(JHUTicker.DATA_SOURCE, JHUTicker.VISIBLE_DATA_SOURCE, e)
            )
        }

        return tickers
    }

    fun parse(document: Document, vararg locations: String): List<LiveTicker> {
        return parseLiveTickers(document, *locations)
    }

    fun parseNoErrors(document: Document, vararg locations: String) =
            parse(document, *locations).filter { !it.isError() }

    fun parseNoInternalErrors(document: Document, vararg locations: String) =
            parse(document, *locations).filter {
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
        return parse(downloadDocument(), *locations)
    }
}