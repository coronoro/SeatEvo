package data

import json.JsonDataWriter
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
        var timeTables = mutableListOf<TimeTable>()

        var departureMap = HashMap<String, MutableList<String>>()

        var stationsQueue = ArrayDeque<String>()
        var processedStations = mutableListOf<String>()
        stationsQueue.add("Leipzig HBF")
        processedStations.add("Leipzig HBF")


        while (!stationsQueue.isEmpty()){
            val search = MarudorApi.searchStations(stationsQueue.pop())
            if (!search.isEmpty()) {
                val station = search.get(0)
                stations.put(station.id, Station(station.title, emptyList(), station.id))
                val departuresInfo = MarudorApi.getDeparturesInfo(station)
                if (departuresInfo != null){
                    departuresInfo.departures.forEach { departure ->
                        // only IC/E
                        if (departure.train.type.toLowerCase().startsWith("ic")){
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

        JsonDataWriter.writeTrains(trains.values.toList())
        JsonDataWriter.writeStations(stations.values.toList())

        departureMap.keys.forEach { key -> 
            val departuresList = departureMap.get(key)
            val stops = mutableListOf<StationStop>()
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


                            val station = stations.get(stop.station.id)
                            var arrival: Duration? = null
                            var departure: Duration? = null
                            var track = Track(-1, null, -1)
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
                                track = Track(-1, null, m.group(0).toInt())
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

                            stops.add(StationStop(station!!,arrival, departure, track, offset, direction))
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
            timeTables.add(TimeTable(train, departures, stops))
        }
        
        

    }



}