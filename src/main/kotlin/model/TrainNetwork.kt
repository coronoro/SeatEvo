package model

import graph.WeightedDataEdge
import model.timetable.TimeTable
import org.jgrapht.graph.DefaultDirectedWeightedGraph
import util.GraphUtil
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class TrainNetwork(val timeTables: List<TimeTable>) {

    var g = DefaultDirectedWeightedGraph<Pair<Station, LocalTime>, WeightedDataEdge>(WeightedDataEdge::class.java)

    var timeTableMap = HashMap<Int, TimeTable>()

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
        GraphUtil.visualize(g)
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

    fun getTrack(train: Train, station: Station): Track? {
        var trackForStation: Track? = null
        val timeTable = getTimeTable(train)
        if (timeTable != null) {
            trackForStation = timeTable.getTrackForStation(station)
        }
        return trackForStation
    }

    /**
     * gets distance between two trainwagons on a specific station
     */
    fun getDistance(from: Train, fromWagonNumber: Int, to: Train, toWagonNumber: Int, station: Station): Int {
        var result = 0
        val fromTrack = getTrack(from, station)
        val toTrack = getTrack(to, station)
        //TODO implement
        result = abs(fromWagonNumber - toWagonNumber) + 1
        return result
    }

    /**
     * gets distance to board or leave a train on a specifiv station
     */
    fun getDistance(train: Train, wagon: Int, station: Station): Int {
        var result = 0
        val track = getTrack(train, station)
        //TODO implement
        result = wagon + 1
        return result
    }

}