package com.asdoi.corona

import com.asdoi.corona.lgl.LGLParser
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import com.asdoi.corona.rki.counties.RKICountiesParser
import com.asdoi.corona.rki.germany.RKIGermanyParser
import com.asdoi.corona.rki.states.RKIStatesParser
import com.asdoi.corona.worldometers.continents.WmContinentsParser
import com.asdoi.corona.worldometers.world.WmWorldParser
import java.io.IOException

object ParserDE : LiveTickerParser() {

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
                    worldTicker = WmWorldParser.parse(*locations)
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
        threadContinentsWm.start()
        threadWorldWm.start()

        try {
            threadLGL.join()
            threadDistrictsRKI.join()
            threadCountiesRKI.join()
            threadGermanyRKI.join()
            threadContinentsWm.join()
            threadWorldWm.join()
        } catch (ignore: Exception) {
        }

        val tickers: MutableList<LiveTicker> = mutableListOf()
        tickers.addAll(tickersLGL)
        tickers.addAll(tickersDistrictsRKI)
        tickers.addAll(tickersCountiesRKI)
        tickers.addAll(tickerGermanyRKI)
        tickers.addAll(tickersContinentsWm)
        tickers.addAll(worldTicker)

        if (errorLGL &&
            errorDistrictsRKI &&
            errorCountiesRKI &&
            errorGermanyRKI &&
            errorWorldTicker
        ) {
            throw IOException()
        }

        return parse(tickers)
    }


    fun parse(liveTickers: List<LiveTicker>, vararg cities: String) =
        Parser.parse(liveTickers, *cities)

    fun parseNoErrors(liveTickers: List<LiveTicker>, vararg cities: String) =
        parse(liveTickers, *cities).filter { !it.isError() }

    fun parseNoInternalErrors(liveTickers: List<LiveTicker>, vararg cities: String) =
        parse(liveTickers, *cities).filter {
            if (it.isError()) {
                !(it as ParseError).isInternalError()
            } else
                true
        }


    fun parse(liveTickers: List<LiveTicker>) = Parser.parse(liveTickers)

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
        list.addAll(WmContinentsParser.getSuggestions())
        list.addAll(WmWorldParser.getSuggestions())
        return list.distinct()
    }
}