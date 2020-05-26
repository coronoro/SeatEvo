package analysis

import data.DataGenerator
import json.JsonDataLoader
import model.TrainNetwork
import model.Traveler
import model.timetable.TimeTable
import org.graphstream.graph.Edge
import org.graphstream.graph.Node
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

        val timeTables = JsonDataLoader.fillTimeTables(true)

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
                    get = it.value + get
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
                        get = it.value + get
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
                    val get = averageHashmap.get(key)
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
    fun analyseMarudorTravelerGraph(traveleramount:Int){
        val timeTables = JsonDataLoader.fillTimeTables(true)

        val ttMap = HashMap<String, TimeTable>()
        timeTables.forEach { tt ->
            var entry = ttMap.get(tt.train.id)
            if (entry == null){
                entry = tt
            }
            ttMap.put(tt.train.id, entry)
        }
        println("done")
        val trainNetwork = TrainNetwork(ttMap.values.toList())
        //trainNetwork.graph.display(false)

        val graph = SingleGraph("Gridsize")
        trainNetwork.stations.forEach{
            val node = graph.addNode<Node>(it.name)
            node.addAttribute("ui.label", it.name)
            val idNumber = it.id.toInt()
        }

        var routeCount = 0.0
        val averageWayMap = HashMap<String, Double>()
        var travelerRouteCount = 0.0

        val rounds = 10
        for (i in 0 until rounds) {
            println("round " +i +" of " + rounds )
            //println("========================== #"+i)
            val travelers = RandomDataUtil.generateTravelers(trainNetwork, traveleramount)
            travelers.forEach { traveler ->
                travelerRouteCount += traveler.route.waypoints.size - 1
                traveler.route.waypoints.forEach {
                    routeCount++
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
        println(travelerRouteCount/(rounds*traveleramount))

        var max = 0.0
        var min = Double.MAX_VALUE

        graph.getEdgeSet<Edge>().forEach {edge ->
            val count = edge.getAttribute<Int>("count")
            val percentage = count * 100 / routeCount
            if (percentage < min )
                min = percentage
            if (max < percentage)
                max = percentage
//            edge.addAttribute("ui.label", "%.2f".format(percentage) + "%")
//            edge.addAttribute("ui.style", "text-background-mode:plain;text-size:25px;")

        }
        println(max)
        println(min)

//        graph.addAttribute("ui.quality", 4)
//        graph.addAttribute("ui.antialias")

//        val display = graph.display(false)
//        display.enableXYZfeedback(false)
        //display.disableAutoLayout()
    }


    /**
     * shows the percentage of the travelers in the graph
     */
    fun analyseGridTravelerGraph(gridSize: Int, traveleramount:Int){
        DataGenerator.generateGridNetwork(Pair(gridSize,gridSize), 1, 1, 5)
        val timeTables = JsonDataLoader.loadTimeTables(true)
        val trainNetwork = TrainNetwork(timeTables)
        //trainNetwork.graph.display(false)

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

        var routeCount = 0.0
        val averageWayMap = HashMap<String, Double>()
        var travelerRouteCount = 0.0

        val rounds = 1000
        for (i in 0 until rounds) {
            //println("========================== #"+i)
            val travelers = RandomDataUtil.generateTravelers(trainNetwork, traveleramount)
            travelers.forEach { traveler ->
                travelerRouteCount += traveler.route.waypoints.size
                traveler.route.waypoints.forEach {
                    routeCount++
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
        println(travelerRouteCount/(rounds*traveleramount))


        var max = 0.0
        var min = Double.MAX_VALUE

        graph.getEdgeSet<Edge>().forEach {edge ->
            val count = edge.getAttribute<Int>("count")
            val percentage = count * 100 / routeCount
            if (percentage < min )
                min = percentage
            if (max < percentage)
                max = percentage
            edge.addAttribute("ui.label", "%.2f".format(percentage) + "%")
            edge.addAttribute("ui.style", "text-background-mode:plain;text-size:25px;")

        }
        graph.addAttribute("ui.quality", 4)
        graph.addAttribute("ui.antialias")
        println(max)
        println(min)
        val display = graph.display(false)
        display.enableXYZfeedback(false)
        //display.disableAutoLayout()
    }

    /**
     * shows the percentage of the travelers in the graph
     */
    fun analyseStairTravelerGraph(stations: Int, traveleramount:Int){
        DataGenerator.generateStairNetwork(stations, false, 1,5,5)
        val timeTables = JsonDataLoader.loadTimeTables(true)
        val trainNetwork = TrainNetwork(timeTables)
        //trainNetwork.graph.display(false)

        val graph = SingleGraph("Stationen" + stations)

        val xCircleOffset = 10
        val yCircleOffset = 10
        val radius = 5
        val angle = 360.0 / trainNetwork.stations.size


        trainNetwork.stations.sortedBy { it.name }.forEachIndexed { index, station ->
            val node = graph.addNode<Node>(station.name)
            node.addAttribute("ui.label", station.name)
            val idNumber = station.id.toInt()

            val radian = Math.toRadians(index*angle)
            val x = (radius * Math.sin(radian)) + xCircleOffset
            val y = (radius * Math.cos(radian)) + yCircleOffset

            node.addAttribute("xyz", x, y, 0)
            node.addAttribute("ui.style", "text-alignment: above; text-background-mode:plain;text-size:25px;size:25px;")

        }

        val averageWayMap = HashMap<String, Double>()
        var routeCount = 0.0

        var travelerRouteCount = 0.0
        val rounds = 1000
        for (i in 0 until rounds) {
            //println("========================== #"+i)
            val travelers = RandomDataUtil.generateTravelers(trainNetwork, traveleramount)
            travelers.forEach { traveler ->
                travelerRouteCount += traveler.route.waypoints.size
                traveler.route.waypoints.forEach {
                    routeCount++
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

        println(travelerRouteCount/(rounds*traveleramount))
        graph.getEdgeSet<Edge>().forEach {edge ->
            val count = edge.getAttribute<Int>("count")
            val percentage = count * 100 / routeCount

            edge.addAttribute("ui.label", "%.2f".format(percentage) + "%")
            edge.addAttribute("ui.style", "text-background-mode:plain;text-size:25px;")

        }
        graph.addAttribute("ui.quality", 4)
        graph.addAttribute("ui.antialias")

        val display = graph.display(false)
        display.enableXYZfeedback(false)
        //display.disableAutoLayout()

    }


}