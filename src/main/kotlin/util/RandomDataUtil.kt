package util

import graph.WeightedDataEdge
import model.Station
import model.Train
import model.TrainNetwork
import model.Traveler
import model.route.RouteItem
import model.route.TravelerRoute
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import java.time.LocalTime

object RandomDataUtil {

    fun generateTravelers(network: TrainNetwork, amount: Int): List<Traveler> {
        val result = mutableListOf<Traveler>()

        val vertexSet = network.g.vertexSet()
        val stations = network.stations.toList()

        for (i in 0..amount) {
            //get a valid start point
            var startStationIndex: Int
            var startVertices: List<Pair<Station, LocalTime>>
            do {
                startStationIndex = RandomUtil.seed.nextInt(0, stations.size - 1)
                val startStation = stations.get(startStationIndex)
                val vertices = vertexSet.filter { it -> it.first == startStation }
                // get only vertices with an edge that leads to another station as a target
                // might not be relevant in a real world szenario
                startVertices = vertices.filter { it ->
                    var filter = false
                    val outgoing = network.g.outgoingEdgesOf(it)
                    outgoing.forEach { edge ->
                        val vertex = network.g.getEdgeTarget(edge)
                        if (vertex.first != startStation) {
                            filter = true
                        }
                    }
                    filter
                }
            } while (startVertices.isEmpty())

            //TODO might be better to just get a random time?
            val vertexIndex = RandomUtil.seed.nextInt(0, startVertices.size - 1)
            val startVertex = startVertices.get(vertexIndex)

            //get target of the journey
            var endStationIndex: Int
            var path: List<WeightedDataEdge>? = null
            do {
                endStationIndex = RandomUtil.seed.nextInt(0, stations.size - 1)
                if (startStationIndex != endStationIndex) {

                    var pathWeight = Double.MAX_VALUE
                    val endVertices = vertexSet.filter { it -> it.first == stations.get(endStationIndex) }
                    endVertices.forEach { vertex ->
                        val graphData = DijkstraShortestPath.findPathBetween(network.g, startVertex, vertex);
                        if (graphData != null) {
                            val weight =
                                graphData.edgeList.fold(0.0, { acc, edge -> acc + network.g.getEdgeWeight(edge) })
                            if (weight < pathWeight) {
                                path = graphData.edgeList
                                pathWeight = weight
                            }
                        }
                    }
                }
            } while (startStationIndex == endStationIndex || path == null)

            val routeList = mutableListOf<RouteItem>()
            var previousTrain: Train? = null
            path?.forEach { piece ->
                val data = piece.data
                if (data != null && data.train != previousTrain) {
                    val from = network.g.getEdgeSource(piece)
                    previousTrain = data.train
                    val item = RouteItem(from.first, from.second, data.train, -1)
                    routeList.add(item)
                }
            }
            val last = path?.last()
            val data = last?.data
            val from = network.g.getEdgeTarget(last)
            previousTrain = data?.train
            val item = RouteItem(from.first, from.second, data?.train!!, -1)
            routeList.add(item)

            val route = TravelerRoute(routeList, i)
            val traveler = Traveler(route)
            result.add(traveler)

        }
        return result
    }


}