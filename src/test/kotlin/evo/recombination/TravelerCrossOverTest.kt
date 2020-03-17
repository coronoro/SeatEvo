package evo.recombination

import evo.Individual
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import util.RandomUtil
import java.util.*
import java.util.Collections.emptyList
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TravelerCrossOverTest {

    private var individuals: MutableList<Individual> = mutableListOf<Individual>()

    @BeforeAll
    fun setUp() {
        RandomUtil.seed = Random(42) as Random.Default
        val dataA: List<List<Int>> = listOf(listOf(1,2,3,4,5,6), listOf(7,8,9,10), listOf(11,12,13,14,15))
        individuals.add(Individual(dataA))
        val dataB: List<List<Int>> = listOf(listOf(6,5,4,3,2,1), listOf(10,9,8,7), listOf(15,12,13,14,11))
        individuals.add(Individual(dataB))
    }


    //@Test
    fun recombineOnePoint() {
        val recombinator = TravelerCrossOver(1.0)
        val recombined = recombinator.recombine(individuals)
        Assertions.assertArrayEquals(individuals[0].data[0].toIntArray(),recombined[0].data[0].toIntArray())
        Assertions.assertArrayEquals(individuals[1].data[1].toIntArray(),recombined[0].data[1].toIntArray())
        Assertions.assertArrayEquals(individuals[1].data[2].toIntArray(),recombined[0].data[2].toIntArray())

    }

    //@Test
    fun recombineTwoPoint() {
        val recombinator = TravelerCrossOver(1.0,2)
        val recombined = recombinator.recombine(individuals)
        Assertions.assertArrayEquals(individuals[0].data[0].toIntArray(),recombined[0].data[0].toIntArray())
        Assertions.assertArrayEquals(individuals[1].data[1].toIntArray(),recombined[0].data[1].toIntArray())
        Assertions.assertArrayEquals(individuals[1].data[2].toIntArray(),recombined[0].data[2].toIntArray())

    }
}