package model

import graph.WeightedDataEdge
import model.timetable.DrivingDirection
import model.timetable.StationStop
import model.timetable.TimeTable
import org.jgrapht.graph.DefaultDirectedWeightedGraph
import java.lang.Exception
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class TrainNetwork(val timeTables: List<TimeTable>) {

    var g = DefaultDirectedWeightedGraph<Pair<Station, LocalTime>, WeightedDataEdge>(WeightedDataEdge::class.java)

    var timeTableMap = HashMap<String, TimeTable>()

    val stations: HashSet<Station>

    init {
        timeTables.forEach { timetable ->
            timeTableMap.put(timetable.train.id, timetable)
            timetable.departures.forEach { departure ->
                for (i in 0 until timetable.stops.size) {
                    val first = timetable.stops[i]
                    if (i + 1 >= timetable.stops.size) {
                        break
                    }
                    val second = timetable.stops[i + 1]

                    if (first.departure != null && second.arrival != null) {
                        val departureTime = departure.plus(first.departure)
                        val vertex1 = Pair(first.station, departureTime)
                        g.addVertex(vertex1)

                        val arrivalTime = departure.plus(second.arrival)
                        val vertex2 = Pair(second.station, arrivalTime)
                        g.addVertex(vertex2)

                        val duration = second.arrival!!.minus(first.departure)
                        val edge = g.addEdge(vertex1, vertex2)
                        edge.data = timetable
                        //TODO
                        g.setEdgeWeight(edge, duration.toMinutes().toDouble())
                    }
                }
            }
        }
        // set stations
        val vertexSet = g.vertexSet()
        stations = vertexSet.mapTo(HashSet()) { vertex -> vertex.first }

        //add edges for waiting
        addWaitingEdges()
    }


    private fun addWaitingEdges() {
        val vertexSet = g.vertexSet()
        val stations = vertexSet.mapTo(HashSet()) { vertex -> vertex.first }
        stations.forEach { station ->
            val filter = vertexSet.filter { vertex -> vertex.first == station }
            val sorted = filter.sortedBy { it.second }
            for (i in 0 until sorted.size) {
                if (i + 1 >= sorted.size) {
                    break
                }
                val first = sorted.get(i)
                val second = sorted.get(i + 1)
                val waiting = g.addEdge(first, second)
                val between = ChronoUnit.MINUTES.between(first.second, second.second)
                g.setEdgeWeight(waiting, between.toDouble())
            }
        }
    }

    fun getTimeTable(train: Train): TimeTable? {
        val timeTable = timeTableMap.get(train.id)
        return timeTable
    }

    fun getStationStop(train: Train, station: Station): StationStop? {
        var stop: StationStop? = null
        val timeTable = getTimeTable(train)
        if (timeTable != null) {
            stop = timeTable.getStationStop(station)
        }
        return stop
    }

    /**
     * gets distance between two train wagons on a specific station
     */
    fun getDistance(from: Train, fromWagonNumber: Int, to: Train, toWagonNumber: Int, station: Station): Double {
        var result = 0.0
        val fromStop = getStationStop(from, station)
        val toStop = getStationStop(to, station)
        if (fromStop != null && toStop != null) {
            val fromTrack = fromStop.track
            val toTrack = toStop.track

            var wagonOffsetFrom = fromWagonNumber
            if (fromStop.direction == DrivingDirection.FORWARD){
                wagonOffsetFrom = from.wagons.size - fromWagonNumber
            }

            var wagonOffsetTo = toWagonNumber
            if (toStop.direction == DrivingDirection.FORWARD){
                wagonOffsetTo = to.wagons.size - toWagonNumber
            }

            val fromTrackPosition =  wagonOffsetFrom + fromStop.trackOffset
            val toTrackPosition =  wagonOffsetTo + toStop.trackOffset

            if ((fromTrack.pair != null && fromTrack.pair == toTrack.id) || fromTrack.id == toTrack.id) {
                result = (abs(fromTrackPosition - toTrackPosition) + 1).toDouble()
            }else{
                // from wagon to exit
                result = (Math.abs(fromTrackPosition - fromTrack.access) + 1).toDouble()
                // from track to track
                result += Math.abs(fromTrack.id - toTrack.id)
                // from exit to wagon
                result += Math.abs(toTrackPosition - toTrack.access) + 1
            }
        }else{
            throw Exception("no Stops were defined for Train "+from.id + ", " + to.id + " at station: "+ station.id)
        }
        return result
    }

    /**
     * gets distance to board or leave a train on a specific station
     */
    fun getDistance(train: Train, wagon: Int, station: Station): Double {
        var result = 0.0
        val stop = getStationStop(train, station)
        if (stop != null){
            var wagonOffsetTo = wagon
            if (stop.direction == DrivingDirection.FORWARD){
                wagonOffsetTo = train.wagons.size - wagon
            }

            val toTrackPosition =  wagonOffsetTo + stop.trackOffset
            result += Math.abs(toTrackPosition - stop.track.access) + 1
        }else{
            throw Exception()
        }
        return result
    }

}