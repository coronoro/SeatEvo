package data

import json.JsonDataWriter
import json.JsonStationStop
import json.JsonTimeTable
import json.JsonTrack
import marudor.MarudorApi
import model.Station
import model.track.Track
import model.Train
import model.Wagon
import model.timetable.DrivingDirection
import model.timetable.StationStop
import model.timetable.TimeTable
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
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

    fun loadICE(){
        var stations = HashMap<String,Station>()
        var trains = HashMap<String,Train>()
        var timeTables = mutableListOf<JsonTimeTable>()

        var departureMap = HashMap<String, MutableList<String>>()

        var stationsQueue = ArrayDeque<String>()
        var processedStations = mutableListOf<String>()
        // init queue with one start station
        val startStation = "Leipzig HBF"
        stationsQueue.add(startStation)
        processedStations.add(startStation)


        while (!stationsQueue.isEmpty()){
            val search = MarudorApi.searchStations(stationsQueue.pop())
            if (!search.isEmpty()) {
                search.forEach { station ->
                    // check if the station has been processed already
                    if (!processedStations.contains(station.id)){
                        // add it because we are processing it now
                        processedStations.add(station.id)
                        val stationRep = Station(station.title, emptyList(), station.id)
                        stations.put(station.id, stationRep)

                        val trackSet = HashSet<Int>()
                        val departuresInfo = MarudorApi.getDeparturesInfo(station)
                        if (departuresInfo != null){
                            departuresInfo.departures.forEach { departure ->
                                // only IC/E
                                if (departure.train.type.toLowerCase().startsWith("ic")){
                                    //add track
                                    trackSet.add(departure.platform.toInt())
                                    // empty wagons
                                    var wagons = mutableListOf<Wagon>()
                                    val trainId = departure.train.number
                                    val trainName = departure.train.name
                                    trains.put(trainName, Train(wagons, trainId, trainName))
                                    //load all stations for this route which havent been loaded yet
                                    departure.route.forEach { route ->
                                        if (!processedStations.contains(route.name)){
                                            //stationsQueue.add(route.name)
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
                    val trainDetails = MarudorApi.getTrainDetails(key, departure)
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
                                var string = "-1"
                                if (departureInfo.scheduledPlatform != null){
                                    string = departureInfo.scheduledPlatform!!
                                }else{
                                    string = departureInfo.platform
                                }
                                val p: Pattern = Pattern.compile("\\d+")
                                val m: Matcher = p.matcher(string)
                                track = JsonTrack( m.group(0).toInt())
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

                            stops.add(JsonStationStop(stop.station.id,arrival, departure, track, offset, direction))
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

        JsonDataWriter.writeTrains(trains.values.toList())
        JsonDataWriter.writeStations(stations.values.toList())
        JsonDataWriter.writeJSonTimeTables(timeTables)

    }



}