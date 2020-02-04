package evo.mutation

import evo.Individual
import model.route.RouteItem
import util.RandomUtil

class WagonSwapMutation(mutationRate: Double) : MutationFunction(mutationRate) {

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
                val travelerWagon = individual.data[travelerIndex][wagonIndex]

                val traveler = this.travelers.get(travelerIndex)
                val routeItem = traveler.route.waypoints.get(wagonIndex)


                val list = findSameTrainTravelers(routeItem)
                if (!list.isEmpty()){
                    val swapPairIndex = RandomUtil.seed.nextInt(0, list.size)
                    val coordinates = list.get(swapPairIndex)
                    val swapPath = individual.data.get(coordinates.first).toMutableList()

                    travelerPath.set(wagonIndex, swapPath[coordinates.second])
                    swapPath.set(coordinates.second, travelerWagon)

                }
            }
        }
        return individuals
    }

    fun findSameTrainTravelers(target: RouteItem): List<Pair<Int,Int>>{
        val result = mutableListOf<Pair<Int,Int>>()
        this.travelers.forEachIndexed { index, traveler ->
            traveler.route.waypoints.forEachIndexed { wayPointIndex, routeItem ->
                if (target.train.id == routeItem.train.id && routeItem.fromTime <= target.toTime && target.fromTime <= routeItem.toTime)
                    result.add(Pair(index, wayPointIndex))
            }
        }

        return result
    }
}