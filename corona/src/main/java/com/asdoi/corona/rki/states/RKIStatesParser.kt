package com.asdoi.corona.rki.states

import com.asdoi.corona.ParseError
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*


object RKIStatesParser : LiveTickerParser() {
    private const val DOCUMENT_URL =
        "https://www.rki.de/DE/Content/InfAZ/N/Neuartiges_Coronavirus/Fallzahlen.html"

    private fun getAPIUrl(state: String): String {
        return "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/Coronaf%C3%A4lle_in_den_Bundesl%C3%A4ndern/FeatureServer/0/query?where=LAN_ew_GEN%20%3D%20%27${state}%27&outFields=*&outSR=4326&f=json"
    }

    private fun parseLiveTickers(
        tableDocument: Document?,
        vararg documents: Document
    ): List<LiveTicker> {
        val tickers: MutableList<LiveTicker> = mutableListOf()

        for (document in documents) {
            try {
                val json = JSONObject(document.text())
                val featuresArray = json.getJSONArray("features").getJSONObject(0)
                val attributes = featuresArray.getJSONObject("attributes")

                val location = attributes.getString("LAN_ew_GEN")
                try {
                    val cases = attributes.getInt("Fallzahl")
                    val deaths = attributes.getInt("Death")
                    val casesPerOneHundredThousands = attributes.getDouble("faelle_100000_EW")
                    val sevenDayIncidencePerOneHundredThousands =
                        attributes.getDouble("cases7_bl_per_100k")

                    val lastUpdateMillis = attributes.getString("Aktualisierung")
                    val lastUpdate: Calendar =
                        try {
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = lastUpdateMillis.toLong()
                            calendar
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Calendar.getInstance()
                        }

                    try {
                        if (tableDocument != null) {
                            val table = tableDocument.select("#content #main .text table")[0]
                            val rows = table.select("tbody tr")

                            val headline = table.select("thead tr")[1].select("th").eachText()
                            val todayCasesIndex =
                                headline.indexOf("Differenz zum Vortag") + 1
                            val casesInTheLast7DaysIndex =
                                headline.indexOf("Fälle in den letzten 7 Tagen") + 1

                            var nextTicker = false
                            for (row in rows) {
                                val columns = row.select("td")
                                if (columns[0].text().toUpperCase() == location.toUpperCase()) {
                                    val todayCasesString =
                                        columns[todayCasesIndex].text().replace("+", "")
                                            .replace(".", "").trim()
                                    val todayCases =
                                        if (todayCasesString == "-")
                                            0
                                        else todayCasesString.toInt()
                                    val casesInTheLast7Days =
                                        columns[casesInTheLast7DaysIndex].text().replace(".", "")
                                            .trim().toInt()

                                    tickers.add(
                                        RKIStateFullTicker(
                                            location,
                                            lastUpdate,
                                            cases,
                                            deaths,
                                            todayCases,
                                            casesPerOneHundredThousands,
                                            casesInTheLast7Days,
                                            sevenDayIncidencePerOneHundredThousands
                                        )
                                    )
                                    nextTicker = true
                                }
                            }
                            if (nextTicker)
                                continue
                        }
                    } catch (ignore: Exception) {
                    }

                    tickers.add(
                        RKIStateTicker(
                            location,
                            lastUpdate,
                            cases,
                            deaths,
                            casesPerOneHundredThousands,
                            sevenDayIncidencePerOneHundredThousands
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    tickers.add(
                        ParseError(
                            location,
                            RKIStateTicker.DATA_SOURCE,
                            RKIStateTicker.VISIBLE_DATA_SOURCE,
                            e
                        )
                    )
                }
            } catch (ignore: Exception) {
            }
        }

        return tickers
    }

    fun parse(tableDocument: Document?, vararg documents: Document): List<LiveTicker> {
        return parseLiveTickers(tableDocument, *documents)
    }

    fun parseNoErrors(tableDocument: Document?, vararg documents: Document) =
        parse(tableDocument, *documents).filter { !it.isError() }

    fun parseNoInternalErrors(tableDocument: Document?, vararg documents: Document) =
        parse(tableDocument, *documents).filter {
            if (it.isError()) {
                !(it as ParseError).isInternalError()
            } else
                true
        }

    @Throws(IOException::class)
    fun downloadDocuments(vararg cities: String): List<Document> {
        val documents: MutableList<Document> = mutableListOf()
        for (city in cities) {
            val document = Jsoup.connect(getAPIUrl(city)).ignoreContentType(true).get()

            if (document != null)
                documents.add(document)
            else
                throw IOException()
        }
        return documents
    }

    @Throws(IOException::class)
    fun downloadTableDocument(): Document? {
        return Jsoup.connect(DOCUMENT_URL).get()
    }

    @Throws(IOException::class)
    override fun parse(vararg locations: String): List<LiveTicker> {
        return parse(downloadTableDocument(), *downloadDocuments(*locations).toTypedArray())
    }

    override fun getSuggestions(): List<String> {
        return listOf(
            "Schleswig-Holstein",
            "Hamburg",
            "Niedersachsen",
            "Bremen",
            "Nordrhein-Westfalen",
            "Hessen",
            "Rheinland-Pfalz",
            "Baden-Württemberg",
            "Bayern",
            "Saarland",
            "Berlin",
            "Brandenburg",
            "Mecklenburg-Vorpommern",
            "Sachsen",
            "Sachsen-Anhalt",
            "Thüringen"
        )
    }
}