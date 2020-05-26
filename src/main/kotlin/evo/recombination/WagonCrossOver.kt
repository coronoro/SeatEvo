package evo.recombination

import evo.Individual
import util.RandomUtil

class WagonCrossOver(recombinationRate: Double, var k: Int = 1) : RecombinationFunction(recombinationRate) {

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
                val aFlat = a.data.flatten()
                val bFlat = b.data.flatten()
                val dataSize = aFlat.size
                var swapIndizes = mutableSetOf<Int>()
                for (j in 0..k) {
                    if (j == k) {
                        swapIndizes.add(dataSize)
                    } else {
                        swapIndizes.add(RandomUtil.seed.nextInt(1, dataSize))
                    }
                }
                var previous = 0
                val newDataA = mutableListOf<Int>()
                val newDataB = mutableListOf<Int>()
                val sorted = swapIndizes.sorted()
                sorted.forEachIndexed { j,swapIndex ->
                    var subListA: List<Int>
                    var subListB: List<Int>
                    if (j % 2 == 0) {
                        subListA = bFlat.subList(previous, swapIndex)
                        subListB = aFlat.subList(previous, swapIndex)
                    } else {
                        subListA = aFlat.subList(previous, swapIndex)
                        subListB = bFlat.subList(previous, swapIndex)
                    }
                    newDataA.addAll(subListA)
                    newDataB.addAll(subListB)
                    previous = swapIndex
                }
                a = Individual(deflatten(newDataA, a.data))
                b = Individual(deflatten(newDataB, b.data))
            }

            result.add(a)
            result.add(b)
        }
        return result
    }

    private fun deflatten(
        newData: MutableList<Int>,
        format: List<List<Int>>
    ): List<List<Int>> {
        var data = newData
        var firstIndex = 0
        val result = mutableListOf<MutableList<Int>>();
        for (i in 0 until format.size){
            val list = data.subList(firstIndex, firstIndex + format[i].size)
            firstIndex = firstIndex + format[i].size
            result.add(i, list)
        }
        return result
    }
}