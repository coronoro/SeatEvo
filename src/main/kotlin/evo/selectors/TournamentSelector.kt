package evo.selectors

import evo.Individual
import util.RandomUtil

class TournamentSelector(var size:Int) : SelectorFunction(){

    override fun select(population: List<Individual>): List<Individual> {
        val result = mutableListOf<Individual>()
        if (size >=2 ){
            for (i in 0 until population.size){
                var best: Individual? = null
                var fitness = Double.MAX_VALUE
                for (j in 0 until size){
                    val participant = population.get(RandomUtil.seed.nextInt(0, population.size))
                    if (participant.fitness < fitness){
                        best = participant
                        fitness = best.fitness
                    }
                }
                if (best != null)
                    result.add(best)
            }
        }else{
            throw Exception("invalid tournament size")
        }
        return result
    }

}