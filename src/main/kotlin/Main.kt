import evo.SeatEvo
import evo.mutation.ChangeWagonMutation
import evo.recombination.TravelerCrossOver
import evo.selectors.InverseStochasticUniversalSampling
import json.JsonDataLoader
import model.TrainNetwork
import model.Traveler
import util.RandomDataUtil
import java.io.File

fun main(args: Array<String>) {
    //MarudorLoader.loadICE()
    loadMinimumExample()

}


fun loadMinimumExample() {
    val timeTables = JsonDataLoader.loadTimeTables()
    val trainNetwork = TrainNetwork(timeTables)
    val travelers = RandomDataUtil.generateTravelers(trainNetwork, 60)

    val popSize = 400
    val cycles = 600
    val genetic = SeatEvo(
        trainNetwork,
        travelers,
        popSize,
        cycles,
        //InverseFitnessProportionalSelector(popSize),
        InverseStochasticUniversalSampling(popSize),
        TravelerCrossOver(0.75),
        ChangeWagonMutation(0.8)
    )
    genetic.logging = false
    val result = genetic.evolution()

    println("==== result ====")
    println(result.toString() + "fitness: " + result.fitness)
    genetic.printConfig()

    println("==== travelers ====")

    genetic.analysis.showChart()
    genetic.analysis.setVisible(true);

    travelers.forEach {
        printTraveler(it)
    }

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

