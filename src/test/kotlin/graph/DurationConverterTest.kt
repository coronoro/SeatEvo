package graph

import com.beust.klaxon.Klaxon
import json.DurationConverter
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.assertEquals

class DurationConverterTest {

    @Test
    fun test(){
        val duration = Duration.ofMinutes(15)
        val klaxon = Klaxon().converter(DurationConverter())
        val toJsonString = klaxon.toJsonString(duration)
        println(toJsonString)
        val parse = klaxon.parse<Duration>(toJsonString)
        println(parse)
        assertEquals(duration, parse)

    }

}