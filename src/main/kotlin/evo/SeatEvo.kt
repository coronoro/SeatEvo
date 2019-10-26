package evo

import model.TrainNetwork
import model.Traveler
import org.apache.commons.lang3.RandomUtils
import util.RandomDataUtil

class SeatEvo(
    var timeTable: TrainNetwork,
    var travelers: List<Traveler>,
    var mutRate: Double = 1.0,
    var recombRate: Double = 0.6,
    var popSize: Int = 8,
    val tCount: Int = 4) {

    fun evolution(cycles: Int){
        val population = createRandomPopulation()
        for(i in 0 .. cycles){
            // evaluate individuals
        }
    }

    private fun createRandomPopulation(): MutableList<Individual> {
        val population = mutableListOf<Individual>()
        for (i in 0 until 8){
            val individual = createRandomIndividual()
            population.add(individual)
        }
        return population;
    }


    private fun evaluate(individual: Individual){
        for (i in 0 until travelers.size){
            val wagonData = individual.data.get(i)
            val traveler = travelers.get(0)


        }

    }

    private fun createRandomIndividual(): Individual {
        val data = mutableListOf<List<Int>>()
        travelers.forEach { traveler ->
            val routeWagons = mutableListOf<Int>()
            traveler.route.stops.forEach{ entry ->
                val max = entry.train.wagons.size
                val wagonNumber = RandomDataUtil.seed.nextInt(0, max)
                routeWagons.add(wagonNumber)
            }
            data.add(routeWagons)
        }
        return Individual(data)
    }

}