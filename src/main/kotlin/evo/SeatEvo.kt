package evo

import evo.mutation.MutationFunction
import evo.recombination.RecombinationFunction
import evo.selectors.SelectorFunction
import model.TrainNetwork
import model.Traveler
import util.RandomUtil

class SeatEvo(
    var network: TrainNetwork,
    var travelers: List<Traveler>,
    val popSize: Int = 8,
    val selector: SelectorFunction,
    val recobinator: RecombinationFunction,
    val mutator: MutationFunction
) {

    var logging = true

    fun evolution(cycles: Int): Individual {
        mutator.travelers = travelers
        mutator.network = network
        //create initial population

        var best = Individual(emptyList())
        best.fitness = Double.MAX_VALUE
        var population = createRandomPopulation()
        for (i in 0..cycles) {
            println("========================")
            // evaluate individuals
            population.forEach {
                val evaluate = evaluate(it)
                it.fitness = evaluate
                println(it.toString() + " fitness: " + evaluate)
                if (evaluate <= best.fitness)
                    best = it
            }

            val selected = selector.select(population)
            population = recobinator.recombine(selected).toMutableList()
            population = mutator.mutate(population).toMutableList()
        }
        //evaluate the last population
        population.forEach {
            val evaluate = evaluate(it)
            it.fitness = evaluate
            if (evaluate < best.fitness)
                best = it
        }


        return best
    }


    private fun insert(individual: Individual) {

    }

    private fun logging(message: Any?){
        if (logging){
            println(message)
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


    private fun evaluate(individual: Individual): Double {
        var fitness = 0.0
        for (i in 0 until travelers.size) {
            val wagonData = individual.data.get(i)
            val traveler = travelers.get(0)
            //ignore the last entry
            val stopsSize = traveler.route.waypoints.size - 1
            var distance = 0

            for (j in 0 until stopsSize) {
                val wp = traveler.route.waypoints.get(j)
                val wagonNumber = wagonData.get(j)
                //if there is no predecessor
                if (j-1 < 0){
                    //TODO check if there is any space in the wagon -> fitness penalty
                    distance = network.getDistance(wp.train, wagonNumber, wp.station)
                }else{
                    val previousWp = traveler.route.waypoints.get(j - 1)
                    val previousWagonNumber = wagonData.get(j - 1)
                    //TODO check if there is any space in the wagon -> fitness penalty
                    distance = network.getDistance(wp.train, wagonNumber, previousWp.train, previousWagonNumber, wp.station)
                }
                fitness += distance
            }
            //get the distance for leaving
            val last = traveler.route.waypoints.last()
            fitness += network.getDistance(last.train, wagonData.last(), last.station)
        }
        return fitness
    }

    private fun createRandomIndividual(): Individual {
        val data = mutableListOf<List<Int>>()
        travelers.forEach { traveler ->
            val routeWagons = mutableListOf<Int>()
            for (i in 0 until traveler.route.waypoints.size - 1){
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