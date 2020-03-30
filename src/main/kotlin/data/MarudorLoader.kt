package data

import json.*
import marudor.MarudorApi
import marudor.departure.DeparturesInfo
import marudor.hafas.TrainDetails
import marudor.wagonSequence.WagonSequence
import model.Station
import model.track.Track
import model.Train
import model.Wagon
import model.timetable.DrivingDirection
import java.lang.Exception
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.HashMap
import kotlin.collections.HashSet


object MarudorLoader {

    fun searchStation(name:String): Station {
        val search = MarudorApi.searchStations(name)
        if (search.size > 1){
            println("found more than one result for " + name)
        }
        val first = search.first()
        val tracks = emptyList<Track>()
        return Station(first.title, tracks, first.id)
    }

    fun loadAllStationTracks(){
        val stations = JsonDataLoader.loadStations(true)
        stations.forEach { station ->
            val departuresInfo = MarudorApi.getDeparturesInfo(station.id, lookahead = 420, lookbehind = 420)
            val platformSet = HashSet<Int>()
            if (departuresInfo != null){
                departuresInfo.departures.forEach { deparure ->
                    val platform = deparure.platform
                    if (platform != null) {
                        val p: Pattern = Pattern.compile("\\d+")
                        val m: Matcher = p.matcher(platform)
                        if (m.matches())
                            platformSet.add(m.group(0).toInt())
                    }
                }
                val trackList = mutableListOf<Track>()
                platformSet.forEach {
                    trackList.add(Track(0, null, it))
                }
                station.tracks = trackList
            }
        }
        JsonDataWriter.writeStations(stations)
    }

    fun platformParse(string:String):Int?{
        var result : Int? = null
        val p: Pattern = Pattern.compile("\\d+")
        val m: Matcher = p.matcher(string)
        if (m.matches())
            result = m.group(0).toInt()
        return result
    }

    fun loadAllStations(load: Boolean){
        LoggingConfig()
        var stations = mutableListOf<Station>()
        var processedStations = HashSet<String>()

        var searchTerms = ArrayDeque<String>()
        var processedSearchTerms = HashSet<String>()

//        if (load){
//            val loadStations = JsonDataLoader.loadStations(true, "")
//            loadStations.forEach { station ->
//                processedStations.add(station.id)
//                stations.add(station)
//            }
//        }

        // init queue with one start station
        val startStation = "Leipzig HBF".toLowerCase()
        searchTerms.add(startStation)

        while (!searchTerms.isEmpty()){
            var search : List<marudor.station.Station>  = emptyList()
            var pop = ""
            try {
                pop = searchTerms.pop()
                if (!processedSearchTerms.contains(pop)){
                    processedSearchTerms.add(pop)
                    println("searching " + pop)
                    search = MarudorApi.searchStations(pop)
                }
            }catch (e:Exception){
                processedSearchTerms.remove(pop)
                e.printStackTrace()
            }
            if (search.isEmpty()){
                println("no search result found ")
            }else{
                println("found " + search.size + " results")
                search.forEach { station ->
                    // check if the station has been processed already
                    if (processedStations.contains(station.id)){
                        println("already know station with id " + station.id)
                    }else{
                        println("new station id " + station.id)
                        // add it because we are processing it now
                        processedStations.add(station.id)
                        val stationRep = Station(station.title, emptyList(), station.id)
                        stations.add(stationRep)

                        var departuresInfo: DeparturesInfo? = null
                        try{
                            departuresInfo = MarudorApi.getDeparturesInfo(station.id, lookahead = 420, lookbehind = 420)
                        }catch (e: Exception){
                            e.printStackTrace()
                        }
                        val platformSet = HashSet<Int>()
                        if (departuresInfo != null){
                            departuresInfo.departures.forEach { departure ->
                                if (departure.train.type.toLowerCase().startsWith("i")) {
                                    //load all stations for this route which havent been loaded yet
                                    departure.route.forEach { route ->
                                        val routeName = route.name.toLowerCase()
                                        if (!processedStations.contains(routeName)) {
                                            searchTerms.add(route.name)
                                        }
                                    }
                                }
                                // get platforms
                                val platform = departure.platform
                                if (platform != null) {
                                    val p: Pattern = Pattern.compile("\\d+")
                                    val m: Matcher = p.matcher(platform)
                                    if (m.matches())
                                        platformSet.add(m.group(0).toInt())
                                }
                            }
                            val trackList = mutableListOf<Track>()
                            platformSet.forEach {
                                trackList.add(Track(0, null, it))
                            }
                            stationRep.tracks = trackList
                        }
                        JsonDataWriter.writeStations(stations)
                    }
                }
            }
        }
        JsonDataWriter.writeStations(stations)
    }



    fun loadICETimeTables(){
        LoggingConfig()
        val timetables = JsonDataLoader.loadJsonTimeTable(true,"marudor-")
        val timetableMap = HashMap<Triple<String, String, String>, JsonTimeTable>()

        timetables.forEach { timetable ->
            val trainID = timetable.train
            val firstStopID = timetable.stops.first().station
            val lastStopID = timetable.stops.last().station
            val key = Triple(trainID, firstStopID, lastStopID)
            var item = timetableMap.get(key)
            if (item == null){
                item = timetable
            }
            timetableMap.put(key, item)
        }


        val trains = mutableListOf<Train>()
        var stations = JsonDataLoader.loadStations(true,"marudor-").toMutableList()
        var stationsMap = stations.associate {  it.id to it}.toMutableMap()

        stations.forEach { station ->
            println("loading for station:" + station.name)
            var departuresInfo : DeparturesInfo? = null
            try{
                departuresInfo = MarudorApi.getDeparturesInfo(station.id, lookahead = 1440, lookbehind = 0)
            }catch (e:Exception){
                e.printStackTrace()
            }
            if (departuresInfo == null){
                println("No departureInfo found")
            }else{
                println("found " + departuresInfo.departures.size + " departures")
                departuresInfo.departures.forEach { departure ->
                    if (departure.train.type.toLowerCase().startsWith("ic")) {
                        val trainId = departure.train.number
                        val trainName = departure.train.name

                        var wagonSize = 0
                        val wagons = mutableListOf<Wagon>()
                        var date:Date? = null
                        if (departure.departure != null){
                            date = Date(departure.departure!!.scheduledTime)
                        }
                        var trainDetails: TrainDetails? = null
                        try {
                            trainDetails = MarudorApi.getTrainDetails(departure.train, date)
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                        if (trainDetails == null){
                            println("No Train Details for " + departure.train)
                        }else {
                            val firstStopId = trainDetails.stops.first().station.id
                            val lastStopId = trainDetails.stops.last().station.id
                            val key = Triple<String, String, String>(trainId,firstStopId,lastStopId)
                            val mapEntry = timetableMap.get(key)
                            if (mapEntry != null){
                                println("this connection is already known")
                                val ofEpochMilli = Instant.ofEpochMilli(departure.initialDeparture)
                                val zone = ofEpochMilli.atZone(ZoneId.of("Europe/Berlin"))
                                val time = LocalTime.of(zone.hour, zone.minute)
                                if (!mapEntry.departures.contains(time)){
                                    mapEntry.departures.add(time)
                                }
                            }else{
                                println("a new connection")

                                var departures = mutableListOf<LocalTime>()
                                val stops = mutableListOf<JsonStationStop>()
                                var start: Instant? = null
                                trainDetails.stops.forEach { stop ->
                                    // create a new station if its not known
                                    val id = stop.station.id
                                    var get = stationsMap.get(id)
                                    if (get == null){
                                        println("no station found for id")
                                        get = Station(stop.station.title, emptyList(), id)
                                        stationsMap.put(id, get)
                                    }
                                    // calculate arrival
                                    var arrivalTime: Duration? = null
                                    var departureTime: Duration? = null

                                    if (stop.arrival != null){
                                        val arrivalInstant = Instant.ofEpochMilli(stop.arrival!!.scheduledTime)
                                        arrivalTime = Duration.between(start, arrivalInstant)
                                    }else if(start != null && stop.departure != null){
                                        //sometimes there is no arrival even though its not the first station
                                        val arrivalInstant = Instant.ofEpochMilli(stop.departure!!.scheduledTime).minus(1, ChronoUnit.MINUTES)
                                        arrivalTime = Duration.between(start, arrivalInstant)
                                    }
                                    if (stop.departure != null){
                                        val departureInstant = Instant.ofEpochMilli(stop.departure!!.scheduledTime)
                                        // first stop
                                        if (start == null){
                                            start = departureInstant
                                            val zone = departureInstant.atZone(ZoneId.of("Europe/Berlin"))
                                            departures.add(LocalTime.of(zone.hour, zone.minute))
                                        }
                                        departureTime = Duration.between(start, departureInstant)
                                    }


                                    // set driving direction
                                    var wagonSequence: WagonSequence? = null
                                    if (stop.departure == null && stop.arrival == null) {
                                        println(get)
                                        println("no departure or arrival for train: " + trainId + " | departure: " + departure)
                                    }else{
                                        try {
                                            wagonSequence = MarudorApi.getWagonSequence(trainId, stop.departure!!.scheduledTime.toString())
                                        } catch (e: Exception) {
                                            if (stop.departure != null)
                                                println("error with:" + trainId + "," + stop.departure!!.scheduledTime)
                                            try {
                                                wagonSequence = MarudorApi.getWagonSequence(trainId, stop.arrival!!.scheduledTime.toString())
                                            }catch (ex: Exception){
                                                if (stop.arrival != null)
                                                    println("error with:" + trainId + "," + stop.arrival!!.scheduledTime)
                                            }
                                        }
                                    }

                                    var direction = DrivingDirection.BACKWARD
                                    if (wagonSequence != null && "VORWAERTS".equals(wagonSequence!!.fahrtrichtung)){
                                        direction = DrivingDirection.FORWARD
                                    }

                                    // get wagons size maximum
                                    if (wagonSequence != null && wagonSequence.allFahrzeuggruppe.size > wagonSize){
                                        wagonSize = wagonSequence.allFahrzeuggruppe.size
                                    }

                                    // get platform
                                    var platform = -1
                                    if(stop.departure != null){
                                        platform = platformParse(stop.departure!!.platform ?: "-1") ?: -1
                                    }
                                    // if platform is unknown make a new one
                                    var filter = get.tracks.filter { it.id == platform }
                                    if (filter.isEmpty()){
                                        val trackList = get.tracks.toMutableList()
                                        trackList.add(Track(0, null, platform))
                                        get.tracks = trackList
                                    }
                                    // TODO get track offset
                                    var offset = 0
                                    stops.add(JsonStationStop(stop.station.id, arrivalTime, departureTime, JsonTrack(platform), offset, direction))
                                }

                                for (i in 0 until wagonSize){
                                    wagons.add(Wagon(-1))
                                }
                                var train = Train(wagons, trainId, trainName)
                                println("adding train:" + train)
                                trains.add(train)
                                timetableMap.put(key,JsonTimeTable(train.id, departures, stops))
                            }
                        }
                    }
                }
            }
            JsonDataWriter.writeJSonTimeTables(timetables)
            JsonDataWriter.writeTrains(trains)
            JsonDataWriter.writeStations(stationsMap.values.toList())
        }
    }


    fun loadICE(){
        LoggingConfig()
        //AbstractRestApi.wait = 10000
        var stations = HashMap<String,Station>()
        var trains = HashMap<String,Train>()
        var timeTables = mutableListOf<JsonTimeTable>()

        var departureMap = HashMap<String, MutableList<String>>()

        var stationsQueue = ArrayDeque<String>()
        var processedStations = HashSet<String>()
        // init queue with one start station
        val startStation = "Leipzig HBF"
        stationsQueue.add(startStation)


        while (!stationsQueue.isEmpty()){
            var search : List<marudor.station.Station>  = emptyList()
            try {
                val pop = stationsQueue.pop()
                if (!processedStations.contains(pop.toLowerCase())) {
                    println("searching. " + pop)
                    search = MarudorApi.searchStations(pop)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }

            if (!search.isEmpty()) {
                search.forEach { station ->
                    // check if the station has been processed already
                    if (!processedStations.contains(station.title)){
                        // add it because we are processing it now
                        processedStations.add(station.title.toLowerCase())
                        val stationRep = Station(station.title, emptyList(), station.id)
                        stations.put(station.id, stationRep)

                        val trackSet = HashSet<Int>()
                        val departuresInfo = MarudorApi.getDeparturesInfo(station)
                        if (departuresInfo != null){
                            departuresInfo.departures.forEach { departure ->
                                // only IC/E
                                if (departure.train.type.toLowerCase().startsWith("ic")){
                                    //add track
                                    val platform = departure.platform
                                    if (platform != null){
                                        val p: Pattern = Pattern.compile("\\d+")
                                        val m: Matcher = p.matcher(platform)
                                        if (m.matches())
                                            trackSet.add(m.group(0).toInt())
                                    }

                                    // empty wagons
                                    var wagons = mutableListOf<Wagon>()
                                    val trainId = departure.train.number
                                    val trainName = departure.train.name
                                    trains.put(trainName, Train(wagons, trainId, trainName))
                                    //load all stations for this route which havent been loaded yet
                                    departure.route.forEach { route ->
                                        if (!processedStations.contains(route.name.toLowerCase()) && !stationsQueue.contains(route.name)){
                                            stationsQueue.add(route.name)
                                        }
                                    }
                                    var list = departureMap.get(trainId)
                                    if (list == null){
                                        list = mutableListOf()
                                    }
                                    list.add(departure.initialDeparture.toString())
                                    departureMap.put(trainName, list)
                                }
                            }
                        }
                    }
                }
            }
        }

        JsonDataWriter.writeTrains(trains.values.toList())
        JsonDataWriter.writeStations(stations.values.toList())

        departureMap.keys.forEach { key -> 
            val departuresList = departureMap.get(key)
            val stops = mutableListOf<JsonStationStop>()
            val departures = mutableListOf<LocalTime>()
            // get the train
            val train = trains.get(key)!!
            if (departuresList != null){
                // get the stationStops
                if (!departuresList.isEmpty()){
                    val departure = departuresList.first()

                    var trainDetails: TrainDetails? = null
                    while (trainDetails == null){
                        try {
                            trainDetails = MarudorApi.getTrainDetails(key, departure)
                        }catch (e: Exception){
                            e.printStackTrace()
                        }
                    }

                    if (trainDetails != null){
                        var start =  Instant.now()
                        trainDetails.stops.forEachIndexed { index, stop ->
                            val departureInfo = stop.departure
                            val arrivalInfo = stop.arrival

                            var arrival: Duration? = null
                            var departure: Duration? = null
                            var track = JsonTrack(-1)
                            //TODO offset
                            var offset = 0
                            var direction = DrivingDirection.FORWARD
                            if (departureInfo != null){
                                var string: String
                                if (departureInfo.scheduledPlatform != null){
                                    string = departureInfo.scheduledPlatform!!
                                }else{
                                    string = departureInfo.platform ?: "-1"
                                }
                                val p: Pattern = Pattern.compile("\\d+")
                                val m: Matcher = p.matcher(string)
                                if (m.matches())
                                    track = JsonTrack(m.group(0).toInt())
                            }

                            if (index == 0) {
                                if (departureInfo != null){
                                    start = Instant.ofEpochMilli(departureInfo.scheduledTime)
                                }
                            }
                            if (departureInfo != null){
                                var end = Instant.ofEpochMilli(departureInfo.scheduledTime)
                                departure = Duration.between(start, end)
                                //departure = Instant.ofEpochMilli(departureInfo.scheduledTime - start).atZone(ZoneId.systemDefault()).toLocalDateTime()
                            }
                            if (arrivalInfo != null){
                                var end = Instant.ofEpochMilli(arrivalInfo.scheduledTime)
                                arrival = Duration.between(start, end)
                            }

                            stops.add(JsonStationStop(stop.station.id, arrival, departure, track, offset, direction))
                        }
                    }
                }

                departuresList.forEach { departure ->
                    val trainDetails = MarudorApi.getTrainDetails(key, departure)
                    if (trainDetails != null){
                        val stopInfo = trainDetails.departure
                        if (stopInfo != null){
                            departures.add(Instant.ofEpochMilli(stopInfo.scheduledTime).atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalTime())
                        }
                    }

                }
            }
            timeTables.add(JsonTimeTable(train.id, departures, stops))
        }


        JsonDataWriter.writeJSonTimeTables(timeTables)

    }



}