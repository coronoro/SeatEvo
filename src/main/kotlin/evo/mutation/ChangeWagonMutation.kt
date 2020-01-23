package evo.mutation

import evo.Individual
import util.RandomUtil

class ChangeWagonMutation(mutationRate: Double) : MutationFunction(mutationRate) {

    override fun mutate(individuals: List<Individual>): List<Individual> {
        individuals.forEach { individual ->
            val mutate = RandomUtil.seed.nextDouble(0.0, 1.0)
            if (mutate > 1 - mutationRate) {
                val travelerIndex = RandomUtil.seed.nextInt(0, individual.data.size - 1)
                val travelerPath = individual.data.get(travelerIndex).toMutableList()
                var wagonIndex = 0
                if (travelerPath.size > 1){
                    wagonIndex = RandomUtil.seed.nextInt(0, travelerPath.size - 1)
                }

                val traveler = this.travelers.get(travelerIndex)
                val routeItem = traveler.route.waypoints.get(wagonIndex)
                val randomWagon = RandomUtil.seed.nextInt(0, routeItem.train.wagons.size)
                travelerPath.set(wagonIndex, randomWagon)
            }
        }
        return individuals
    }
}