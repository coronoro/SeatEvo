import evo.SeatEvo
import evo.mutation.ChangeWagonMutation
import evo.recombination.TravelerCrossOver
import evo.selectors.InverseFitnessProportionalSelector
import json.JsonDataLoader
import model.TrainNetwork
import model.Traveler
import util.RandomDataUtil
import java.io.File

fun main(args: Array<String>) {
    loadMinimumExample()
    /*
    val stations = mutableListOf<Station>()
    val trains = mutableListOf<Train>()
    val locations = FreePlanApi.findLocations("Leipzig")
    var routeID = 0
    stations.addAll(FreePlanConverter.convertAll(locations))

    locations.forEach{location ->
        val arrivalBoards = FreePlanApi.getArrivalBoard(location)
        arrivalBoards.forEach{board ->
            val trainId = StringUtil.stripNonDigits(board.name).toInt()
            val route = mutableListOf<Pair<Station, Track>>()
            val stops = FreePlanApi.getJourney(board)
            stops.sortedBy { it.depTime }
            stops.forEach { stop ->
                val filter = stations.filter { station -> station.id == stop.stopId }
                var station: Station
                if (filter.isEmpty()){
                    val location = FreePlanApi.findLocation(stop.stopName, stop.stopId)
                    if (location == null){
                        throw Exception("no location found for: " + stop.stopName)
                    }
                    station = FreePlanConverter.convert(location)
                    stations.add(station)
                }else{
                    station = filter.get(0)
                }
                route.add(Pair(station,Track(-1)))
            }
            val trainRoute = TrainRoute(route,routeID)
            routeID++
            trains.add(Train(emptyList(), trainRoute, trainId))
        }
    }

    val klaxon = Klaxon()
    val json = klaxon.toJsonString(stations)
    save( klaxon.toJsonString(stations),"/stations/freeplan_stations.json")
    save( klaxon.toJsonString(trains),"/stations/freeplan_trains.json")

*/

/*
    val timeTable = JsonDataLoader.loadTimeTable()
    val maximum = timeTable.getFlowMaximum()
    val travelers = mutableListOf<Traveler>()

    var seed = Random.Default
    val cityCount = timeTable.stations.size

    for (i in 0 .. maximum){
        val start = seed.nextInt(0, cityCount)
        var end : Int
        do {
            end = seed.nextInt(0, cityCount)
        }while (start == end)

    }

    println(timeTable)
    //val seatEvo = SeatEvo(timeTable)
*/

}


fun loadMinimumExample() {
    val timeTables = JsonDataLoader.loadTimeTables()
    val trainNetwork = TrainNetwork(timeTables)

    val travelers = RandomDataUtil.generateTravelers(trainNetwork, 3)
    travelers.forEach {
        printTraveler(it)
    }
    var popSize = 8
    val genetic = SeatEvo(
        trainNetwork,
        travelers,
        popSize,
        InverseFitnessProportionalSelector(popSize),
        TravelerCrossOver(0.6),
        ChangeWagonMutation(0.85)
    )
    val result = genetic.evolution(20)

    println("==== result ====")
    println(result.toString() + "fitness: "+ result.fitness)


    /*

    val cityCount = timeTable.stations.size

    for (i in 0 .. maximum){
        val start = seed.nextInt(0, cityCount)
        var end : Int
        do {
            end = seed.nextInt(0, cityCount)
        }while (start == end)

    }

    println(timeTable)
    //val seatEvo = SeatEvo(timeTable, )

     */
}

fun printTraveler(traveler: Traveler){
    println("RouteId:" +traveler.route.id)
    traveler.route.waypoints.forEach {
        println("\t"+ it)
    }

}

fun save(data: String, resourcePath: String) {
    val resource = JsonDataLoader::class.java.getResource(resourcePath)
    if (resource != null) {
        File(resource.toURI()).writeText(data)
    }
}

