package evo.recombination

import evo.Individual
import util.RandomUtil
import java.lang.Exception

class TravelerCrossOver(recombinationRate: Double, var k : Int = 1): RecombinationFunction(recombinationRate) {

    override fun recombine(individuals: List<Individual>): MutableList<Individual> {
        if (individuals.size % 2 != 1 ){
            throw Exception("expected even population size")
        }
        val result = mutableListOf<Individual>()
        for (i in 0 until individuals.size step 2){
            var a = individuals.get(i)
            var b = individuals.get(i+1)
            val recomb = RandomUtil.seed.nextDouble(0.0,1.0)
            if (recomb > recombinationRate){
                val dataSize = a.data.size
                val newDataA = mutableListOf<List<Int>>()
                val newDataB = mutableListOf<List<Int>>()
                var previous = 0
                for (j in 0 .. k){
                    var swapIndex:Int
                    if (j == k ){
                        swapIndex = dataSize
                    }else{
                        swapIndex = RandomUtil.seed.nextInt(0, dataSize-1)
                    }

                    var subListA : List<List<Int>>
                    var subListB : List<List<Int>>
                    if (j % 2 == 0){
                        subListA = b.data.subList(previous,swapIndex)
                        subListB = a.data.subList(previous,swapIndex)
                    }else{
                        subListA = a.data.subList(previous,swapIndex)
                        subListB = b.data.subList(previous,swapIndex)
                    }
                    newDataA.addAll(subListA)
                    newDataB.addAll(subListB)
                    previous = swapIndex
                }
                a =Individual(newDataA)
                b = Individual(newDataB)
            }

            result.add(a)
            result.add(b)
        }
        return result
    }
}