package data

import json.JsonDataWriter
import json.JsonStationStop
import json.JsonTimeTable
import json.JsonTrack
import model.Station
import model.Train
import model.Wagon
import model.timetable.DrivingDirection
import model.track.Track
import java.lang.Exception
import java.time.Duration
import java.time.LocalTime

object DataGenerator {

    private val stationNamePrefix = "Station"

    private val trainPrefix = "Train"

    fun generateStairNetwork(count: Int, bidirectional: Boolean, trainFrequency: Int, wagonNumber:Int, wagonSize: Int){
        val trains = mutableListOf<Train>()
        val stations = mutableListOf<Station>()
        val timetables = mutableListOf<JsonTimeTable>()
        var stationID = 0
        var trainID = 0
        val timeDifference = 3
        val baseTime = LocalTime.of(0, 0 )

        //generate tracks for stations
        val trackList = generateTrackList(bidirectional)
        //generate wagons for trains
        val wagons = generateWagons(wagonNumber,wagonSize)


        var previousStation: Station? = null
        for (i in 0 .. count){
            val station = Station(stationNamePrefix + i, trackList, stationID.toString())
            stationID ++
            stations.add(station)
            if (previousStation != null){
                //create the train between the two stations
                var train = Train(wagons, trainID.toString(), trainPrefix + trainID)
                trains.add(train)
                trainID ++

                val time = baseTime.plusMinutes((timeDifference*i).toLong())
                val departures =generateDepartures(trainFrequency,time)
                val stops = mutableListOf<JsonStationStop>()
                stops.add(JsonStationStop(previousStation.id,null, Duration.ZERO, JsonTrack(0),0, DrivingDirection.FORWARD))
                stops.add(JsonStationStop(station.id, Duration.ofMinutes((timeDifference-1).toLong()), null, JsonTrack(0),0, DrivingDirection.FORWARD))
                timetables.add(JsonTimeTable(train.id, departures, stops))
            }
            previousStation = station
        }


        JsonDataWriter.writeTrains(trains)
        JsonDataWriter.writeStations(stations)
        JsonDataWriter.writeJSonTimeTables(timetables)
    }

    private fun generateTrackList(bidirectional: Boolean): MutableList<Track> {
        //TODO improve parameter
        val trackList = mutableListOf<Track>()
        val pairTrackID: Int? = if (bidirectional) 1 else null
        trackList.add(Track(0, pairTrackID,0))
        if (bidirectional)
            trackList.add(Track(0, 0,1))
        return trackList
    }

    private fun generateWagons(wagonNumber: Int, wagonSize:Int): MutableList<Wagon> {

        val wagons = mutableListOf<Wagon>()
        for (i in 0 .. wagonNumber){
            wagons.add(Wagon(wagonSize))
        }
        return wagons
    }

    private fun generateDepartures(trainFrequency:Int, baseTime:LocalTime): MutableList<LocalTime> {
        //generate departure times
        val departures = mutableListOf<LocalTime>()
        val timeDifference = 15
        for (i in 0 until trainFrequency){
            departures.add(baseTime.plusMinutes((i*timeDifference).toLong()))
        }
        return departures
    }

    fun generateGridNetwork(dimension: Pair<Int,Int>, trainFrequency:Int, wagonNumber:Int, wagonSize:Int){
        if (dimension.first < 2 || dimension.second < 2){
            throw Exception("no valid dimension given")
        }
        val trains = mutableListOf<Train>()
        val stations = mutableListOf<Station>()
        val timetables = mutableListOf<JsonTimeTable>()
        var stationID = 0
        var trainID = 0
        val timeDifference = 10
        val baseTime = LocalTime.of(0, 0 )


        val trackList = generateTrackList(false)
        val wagons = generateWagons(wagonNumber,wagonSize)


        val gridStations = Array(dimension.first) {Array<Station?>(dimension.second) {null} }
        for (i in 0 until dimension.first){
            for (j in 0 until dimension.second){
                var station = Station(stationNamePrefix +i +"|"+j, trackList, stationID.toString())
                stationID ++
                stations.add(station)
                gridStations[i][j] = station
            }
        }

        for (i in 0 until dimension.first){
            val train = Train(wagons, trainID.toString(),trainPrefix+i)
            trains.add(train)
            trainID ++
            val time = baseTime.plusMinutes((timeDifference*i).toLong())
            val departures =generateDepartures(trainFrequency,time)
            val stops = mutableListOf<JsonStationStop>()
            for (j in 0 until dimension.second){
                val station = gridStations[i][j]
                var arrival: Duration? = null
                var depart: Duration? = null
                if (j != 0)
                    arrival = Duration.ofMinutes((timeDifference*j).toLong() - 1)
                if (j < dimension.second - 1)
                    depart = Duration.ofMinutes((timeDifference*j).toLong())
                stops.add(JsonStationStop(station!!.id, arrival, depart, JsonTrack(0),0, DrivingDirection.FORWARD))
            }
            timetables.add(JsonTimeTable(train.id, departures, stops))
        }

        for (i in 0 until dimension.second){
            val train = Train(wagons, trainID.toString(),trainPrefix+i)
            trains.add(train)
            trainID ++
            val time = baseTime.plusMinutes((timeDifference*i).toLong())
            val departures =generateDepartures(trainFrequency,time)
            val stops = mutableListOf<JsonStationStop>()
            for (j in 0 until dimension.first){
                val station = gridStations[j][i]
                var arrival: Duration? = null
                var depart: Duration? = null
                if (j != 0)
                    arrival = Duration.ofMinutes((timeDifference*j).toLong() - 1)
                if (j < dimension.first - 1)
                    depart = Duration.ofMinutes((timeDifference*j).toLong())
                stops.add(JsonStationStop(station!!.id, arrival, depart, JsonTrack(0),0, DrivingDirection.FORWARD))
            }
            timetables.add(JsonTimeTable(train.id, departures, stops))
        }

        JsonDataWriter.writeTrains(trains)
        JsonDataWriter.writeStations(stations)
        JsonDataWriter.writeJSonTimeTables(timetables)
    }

}