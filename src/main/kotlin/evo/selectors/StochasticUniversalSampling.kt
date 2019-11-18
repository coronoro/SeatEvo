package evo.selectors

import evo.Individual
import util.RandomUtil

class StochasticUniversalSampling(val amount: Int) : SelectorFunction() {

    override fun select(population: List<Individual>): List<Individual> {
        val result = mutableListOf<Individual>()
        val sum = population.sumByDouble { individual -> individual.fitness }
        val distance = sum / amount
        val start = RandomUtil.seed.nextDouble(0.0, distance)

        var fitnessSum = 0.0
        for (i in 0 until amount) {
            var j = 0
            var individual = population.get(j)
            while (fitnessSum > start + (i * distance)) {
                individual = population.get(j)
                fitnessSum += individual.fitness
                j++
            }
            result.add(individual)
        }

        return result
    }
}