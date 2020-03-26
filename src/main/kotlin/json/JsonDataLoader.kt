package json

import model.Identifiable
import model.Station
import model.Train
import model.timetable.StationStop
import model.timetable.TimeTable
import util.JsonUtil
import util.RandomUtil
import java.io.File
import javax.sound.midi.Track

object JsonDataLoader {

    private enum class DataType(var location:String, var filename: String ){
        Timetable("/timetable/","timetable"),
        Station("/stations/","stations"),
        Train("/trains/","trains")
    }

    private val fileFormat = ".json"
    private val snapInfix = "-SNAP"

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

    private fun getFileName(type: DataType, prefix: String, infix:String): String {
        var path = type.location + prefix + type.filename + infix + fileFormat
        return path
    }

    fun loadTrains(snap: Boolean = false, prefix: String = ""): List<Train> {
        val infix = if (snap) snapInfix else ""
        val trainFile = getFileName(DataType.Train, prefix, infix)
        val trains = loadJsonList(trainFile, Train::class.java)
        return trains
    }

    fun loadStations(snap: Boolean = false, prefix: String = ""): List<Station> {
        val infix = if (snap) snapInfix else ""
        val stationFile = getFileName(DataType.Station, prefix, infix)
        val stations = loadJsonList(stationFile, Station::class.java)
        return stations
    }

    fun loadTimeTables(snap: Boolean = false, prefix: String = ""): List<TimeTable> {
        val infix = if (snap) snapInfix else ""
        val timetableFile = getFileName(DataType.Timetable, prefix, infix)

        var result = mutableListOf<TimeTable>()
        val stations = loadStations(snap, prefix)
        val trains = loadTrains(snap, prefix)

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

    fun repairTracks(snap: Boolean = false, prefix: String = ""): List<JsonTimeTable> {
        val infix = if (snap) snapInfix else ""
        val timetableFile = getFileName(DataType.Timetable, prefix, infix)

        //var result = mutableListOf<JsonTimeTable>()
        val stations = loadStations(snap, prefix)
        val trains = loadTrains(snap, prefix)

        var trackCount = 0
        var stationCount = 0

        val jsonTimetables = loadJsonList(timetableFile, JsonTimeTable::class.java)
        jsonTimetables.forEach { json ->
            val train = findIdentifier(trains, json.train)
            var stops = mutableListOf<StationStop>()
            json.stops.forEach { jsonStop ->
                val station = findIdentifier(stations, jsonStop.station)
                val track : Track
                try {
                    val track = station.tracks[jsonStop.track.number]
                }catch (e: Exception){
                    trackCount++
                    try{
                        val nextInt = RandomUtil.seed.nextInt(0, station.tracks.size)
                        jsonStop.track.number = station.tracks[nextInt].id
                    }catch(ex: Exception) {
                        stationCount++
                        println(station.name + " - id: " + station.id)
                        println(station.tracks)
                    }
                }
            }
            //result.add(json)
        }
        println("trackCount:" + trackCount)
        println("stationCount:" + stationCount)
        return jsonTimetables

    }

    fun <T : Identifiable> findIdentifier(list: List<T>, id: String): T {
        val filter = list.filter { item -> item.id == id }
        if (filter.size == 1) {
            return filter[0]
        } else
            throw Exception("used not defined Identifier: " + id)
    }


}