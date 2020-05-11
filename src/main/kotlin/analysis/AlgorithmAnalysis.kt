package analysis

import data.DataGenerator
import evo.SeatEvo
import evo.mutation.ChangeWagonMutation
import evo.recombination.TravelerCrossOver
import evo.recombination.WagonCrossOver
import evo.selectors.InverseFitnessProportionalSelector
import evo.selectors.InverseStochasticUniversalSampling
import evo.selectors.TournamentSelector
import json.JsonDataLoader
import model.TrainNetwork
import org.jfree.data.xy.XYSeries
import util.RandomDataUtil

object AlgorithmAnalysis {


    fun stairAnalysis(stations:Int, travelerAmount: Int, wagonNumber:Int, wagonSize:Int){

        DataGenerator.generateStairNetwork(stations, false,1, wagonNumber, wagonSize)
        val timeTables = JsonDataLoader.loadTimeTables(true)
        val trainNetwork = TrainNetwork(timeTables)
        val travelers = RandomDataUtil.generateTravelers(trainNetwork, travelerAmount)

        val repetitions = 100

        val popSize = 300
        val cycles = 100

        val minDataSets = mutableListOf<XYSeries>()
        val maxDataSets = mutableListOf<XYSeries>()
        val averageTravelDistanceSet = mutableListOf<XYSeries>()
        for (i in 1.. repetitions step 1){
            println("repetition: " + i +" of "+ repetitions)
            val genetic = SeatEvo(
                trainNetwork,
                travelers,
                popSize,
                cycles,
              InverseFitnessProportionalSelector(popSize),
//              InverseStochasticUniversalSampling(popSize),
//                TournamentSelector(4),
                TravelerCrossOver(0.7),
                //WagonCrossOver(0.6),
                ChangeWagonMutation(0.8)
//              WagonSwapMutation(0.9)
            )
            genetic.logging = false
            val result = genetic.evolution()
            minDataSets.add(genetic.analysis.minDataSet)
            maxDataSets.add(genetic.analysis.maxDataSet)
        }
        for (i in 0 .. cycles){
            var minAverage = 0.0
            var maxAverage = 0.0
            var travelAverage = 0.0
            minDataSets.forEach { set ->
                val y = set.getY(i)
                minAverage = minAverage + y.toFloat()
            }
            maxDataSets.forEach { set ->
                val y = set.getY(i)
                maxAverage = maxAverage + y.toFloat()
            }
            averageTravelDistanceSet.forEach { set ->
                val y = set.getY(i)
                travelAverage = travelAverage + y.toFloat()
            }
            minAverage = minAverage / repetitions
            maxAverage = maxAverage / repetitions
            travelAverage = travelAverage / repetitions
            println(i.toString()+"\t" + minAverage +"\t" + maxAverage + "\t" + travelAverage)
        }
    }

    fun stairAnalysisPopulation(stations:Int, travelerAmount: Int, wagonNumber:Int, wagonSize:Int){

        DataGenerator.generateStairNetwork(stations, false,1, wagonNumber, wagonSize)
        val timeTables = JsonDataLoader.loadTimeTables(true)
        val trainNetwork = TrainNetwork(timeTables)
        val travelers = RandomDataUtil.generateTravelers(trainNetwork, travelerAmount)

        val popSize = 400
        println(popSize)
        val cycles = 100
        println(cycles)
        val Pr = 0.9
        println(Pr)
        val Pm = 0.4
        println(Pm)


        val minDataSets = mutableListOf<XYSeries>()
        val maxDataSets = mutableListOf<XYSeries>()
        val minAverageTravelDistanceSet = mutableListOf<XYSeries>()
        val maxAverageTravelDistanceSet = mutableListOf<XYSeries>()
        val repetitions = 100
        for (i in 1.. repetitions step 1){
            println("repetition: " + i +" of "+ repetitions)
            val genetic = SeatEvo(
                trainNetwork,
                travelers,
                popSize,
                cycles,
                InverseFitnessProportionalSelector(popSize),
//                InverseStochasticUniversalSampling(popSize),
//                    TournamentSelector(4),
                //TravelerCrossOver(i/10.0),
                WagonCrossOver(Pr),
                ChangeWagonMutation(Pm)
//              WagonSwapMutation(0.9)
            )
            genetic.logging = false
            val result = genetic.evolution()
            minDataSets.add(genetic.analysis.minDataSet)
            maxDataSets.add(genetic.analysis.maxDataSet)
            minAverageTravelDistanceSet.add(genetic.analysis.minAverageTravelDistance)
            maxAverageTravelDistanceSet.add(genetic.analysis.maxAverageTravelDistance)
        }
        for (i in 0 .. cycles){
            var minAverage = 0.0
            var maxAverage = 0.0
            var minTravelAverage = 0.0
            var maxTravelAverage = 0.0
            minDataSets.forEach { set ->
                val y = set.getY(i)
                minAverage = minAverage + y.toFloat()
            }
            maxDataSets.forEach { set ->
                val y = set.getY(i)
                maxAverage = maxAverage + y.toFloat()
            }
            minAverageTravelDistanceSet.forEach { set ->
                val y = set.getY(i)
                minTravelAverage = minTravelAverage + y.toFloat()
            }
            maxAverageTravelDistanceSet.forEach { set ->
                val y = set.getY(i)
                maxTravelAverage = maxTravelAverage + y.toFloat()
            }
            minAverage = minAverage / minDataSets.size
            maxAverage = maxAverage / maxDataSets.size
            minTravelAverage = minTravelAverage / repetitions
            maxTravelAverage = maxTravelAverage / repetitions
            println(i.toString()+"\t" + minAverage +"\t" + maxAverage + "\t" + minTravelAverage + "\t" + maxTravelAverage)
        }

    }


    fun gridAnalysis(gridSize:Int, travelerAmount: Int, wagonNumber:Int, wagonSize:Int){

        DataGenerator.generateGridNetwork(Pair(gridSize,gridSize), 1, wagonNumber, wagonSize)
        val timeTables = JsonDataLoader.loadTimeTables(true)
        val trainNetwork = TrainNetwork(timeTables)
        val travelers = RandomDataUtil.generateTravelers(trainNetwork, travelerAmount)

        val repetitions = 1

        val popSize = 300
        val cycles = 100

        val minDataSets = mutableListOf<XYSeries>()
        val maxDataSets = mutableListOf<XYSeries>()
        for (i in 1.. repetitions step 1){
            println("repetition: " + i)
            val genetic = SeatEvo(
                trainNetwork,
                travelers,
                popSize,
                cycles,
//              InverseFitnessProportionalSelector(popSize),
//              InverseStochasticUniversalSampling(popSize),
                TournamentSelector(4),
                //TravelerCrossOver(i/10.0),
                WagonCrossOver(0.7),
                ChangeWagonMutation(0.9)
//              WagonSwapMutation(0.9)
            )
            genetic.logging = false
            val result = genetic.evolution()
            minDataSets.add(genetic.analysis.minDataSet)
            maxDataSets.add(genetic.analysis.maxDataSet)
        }
        for (i in 0 .. cycles){
            var minAverage = 0.0
            var maxAverage = 0.0
            minDataSets.forEach { set ->
                val y = set.getY(i)
                minAverage = minAverage + y.toFloat()
            }
            maxDataSets.forEach { set ->
                val y = set.getY(i)
                maxAverage = maxAverage + y.toFloat()
            }
            minAverage = minAverage / cycles
            maxAverage = maxAverage / cycles
            println(i.toString()+"\t" + minAverage +"\t" + maxAverage)
        }
    }


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
                ChangeWagonMutation(0.6)
//        WagonSwapMutation(0.9)
            )
            genetic.logging = false
            val result = genetic.evolution()
            genetic.analysis.showChart("InvFitProp" +i)
            genetic.analysis.setVisible(true);
        }
    }

}