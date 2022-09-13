package com.asdoi.corona.lgl

import com.asdoi.corona.ParseError
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object LGLParser : LiveTickerParser() {
    private const val DOCUMENT_URL =
        "https://www.lgl.bayern.de/gesundheit/infektionsschutz/infektionskrankheiten_a_z/coronavirus/karte_coronavirus/"
    private const val PUBLICATION_VAR = "var publikationsDatum = "

    private fun parseLiveTickers(doc: Document, vararg cities: String): List<LiveTicker> {
        val citiesList: MutableList<String> = mutableListOf()
        for (city in cities) {
            citiesList.add(city.uppercase())
        }

        val tickers: MutableList<LiveTicker> = mutableListOf()

        try {
            val date: Calendar =
                try {
                    var dateText =
                        doc.select("#content #content_1c ").toString().split("<script>")[1]
                    val index =
                        dateText.indexOf(PUBLICATION_VAR) + PUBLICATION_VAR.length + 1
                    dateText = dateText.substring(index, index + 10)

                    val dateFormat = SimpleDateFormat("dd.MM.yyy", Locale.GERMANY)

                    val calendar = Calendar.getInstance()
                    calendar.time = dateFormat.parse(dateText)!!
                    //The hour is always 8 o'clock (hardcoded)
                    calendar.set(Calendar.HOUR_OF_DAY, 8)
                    calendar
                } catch (e: Exception) {
                    e.printStackTrace()
                    Calendar.getInstance()
                }

            val table = doc.select("table#tableLandkreise")
            val rows = table.select("tbody tr")

            val headline = table.select("th").eachText()
            val cityIndex: Int = headline.indexOf("Landkreis (LK)/Stadt")
            val infectionsIndex: Int = headline.indexOf("Anzahl der Fälle")
            val infectionsYesterdayTodayIndex: Int = headline.indexOf("Fälle Änderung zum Vortag")
            val infectionsPerOneHundredThousandsIndex: Int =
                headline.indexOf("Fallzahl pro 100.000 Einwohner")
            val infectionsInTheLastSevenDaysIndex: Int =
                headline.indexOf("Fälle der letzten 7 Tage")
            val sevenDayIncidencePerOneHundredThousandsIndex: Int =
                headline.indexOf("7-Tage-Inzidenz pro 100.000 Einwohner")
            val deathsIndex: Int = headline.indexOf("Anzahl der Todesfälle")
            val deathsYesterdayTodayIndex: Int = headline.indexOf("Todesfälle Änderung zum Vortag")

            for (line in 1 until rows.size - 1) {
                val row = rows[line].select("td").eachText()
                if (citiesList.contains(row[cityIndex].uppercase())
                    || citiesList.contains(row[cityIndex].uppercase().removeSuffix(" (STADT)"))
                    || citiesList.contains(row[cityIndex].uppercase().removeSuffix(" (LK)"))
                ) {
                    val location =
                        if (citiesList.contains(row[cityIndex].uppercase().removeSuffix(" (STADT)"))
                        ) {
                            row[cityIndex].removeSuffix(" (Stadt)")
                        } else if (citiesList.contains(
                                row[cityIndex].uppercase().removeSuffix(" (LK)")
                            )
                        ) {
                            row[cityIndex].removeSuffix(" (LK)")
                        } else {
                            row[cityIndex]
                        }

                    citiesList.remove(location.uppercase())

                    try {
                        val infections = row[infectionsIndex].replace(".", "").toInt()
                        val infectionsYesterdayTodayString = row[infectionsYesterdayTodayIndex]
                            .replace("(", "").replace(")", "").replace("+", "").replace(" ", "")
                            .replace(".", "").trim()
                        val infectionsYesterdayToday =
                            if (infectionsYesterdayTodayString == "-")
                                0
                            else infectionsYesterdayTodayString.toInt()
                        val infectionsPerOneHundredThousands =
                            row[infectionsPerOneHundredThousandsIndex].replace(".", "")
                                .replace(",", ".").toDouble()
                        val infectionsInTheLastSevenDays =
                            row[infectionsInTheLastSevenDaysIndex].replace(".", "").toInt()
                        val sevenDayIncidencePerOneHundredThousands =
                            row[sevenDayIncidencePerOneHundredThousandsIndex].replace(".", "")
                                .replace(",", ".")
                                .toDouble()
                        val deaths = row[deathsIndex].replace(".", "").toInt()
                        val deathsYesterdayTodayString = row[deathsYesterdayTodayIndex]
                            .replace("(", "").replace(")", "").replace("+", "").replace(" ", "")
                            .replace(".", "").trim()
                        val deathsYesterdayToday =
                            if (deathsYesterdayTodayString == "-")
                                0
                            else deathsYesterdayTodayString.toInt()

                        tickers.add(
                            LGLTicker(
                                location,
                                date,
                                infections,
                                infectionsYesterdayToday,
                                infectionsPerOneHundredThousands,
                                infectionsInTheLastSevenDays,
                                sevenDayIncidencePerOneHundredThousands,
                                deaths,
                                deathsYesterdayToday
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        tickers.add(
                            ParseError(
                                row[cityIndex],
                                LGLTicker.DATA_SOURCE,
                                LGLTicker.VISIBLE_DATA_SOURCE,
                                e
                            )
                        )
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            tickers.add(
                ParseError(LGLTicker.DATA_SOURCE, LGLTicker.VISIBLE_DATA_SOURCE, e)
            )
        }

        return tickers
    }


    fun parse(document: Document, vararg cities: String): List<LiveTicker> {
        return parseLiveTickers(document, *cities)
    }

    fun parseNoErrors(document: Document, vararg cities: String) =
        parse(document, *cities).filter { !it.isError() }

    fun parseNoInternalErrors(document: Document, vararg cities: String) =
        parse(document, *cities).filter {
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
        return parse(downloadDocument(), *locations)
    }

    override fun getSuggestions(): List<String> {
        return listOf(
            "Altötting",
            "Amberg",
            "Amberg-Sulzbach",
            "Ansbach",
            "Aschaffenburg",
            "Augsburg",
            "Bad Kissingen",
            "Bad Tölz",
            "Bamberg",
            "Bayreuth",
            "Berchtesgadener Land",
            "Cham",
            "Coburg",
            "Dachau",
            "Deggendorf",
            "Dillingen a.d. Donau",
            "Dingolfing-Landau",
            "Donau-Ries",
            "Ebersberg",
            "Eichstätt",
            "Erding",
            "Erlangen",
            "Erlangen-Höchstadt",
            "Forchheim",
            "Freising",
            "Freyung-Grafenau",
            "Fürstenfeldbruck",
            "Fürth",
            "Garmisch-Partenkirchen",
            "Günzburg",
            "Haßberge",
            "Hof",
            "Ingolstadt",
            "Kaufbeuren",
            "Kelheim",
            "Kempten",
            "Kitzingen",
            "Kronach",
            "Kulmbach",
            "Landsberg am Lech",
            "Landshut",
            "Lichtenfels",
            "Lindau (Bodensee)",
            "Main-Spessart",
            "Memmingen",
            "Miesbach",
            "Miltenberg",
            "Mühldorf a.Inn",
            "München",
            "Neu-Ulm",
            "Neuburg-Schrobenhausen",
            "Neumarkt i.d.Opf.",
            "Neustadt a.d. Aisch-Bad Windsheim",
            "Neustadt a.d. Waldnaab",
            "Nürnberg",
            "Nürnberger Land",
            "Oberallgäu",
            "Ostallgäu",
            "Passau",
            "Pfaffenhofen a.d.Ilm",
            "Regen",
            "Regensburg",
            "Rhön-Grabfeld",
            "Rosenheim",
            "Roth",
            "Rottal-Inn",
            "Schwabach",
            "Schwandorf",
            "Schweinfurt",
            "Starnberg",
            "Straubing",
            "Straubing-Bogen",
            "Tirschenreuth",
            "Traunstein",
            "Unterallgäu",
            "Weiden",
            "Weilheim-Schongau",
            "Weißenburg-Gunzenhausen",
            "Wunsiedel i.Fichtelgebirge",
            "Würzburg"
        )
    }
}