package evo.recombination

import evo.Individual
import util.RandomUtil

class TravelerCrossOver(recombinationRate: Double, var k: Int = 1) : RecombinationFunction(recombinationRate) {

    override fun recombine(individuals: List<Individual>): MutableList<Individual> {
        if (individuals.size % 2 != 0) {
            throw Exception("expected even population size")
        }
        val shuffled = individuals.shuffled(RandomUtil.seed)
        val result = mutableListOf<Individual>()
        for (i in 0 until shuffled.size step 2) {
            var a = shuffled.get(i)
            var b = shuffled.get(i + 1)
            val recomb = RandomUtil.seed.nextDouble(0.0, 1.0)
            if (recomb > 1 - recombinationRate) {
                val dataSize = a.data.size
                var swapIndizes = mutableSetOf<Int>()
                for (j in 0..k) {
                    if (j == k) {
                        swapIndizes.add(dataSize)
                    } else {
                        swapIndizes.add(RandomUtil.seed.nextInt(1, dataSize))
                    }
                }
                var previous = 0
                val newDataA = mutableListOf<List<Int>>()
                val newDataB = mutableListOf<List<Int>>()
                val sorted = swapIndizes.sorted()
                sorted.forEachIndexed { j,swapIndex ->
                    var subListA: List<List<Int>>
                    var subListB: List<List<Int>>
                    if (j % 2 == 0) {
                        subListA = b.data.subList(previous, swapIndex)
                        subListB = a.data.subList(previous, swapIndex)
                    } else {
                        subListA = a.data.subList(previous, swapIndex)
                        subListB = b.data.subList(previous, swapIndex)
                    }
                    newDataA.addAll(subListA)
                    newDataB.addAll(subListB)
                    previous = swapIndex
                }
                a = Individual(newDataA)
                b = Individual(newDataB)
            }

            result.add(a)
            result.add(b)
        }
        return result
    }
}