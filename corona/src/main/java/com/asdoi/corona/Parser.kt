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
    override fun parse(vararg locations: String): List<LiveTicker> {
        if (locations.isEmpty())
            return listOf()

        var tickersLGL: List<LiveTicker> = listOf()
        var errorLGL = false
        val threadLGL = Thread {
            try {
                tickersLGL = LGLParser.parse(*locations)
            } catch (e: Exception) {
                errorLGL = true
                e.printStackTrace()
            }
        }

        var tickersDistrictsRKI: List<LiveTicker> = listOf()
        var errorDistrictsRKI = false
        val threadDistrictsRKI = Thread {
            try {
                tickersDistrictsRKI = RKICountiesParser.parse(*locations)
            } catch (e: Exception) {
                errorDistrictsRKI = true
                e.printStackTrace()
            }
        }

        var tickersCountiesRKI: List<LiveTicker> = listOf()
        var errorCountiesRKI = false
        val threadCountiesRKI = Thread {
            try {
                tickersCountiesRKI = RKIStatesParser.parse(*locations)
            } catch (e: Exception) {
                errorCountiesRKI = true
                e.printStackTrace()
            }
        }

        var tickerGermanyRKI: List<LiveTicker> = listOf()
        var errorGermanyRKI = false
        val threadGermanyRKI = Thread {
            try {
                if (locations.any { it.toUpperCase() == RKIGermanyParser.location.toUpperCase() })
                    tickerGermanyRKI = RKIGermanyParser.parse(*locations)
                else
                    errorGermanyRKI = true
            } catch (e: Exception) {
                errorGermanyRKI = true
                e.printStackTrace()
            }
        }

        var tickerJHU: List<LiveTicker> = listOf()
        var errorJHU = false
        val threadJHU = Thread {
            try {
                tickerJHU = JHUParser.parse(*locations)
            } catch (e: Exception) {
                errorJHU = true
                e.printStackTrace()
            }
        }

        var tickersStatesWm: List<LiveTicker> = listOf()
        var errorStatesWm = false
        val threadStatesWm = Thread {
            try {
                tickersStatesWm = WmStatesParser.parse(*locations)
            } catch (e: Exception) {
                errorStatesWm = true
                e.printStackTrace()
            }
        }

        var tickersCountriesWm: List<LiveTicker> = listOf()
        var errorCountriesWm = false
        val threadCountriesWm = Thread {
            try {
                tickersCountriesWm = WmCountriesParser.parse(*locations)
            } catch (e: Exception) {
                errorCountriesWm = true
                e.printStackTrace()
            }
        }

        var tickersContinentsWm: List<LiveTicker> = listOf()
        var errorContinentsWm = false
        val threadContinentsWm = Thread {
            try {
                tickersContinentsWm = WmContinentsParser.parse(*locations)
            } catch (e: Exception) {
                errorContinentsWm = true
                e.printStackTrace()
            }
        }

        var worldTicker: List<LiveTicker> = listOf()
        var errorWorldTicker = false
        val threadWorldWm = Thread {
            try {
                if (locations.any { it.toUpperCase() == WmWorldParser.location.toUpperCase() })
                    worldTicker = WmWorldParser.parse(WmWorldParser.location)
                else
                    errorWorldTicker = true
            } catch (e: Exception) {
                errorWorldTicker = true
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

        if (errorLGL &&
            errorDistrictsRKI &&
            errorCountiesRKI &&
            errorGermanyRKI &&
            errorJHU &&
            errorStatesWm &&
            errorCountriesWm &&
            errorContinentsWm &&
            errorWorldTicker
        ) {
            throw IOException()
        }

        return parse(tickers)
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

    override fun getSuggestions(): List<String> {
        val list = mutableListOf<String>()
        list.addAll(LGLParser.getSuggestions())
        list.addAll(RKICountiesParser.getSuggestions())
        list.addAll(RKIStatesParser.getSuggestions())
        list.addAll(RKIGermanyParser.getSuggestions())
        list.addAll(JHUParser.getSuggestions())
        list.addAll(WmStatesParser.getSuggestions())
        list.addAll(WmCountriesParser.getSuggestions())
        list.addAll(WmContinentsParser.getSuggestions())
        list.addAll(WmWorldParser.getSuggestions())
        return list.distinct()
    }
}