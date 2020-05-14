package evo

import evo.mutation.MutationFunction
import evo.recombination.RecombinationFunction
import evo.selectors.SelectorFunction
import model.TrainNetwork
import model.Traveler
import model.route.RouteItem
import util.FileLogger
import util.RandomUtil
import java.util.*
import kotlin.collections.HashMap

class SeatEvo(
    var network: TrainNetwork,
    var travelers: List<Traveler>,
    val popSize:Int,
    val cycles: Int,
    val selector: SelectorFunction,
    val recobinator: RecombinationFunction,
    val mutator: MutationFunction
) {

    var logging = true

    var groupSoftConstraint = false
    //contains the indizes in the genotype for members of the same group
    var groupMap = HashMap<Int,MutableList<Int>>()

    var analysis = EvoAnalysis()

    /**
     * the penalty for overfilling a wagon
     */
    var penalty = 100L;

    fun evolution(): Individual {
        mutator.travelers = travelers
        mutator.network = network
        //create grouplist for easier access
        fillGroupMap()

        //create initial population
        var best = Individual(emptyList())
        var bestFitness = Long.MAX_VALUE
        var bestCircle = 0;
        var population = createRandomPopulation()
        for (i in 0..cycles) {
//            val start = System.currentTimeMillis()
            logging("======================== " + popSize + " | #" + i + " ======================== ")
            // evaluate individuals

            population.forEach {
//                val start = System.currentTimeMillis()
                if (!groupSoftConstraint){
                    repairIndividualGroups(it)
                }
                var evaluate = evaluate(it,i)

                // check for wagon penalty

                val overload = getWagonOverload(it)
                var penalty = penalty * overload
                //penalty = penalty * i /cycles
                evaluate += penalty


                //check for group penalty
                if (groupSoftConstraint){
                    val splits = getGroupSplit(it)
                    evaluate += penalty * splits
                }

                it.fitness = evaluate
                logging(it.toString() + " fitness: " + evaluate)
                if (evaluate < bestFitness){
                    best = it
                    bestFitness = evaluate
                    bestCircle = i
                }
                val end = System.currentTimeMillis()
//                println("eval took " + (end-start))
            }

            analyzePopulation(population, i)

            val selected = selector.select(population)
            population = recobinator.recombine(selected).toMutableList()
            population = mutator.mutate(population).toMutableList()
//            val end = System.currentTimeMillis()
//            println("cycle took " + (end-start))
        }

        logging("best found in circle " + bestCircle)
        return best
    }

    private fun analyzePopulation(population: MutableList<Individual>, circle: Int) {
        var max = Long.MIN_VALUE
        var min = Long.MAX_VALUE
        var maxIndividual: Individual? = null;
        var minIndividual: Individual? = null;
        var sum = 0.0
        var populationDivergence = 1
        val count = HashMap<Int, Int>()
        population.forEach {
            sum += it.fitness
            if (it.fitness > max){
                maxIndividual = it;
                max = it.fitness
            }else if (it.fitness < min){
                minIndividual = it
                min = it.fitness
            }
            val hash = it.data.hashCode()
            var get = count.get(hash)
            if (get == null){
                get = 0
            }
            get ++
            count.put(hash,get)
        }
        sum = sum/population.size

        if (minIndividual != null){
            analysis.minAverageTravelDistance.add(circle, getAverageTravelDistance(minIndividual!!))
            analysis.minWagonOverload.add(circle, getWagonOverload(minIndividual!!))
        }
        if (maxIndividual != null){
            analysis.maxAverageTravelDistance.add(circle, getAverageTravelDistance(maxIndividual!!))
            analysis.maxWagonOverload.add(circle, getWagonOverload(maxIndividual!!))
        }

        analysis.minDataSet.add(circle, min)
        analysis.maxDataSet.add(circle, max)
        FileLogger.write("" + circle + "," + min + "," + max + "," +sum +"\n")
    }

    fun printConfig(){
        println("================================ Config")
        println("popsize: " + popSize)
        println("cycles: " + cycles)
        println("selector: " + selector::class.simpleName)
        println("recobinator: " + recobinator::class.simpleName)
        println("mutator: " + mutator::class.simpleName)
        println("================================")
    }


    private fun logging(message: Any?){
        if (logging){
            println(message)
        }
    }

    private fun fillGroupMap(){
        travelers.forEachIndexed { index, traveler ->
            val group = traveler.group
            if (group != null){
                var list = groupMap.get(group)
                if (list == null){
                    list = mutableListOf()
                }
                list.add(index)
            }
        }
    }

    private fun createRandomPopulation(): MutableList<Individual> {
        val population = mutableListOf<Individual>()
        for (i in 0 until popSize) {
            val individual = createRandomIndividual()
            population.add(individual)
        }
        return population
    }


    fun evaluate(individual: Individual,  cycle: Int? = null, pow: Boolean = true): Long {
        var fitness = 0L
        for (i in 0 until travelers.size) {
            val wagonData = individual.data.get(i)
            val traveler = travelers.get(i)
            //ignore the last entry
            val stopsSize = traveler.route.waypoints.size
            var distanceSum = 0L
            for (j in 0 until stopsSize) {
                var distance = 0L
                val wp = traveler.route.waypoints.get(j)
                val wagonNumber = wagonData.get(j)
                //if there is no predecessor
                if (j-1 < 0){
                    distance = network.getDistance(wp.train, wagonNumber, wp.fromStation)
                }else{
                    val previousWp = traveler.route.waypoints.get(j - 1)
                    val previousWagonNumber = wagonData.get(j - 1)
                    distance = network.getDistance(wp.train, wagonNumber, previousWp.train, previousWagonNumber, wp.fromStation)
                }
                //get the distance for leaving
                if (j == stopsSize -1){
                    val last = traveler.route.waypoints.last()
                    val distance = network.getDistance(last.train, wagonData.last(), last.toStation)
                }
                distanceSum += distance;
                if (pow)
                    distance = distance * distance
                fitness += distance

                //analysis stuff
                //if (cycle != null)
                    //analysis.averageTravelDistance.add(cycle, distance/stopsSize)
            }
        }
        return fitness
    }

    fun getAverageTravelDistance(individual: Individual): Double {
        var result = 0.0
        for (i in 0 until travelers.size) {
            val wagonData = individual.data.get(i)
            val traveler = travelers.get(i)
            //ignore the last entry
            val stopsSize = traveler.route.waypoints.size
            var distanceSum = 0.0
            for (j in 0 until stopsSize) {
                var distance = 0L
                val wp = traveler.route.waypoints.get(j)
                val wagonNumber = wagonData.get(j)
                //if there is no predecessor
                if (j-1 < 0){
                    distance = network.getDistance(wp.train, wagonNumber, wp.fromStation)
                }else{
                    val previousWp = traveler.route.waypoints.get(j - 1)
                    val previousWagonNumber = wagonData.get(j - 1)
                    distance = network.getDistance(wp.train, wagonNumber, previousWp.train, previousWagonNumber, wp.fromStation)
                }
                //get the distance for leaving
                if (j == stopsSize -1){
                    val last = traveler.route.waypoints.last()
                    val distance = network.getDistance(last.train, wagonData.last(), last.toStation)
                }
                distanceSum += distance;
            }
            result += distanceSum / stopsSize
        }
        return result / travelers.size
    }

    private fun repairIndividualGroups(individual: Individual) {
        val data = individual.data.toMutableList()
        
        this.groupMap.keys.forEach { key ->
            val indizes = groupMap.get(key)
            if (indizes != null){
                val first = indizes.first()
                val sample = data[first]
                var fix = false;
                for (i in 1 until indizes.size){
                    val index = indizes[i]
                    val wagons = data[index]
                    if (!wagons.equals(sample)){
                        fix = true
                        break;
                    }
                }
                if (fix){
                    val templateIndex = RandomUtil.seed.nextInt(0,indizes.size)
                    val template = data[templateIndex]
                    for (i in 0 until indizes.size){
                        val index = indizes[i]
                        data[index] = template

                    }
                }
            }
        }
        individual.data = data
    }

    private fun getGroupSplit(individual: Individual): Int {
        var splitAmount = 0
        this.groupMap.keys.forEach { key ->
            val indizes = groupMap.get(key)
            if (indizes != null){
                val first = indizes.first()
                val sample = individual.data[first]
                for (i in 1 until indizes.size){
                    val index = indizes[i]
                    val wagons = individual.data[index]
                    if (!wagons.equals(sample)){
                        splitAmount ++
                    }
                }
            }

        }
        return splitAmount
    }

    private fun getWagonOverload(individual: Individual): Long {
        var overload = 0L
        val trainmap = HashMap<String, MutableList<RouteItem>>()
        travelers.forEachIndexed { i, traveler ->
            traveler.route.waypoints.forEachIndexed { j, it ->
                it.wagonNumber = individual.data[i][j]
                var list = trainmap.get(it.train.id)
                if (list == null) {
                    list = mutableListOf()
                }
                list.add(it)
                it.train.clean()
                trainmap.put(it.train.id, list)
            }
        }

        trainmap.keys.forEach { key ->
            val list = trainmap.get(key)
            if (list != null){
                val past = ArrayDeque<RouteItem>()
                list.sortBy { routeItem -> routeItem.fromTime }
                list.forEach { routeItem ->
                    val current = routeItem.fromTime
                    val iterator = past.iterator()
                    iterator.forEach {item ->
                        if (item.toTime <= current){
                            item.train.wagons[item.wagonNumber].occupied -= 1
                            iterator.remove()
                        }
                    }
                    val wagon = routeItem.train.wagons.get(routeItem.wagonNumber)
                    wagon.occupied += 1
                    if (wagon.maxCapacity < wagon.occupied){
                        logging("overload in train: " + key + " in wagon: "+  routeItem.wagonNumber)
                        overload += wagon.occupied - wagon.maxCapacity
                    }
                    past.push(routeItem)
                }
            }
        }
        return overload
    }

    private fun createRandomIndividual(): Individual {
        val data = mutableListOf<List<Int>>()
        travelers.forEach { traveler ->
            val routeWagons = mutableListOf<Int>()
            for (i in 0 until traveler.route.waypoints.size){
                val entry = traveler.route.waypoints[i]
                val max = entry.train.wagons.size
                val wagonNumber = RandomUtil.seed.nextInt(0, max)
                routeWagons.add(wagonNumber)
            }
            data.add(routeWagons)
        }
        return Individual(data)
    }


}