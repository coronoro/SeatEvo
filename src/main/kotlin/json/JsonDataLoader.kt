package json

import model.Identifiable
import model.Station
import model.Train
import model.timetable.StationStop
import model.timetable.TimeTable
import util.JsonUtil
import java.io.File

class JsonDataLoader {

    companion object {

        private val trainRouteLocation = "/routes/trainRoutes.json"
        private val timeTableLocation = "/timetable/timetable.json"

        private val stationsLocation = "/stations/stations.json"
        private val trainsLocation = "/trains/trains.json"

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

        fun loadTimeTables(): List<TimeTable> {
            var result = mutableListOf<TimeTable>()

            val stations = loadJsonList(stationsLocation, Station::class.java)
            val trains = loadJsonList(trainsLocation, Train::class.java)

            val jsonTimetables = loadJsonList(timeTableLocation, JsonTimeTable::class.java)
            jsonTimetables.forEach { json ->
                val train = findIdentifier(trains, json.train)
                var stops = mutableListOf<StationStop>()
                json.stops.forEach { jsonStop ->
                    val station = findIdentifier(stations, jsonStop.station)
                    val stop = StationStop(station, jsonStop.arrival, jsonStop.departure, jsonStop.track)
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
}