package com.asdoi.corona.model

import com.asdoi.corona.ParseError
import java.io.IOException

abstract class LiveTickerParser {
    @Throws(IOException::class)
    abstract fun parse(vararg cities: String): List<LiveTicker>

    @Throws(IOException::class)
    fun parseNoErrors(vararg cities: String): List<LiveTicker> {
        return parse(*cities).filter { !it.isError() }
    }

    @Throws(IOException::class)
    fun parseNoInternalErrors(vararg cities: String): List<LiveTicker> {
        return parse(*cities).filter {
            if (it.isError()) {
                !(it as ParseError).isInternalError()
            } else
                true
        }
    }
}