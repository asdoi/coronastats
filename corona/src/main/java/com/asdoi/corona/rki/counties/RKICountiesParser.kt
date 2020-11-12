package com.asdoi.corona.rki.counties

import com.asdoi.corona.ParseError
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


object RKICountiesParser : LiveTickerParser() {
    private fun getAPIUrl(city: String): String {
        return "https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/RKI_Landkreisdaten/FeatureServer/0/query?where=GEN%20%3D%20%27$city%27&outFields=GEN,BEZ,death_rate,cases,deaths,cases_per_100k,cases_per_population,BL,county,last_update,cases7_per_100k,recovered,cases7_bl_per_100k&outSR=4326&f=json"
    }

    private fun parseLiveTickers(vararg documents: Document): List<LiveTicker> {
        val tickers: MutableList<LiveTicker> = mutableListOf()

        for (document in documents) {
            try {
                val json = JSONObject(document.text())
                val featuresArray = json.getJSONArray("features").getJSONObject(0)
                val attributes = featuresArray.getJSONObject("attributes")

                val location = attributes.getString("GEN")
                try {
                    val cases = attributes.getInt("cases")
                    val deaths = attributes.getInt("deaths")
                    val casesPerOneHundredThousands = attributes.getDouble("cases_per_100k")
                    val sevenDayIncidencePerOneHundredThousands =
                            attributes.getDouble("cases7_per_100k")
                    val deathRate = attributes.getDouble("death_rate")
                    val infectionsPerPopulation = attributes.getDouble("cases_per_population")

                    val lastUpdateString = attributes.getString("last_update").replace("Uhr", "")
                    val lastUpdate: Calendar =
                            try {
                                val dateFormat = SimpleDateFormat("dd.MM.yyy, mm:HH", Locale.GERMANY)

                                val calendar = Calendar.getInstance()
                                calendar.time = dateFormat.parse(lastUpdateString)!!
                                calendar
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Calendar.getInstance()
                            }

                    tickers.add(
                            RKICountyTicker(
                                    location,
                                    lastUpdate,
                                    cases,
                                    deaths,
                                    casesPerOneHundredThousands,
                                    sevenDayIncidencePerOneHundredThousands,
                                    deathRate,
                                    infectionsPerPopulation
                            )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    tickers.add(
                            ParseError(
                                    location,
                                    RKICountyTicker.DATA_SOURCE,
                                    RKICountyTicker.VISIBLE_DATA_SOURCE,
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

    fun parseNoErrors(vararg documents: Document) = parse(*documents).filter { !it.isError() }

    fun parseNoInternalErrors(vararg documents: Document) = parse(*documents).filter {
        if (it.isError()) {
            !(it as ParseError).isInternalError()
        } else
            true
    }

    @Throws(IOException::class)
    fun downloadDocuments(vararg cities: String): List<Document> {
        val documents: MutableList<Document> = mutableListOf()
        for (city in cities) {
            val document = Jsoup.connect(getAPIUrl(city)).get()

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
}