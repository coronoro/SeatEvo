package evo.selectors

import evo.Individual

abstract class SelectorFunction {

    abstract fun select(population: List<Individual>): List<Individual>
}