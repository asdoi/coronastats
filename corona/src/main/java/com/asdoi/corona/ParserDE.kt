package com.asdoi.corona

import com.asdoi.corona.lgl.LGLParser
import com.asdoi.corona.model.LiveTicker
import com.asdoi.corona.model.LiveTickerParser
import com.asdoi.corona.rki.counties.RKICountiesParser
import com.asdoi.corona.rki.germany.RKIGermanyParser
import com.asdoi.corona.rki.states.RKIStatesParser
import com.asdoi.corona.worldometers.world.WmWorldParser
import java.io.IOException

object ParserDE : LiveTickerParser() {

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

        var worldTicker: List<LiveTicker> = listOf()
        val threadWorldWm = Thread {
            try {
                if (cities.any { it.toUpperCase() == WmWorldParser.location.toUpperCase() })
                    worldTicker = WmWorldParser.parse(*cities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        threadLGL.start()
        threadDistrictsRKI.start()
        threadCountiesRKI.start()
        threadGermanyRKI.start()
        threadWorldWm.start()

        try {
            threadLGL.join()
            threadDistrictsRKI.join()
            threadCountiesRKI.join()
            threadGermanyRKI.join()
            threadWorldWm.join()
        } catch (ignore: Exception) {
        }

        try {

            val tickers: MutableList<LiveTicker> = mutableListOf()
            tickers.addAll(tickersLGL)
            tickers.addAll(tickersDistrictsRKI)
            tickers.addAll(tickersCountiesRKI)
            tickers.addAll(tickerGermanyRKI)
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
}