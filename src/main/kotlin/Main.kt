import data.DataGenerator
import data.MarudorLoader
import evo.SeatEvo
import evo.mutation.ChangeWagonMutation
import evo.mutation.WagonSwapMutation
import evo.recombination.TravelerCrossOver
import evo.selectors.InverseStochasticUniversalSampling
import evo.selectors.TournamentSelector
import json.JsonDataLoader
import model.TrainNetwork
import model.Traveler
import util.RandomDataUtil
import java.io.File

fun main(args: Array<String>) {
    //MarudorLoader.loadICE()
    DataGenerator.generateStairNetwork(12, false, 24, 5, 6)
    //loadMinimumExample()
    //analyseProblemDifficulty()
    //analyseTravelerGen()
    analyseTrainStationLinaerity()
}

fun compareSelection(){
    val timeTables = JsonDataLoader.loadTimeTables(true)
    val trainNetwork = TrainNetwork(timeTables)
    val travelers = RandomDataUtil.generateTravelers(trainNetwork, 700)
}


fun analyseProblemDifficulty(){

    val popSize = 300
    val cycles = 200
    val travelerAmount = 500
    for (i in 2 .. 6){
        DataGenerator.generateStairNetwork(i, false, 6,5,4)
        val timeTables = JsonDataLoader.loadTimeTables(true)
        val trainNetwork = TrainNetwork(timeTables)
        val travelers = RandomDataUtil.generateTravelers(trainNetwork, travelerAmount)
        val genetic = SeatEvo(
            trainNetwork,
            travelers,
            popSize,
            cycles,
            //InverseFitnessProportionalSelector(popSize),
//            InverseStochasticUniversalSampling(popSize),
            TournamentSelector(2),
            TravelerCrossOver(0.5),
            ChangeWagonMutation(0.9)
        )
        genetic.logging = false
        val result = genetic.evolution()
        println("==== result ====")
        println(result.toString() + "fitness: " + result.fitness)
        genetic.printConfig()
        genetic.analysis.showChart("Stair#" + i)
        genetic.analysis.setVisible(true);
    }

}


fun analyseTravelerGen(){
    //for (f in 5 .. 25 step 5) {
    for (f in 0 .. 5) {
        println("frequency: " +f)
        //DataGenerator.generateStairNetwork(11, false, f, 5, 4)
        DataGenerator.generateStairNetwork(f, false, (3+2*f), 5, 4)
        val timeTables = JsonDataLoader.loadTimeTables(true)
        val trainNetwork = TrainNetwork(timeTables)
        val rounds = 100

        for (travelerAmount in 50..500 step 50) {
            var averageHashmap = HashMap<Int, Int>()
            for (i in 0 until rounds) {
                //println("========================== #"+i)
                val travelers = RandomDataUtil.generateTravelers(trainNetwork, travelerAmount)
                val analyzeTravelers = analyzeTravelers(travelers, false)
                analyzeTravelers.entries.forEach {
                    var get = averageHashmap.get(it.key)
                    if (get == null) {
                        get = 0
                    }
                    get = it.value + get ?: 0
                    averageHashmap.put(it.key, get)
                }
            }
            print(travelerAmount.toString() + "\t")
            averageHashmap.keys.sorted().forEach { key ->
                var get = averageHashmap.get(key)
                val average = (get ?: 0) / rounds
                print(average.toString() + "\t")
            }
            println()
        }
    }
}

fun analyseTrainStationLinaerity(){
    //for (f in 5 .. 25 step 5) {
    for (f in 0 .. 10) {
        println("frequency: " +f)
        //DataGenerator.generateStairNetwork(11, false, f, 5, 4)
        DataGenerator.generateStairNetwork(f, false, (2+2*f), 5, 4)
        val timeTables = JsonDataLoader.loadTimeTables(true)
        val trainNetwork = TrainNetwork(timeTables)
        val rounds = 50
        for (travelerAmount in 150..300 step 50) {
            var averageHashmap = HashMap<Int, Int>()
            for (i in 0 until rounds) {
                //println("========================== #"+i)
                val travelers = RandomDataUtil.generateTravelers(trainNetwork, travelerAmount)
                val analyzeTravelers = analyzeTravelers(travelers, false)
                analyzeTravelers.entries.forEach {
                    var get = averageHashmap.get(it.key)
                    if (get == null) {
                        get = 0
                    }
                    get = it.value + get ?: 0
                    averageHashmap.put(it.key, get)
                }
            }
            print(travelerAmount.toString() + "\t")
            averageHashmap.keys.sorted().forEach { key ->
                var get = averageHashmap.get(key)
                val average = (get ?: 0) / rounds
                print(average.toString() + "\t")
            }
            println()
        }
    }
}


fun loadMinimumExample() {
    val timeTables = JsonDataLoader.loadTimeTables(true)
    val trainNetwork = TrainNetwork(timeTables)
    val travelers = RandomDataUtil.generateTravelers(trainNetwork, 700)

    val popSize = 500
    val cycles = 200
    val genetic = SeatEvo(
        trainNetwork,
        travelers,
        popSize,
        cycles,
        //InverseFitnessProportionalSelector(popSize),
        InverseStochasticUniversalSampling(popSize),
//        TournamentSelector(2),
        TravelerCrossOver(0.5),
      ChangeWagonMutation(0.9)
//        WagonSwapMutation(0.9)
    )
    genetic.logging = false
    val result = genetic.evolution()

    println("==== result ====")
    println(result.toString() + "fitness: " + result.fitness)
    genetic.printConfig()

    println("==== travelers ====")

    genetic.analysis.showChart()
    genetic.analysis.setVisible(true);

    /*
    travelers.forEach {
        printTraveler(it)
    }
    */
    analyzeTravelers(travelers)
}

fun analyzeTravelers(travelers: List<Traveler>, print:Boolean = true): HashMap<Int, Int> {
    val hashmap = HashMap<Int,Int>()
    travelers.forEach { traveler ->
        val size = traveler.route.waypoints.size
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

