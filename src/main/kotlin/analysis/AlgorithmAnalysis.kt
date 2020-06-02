package analysis

import data.DataGenerator
import evo.Individual
import evo.SeatEvo
import evo.mutation.ChangeWagonMutation
import evo.mutation.WagonSwapMutation
import evo.recombination.TravelerCrossOver
import evo.recombination.WagonCrossOver
import evo.selectors.InverseFitnessProportionalSelector
import evo.selectors.TournamentSelector
import json.JsonDataLoader
import json.JsonDataWriter
import json.JsonTimeTable
import model.TrainNetwork
import model.Traveler
import model.Wagon
import model.route.RouteItem
import model.timetable.TimeTable
import org.jfree.data.xy.XYDataItem
import org.jfree.data.xy.XYSeries
import scala.Mutable
import util.RandomDataUtil

object AlgorithmAnalysis {


    fun stairAnalysis(stations:Int, travelerAmount: Int, wagonNumber:Int, wagonSize:Int){

        DataGenerator.generateStairNetwork(stations, false,1, wagonNumber, wagonSize)
        val timeTables = JsonDataLoader.loadTimeTables(true)
        val trainNetwork = TrainNetwork(timeTables)
        val travelers = RandomDataUtil.generateTravelers(trainNetwork, travelerAmount)

        val repetitions = 1000

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


        val routeCount = travelers.fold(0.0, { acc: Double, traveler: Traveler -> acc + traveler.route.waypoints.size })


        val popSize = 500
        println(popSize)
        val cycles = 60
        println(cycles)
//        val Pr = 1.0
        val Pr = 0.7
        println(Pr)
        val k = 1
        println(k)
//        val Pm = 1.0
        val Pm = 0.4
        println(Pm)
//        val k = Math.floor(travelers.size/2.0).toInt();



        val minDataSets = mutableListOf<XYSeries>()
        val maxDataSets = mutableListOf<XYSeries>()
        val minAverageTravelDistanceSet = mutableListOf<XYSeries>()
        val maxAverageTravelDistanceSet = mutableListOf<XYSeries>()
        val minWagonOverloadSet = mutableListOf<XYSeries>()
        val maxWagonOverloadSet = mutableListOf<XYSeries>()
        val repetitions = 1000
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
                TravelerCrossOver(Pr, k),
//                WagonCrossOver(Pr, k),
                ChangeWagonMutation(Pm)
//              WagonSwapMutation(Pm)
            )
            genetic.logging = false
            val result = genetic.evolution()
            minDataSets.add(genetic.analysis.minDataSet)
            maxDataSets.add(genetic.analysis.maxDataSet)
            minAverageTravelDistanceSet.add(genetic.analysis.minAverageTravelDistance)
            maxAverageTravelDistanceSet.add(genetic.analysis.maxAverageTravelDistance)
            minWagonOverloadSet.add(genetic.analysis.minWagonOverload)
            maxWagonOverloadSet.add(genetic.analysis.maxWagonOverload)
        }
        for (i in 0 .. cycles){
            var minAverage = 0.0
            var maxAverage = 0.0
            var minTravelAverage = 0.0
            var maxTravelAverage = 0.0
            var minWagonOverload = 0.0
            var maxWagonOverload = 0.0
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
            minWagonOverloadSet.forEach { set ->
                val y = set.getY(i)
                minWagonOverload = minWagonOverload + y.toFloat()
            }
            maxWagonOverloadSet.forEach { set ->
                val y = set.getY(i)
                maxWagonOverload = maxWagonOverload + y.toFloat()
            }
            minAverage = minAverage / minDataSets.size
            maxAverage = maxAverage / maxDataSets.size
            minTravelAverage = minTravelAverage / minAverageTravelDistanceSet.size
            maxTravelAverage = maxTravelAverage / maxAverageTravelDistanceSet.size
            minWagonOverload = minWagonOverload / maxWagonOverloadSet.size
            maxWagonOverload = maxWagonOverload / maxWagonOverloadSet.size

            println(i.toString()+"\t" + minAverage +"\t" + maxAverage + "\t" + minTravelAverage + "\t" + maxTravelAverage +"\t" + minWagonOverload +"\t" + maxWagonOverload)
        }
    }


    fun gridAnalysis(gridSize:Int, travelerAmount: Int, wagonNumber:Int, wagonSize:Int){

        DataGenerator.generateGridNetwork(Pair(gridSize,gridSize), 1, wagonNumber, wagonSize)
        val timeTables = JsonDataLoader.loadTimeTables(true)
        val trainNetwork = TrainNetwork(timeTables)
        var travelers = RandomDataUtil.generateTravelers(trainNetwork, travelerAmount).toMutableList()

        val groupPart = 0.0
        println("groupPart: " + groupPart)
        val maximumMembers = 3
        val groupSoftConstraint = false;
        println("goup soft: " + groupSoftConstraint)
        val newTravelers = mutableListOf<Traveler>()
        val amount = Math.floor((groupPart * travelers.size) / maximumMembers)
        var current = 1
        var currentGroup = 0;
        travelers.forEach { traveler ->
            if (current <= amount){
                traveler.group = currentGroup
                val copy1 = traveler.copy()
                newTravelers.add(copy1)
                val copy2 = traveler.copy()
                newTravelers.add(copy2)
                newTravelers.add(traveler)
                currentGroup ++
            }
            current ++
        }
        newTravelers.addAll(travelers.subList(newTravelers.size, travelers.size))
        travelers = newTravelers

        val popSize = 700
        println("popsize: " + popSize)
        val cycles = 100
        println("cycles: " + cycles)
        val Pr = 1.0
        println("Pr: " + Pr)
        val Pm = 0.8
        println("Pm: "+Pm)
//        val k = 5
        val k = Math.floor(travelers.size/2.0).toInt();
        println("k: "+ k)


        val minDataSets = mutableListOf<XYSeries>()
        val maxDataSets = mutableListOf<XYSeries>()
        val minAverageTravelDistanceSet = mutableListOf<XYSeries>()
        val maxAverageTravelDistanceSet = mutableListOf<XYSeries>()
        val minWagonOverloadSet = mutableListOf<XYSeries>()
        val maxWagonOverloadSet = mutableListOf<XYSeries>()
        val minGroupSplitSet = mutableListOf<XYSeries>()
        val maxGroupSplitSet = mutableListOf<XYSeries>()
        val repetitions = 1000
        for (i in 1.. repetitions step 1){
            println("repetition: " + i +" of "+ repetitions)
            val genetic = SeatEvo(
                trainNetwork,
                travelers,
                popSize,
                cycles,
//                InverseFitnessProportionalSelector(popSize),
//                InverseStochasticUniversalSampling(popSize),
                TournamentSelector(4),
                TravelerCrossOver(Pr,k),
//                WagonCrossOver(Pr, k),
                ChangeWagonMutation(Pm)
//              WagonSwapMutation(Pm)
            )
            genetic.logging = false
            genetic.groupSoftConstraint = groupSoftConstraint
            val result = genetic.evolution()
            minDataSets.add(genetic.analysis.minDataSet)
            maxDataSets.add(genetic.analysis.maxDataSet)
            minAverageTravelDistanceSet.add(genetic.analysis.minAverageTravelDistance)
            maxAverageTravelDistanceSet.add(genetic.analysis.maxAverageTravelDistance)
            minWagonOverloadSet.add(genetic.analysis.minWagonOverload)
            maxWagonOverloadSet.add(genetic.analysis.maxWagonOverload)
            minGroupSplitSet.add(genetic.analysis.minGroupSplit)
            maxGroupSplitSet.add(genetic.analysis.maxGroupSplit)
        }
        for (i in 0 .. cycles){
            var minAverage = 0.0
            var maxAverage = 0.0

            var minTravelAverage = 0.0
            var maxTravelAverage = 0.0

            var minWagonOverload = 0.0
            var maxWagonOverload = 0.0

            var minGroupSplit = 0.0
            var maxGroupSplit = 0.0
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
            maxWagonOverloadSet.forEach { set ->
                val y = set.getY(i)
                maxWagonOverload = maxWagonOverload + y.toFloat()
            }
            minGroupSplitSet.forEach { set ->
                val y = set.getY(i)
                minGroupSplit = minGroupSplit + y.toFloat()
            }
            maxGroupSplitSet.forEach { set ->
                val y = set.getY(i)
                maxGroupSplit = maxGroupSplit + y.toFloat()
            }


            minAverage = minAverage / minDataSets.size
            maxAverage = maxAverage / maxDataSets.size
            minTravelAverage = minTravelAverage / minAverageTravelDistanceSet.size
            maxTravelAverage = maxTravelAverage / maxAverageTravelDistanceSet.size
            minWagonOverload = minWagonOverload / maxWagonOverloadSet.size
            maxWagonOverload = maxWagonOverload / maxWagonOverloadSet.size
            minGroupSplit = minGroupSplit / minGroupSplitSet.size
            maxGroupSplit = maxGroupSplit / maxGroupSplitSet.size

            println(i.toString()+"\t" + minAverage +"\t" + maxAverage + "\t" + minTravelAverage + "\t" + maxTravelAverage+"\t" + minWagonOverload +"\t" + maxWagonOverload +"\t" + minGroupSplit +"\t" + maxGroupSplit)
        }
    }


    fun gridDistributionAnalysis(gridSize:Int, travelerAmount: Int, wagonNumber:Int, wagonSize:Int){

        DataGenerator.generateGridNetwork(Pair(gridSize,gridSize), 1, wagonNumber, wagonSize)
        val timeTables = JsonDataLoader.loadTimeTables(true)
        val trainNetwork = TrainNetwork(timeTables)
        var travelers = RandomDataUtil.generateTravelers(trainNetwork, travelerAmount).toMutableList()

        val groupPart = 0.0
        println("groupPart: " + groupPart)
        val maximumMembers = 3
        val groupSoftConstraint = false;
        println("goup soft: " + groupSoftConstraint)
        val newTravelers = mutableListOf<Traveler>()
        val amount = Math.floor((groupPart * travelers.size) / maximumMembers)
        var current = 1
        var currentGroup = 0;
        travelers.forEach { traveler ->
            if (current <= amount){
                traveler.group = currentGroup
                val copy1 = traveler.copy()
                newTravelers.add(copy1)
                val copy2 = traveler.copy()
                newTravelers.add(copy2)
                newTravelers.add(traveler)
                currentGroup ++
            }
            current ++
        }
        newTravelers.addAll(travelers.subList(newTravelers.size, travelers.size))
        travelers = newTravelers

        val popSize = 700
        println("popsize: " + popSize)
        val cycles = 100
        println("cycles: " + cycles)
        val Pr = 1.0
        println("Pr: " + Pr)
        val Pm = 0.8
        println("Pm: "+Pm)
//        val k = 4
        val k = Math.floor(travelers.size/2.0).toInt();
        println("k: "+ k)


        val minDataSets = mutableListOf<XYSeries>()
        val maxDataSets = mutableListOf<XYSeries>()
        val minAverageTravelDistanceSet = mutableListOf<XYSeries>()
        val maxAverageTravelDistanceSet = mutableListOf<XYSeries>()
        val minWagonOverloadSet = mutableListOf<XYSeries>()
        val maxWagonOverloadSet = mutableListOf<XYSeries>()
        val minGroupSplitSet = mutableListOf<XYSeries>()
        val maxGroupSplitSet = mutableListOf<XYSeries>()
        var xySeries: XYSeries = XYSeries("")

        val repetitions = 1000
        for (i in 1.. repetitions step 1){
            println("repetition: " + i +" of "+ repetitions)
            val genetic = SeatEvo(
                trainNetwork,
                travelers,
                popSize,
                cycles,
//                InverseFitnessProportionalSelector(popSize),
//                InverseStochasticUniversalSampling(popSize),
                TournamentSelector(4),
                TravelerCrossOver(Pr,k),
//                WagonCrossOver(Pr, k),
                ChangeWagonMutation(Pm)
//              WagonSwapMutation(Pm)
            )
            genetic.logging = false
            genetic.groupSoftConstraint = groupSoftConstraint
            val result = genetic.evolution()
            minDataSets.add(genetic.analysis.minDataSet)
            maxDataSets.add(genetic.analysis.maxDataSet)
            minAverageTravelDistanceSet.add(genetic.analysis.minAverageTravelDistance)
            maxAverageTravelDistanceSet.add(genetic.analysis.maxAverageTravelDistance)
            minWagonOverloadSet.add(genetic.analysis.minWagonOverload)
            maxWagonOverloadSet.add(genetic.analysis.maxWagonOverload)
            minGroupSplitSet.add(genetic.analysis.minGroupSplit)
            maxGroupSplitSet.add(genetic.analysis.maxGroupSplit)
            xySeries = genetic.analysis.travelDistribution.get(100)!!
        }

        for (i in xySeries.getItems()) {
            val item = i as XYDataItem
            val x = item.xValue
            val y = item.yValue
            println(""+ x + "\t" + y)
        }

        for (i in 0 .. cycles){
            var minAverage = 0.0
            var maxAverage = 0.0

            var minTravelAverage = 0.0
            var maxTravelAverage = 0.0

            var minWagonOverload = 0.0
            var maxWagonOverload = 0.0

            var minGroupSplit = 0.0
            var maxGroupSplit = 0.0
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
            minWagonOverloadSet.forEach { set ->
                val y = set.getY(i)
                minWagonOverload = minWagonOverload + y.toFloat()
            }
            maxWagonOverloadSet.forEach { set ->
                val y = set.getY(i)
                maxWagonOverload = maxWagonOverload + y.toFloat()
            }
            minGroupSplitSet.forEach { set ->
                val y = set.getY(i)
                minGroupSplit = minGroupSplit + y.toFloat()
            }
            maxGroupSplitSet.forEach { set ->
                val y = set.getY(i)
                maxGroupSplit = maxGroupSplit + y.toFloat()
            }

            minAverage = minAverage / minDataSets.size
            maxAverage = maxAverage / maxDataSets.size
            minTravelAverage = minTravelAverage / minAverageTravelDistanceSet.size
            maxTravelAverage = maxTravelAverage / maxAverageTravelDistanceSet.size
            minWagonOverload = minWagonOverload / maxWagonOverloadSet.size
            maxWagonOverload = maxWagonOverload / maxWagonOverloadSet.size
            minGroupSplit = minGroupSplit / minGroupSplitSet.size
            maxGroupSplit = maxGroupSplit / maxGroupSplitSet.size

            println(i.toString()+"\t" + minAverage +"\t" + maxAverage + "\t" + minTravelAverage + "\t" + maxTravelAverage+"\t" + minWagonOverload +"\t" + maxWagonOverload +"\t" + minGroupSplit +"\t" + maxGroupSplit)
        }
    }


    fun loadMarudorTimeTables(wagonNumber:Int, wagonSize:Int): List<TimeTable>{

        val timeTables = JsonDataLoader.fillTimeTables(true)
//        val timeTables = timeTables = JsonDataLoader.fillTimeTables(true, "marudor-")
        timeTables.forEach { tt ->
            var wagons = mutableListOf<Wagon>()
            for (i in 0 until wagonNumber){
                wagons.add(Wagon(wagonSize,0))
            }
            tt.train.wagons = wagons
        }
        return timeTables
    }

    fun marudorDistributionAnalysis( travelerAmount: Int, wagonNumber:Int, wagonSize:Int){
        println("travelers: " + travelerAmount)

        val popSize = 700
        println("popsize: " + popSize)
        val cycles = 150
        println("cycles: " + cycles)
        val Pr = 0.9
        println("Pr: " + Pr)
        val Pm = .9
        println("Pm: "+Pm)

        val timeTables = loadMarudorTimeTables(wagonNumber, wagonSize)

        val trainNetwork = TrainNetwork(timeTables)
        var travelers = RandomDataUtil.generateTravelers(trainNetwork, travelerAmount).toMutableList()

        var distance:Long = 0
        var count = 0
        travelers.forEach { t ->
            if (t.route.waypoints.size > 1){

                var prev: RouteItem? = null
                t.route.waypoints.forEach {
                    if (prev != null){
                        count ++
                        distance += trainNetwork.getTrackDistance(prev!!.train ,it.train, prev!!.toStation)
                    }

                    prev = it
                }
            }
        }
        println(distance /count)
        val groupPart = 0.0
        println("groupPart: " + groupPart)
        val maximumMembers = 3
        val groupSoftConstraint = false;
        println("goup soft: " + groupSoftConstraint)
        val newTravelers = mutableListOf<Traveler>()
        val amount = Math.floor((groupPart * travelers.size) / maximumMembers)
        var current = 1
        var currentGroup = 0;
        travelers.forEach { traveler ->
            if (current <= amount){
                traveler.group = currentGroup
                val copy1 = traveler.copy()
                newTravelers.add(copy1)
                val copy2 = traveler.copy()
                newTravelers.add(copy2)
                newTravelers.add(traveler)
                currentGroup ++
            }
            current ++
        }
        newTravelers.addAll(travelers.subList(newTravelers.size, travelers.size))
        travelers = newTravelers

//        val k = 5
        val k = Math.floor(travelers.fold( 0, { acc: Int, t: Traveler -> acc + t.route.waypoints.size })/2.0).toInt()
//        val k = Math.floor(travelers.size/2.0).toInt();
        println("k: "+ k)


        val minDataSets = mutableListOf<XYSeries>()
        val maxDataSets = mutableListOf<XYSeries>()
        val minAverageTravelDistanceSet = mutableListOf<XYSeries>()
        val maxAverageTravelDistanceSet = mutableListOf<XYSeries>()
        val minWagonOverloadSet = mutableListOf<XYSeries>()
        val maxWagonOverloadSet = mutableListOf<XYSeries>()
        val minGroupSplitSet = mutableListOf<XYSeries>()
        val maxGroupSplitSet = mutableListOf<XYSeries>()
        var xySeries: XYSeries = XYSeries("")

        val repetitions = 1
        for (i in 1.. repetitions step 1){
            println("repetition: " + i +" of "+ repetitions)
            val genetic = SeatEvo(
                trainNetwork,
                travelers,
                popSize,
                cycles,
//                InverseFitnessProportionalSelector(popSize),
//                InverseStochasticUniversalSampling(popSize),
                TournamentSelector(4),
//                TravelerCrossOver(Pr,k),
                WagonCrossOver(Pr, k),
                ChangeWagonMutation(Pm)
//              WagonSwapMutation(Pm)
            )
            genetic.logging = false
            genetic.groupSoftConstraint = groupSoftConstraint
            val result = genetic.evolution()
            minDataSets.add(genetic.analysis.minDataSet)
            maxDataSets.add(genetic.analysis.maxDataSet)
            minAverageTravelDistanceSet.add(genetic.analysis.minAverageTravelDistance)
            maxAverageTravelDistanceSet.add(genetic.analysis.maxAverageTravelDistance)
            minWagonOverloadSet.add(genetic.analysis.minWagonOverload)
            maxWagonOverloadSet.add(genetic.analysis.maxWagonOverload)
            minGroupSplitSet.add(genetic.analysis.minGroupSplit)
            maxGroupSplitSet.add(genetic.analysis.maxGroupSplit)
            xySeries = genetic.analysis.travelDistribution.get(100)!!
        }

        for (i in xySeries.getItems()) {
            val item = i as XYDataItem
            val x = item.xValue
            val y = item.yValue
            println(""+ x + "\t" + y)
        }

        for (i in 0 .. cycles){
            var minAverage = 0.0
            var maxAverage = 0.0

            var minTravelAverage = 0.0
            var maxTravelAverage = 0.0

            var minWagonOverload = 0.0
            var maxWagonOverload = 0.0

            var minGroupSplit = 0.0
            var maxGroupSplit = 0.0
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
            minWagonOverloadSet.forEach { set ->
                val y = set.getY(i)
                minWagonOverload = minWagonOverload + y.toFloat()
            }
            maxWagonOverloadSet.forEach { set ->
                val y = set.getY(i)
                maxWagonOverload = maxWagonOverload + y.toFloat()
            }
            minGroupSplitSet.forEach { set ->
                val y = set.getY(i)
                minGroupSplit = minGroupSplit + y.toFloat()
            }
            maxGroupSplitSet.forEach { set ->
                val y = set.getY(i)
                maxGroupSplit = maxGroupSplit + y.toFloat()
            }

            minAverage = minAverage / minDataSets.size
            maxAverage = maxAverage / maxDataSets.size
            minTravelAverage = minTravelAverage / minAverageTravelDistanceSet.size
            maxTravelAverage = maxTravelAverage / maxAverageTravelDistanceSet.size
            minWagonOverload = minWagonOverload / maxWagonOverloadSet.size
            maxWagonOverload = maxWagonOverload / maxWagonOverloadSet.size
            minGroupSplit = minGroupSplit / minGroupSplitSet.size
            maxGroupSplit = maxGroupSplit / maxGroupSplitSet.size

            println(i.toString()+"\t" + minAverage +"\t" + maxAverage + "\t" + minTravelAverage + "\t" + maxTravelAverage+"\t" + minWagonOverload +"\t" + maxWagonOverload +"\t" + minGroupSplit +"\t" + maxGroupSplit)
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