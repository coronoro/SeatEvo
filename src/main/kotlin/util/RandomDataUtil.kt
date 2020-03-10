package util

import model.Station
import model.Train
import model.TrainNetwork
import model.Traveler
import model.route.RouteItem
import model.route.TravelerRoute
import model.timetable.TimeTable
import org.graphstream.graph.Edge
import org.graphstream.graph.Node
import java.time.LocalTime
import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Path

object RandomDataUtil {

    fun generateTravelers(network: TrainNetwork, amount: Int): List<Traveler> {
        val result = mutableListOf<Traveler>()

        val nodeSet = network.graph.getNodeSet<Node>()
        val stations = network.stations.toList()
        val dijkstra = Dijkstra(Dijkstra.Element.EDGE, null, "weight");
        dijkstra.init(network.graph);

        for (i in 0 until amount) {
            //get a valid start point
            var startStationIndex: Int
            var startVertices: List<Node>
            do {
                startStationIndex = RandomUtil.seed.nextInt(0, stations.size)
                val startStation = stations.get(startStationIndex)
                val nodes = nodeSet.filter { node -> node.getAttribute<Station>("station") == startStation }
                // get only vertices with an edge that leads to another station as a target
                // might not be relevant in a real world szenario
                startVertices = nodes.filter { it ->
                    var filter = false
                    val outgoing = it.getEachLeavingEdge<Edge>()
                    outgoing.forEach { edge ->
                        val vertex = edge.getTargetNode<Node>()
                        if (vertex.getAttribute<Station>("station") != startStation) {
                            filter = true
                        }
                    }
                    filter
                }
            } while (startVertices.isEmpty())

            //TODO might be better to just get a random time?
            val NodeIndex = RandomUtil.seed.nextInt(0, startVertices.size)
            val startNode = startVertices.get(NodeIndex)

            //get target of the journey
            var endStationIndex: Int
            var path: Path? = null
            dijkstra.setSource(startNode)
            dijkstra.compute()
            do {
                endStationIndex = RandomUtil.seed.nextInt(0, stations.size)
                if (startStationIndex != endStationIndex) {

                    var pathWeight = Double.MAX_VALUE
                    val endNodes = nodeSet.filter { node -> node.getAttribute<Station>("station") == stations.get(endStationIndex) }
                    endNodes.forEach {  endNode ->
                        val dijkstraPath = dijkstra.getPath(endNode)
                        if (dijkstraPath != null && !dijkstraPath.empty()){
                            val weight = dijkstraPath.getPathWeight("weight")
                            if (weight < pathWeight) {
                                path = dijkstraPath
                                pathWeight = weight
                            }
                        }
                    }
                }
            } while (startStationIndex == endStationIndex || path == null)

            val routeList = mutableListOf<RouteItem>()
            var previousTrain: Train? = null
            val pathEdges = path?.getEachEdge<Edge>()
            pathEdges!!.forEach { edge ->
                val data = edge.getAttribute<TimeTable>("timetable")
                //no waypoint if the traveler waits
                if(data != null){
                    // traveler get into a new train -> new routeitem
                    if (data.train != previousTrain){
                        val fromNode = edge.getSourceNode<Node>()
                        val fromStation = fromNode.getAttribute<Station>("station")
                        val fromTime = fromNode.getAttribute<LocalTime>("time")
                        val toNode = edge.getTargetNode<Node>()
                        val toStation = toNode.getAttribute<Station>("station")
                        val toTime = toNode.getAttribute<LocalTime>("time")
                        previousTrain = data.train
                        val item = RouteItem(fromStation, fromTime, toStation, toTime, data.train, -1)
                        routeList.add(item)
                    }else{
                        //traveler still takes the same train so just update the old routeitem
                        val toNode = edge.getTargetNode<Node>()
                        val last = routeList.last()
                        last.toStation = toNode.getAttribute<Station>("station")
                        last.toTime = toNode.getAttribute<LocalTime>("time")
                    }
                }
            }
            if (routeList.isEmpty()){
                println("start" + startNode)
                println("end" + startNode)
                throw Exception("no empty routes allowed")

            }

            val route = TravelerRoute(routeList, i.toString())
            val traveler = Traveler(route)
            result.add(traveler)
        }
        return result
    }


}