package model

import model.timetable.DrivingDirection
import model.timetable.StationStop
import model.timetable.TimeTable
import org.graphstream.graph.Edge
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.DefaultGraph
import org.graphstream.graph.implementations.MultiGraph
import org.graphstream.graph.implementations.SingleGraph
import java.lang.Exception
import java.time.LocalTime
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class TrainNetwork(val timeTables: List<TimeTable>) {

    var graph = MultiGraph("timetable")

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
                        val node1ID = first.station.name + "@" + departureTime
                        var node1 = graph.getNode<Node>(node1ID)
                        if (node1 == null){
                            node1 = graph.addNode(node1ID)
                            node1.addAttribute("ui.label", first.station.name);
                            node1.addAttribute("station", first.station)
                            node1.addAttribute("time", departureTime)
                        }

                        val arrivalTime = departure.plus(second.arrival)
                        val node2ID = second.station.name + "@" + arrivalTime
                        var node2 = graph.getNode<Node>(node2ID)
                        if (node2 == null){
                            node2 = graph.addNode(node2ID)
                            node2.addAttribute("ui.label", second.station.name);
                            node2.addAttribute("station", second.station)
                            node2.addAttribute("time", arrivalTime)
                        }

                        val duration = second.arrival!!.minus(first.departure)
                        val edge: Edge
                        val edgeID = node1ID+"-"+node2ID + "<" + timetable.train.id
                        try {
                            edge  = graph.addEdge(edgeID, node1, node2, true)
                        }catch (e:Exception){
                            val edge1 = graph.getEdge<Edge>(edgeID)
                            val attribute = edge1.getAttribute<TimeTable>("timetable")
                            println("existing: " +attribute.train.id)
                            println("new: " +timetable.train.id)
                            throw e
                        }

                        edge.setAttribute("weight", duration.toMinutes().toDouble())
                        edge.setAttribute("timetable", timetable)
                    }
                }
            }
        }
        // set stations
        val nodeSet = graph.getNodeSet<Node>()
        stations = nodeSet.mapTo(HashSet()) { node -> node.getAttribute<Station>("station") }

        //add edges for waiting
        addWaitingEdges()

        //position nodes for display
        addNodePositions()
    }

    private fun addNodePositions() {
        val sorted = stations.sortedBy { it.name }
        var coordinate = 0.0
        val coordinateMap = sorted.associateBy({ it.name }, {
            coordinate = coordinate + 5
            coordinate
        })
        val nodes = graph.getNodeSet<Node>()
        nodes.forEach { node ->
            val time = node.getAttribute<LocalTime>("time")
            val x = time.get(ChronoField.MINUTE_OF_DAY)
            val station = node.getAttribute<Station>("station")
            val y = coordinateMap.get(station.name)
            node.addAttribute("xyz", x, y, 0)
            node.addAttribute("ui.style", "text-alignment: above; text-background-mode:plain;text-size:15px;");
        }

    }


    private fun addWaitingEdges() {
        val nodeSet = graph.getNodeSet<Node>()
        stations.forEach { station ->
            val filter = nodeSet.filter { node -> node.getAttribute<Station>("station").equals(station) }
            val sorted = filter.sortedBy { it.getAttribute<LocalTime>("time")}
            for (i in 0 until sorted.size) {
                if (i + 1 >= sorted.size) {
                    break
                }
                val first = sorted.get(i)
                val second = sorted.get(i + 1)
                val waiting : Edge = graph.addEdge(first.id + "-"+ second.id, first, second, true)
                val between = ChronoUnit.MINUTES.between(first.getAttribute("time"), second.getAttribute("time")).toDouble()
                waiting.setAttribute("weight", between)
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
    fun getDistance(from: Train, fromWagonNumber: Int, to: Train, toWagonNumber: Int, station: Station): Long {
        var result  = 0L
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
                result = (abs(fromTrackPosition - toTrackPosition) + 1L)
            }else{
                // from wagon to exit
                result = (Math.abs(fromTrackPosition - fromTrack.access) + 1L)
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
    fun getDistance(train: Train, wagon: Int, station: Station): Long {
        var result = 0L
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