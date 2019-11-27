package model.timetable

import model.Station
import model.Track
import model.Train
import java.time.LocalTime

data class TimeTable(var train: Train, val departures: List<LocalTime>, val stops: List<StationStop>) {

    private val stationTrailMap = HashMap<String, Track>()

    init {
        stops.forEach { stop ->
            stationTrailMap.put(stop.station.id, stop.track)
        }
    }

    fun getTrackForStation(station: Station): Track? {
        return stationTrailMap.get(station.id)
    }


}