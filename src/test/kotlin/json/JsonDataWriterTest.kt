package json

import model.Station
import model.Train
import model.timetable.StationStop
import model.timetable.TimeTable
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalTime

internal class JsonDataWriterTest {

    @Test
    fun writeTrains() {
        val trains = mutableListOf<Train>()
        val train = Train(emptyList(), "0" , "Train")
        trains.add(train)
        JsonDataWriter.writeTrains(trains)
    }

    @Test
    fun writeStations() {
        val stations = mutableListOf<Station>()
        stations.add(Station("ABC", emptyList(), "0"))
        JsonDataWriter.writeStations(stations)
    }

    @Test
    fun writeTimeTable(){
        val timetables = mutableListOf<TimeTable>()
        val train = Train(emptyList(), "0" , "Train")
        val departures = mutableListOf<LocalTime>()
        departures.add(LocalTime.now())
        val stops = mutableListOf<StationStop>()
        val timeTable = TimeTable(train, departures, stops)
        timetables.add(timeTable)
        JsonDataWriter.writeTimeTables(timetables)
    }
}