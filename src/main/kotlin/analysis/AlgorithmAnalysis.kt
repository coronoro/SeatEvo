package analysis

import data.DataGenerator
import evo.SeatEvo
import evo.mutation.ChangeWagonMutation
import evo.recombination.WagonCrossOver
import evo.selectors.TournamentSelector
import json.JsonDataLoader
import model.TrainNetwork
import util.RandomDataUtil

object AlgorithmAnalysis {


    fun geneticAnalysis(){
        var stations = 3;
        println("Stations: " + stations)
        var travelerAmount = 120

        //DataGenerator.generateStairNetwork(stations, false, 1, 5, 4)
        DataGenerator.generateGridNetwork(Pair(stations,stations), 1, 1, 5)
        val timeTables = JsonDataLoader.loadTimeTables(true)
        val trainNetwork = TrainNetwork(timeTables)
        val travelers = RandomDataUtil.generateTravelers(trainNetwork, travelerAmount)
        for (i in 1.. 10 step 1){
            val popSize = 300
            val cycles = 100
            val genetic = SeatEvo(
                trainNetwork,
                travelers,
                popSize,
                cycles,
//            InverseFitnessProportionalSelector(popSize),
//            InverseStochasticUniversalSampling(popSize),
                TournamentSelector(4),
                //TravelerCrossOver(i/10.0),
                WagonCrossOver(i/10.0),
                ChangeWagonMutation(0.4)
//        WagonSwapMutation(0.9)
            )
            genetic.logging = false
            val result = genetic.evolution()
            genetic.analysis.showChart("InvFitProp" +i)
            genetic.analysis.setVisible(true);
        }
    }

}