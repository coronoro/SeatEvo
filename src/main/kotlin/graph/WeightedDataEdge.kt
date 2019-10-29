package graph

import model.timetable.TimeTable
import org.jgrapht.graph.DefaultWeightedEdge

//TODO should be generic but there seems to be an issue with the graph
class WeightedDataEdge(var data: TimeTable? = null) : DefaultWeightedEdge() {


}