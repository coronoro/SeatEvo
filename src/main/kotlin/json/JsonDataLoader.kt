package json


import model.Identifiable
import model.Station
import model.Train
import model.timetable.StationStop
import model.timetable.TimeTable
import model.track.Track
import util.JsonUtil
import util.RandomUtil
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Type


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

    fun loadJsonTimeTable(snap: Boolean = false, prefix: String = ""): List<JsonTimeTable> {
        val infix = if (snap) snapInfix else ""
        val timetableFile = getFileName(DataType.Timetable, prefix, infix)
        val jsonTimetables = loadJsonList(timetableFile, JsonTimeTable::class.java)
        return jsonTimetables
    }

    fun loadTimeTables(snap: Boolean = false, prefix: String = ""): List<TimeTable> {
        val infix = if (snap) snapInfix else ""
        val timetableFile = getFileName(DataType.Timetable, prefix, infix)

        var result = mutableListOf<TimeTable>()
        val stations = loadStations(snap, prefix)
        val trains = loadTrains(snap, prefix)

        val jsonTimetables = loadJsonList(timetableFile, JsonTimeTable::class.java)
        println("timetables:" + jsonTimetables.size)
        var  current = 0;
        jsonTimetables.forEach { json ->
            println("#:" + current)
            val train = findIdentifier(trains, json.train)
            var stops = mutableListOf<StationStop>()
            json.stops.forEach { jsonStop ->

                var station:Station
                try {
                    station = findIdentifier(stations, jsonStop.station)
                }catch (e:Exception){
                    throw e
                }
                var track:Track
                try {
                    track = findTrack(station.tracks, jsonStop.track.number)
                    val stop = StationStop(station, jsonStop.arrival, jsonStop.departure, track, jsonStop.offset, jsonStop.direction )
                    stops.add(stop)
                }catch (e:Exception){
                    println("station: "+ station )
                    println("no track with id")
                    throw e
                }

            }

            result.add(TimeTable(train, json.departures, stops))
        }

        return result
    }

    fun fillTimeTables(snap: Boolean = false, prefix: String = ""): List<TimeTable> {
        val infix = if (snap) snapInfix else ""
        val timetableFile = getFileName(DataType.Timetable, prefix, infix)

        var result = mutableListOf<TimeTable>()
        val stations = loadStations(snap, prefix).toMutableList()
        val trains = loadTrains(snap, prefix).toMutableList()

        var tempStationCount = 0

        val jsonTimetables = loadJsonList(timetableFile, JsonTimeTable::class.java)
        jsonTimetables.forEach { json ->
            var train: Train
            try {
                train = findIdentifier(trains, json.train)
            } catch (e: Exception){
                train = Train(emptyList(), json.train, "IC " + json.train)
                trains.add(train)
            }
            var stops = mutableListOf<StationStop>()
            json.stops.forEach { jsonStop ->

                var station:Station
                try {
                    station = findIdentifier(stations, jsonStop.station)
                }catch (e:Exception){
                    station = Station(jsonStop.station + "-TEMP#" + tempStationCount, emptyList(), jsonStop.station)
                    tempStationCount++
                    stations.add(station)
                }
                var track:Track
                try {
                    track = findTrack(station.tracks, jsonStop.track.number)
                }catch (e:Exception){
                    track = Track(0,null, jsonStop.track.number)
                    val tracks = station.tracks.toMutableList()
                    tracks.add(track)
                    station.tracks = tracks
                }
                val stop = StationStop(station, jsonStop.arrival, jsonStop.departure, track, jsonStop.offset, jsonStop.direction )
                stops.add(stop)
            }

            result.add(TimeTable(train, json.departures, stops))
        }
        JsonDataWriter.writeStations(stations)
        JsonDataWriter.writeTrains(trains)



        return result
    }

    fun cleanData(snap: Boolean = false, prefix: String = ""): MutableCollection<JsonTimeTable> {
        val infix = if (snap) snapInfix else ""
        val timetableFile = getFileName(DataType.Timetable, prefix, infix)

        val jsonTimetables = loadJsonList(timetableFile, JsonTimeTable::class.java)
        val stations = loadStations(snap, prefix).toMutableList()
        val trains = loadTrains(snap, prefix)

        val map = HashMap<Triple<String, String, String>, JsonTimeTable>()

        jsonTimetables.forEach { timetable ->
            val trainID = timetable.train
            val firstStopID = timetable.stops.first().station
            val lastStopID = timetable.stops.last().station
            val key = Triple(trainID, firstStopID, lastStopID)
            var get = map.get(key)
            if (get == null){
                get = timetable
            }else{
                // they have the same start, end and trainnumber
                if (timetable.stops.size != get.stops.size){
                    println(key.toString() + " has different stop sizes")
                }
                println("merging timetables")
                timetable.departures.forEach { depature ->
                    if (!get.departures.contains(depature)){
                        get.departures.add(depature)
                    }else{
                        println("already know this departure")
                    }
                }
            }
            map.put(key, get)
        }
        return map.values
    }

    fun cleanData2(snap: Boolean = false, prefix: String = ""): MutableCollection<JsonTimeTable> {
        val infix = if (snap) snapInfix else ""
        val timetableFile = getFileName(DataType.Timetable, prefix, infix)

        val jsonTimetables = loadJsonList(timetableFile, JsonTimeTable::class.java)
        val stations = loadStations(snap, prefix).toMutableList()
        val trains = loadTrains(snap, prefix)

        val map = HashMap<String, JsonTimeTable>()


        jsonTimetables.forEach { timetable ->
            var trainID = timetable.train
            var get = map.get(trainID)
            if (get == null){
                get = timetable
            }else{
                //same train id check if they are the same
                var delta = stopDelta(get.stops, timetable.stops)
                timetable.stops.forEach { depature ->
//                    if (!get.departures.contains(depature)){
                        //get.departures.add(depature)
//                    }else{
//                        println("already know this departure")
//                    }
                }
            }
            map.put(trainID, get)
        }
        return map.values
    }

    fun stopDelta(stop1:List<JsonStationStop>, stop2:List<JsonStationStop>): MutableList<JsonStationStop> {
        var result = mutableListOf<JsonStationStop>()
        var temp = stop2.toMutableList()
        stop2.forEach { stop ->
            if(stop1.contains(stop)){
                temp.remove(stop)
            }
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

    fun <T : Track> findTrack(list: List<T>, id: Int): T {
        val filter = list.filter { item -> item.id == id }
        if (filter.size == 1) {
            return filter[0]
        } else
            throw Exception("used not defined Identifier: " + id)
    }


}