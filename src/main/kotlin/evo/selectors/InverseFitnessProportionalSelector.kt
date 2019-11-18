package evo.selectors

import evo.Individual
import util.RandomUtil

class InverseFitnessProportionalSelector(var amount: Int) : SelectorFunction() {

    override fun select(population: List<Individual>): List<Individual> {
        val selected = mutableListOf<Individual>()
        val fitnessSum = population.fold(0.0, { acc: Double, individual: Individual -> acc + 1/individual.fitness })
        for (i in 0 until amount) {
            var j = 0
            var sum = 1/population.get(j).fitness
            val u = RandomUtil.seed.nextDouble(0.0, fitnessSum)
            while (sum < u) {
                j++
                sum = sum + 1/population.get(j).fitness
            }
            selected.add(population.get(j))
        }
        return selected
    }
}