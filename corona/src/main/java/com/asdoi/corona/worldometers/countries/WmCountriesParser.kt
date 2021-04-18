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

    @Throws(IOException::class)
    override fun parse(vararg locations: String): List<LiveTicker> {
        return parse(downloadDocuments(*locations))
    }

    override fun getSuggestions(): List<String> {
        return listOf(
            "Afghanistan",
            "Albania",
            "Algeria",
            "Andorra",
            "Angola",
            "Anguilla",
            "Antigua and Barbuda",
            "Argentina",
            "Armenia",
            "Aruba",
            "Australia",
            "Austria",
            "Azerbaijan",
            "Bahamas",
            "Bahrain",
            "Bangladesh",
            "Barbados",
            "Belarus",
            "Belgium",
            "Belize",
            "Benin",
            "Bermuda",
            "Bhutan",
            "Bolivia",
            "Bosnia",
            "Botswana",
            "Brazil",
            "British Virgin Islands",
            "Brunei",
            "Bulgaria",
            "Burkina Faso",
            "Burundi",
            "Cabo Verde",
            "Cambodia",
            "Cameroon",
            "Canada",
            "Caribbean Netherlands",
            "Cayman Islands",
            "Central African Republic",
            "Chad",
            "Channel Islands",
            "Chile",
            "China",
            "Colombia",
            "Comoros",
            "Congo",
            "Costa Rica",
            "Croatia",
            "Cuba",
            "Curaçao",
            "Cyprus",
            "Czechia",
            "Côte d'Ivoire",
            "DRC",
            "Denmark",
            "Diamond Princess",
            "Djibouti",
            "Dominica",
            "Dominican Republic",
            "Ecuador",
            "Egypt",
            "El Salvador",
            "Equatorial Guinea",
            "Eritrea",
            "Estonia",
            "Ethiopia",
            "Falkland Islands (Malvinas)",
            "Faroe Islands",
            "Fiji",
            "Finland",
            "France",
            "French Guiana",
            "French Polynesia",
            "Gabon",
            "Gambia",
            "Georgia",
            "Germany",
            "Ghana",
            "Gibraltar",
            "Greece",
            "Greenland",
            "Grenada",
            "Guadeloupe",
            "Guatemala",
            "Guinea",
            "Guinea-Bissau",
            "Guyana",
            "Haiti",
            "Holy See (Vatican City State)",
            "Honduras",
            "Hong Kong",
            "Hungary",
            "Iceland",
            "India",
            "Indonesia",
            "Iran",
            "Iraq",
            "Ireland",
            "Isle of Man",
            "Israel",
            "Italy",
            "Jamaica",
            "Japan",
            "Jordan",
            "Kazakhstan",
            "Kenya",
            "Kuwait",
            "Kyrgyzstan",
            "Lao People's Democratic Republic",
            "Latvia",
            "Lebanon",
            "Lesotho",
            "Liberia",
            "Libyan Arab Jamahiriya",
            "Liechtenstein",
            "Lithuania",
            "Luxembourg",
            "MS Zaandam",
            "Macao",
            "Macedonia",
            "Madagascar",
            "Malawi",
            "Malaysia",
            "Maldives",
            "Mali",
            "Malta",
            "Marshall Islands",
            "Martinique",
            "Mauritania",
            "Mauritius",
            "Mayotte",
            "Mexico",
            "Micronesia",
            "Moldova",
            "Monaco",
            "Mongolia",
            "Montenegro",
            "Montserrat",
            "Morocco",
            "Mozambique",
            "Myanmar",
            "Namibia",
            "Nepal",
            "Netherlands",
            "New Caledonia",
            "New Zealand",
            "Nicaragua",
            "Niger",
            "Nigeria",
            "Norway",
            "Oman",
            "Pakistan",
            "Palestine",
            "Panama",
            "Papua New Guinea",
            "Paraguay",
            "Peru",
            "Philippines",
            "Poland",
            "Portugal",
            "Qatar",
            "Romania",
            "Russia",
            "Rwanda",
            "Réunion",
            "S. Korea",
            "Saint Kitts and Nevis",
            "Saint Lucia",
            "Saint Martin",
            "Saint Pierre Miquelon",
            "Saint Vincent and the Grenadines",
            "Samoa",
            "San Marino",
            "Sao Tome and Principe",
            "Saudi Arabia",
            "Senegal",
            "Serbia",
            "Seychelles",
            "Sierra Leone",
            "Singapore",
            "Sint Maarten",
            "Slovakia",
            "Slovenia",
            "Solomon Islands",
            "Somalia",
            "South Africa",
            "South Sudan",
            "Spain",
            "Sri Lanka",
            "St. Barth",
            "Sudan",
            "Suriname",
            "Swaziland",
            "Sweden",
            "Switzerland",
            "Syrian Arab Republic",
            "Taiwan",
            "Tajikistan",
            "Tanzania",
            "Thailand",
            "Timor-Leste",
            "Togo",
            "Trinidad and Tobago",
            "Tunisia",
            "Turkey",
            "Turks and Caicos Islands",
            "UAE",
            "UK",
            "USA",
            "Uganda",
            "Ukraine",
            "Uruguay",
            "Uzbekistan",
            "Vanuatu",
            "Venezuela",
            "Vietnam",
            "Wallis and Futuna",
            "Western Sahara",
            "Yemen",
            "Zambia",
            "Zimbabwe"
        )
    }
}