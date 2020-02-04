package model

import json.JsonDataWriter
import model.track.Track
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import util.RandomUtil
import java.time.LocalTime

internal class StationTest{

    @Test
    fun generateStations() {
        var stations = mutableListOf<Station>()
        var names = arrayOf("A", "B", "C", "D", "E", "F")
        names.forEachIndexed { index, s ->
            var amount = RandomUtil.seed.nextInt(4, 9)
            stations.add(Station(s, generateTracks(amount), index.toString()))
        }
        JsonDataWriter.writeStations(stations)
    }

    @Test
    fun testTime() {
        val baseTime = LocalTime.of(0, 0 )
        val timeDifference = 15
        for (i in 0 .. 20){
            println(baseTime.plusMinutes((i*timeDifference).toLong()))
        }
    }



    fun generateTracks(amount: Int):List<Track>{
        var tracks = mutableListOf<Track>()
        for (i in 1 .. amount){
            var track = Track(5, null, i)
            if (!tracks.isEmpty() && i%2 == 0){
                track.pair = i-1
                tracks.last().pair = i
            }
            tracks.add(track)
        }
        return tracks
    }

}