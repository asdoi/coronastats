package com.asdoi.corona.model

import com.asdoi.corona.ParseError
import java.io.IOException

abstract class LiveTickerParser {
    @Throws(IOException::class)
    abstract fun parse(vararg locations: String): List<LiveTicker>

    @Throws(IOException::class)
    fun parseNoErrors(vararg locations: String): List<LiveTicker> {
        return parse(*locations).filter { !it.isError() }
    }

    @Throws(IOException::class)
    fun parseNoInternalErrors(vararg locations: String): List<LiveTicker> {
        return parse(*locations).filter {
            if (it.isError()) {
                !(it as ParseError).isInternalError()
            } else
                true
        }
    }

    open fun getSuggestions(): List<String> {
        return listOf()
    }
}