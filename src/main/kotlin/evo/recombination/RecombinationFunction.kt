package evo.recombination

import evo.Individual

abstract class RecombinationFunction(val recombinationRate: Double) {

    abstract fun recombine(individuals: List<Individual>): List<Individual>

}