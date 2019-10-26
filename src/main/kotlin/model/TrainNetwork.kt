package model

import graph.WeightedDataEdge
import model.timetable.TimeTable
import org.jgrapht.graph.DefaultDirectedWeightedGraph
import util.GraphUtil
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class TrainNetwork(timeTables: List<TimeTable>) {

    var g = DefaultDirectedWeightedGraph<Pair<Station,LocalTime>, WeightedDataEdge>(WeightedDataEdge::class.java)

    val stations: HashSet<Station>

    init {
        timeTables.forEach { timetable ->
            timetable.departures.forEach { departure ->
                for (i in 0 until timetable.stops.size) {
                    val first = timetable.stops.get(i)
                    if(i+1 >= timetable.stops.size){
                        break;
                    }
                    val second = timetable.stops.get(i + 1)

                    if (first.departure != null && second.arrival != null){
                        val departureTime = departure.plus(first.departure)
                        var vertex1 = Pair(first.station, departureTime)
                        g.addVertex(vertex1)

                        val arrivalTime = departure.plus(second.arrival)
                        var vertex2 = Pair(second.station, arrivalTime)
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
        stations = vertexSet.mapTo(HashSet<Station>()) { vertex -> vertex.first }

        //add edges for waiting
        addWaitingEdges()
        GraphUtil.visualize(g)
    }


    fun addWaitingEdges(){
        val vertexSet = g.vertexSet()
        val stations = vertexSet.mapTo(HashSet<Station>()) { vertex -> vertex.first }
        stations.forEach { station ->
            val filter = vertexSet.filter { vertex -> vertex.first == station }
            val sorted = filter.sortedBy { it.second }
            for (i in 0 until sorted.size){
                if (i + 1 >= sorted.size){
                    break
                }
                val first = sorted.get(i)
                val second = sorted.get(i+1)
                val waiting = g.addEdge(first, second)
                val between = ChronoUnit.MINUTES.between( first.second, second.second)
                g.setEdgeWeight(waiting,between.toDouble())
            }
        }
    }

}