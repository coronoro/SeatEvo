package analysis

import data.DataGenerator
import json.JsonDataLoader
import model.TrainNetwork
import model.Traveler
import model.timetable.TimeTable
import org.graphstream.graph.Edge
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.MultiGraph
import org.graphstream.graph.implementations.SingleGraph
import util.RandomDataUtil

object TravelerAnalysis {

    fun analyzeTravelers(travelers: List<Traveler>, print:Boolean = true): HashMap<Int, Int> {
        val hashmap = HashMap<Int,Int>()
        travelers.forEach { traveler ->
            val size = traveler.route.waypoints.size - 1
            var amount = hashmap.get(size)
            if (amount == null)
                amount = 0
            amount++
            hashmap.set(size, amount)
        }
        if (print){
            hashmap.entries.forEach {
                println(it.key.toString() + ": " + it.value)
            }
        }
        return hashmap
    }

    fun analyzeTravelerWay(travelers: List<Traveler>, print:Boolean = true): HashMap<String, Int> {
        val hashmap = HashMap<String,Int>()
        travelers.forEach { traveler ->
            traveler.route.waypoints.forEach {
                val key = it.fromStation.name +"-"+ it.toStation.name
                var amount = hashmap.get(key)
                if (amount == null)
                    amount = 0
                amount++
                hashmap.set(key, amount)
            }
        }
        if (print){
            hashmap.entries.forEach {
                println(it.key.toString() + ": " + it.value)
            }
        }
        return hashmap
    }

    fun analyseMarudorTravelers(){

        val timeTables = JsonDataLoader.fillTimeTables(true, "marudor-")

        val ttMap = HashMap<String, TimeTable>()
        timeTables.forEach { tt ->
            var entry = ttMap.get(tt.train.id)
            if (entry == null){
                entry = tt
            }
            ttMap.put(tt.train.id, entry)
        }

        val trainNetwork = TrainNetwork(ttMap.values.toList())

        val rounds = 100

        for (travelerAmount in 4000..5000 step 1000) {
            val averageHashmap = HashMap<Int, Double>()
            val averageWayMap = HashMap<String, Double>()
            for (i in 0 until rounds) {
                //println("========================== #"+i)
                val travelers = RandomDataUtil.generateTravelers(trainNetwork, travelerAmount)
                val analyzeTravelers = analyzeTravelers(travelers, false)
                analyzeTravelers.entries.forEach {
                    var get = averageHashmap.get(it.key)
                    if (get == null) {
                        get = 0.0
                    }
                    get = it.value + get ?: 0.0
                    averageHashmap.put(it.key, get)
                }
                val wayMap = analyzeTravelerWay(travelers, false)
                wayMap.entries.forEach {
                    var get = wayMap.get(it.key)
                    if (get == null) {
                        get = 0
                    }
                    get = it.value + get!!
                    wayMap.put(it.key, get!!)
                }
            }

            print(travelerAmount.toString() + "\t")
            averageHashmap.keys.sorted().forEach { key ->
                var get = averageHashmap.get(key)
                val average = (get ?: 0.0) / rounds
                print(average.toString() + "\t")
            }
            println()

            println("max: " + averageHashmap.keys.sorted().last())
            averageWayMap.entries.forEach {
                val average = it.value / rounds
                println(it.key + "\t" + average)
            }
        }
    }

    fun analyseTravelerGen(){
        val arrayOf = arrayOf(20)
        for (i in 0 until arrayOf.size) {
            var f = arrayOf[i]
            //for (f in 2 .. 25) {
            //for (f in 3 .. 25) {
            println("frequency: " +f)
            //DataGenerator.generateStairNetwork(f, false, 1, 5, 4)
            DataGenerator.generateGridNetwork(Pair(f,f), 1, 1, 5)
            val timeTables = JsonDataLoader.loadTimeTables(true)
            val trainNetwork = TrainNetwork(timeTables)

            val rounds = 2

            for (travelerAmount in 4000..5000 step 1000) {
                val averageHashmap = HashMap<Int, Double>()
                val averageWayMap = HashMap<String, Double>()
                for (i in 0 until rounds) {
                    //println("========================== #"+i)
                    val travelers = RandomDataUtil.generateTravelers(trainNetwork, travelerAmount)
                    val analyzeTravelers = analyzeTravelers(travelers, false)
                    analyzeTravelers.entries.forEach {
                        var get = averageHashmap.get(it.key)
                        if (get == null) {
                            get = 0.0
                        }
                        get = it.value + get ?: 0.0
                        averageHashmap.put(it.key, get)
                    }
                    val wayMap = analyzeTravelerWay(travelers, false)
                    wayMap.entries.forEach {
                        var get = wayMap.get(it.key)
                        if (get == null) {
                            get = 0
                        }
                        get = it.value + get!!
                        wayMap.put(it.key, get!!)
                    }
                }

                print(travelerAmount.toString() + "\t")
                averageHashmap.keys.sorted().forEach { key ->
                    var get = averageHashmap.get(key)
                    val average = (get ?: 0.0) / rounds
                    print(average.toString() + "\t")
                }
                println()

                println("max: " + averageHashmap.keys.sorted().last())
                averageWayMap.entries.forEach {
                    val average = it.value / rounds
                    println(it.key + "\t" + average)
                }

            }
        }
    }

    /**
     * shows the percentage of the travelers in the graph
     */
    fun analyseGridTravelerGraph(gridSize: Int, traveleramount:Int){
        DataGenerator.generateGridNetwork(Pair(gridSize,gridSize), 1, 1, 5)
        val timeTables = JsonDataLoader.loadTimeTables(true)
        val trainNetwork = TrainNetwork(timeTables)
        //trainNetwork.graph.display(false)
        val rounds = 10000
        val graph = SingleGraph("Gridsize" + gridSize)
        trainNetwork.stations.forEach{
            val node = graph.addNode<Node>(it.name)
            node.addAttribute("ui.label", it.name)
            val idNumber = it.id.toInt()

            var x = (idNumber % gridSize).toDouble()
            var y = (idNumber / gridSize).toDouble()
            //verschiebe y
            var yTemp = 0.0
            if (x > 1){
                yTemp = -0.25* Math.pow(x,2.0)+ 0.25*x
            }
            //verschiebe  x
            var xTemp = 0.0
            if (y > 1){
                xTemp = -0.25* Math.pow(y,2.0)+ 0.25*y
            }
            x = x + xTemp
            y = y + yTemp
            node.addAttribute("xyz", x, y, 0)
            node.addAttribute("ui.style", "text-alignment: above; text-background-mode:plain;text-size:25px;size:25px;")

        }

        val averageWayMap = HashMap<String, Double>()
        for (i in 0 until rounds) {
            //println("========================== #"+i)
            val travelers = RandomDataUtil.generateTravelers(trainNetwork, traveleramount)
            travelers.forEach { traveler ->
                traveler.route.waypoints.forEach {
                    val from = graph.getNode<Node>(it.fromStation.name)
                    val to = graph.getNode<Node>(it.toStation.name)
                    val edgeID = from.id + "-" + to.id
                    var edge = graph.getEdge<Edge>(edgeID)
                    if (edge == null){
                        edge = graph.addEdge<Edge>(edgeID, from, to, true)
                        edge.addAttribute("count", 0)
                    }
                    var weight = edge.getAttribute<Int>("count")
                    weight = weight+1
                    edge.addAttribute("count", weight)
                }
            }
        }
        graph.getEdgeSet<Edge>().forEach {edge ->
            val count = edge.getAttribute<Int>("count")
            val average = count / rounds.toDouble()
            val percentage = average * 100 / traveleramount

            edge.addAttribute("ui.label", "%.2f".format(percentage) + "%");
            edge.addAttribute("ui.style", "text-background-mode:plain;text-size:25px;")

        }
        graph.addAttribute("ui.quality", 4)
        graph.addAttribute("ui.antialias")

        val display = graph.display(false)
        display.enableXYZfeedback(false)
        //display.disableAutoLayout()

    }

}