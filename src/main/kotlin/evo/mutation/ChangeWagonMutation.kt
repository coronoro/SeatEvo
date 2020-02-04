package evo.mutation

import evo.Individual
import util.RandomUtil

class ChangeWagonMutation(mutationRate: Double) : MutationFunction(mutationRate) {

    override fun mutate(individuals: List<Individual>): List<Individual> {
        individuals.forEach { individual ->
            val mutate = RandomUtil.seed.nextDouble(0.0, 1.0)
            if (mutate > 1 - mutationRate) {
                val travelerIndex = RandomUtil.seed.nextInt(0, individual.data.size)
                val travelerPath = individual.data.get(travelerIndex).toMutableList()
                var wagonIndex = 0
                if (travelerPath.size > 1){
                    wagonIndex = RandomUtil.seed.nextInt(0, travelerPath.size)
                }

                val traveler = this.travelers[travelerIndex]
                val routeItem = traveler.route.waypoints[wagonIndex]
                val oldWagon = individual.data[travelerIndex][wagonIndex]
                var randomWagon: Int
                //check if there is another wagon
                if (routeItem.train.wagons.size > 1){
                    do{
                        randomWagon = RandomUtil.seed.nextInt(0, routeItem.train.wagons.size)
                    }while (randomWagon == oldWagon)
                }else{
                    // there is no other wagon
                    randomWagon = oldWagon
                }
                travelerPath.set(wagonIndex, randomWagon)
            }
        }
        return individuals
    }
}