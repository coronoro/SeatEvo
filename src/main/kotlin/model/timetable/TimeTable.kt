package model.timetable

import model.Station
import model.track.Track
import model.Train
import java.time.LocalTime

data class TimeTable(var train: Train, val departures: List<LocalTime>, val stops: List<StationStop>) {

    private val stationTrailMap = HashMap<String, StationStop>()

    init {
        stops.forEach { stop ->
            stationTrailMap.put(stop.station.id, stop)
        }
    }

    fun getStationStop(station: Station): StationStop? {
        return stationTrailMap.get(station.id)
    }


}