package evo

import evo.mutation.MutationFunction
import evo.recombination.RecombinationFunction
import evo.selectors.SelectorFunction
import model.TrainNetwork
import model.Traveler
import util.RandomDataUtil
import util.RandomUtil

class SeatEvo(
    var network: TrainNetwork,
    var travelers: List<Traveler>,
    val popSize: Int = 8,
    val selector: SelectorFunction,
    val recobinator: RecombinationFunction,
    val mutator: MutationFunction) {

    fun evolution(cycles: Int): List<Individual> {
        mutator.travelers = travelers
        mutator.network = network
        //create initial population
        var population = createRandomPopulation()
        for(i in 0 .. cycles){
            // evaluate individuals
            population.forEach{
                val evaluate = evaluate(it)
                it.fitness = evaluate
            }

            val selected = selector.select(population)
            population = recobinator.recombine(selected).toMutableList()
            population = mutator.mutate(population).toMutableList()
        }
        population.forEach{
            val evaluate = evaluate(it)
            it.fitness = evaluate
        }

        return population
    }


    private fun insert(population: List<Individual>){


    }

    private fun createRandomPopulation(): MutableList<Individual> {
        val population = mutableListOf<Individual>()
        for (i in 0 until popSize){
            val individual = createRandomIndividual()
            population.add(individual)
        }
        return population
    }


    private fun evaluate(individual: Individual): Int {
        var fitness = 0
        for (i in 0 until travelers.size){
            val wagonData = individual.data.get(i)
            val traveler = travelers.get(0)
            val stopsSize = traveler.route.waypoints.size
            for (j in 0 until stopsSize step 2){
                val wp = traveler.route.waypoints.get(j)
                val wagonNumber = wagonData.get(j)
                if (j+1 < stopsSize){
                    val nextWp = traveler.route.waypoints.get(j+1)
                    val nextWagonNumber = wagonData.get(j + 1)
                    val station = nextWp.station
                    //TODO check if there is any space in the wagon -> fitness penalty

                    val distance = network.getDistance(wp.train, wagonNumber, nextWp.train, nextWagonNumber, station)
                    fitness +=  distance
                }
            }
        }
        return fitness
    }

    private fun createRandomIndividual(): Individual {
        val data = mutableListOf<List<Int>>()
        travelers.forEach { traveler ->
            val routeWagons = mutableListOf<Int>()
            traveler.route.waypoints.forEach{ entry ->
                val max = entry.train.wagons.size
                val wagonNumber = RandomUtil.seed.nextInt(0, max)
                routeWagons.add(wagonNumber)
            }
            data.add(routeWagons)
        }
        return Individual(data)
    }





}