package model

data class Train(val wagons: List<Wagon>, override val id: Int) : Identifiable {

    fun clean() {
        wagons.forEach { wagon ->
            wagon.occupied = 0
        }
    }


}