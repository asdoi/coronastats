package com.asdoi.corona.rki.germany

import com.asdoi.corona.ParseError
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


object RKIGermanyParser : LiveTickerParser() {
    private const val DOCUMENT_URL =
        "https://www.rki.de/DE/Content/InfAZ/N/Neuartiges_Coronavirus/Fallzahlen.html"

    const val location = "Deutschland"

    private fun parseLiveTickers(document: Document): List<LiveTicker> {
        val tickers: MutableList<LiveTicker> = mutableListOf()
        try {
            val lastUpdate: Calendar =
                try {
                    var dateText =
                        document.select("#content #main .text p")[0].text()
                    dateText = dateText.removePrefix("Stand: ")
                    dateText =
                        dateText.substring(0, dateText.indexOf("Uhr")).trim().replace(" ", "")

                    val dateFormat = SimpleDateFormat("dd.MM.yyy,HH:mm", Locale.GERMANY)

                    val calendar = Calendar.getInstance()
                    calendar.time = dateFormat.parse(dateText)!!
                    calendar
                } catch (e: Exception) {
                    e.printStackTrace()
                    Calendar.getInstance()
                }

            val table = document.select("#content #main .text table")[0]
            val rows = table.select("tbody tr")

            val headline = table.select("thead tr")[1].select("th").eachText()
            val casesIndex = headline.indexOf("Anzahl") + 1
            val todayCasesIndex =
                headline.indexOf("Differenz zum Vortag") + 1
            val casesInTheLast7DaysIndex =
                headline.indexOf("Fälle in den letzten 7 Tagen") + 1
            val sevenDayIncidenceHundredThousandIndex = headline.indexOf("7-Tage- Inzidenz") + 1
            val deathsIndex = headline.indexOf("Todesfälle") + 1

            for (row in rows) {
                val columns = row.select("td").eachText()
                if (columns[0].toUpperCase() == "Gesamt".toUpperCase()) {
                    val cases = columns[casesIndex].replace(".", "").toInt()

                    val todayCasesString =
                        columns[todayCasesIndex].replace("+", "")
                            .replace(".", "").trim()
                    val todayCases =
                        if (todayCasesString == "-")
                            0
                        else todayCasesString.toInt()

                    val casesInTheLast7Days =
                        columns[casesInTheLast7DaysIndex].replace(".", "")
                            .trim().toInt()

                    val sevenDayIncidencePerOneHundredThousands =
                        columns[sevenDayIncidenceHundredThousandIndex].replace(",", ".")
                            .toDouble()

                    val deaths = columns[deathsIndex].replace(".", "").toInt()

                    tickers.add(
                        RKIGermanyTicker(
                            location,
                            lastUpdate,
                            cases,
                            deaths,
                            todayCases,
                            casesInTheLast7Days,
                            sevenDayIncidencePerOneHundredThousands
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tickers.add(
                ParseError(
                    location,
                    RKIGermanyTicker.DATA_SOURCE,
                    RKIGermanyTicker.VISIBLE_DATA_SOURCE,
                    e
                )
            )
        }

        return tickers
    }

    fun parse(tableDocument: Document): List<LiveTicker> {
        return parseLiveTickers(tableDocument)
    }

    fun parseNoErrors(tableDocument: Document) =
        parse(tableDocument).filter { !it.isError() }

    fun parseNoInternalErrors(tableDocument: Document) =
        parse(tableDocument).filter {
            if (it.isError()) {
                !(it as ParseError).isInternalError()
            } else
                true
        }

    @Throws(IOException::class)
    fun downloadDocument(): Document {
        return Jsoup.connect(DOCUMENT_URL).get()
    }

    @Throws(IOException::class)
    override fun parse(vararg locations: String): List<LiveTicker> {
        return if (locations.any { it.toUpperCase() == location.toUpperCase() })
            parse(downloadDocument())
        else
            listOf()
    }
}