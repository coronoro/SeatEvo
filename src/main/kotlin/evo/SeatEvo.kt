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
    var penalty = 100;

    fun evolution(): Individual {
        mutator.travelers = travelers
        mutator.network = network
        //create grouplist for easier access
        fillGroupMap()

        //create initial population
        var best = Individual(emptyList())
        var bestFitness = Double.MAX_VALUE
        var bestCircle = 0;
        var population = createRandomPopulation()
        for (i in 0..cycles) {
            logging("======================== " + popSize + " | #" + i + " ======================== ")
            // evaluate individuals
            population.forEach {
                if (!groupSoftConstraint){
                    repairIndividualGroups(it)
                }

                var evaluate = evaluate(it)

                // check for wagon penalty
                val overload = getWagonOverload(it)
                evaluate += penalty * overload

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
            }
            analyzePopulation(population, i)

            val selected = selector.select(population)
            population = recobinator.recombine(selected).toMutableList()
            population = mutator.mutate(population).toMutableList()
        }
        //evaluate the last population
        population.forEach {
            val evaluate = evaluate(it)
            it.fitness = evaluate
            if (evaluate < bestFitness)
                best = it
        }

        logging("best found in circle " + bestCircle)
        return best
    }

    private fun analyzePopulation(population: MutableList<Individual>, circle: Int) {
        var max = Double.MIN_VALUE
        var min = Double.MAX_VALUE
        var sum = 0.0
        var populationDivergence = 1
        val count = HashMap<Int, Int>()
        population.forEach {
            sum += it.fitness
            if (it.fitness > max){
                max = it.fitness
            }else if (it.fitness < min){
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


    fun evaluate(individual: Individual, pow: Boolean = true): Double {
        var fitness = 0.0
        for (i in 0 until travelers.size) {
            val wagonData = individual.data.get(i)
            val traveler = travelers.get(i)
            //ignore the last entry
            val stopsSize = traveler.route.waypoints.size
            var distance: Double
            for (j in 0 until stopsSize) {
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
                if (pow)
                    distance = Math.pow(distance, 2.0)
                fitness += distance
            }
            //get the distance for leaving
            val last = traveler.route.waypoints.last()
            fitness += network.getDistance(last.train, wagonData.last(), last.toStation)
        }
        return fitness
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

    private fun getWagonOverload(individual: Individual): Int {
        var overload = 0
        val trainmap = HashMap<String, MutableList<RouteItem>>()
        travelers.forEachIndexed { i, traveler ->
            traveler.route.waypoints.forEachIndexed { j, it ->
                it.wagonNumber = individual.data[i][j]
                var list = trainmap.get(it.train.id)
                if (list == null){
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