package com.asdoi.corona.unclassified

import com.asdoi.corona.ParseError
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*


object GovDEParser : LiveTickerParser() {
    private const val DOCUMENT_URL = "https://disease.sh/v3/covid-19/gov/Germany"

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
                val province = jsonObject.getString("province")
                val location = if (province == "Total") "Deutschland" else province

                if (locationsList.contains(location.toUpperCase())) {
                    try {
                        val cases = jsonObject.getInt("cases")
                        val casePreviousDayChange = jsonObject.getInt("casePreviousDayChange")
                        val casesPerHundredThousand =
                                jsonObject.getDouble("casesPerHundredThousand")
                        val sevenDayCasesPerHundredThousand =
                                jsonObject.getDouble("sevenDayCasesPerHundredThousand")
                        val deaths = jsonObject.getInt("deaths")

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
                                GovDETicker(
                                        location,
                                        lastUpdate,
                                        cases,
                                        casePreviousDayChange,
                                        casesPerHundredThousand,
                                        sevenDayCasesPerHundredThousand,
                                        deaths
                                )
                        )

                    } catch (e: Exception) {
                        e.printStackTrace()
                        tickers.add(
                                ParseError(
                                        location,
                                        GovDETicker.DATA_SOURCE,
                                        GovDETicker.VISIBLE_DATA_SOURCE,
                                        e
                                )
                        )
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            tickers.add(
                    ParseError(GovDETicker.DATA_SOURCE, GovDETicker.VISIBLE_DATA_SOURCE, e)
            )
        }

        return tickers
    }

    fun parse(document: Document, vararg counties: String): List<LiveTicker> {
        return parseLiveTickers(document, *counties)
    }

    fun parseNoErrors(document: Document, vararg counties: String) =
            parse(document, *counties).filter { !it.isError() }

    fun parseNoInternalErrors(document: Document, vararg counties: String) =
            parse(document, *counties).filter {
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


    override fun parse(vararg counties: String): List<LiveTicker> {
        return parse(downloadDocument(), *counties)
    }
}