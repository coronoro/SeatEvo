import analysis.TravelerAnalysis
import data.DataGenerator
import data.MarudorLoader
import evo.SeatEvo
import evo.mutation.ChangeWagonMutation
import evo.mutation.WagonSwapMutation
import evo.recombination.TravelerCrossOver
import evo.recombination.WagonCrossOver
import evo.selectors.InverseStochasticUniversalSampling
import evo.selectors.TournamentSelector
import json.JsonDataLoader
import json.JsonDataWriter
import json.JsonTimeTable
import marudor.MarudorApi
import model.Train
import model.TrainNetwork
import model.Traveler
import util.RandomDataUtil
import java.io.File

fun main(args: Array<String>) {
    System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

    //TravelerAnalysis.analyseTravelerGen()
    //TravelerAnalysis.analyseGridTravelerGraph(3, 4000)

    //load data from marudor
    //MarudorLoader.loadICE()
    //MarudorLoader.loadAllStationTracks()
    //MarudorLoader.loadICETimeTables()

    repairTracks()
    //trainSet()

    //analyse genetic algorithm
    //loadSnap()


    //loadMinimumExample()
    //analyseProblemDifficulty()
    //analyseTrainStationLinaerity()
    //analyseGridNetwork()
    //geneticAnalysis()

}

fun trainSet(){
    val loadTrains = JsonDataLoader.loadTrains(true, "marudor-")
    val map = HashMap<String, Train>()
    loadTrains.forEach { train ->
        var get = map.get(train.id)
        if (get == null){
            get = train
            map.put(get.id, get)
        }
    }
    JsonDataWriter.writeTrains(map.values.toList())
}

fun repairTracks(){
    val jsonTimetables = JsonDataLoader.repairTracks(true,"marudor-")
    JsonDataWriter.writeJSonTimeTables(jsonTimetables)
    loadSnap()
}


fun loadSnap(){
    val timeTables = JsonDataLoader.loadTimeTables(true,"marudor-")
    val trainNetwork = TrainNetwork(timeTables)
}



fun analyseGridNetwork(){
    DataGenerator.generateGridNetwork(Pair(3,3),1,8,5)
    val timeTables = JsonDataLoader.loadTimeTables(true)
    val trainNetwork = TrainNetwork(timeTables)
    val travelers = RandomDataUtil.generateTravelers(trainNetwork, 120)

    val popSize = 300
    val cycles = 100
    val genetic = SeatEvo(
        trainNetwork,
        travelers,
        popSize,
        cycles,
        //InverseFitnessProportionalSelector(popSize),
        InverseStochasticUniversalSampling(popSize),
//        TournamentSelector(4),
        TravelerCrossOver(0.4),
      ChangeWagonMutation(0.8)
//        WagonSwapMutation(0.9)
    )
    genetic.logging = false
    val result = genetic.evolution()

    println("==== result ====")
    println(result.toString())
    println(genetic.evaluate(result))
    genetic.printConfig()

    println("==== travelers ====")

    genetic.analysis.showChart()
    genetic.analysis.setVisible(true);

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




fun loadMinimumExample() {
    val timeTables = JsonDataLoader.loadTimeTables(false)
    val trainNetwork = TrainNetwork(timeTables)
    val display = trainNetwork.graph.display()
    display.disableAutoLayout()
    val travelers = RandomDataUtil.generateTravelers(trainNetwork, 900)

    val popSize = 2000
    val cycles = 200
    val genetic = SeatEvo(
        trainNetwork,
        travelers,
        popSize,
        cycles,
        //InverseFitnessProportionalSelector(popSize),
//        InverseStochasticUniversalSampling(popSize),
        TournamentSelector(4),
        TravelerCrossOver(0.5),
//      ChangeWagonMutation(0.9)
        WagonSwapMutation(0.9)
    )
    genetic.logging = false
    val result = genetic.evolution()

    println("==== result ====")
    println(result.toString() + "fitness: " + result.fitness)
    genetic.printConfig()

    println("==== travelers ====")

    genetic.analysis.showChart()
    genetic.analysis.setVisible(true);


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

