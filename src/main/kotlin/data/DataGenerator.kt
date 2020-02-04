package data

import json.JsonDataWriter
import json.JsonStationStop
import json.JsonTimeTable
import json.JsonTrack
import model.Station
import model.Train
import model.Wagon
import model.timetable.DrivingDirection
import model.timetable.StationStop
import model.timetable.TimeTable
import model.track.Track
import java.time.Duration
import java.time.LocalTime

object DataGenerator {

    private val stationNamePrefix = "Station"
    private val startStationInfix = ":S"
    private val ventureStationInfix = ":V"
    private val endStationInfix = ":E"

    private val trainPrefix = "Train"

    fun generateStairNetwork(steps: Int, bidirectional: Boolean, trainfrequency: Int, wagonNumber:Int, wagonSize: Int){
        val trains = mutableListOf<Train>()
        val stations = mutableListOf<Station>()
        val timetables = mutableListOf<JsonTimeTable>()
        var stationID = 0
        var trainID = 0
        //generate tracks for stations
        val trackList = mutableListOf<Track>()
        val pairTrackID: Int? = if (bidirectional) 1 else null
        trackList.add(Track(0, pairTrackID,0))
        if (bidirectional)
            trackList.add(Track(0, 0,1))
        //generate wagons for trains
        val wagons = mutableListOf<Wagon>()
        for (i in 0 .. wagonNumber){
            wagons.add(Wagon(wagonSize))
        }
        //generate departure times
        val depratures = mutableListOf<LocalTime>()
        val baseTime = LocalTime.of(0, 0 )
        val timeDifference = 15
        for (i in 0 until trainfrequency){
            depratures.add(baseTime.plusMinutes((i*timeDifference).toLong()))
        }

        var startStation = Station(stationNamePrefix + startStationInfix, trackList, stationID.toString())
        stationID ++
        stations.add(startStation)
        for (i in 0 .. steps){
            // create stations
            var ventureStation = Station(stationNamePrefix + ventureStationInfix+i, trackList, stationID.toString())
            stationID ++
            stations.add(ventureStation)
            var endStation = Station(stationNamePrefix + endStationInfix+i, trackList, stationID.toString())
            stationID ++
            stations.add(endStation)
            // create trains
            var svTrain = Train(wagons, trainID.toString(), trainPrefix + trainID)
            trains.add(svTrain)
            trainID ++
            var veTrain = Train(wagons, trainID.toString(), trainPrefix + trainID)
            trains.add(veTrain)
            trainID ++
            // create timetables
            val svStops = mutableListOf<JsonStationStop>()
            svStops.add(JsonStationStop(startStation.id,null, Duration.ZERO, JsonTrack(0),0, DrivingDirection.FORWARD))
            svStops.add(JsonStationStop(ventureStation.id, Duration.ofMinutes((timeDifference-1).toLong()), null, JsonTrack(0),0, DrivingDirection.FORWARD))
            timetables.add(JsonTimeTable(svTrain.id, depratures, svStops))

            val veStops = mutableListOf<JsonStationStop>()
            veStops.add(JsonStationStop(ventureStation.id,null, Duration.ZERO, JsonTrack(0),0, DrivingDirection.FORWARD))
            veStops.add(JsonStationStop(endStation.id, Duration.ofMinutes((timeDifference-1).toLong()), null, JsonTrack(0),0, DrivingDirection.FORWARD))
            timetables.add(JsonTimeTable(veTrain.id, depratures, veStops))

            if (bidirectional){
                //TODO
            }

            startStation = endStation
        }

        JsonDataWriter.writeTrains(trains)
        JsonDataWriter.writeStations(stations)
        JsonDataWriter.writeJSonTimeTables(timetables)
    }


}