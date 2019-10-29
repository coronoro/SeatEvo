package evo.mutation

import evo.Individual
import model.TrainNetwork
import model.Traveler

/**
 * @param mutationRate determines wethere an individual is mutated or not
 */
abstract class MutationFunction(val mutationRate: Double) {

    var travelers = listOf<Traveler>()
    var network: TrainNetwork? = null

    abstract fun mutate(individuals: List<Individual>) : List<Individual>

}