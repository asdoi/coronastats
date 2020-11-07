package com.asdoi.corona

import com.asdoi.corona.johnshopkins.JHUParser
import com.asdoi.corona.lgl.LGLParser
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import com.asdoi.corona.rki.counties.RKICountiesParser
import com.asdoi.corona.rki.germany.RKIGermanyParser
import com.asdoi.corona.rki.states.RKIStatesParser
import com.asdoi.corona.worldometers.continents.WmContinentsParser
import com.asdoi.corona.worldometers.countries.WmCountriesParser
import com.asdoi.corona.worldometers.states.WmStatesParser
import com.asdoi.corona.worldometers.world.WmWorldParser
import java.io.IOException

object Parser : LiveTickerParser() {

    @Throws(IOException::class)
    override fun parse(vararg cities: String): List<LiveTicker> {
        if (cities.isEmpty())
            return listOf()

        var tickersLGL: List<LiveTicker> = listOf()
        val threadLGL = Thread {
            try {
                tickersLGL = LGLParser.parse(*cities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var tickersDistrictsRKI: List<LiveTicker> = listOf()
        val threadDistrictsRKI = Thread {
            try {
                tickersDistrictsRKI = RKICountiesParser.parse(*cities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var tickersCountiesRKI: List<LiveTicker> = listOf()
        val threadCountiesRKI = Thread {
            try {
                tickersCountiesRKI = RKIStatesParser.parse(*cities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var tickerGermanyRKI: List<LiveTicker> = listOf()
        val threadGermanyRKI = Thread {
            try {
                tickerGermanyRKI = RKIGermanyParser.parse(*cities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var tickerJHU: List<LiveTicker> = listOf()
        val threadJHU = Thread {
            try {
                tickerJHU = JHUParser.parse(*cities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var tickersStatesWm: List<LiveTicker> = listOf()
        val threadStatesWm = Thread {
            try {
                tickersStatesWm = WmStatesParser.parse(*cities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var tickersCountriesWm: List<LiveTicker> = listOf()
        val threadCountriesWm = Thread {
            try {
                tickersCountriesWm = WmCountriesParser.parse(*cities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var tickersContinentsWm: List<LiveTicker> = listOf()
        val threadContinentsWm = Thread {
            try {
                tickersContinentsWm = WmContinentsParser.parse(*cities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var worldTicker: List<LiveTicker> = listOf()
        val threadWorldWm = Thread {
            try {
                worldTicker = WmWorldParser.parse(*cities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        threadLGL.start()
        threadDistrictsRKI.start()
        threadCountiesRKI.start()
        threadGermanyRKI.start()
        threadJHU.start()
        threadWorldWm.start()
        threadContinentsWm.start()
        threadCountriesWm.start()
        threadStatesWm.start()

        try {
            threadLGL.join()
            threadDistrictsRKI.join()
            threadCountiesRKI.join()
            threadGermanyRKI.join()
            threadJHU.join()
            threadWorldWm.join()
            threadContinentsWm.join()
            threadCountriesWm.join()
            threadStatesWm.join()
        } catch (ignore: Exception) {
        }

        try {

            val tickers: MutableList<LiveTicker> = mutableListOf()
            tickers.addAll(tickersLGL)
            tickers.addAll(tickersDistrictsRKI)
            tickers.addAll(tickersCountiesRKI)
            tickers.addAll(tickerGermanyRKI)
            tickers.addAll(tickersStatesWm)
            tickers.addAll(tickerJHU)
            tickers.addAll(tickersCountriesWm)
            tickers.addAll(tickersContinentsWm)
            tickers.addAll(worldTicker)

            if (tickers.isEmpty()) {
                throw IOException()
            }

            return parse(tickers)
        } catch (e: Exception) {
            e.printStackTrace()
            return listOf(ParseError(ParseError.INTERNAL_ERROR, ParseError.INTERNAL_ERROR, e))
        }
    }


    fun parse(liveTickers: List<LiveTicker>, vararg cities: String): List<LiveTicker> {
        val tickers: MutableList<LiveTicker> = mutableListOf()
        val errors: MutableList<LiveTicker> = mutableListOf()

        for (city in cities) {
            val cityTickers: List<LiveTicker> =
                    liveTickers.filter { it.location.toUpperCase() == city.toUpperCase() }

            var cityTicker: LiveTicker? = null
            for (ticker in cityTickers) {
                if (ticker.isError()) {
                    errors.add(ticker)
                } else if (cityTicker == null) {
                    cityTicker = ticker
                } else if (ticker.priority.isHigher(cityTicker.priority)) {
                    cityTicker = ticker
                } else if (ticker.priority.isEqual(cityTicker.priority)) {
                    if (ticker.cases > cityTicker.cases) {
                        cityTicker = ticker
                    }
                }
            }

            if (cityTicker != null)
                tickers.add(cityTicker)
        }

        //Add Errors
        tickers.addAll(errors)

        return tickers
    }

    fun parseNoErrors(liveTickers: List<LiveTicker>, vararg cities: String) =
            parse(liveTickers, *cities).filter { !it.isError() }

    fun parseNoInternalErrors(liveTickers: List<LiveTicker>, vararg cities: String) =
            parse(liveTickers, *cities).filter {
                if (it.isError()) {
                    !(it as ParseError).isInternalError()
                } else
                    true
            }


    fun parse(liveTickers: List<LiveTicker>): List<LiveTicker> {
        val tickers: MutableList<LiveTicker> = mutableListOf()
        val errors: MutableList<LiveTicker> = mutableListOf()

        for (ticker in liveTickers) {
            var nextTicker = false
            for (alreadyTicker in tickers) {
                if (alreadyTicker.location.toUpperCase() == ticker.location.toUpperCase())
                    nextTicker = true
            }
            if (nextTicker)
                continue

            val cityTickers: List<LiveTicker> =
                    liveTickers.filter { it.location.toUpperCase() == ticker.location.toUpperCase() }

            var cityTicker: LiveTicker? = null
            for (otherTickers in cityTickers) {
                if (otherTickers.isError()) {
                    errors.add(otherTickers)
                } else if (cityTicker == null) {
                    cityTicker = otherTickers
                } else if (otherTickers.priority.isHigher(cityTicker.priority)) {
                    cityTicker = otherTickers
                } else if (otherTickers.priority.isEqual(cityTicker.priority)) {
                    if (otherTickers.cases > cityTicker.cases) {
                        cityTicker = otherTickers
                    }
                }
            }

            if (cityTicker != null)
                tickers.add(cityTicker)
        }

        //Add Errors
        tickers.addAll(errors)

        return tickers
    }

    fun parseNoErrors(liveTickers: List<LiveTicker>) =
            parse(liveTickers).filter { !it.isError() }

    fun parseNoInternalErrors(liveTickers: List<LiveTicker>) =
            parse(liveTickers).filter {
                if (it.isError()) {
                    !(it as ParseError).isInternalError()
                } else
                    true
            }
}