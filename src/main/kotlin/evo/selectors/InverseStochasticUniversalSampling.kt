package evo.selectors

import evo.Individual
import util.RandomUtil

class InverseStochasticUniversalSampling(val amount: Int) : SelectorFunction() {

    override fun select(population: List<Individual>): List<Individual> {
        val result = mutableListOf<Individual>()
        val sum = population.sumByDouble { individual -> 1.0/individual.fitness }
        val distance = sum / amount
        val start = RandomUtil.seed.nextDouble(0.0, distance)


        for (i in 0 until amount) {
            var fitnessSum = 0.0
            var j = 0
            var individual:Individual
            do {
                individual = population.get(j)
                fitnessSum += 1.0/individual.fitness
                j++
            }while (fitnessSum < start + (i * distance))
            result.add(individual)
        }

        return result
    }
}