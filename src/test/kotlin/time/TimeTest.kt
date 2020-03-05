package time

import json.JsonDataWriter
import model.track.Track
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import util.RandomUtil
import java.time.LocalTime

internal class TimeTest{

    @Test
    fun testTime() {
        val baseTime = LocalTime.of(0, 0 )
        val timeDifference = 15
        for (i in 0 .. 20){
            println(baseTime.plusMinutes((i*timeDifference).toLong()))
        }
    }



}