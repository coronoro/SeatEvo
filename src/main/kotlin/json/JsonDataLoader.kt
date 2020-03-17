package json

import model.Identifiable
import model.Station
import model.Train
import model.timetable.StationStop
import model.timetable.TimeTable
import util.JsonUtil
import java.io.File

object JsonDataLoader {

    private val timeTableLocation = "/timetable/timetable.json"
    private val stationsLocation = "/stations/stations.json"
    private val trainsLocation = "/trains/trains.json"

    private val timeTableSnapLocation = "/timetable/timetable-SNAP.json"
    private val stationsSnapLocation = "/stations/stations-SNAP.json"
    private val trainsSnapLocation = "/trains/trains-SNAP.json"

    inline fun <reified T> loadJsonList(location: String, clazz: Class<T>): List<T> {

        var result = emptyList<T>()
        val resource = JsonDataLoader::class.java.getResource(location)
        println("loading data from file: " + resource)
        if (resource != null) {
            val file = File(resource.toURI())
            val parse = JsonUtil.klaxon.parseArray<T>(file)
            if (parse != null) {
                result = parse
            }
        }
        return result
    }

    fun loadTrains(snap: Boolean = false): List<Train> {
        val trainFile = if (snap) trainsSnapLocation else trainsLocation
        val trains = loadJsonList(trainFile, Train::class.java)
        return trains
    }

    fun loadStations(snap: Boolean = false): List<Station> {
        val stationFile = if (snap) stationsSnapLocation else stationsLocation
        val stations = loadJsonList(stationFile, Station::class.java)
        return stations
    }

    fun loadTimeTables(snap: Boolean = false): List<TimeTable> {
        val timetableFile = if (snap) timeTableSnapLocation else timeTableLocation

        var result = mutableListOf<TimeTable>()
        val stations = loadStations()
        val trains = loadTrains()

        val jsonTimetables = loadJsonList(timetableFile, JsonTimeTable::class.java)
        jsonTimetables.forEach { json ->
            val train = findIdentifier(trains, json.train)
            var stops = mutableListOf<StationStop>()
            json.stops.forEach { jsonStop ->
                val station = findIdentifier(stations, jsonStop.station)
                val track = station.tracks[jsonStop.track.number]
                val stop = StationStop(station, jsonStop.arrival, jsonStop.departure, track, jsonStop.offset, jsonStop.direction )
                stops.add(stop)
            }

            result.add(TimeTable(train, json.departures, stops))
        }

        return result

    }

    fun <T : Identifiable> findIdentifier(list: List<T>, id: String): T {
        val filter = list.filter { item -> item.id == id }
        if (filter.size == 1) {
            return filter[0]
        } else
            throw Exception("used not defined Identifier: " + id)
    }


}