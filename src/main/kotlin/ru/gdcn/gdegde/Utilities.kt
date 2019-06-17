package ru.gdcn.gdegde

object Utilities {

    private const val HEX_SOURCE = "0123456789ABDCEF"

    fun generateRandomHex(): String = generateSequence { HEX_SOURCE.random() }.take(6).toString()

}