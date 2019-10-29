package evo.selectors

import evo.Individual
import util.RandomUtil

class FitnessProportionalSelector(var amount:Int): SelectorFunction() {

    override fun select(population: List<Individual>): List<Individual> {
        val selected = mutableListOf<Individual>()
        val fitnessSum = population.fold(0, {acc: Int, individual: Individual ->  acc+individual.fitness})
        for (i in 0 until amount){
            var j = 0
            var sum = population.get(j).fitness
            val u = RandomUtil.seed.nextInt(0, fitnessSum)
            while (sum < u){
                j ++
                sum = sum + population.get(j).fitness
            }
            selected.add(population.get(j))
        }
        return selected
    }
}