package graph

import com.beust.klaxon.Klaxon
import json.LocalTimeConverter
import org.junit.jupiter.api.Test
import java.time.LocalTime
import kotlin.test.assertEquals

class LocalTimeConverterTest {

    @Test
    fun test(){
        val localTime = LocalTime.of(15, 0)
        val klaxon = Klaxon().converter(LocalTimeConverter())
        val toJsonString = klaxon.toJsonString(localTime)
        println(toJsonString)
        val parse = klaxon.parse<LocalTime>(toJsonString)
        println(parse)
        assertEquals(localTime, parse)

    }
}