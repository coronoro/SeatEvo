package model

data class Train(val wagons: List<Wagon>, override val id: String) : Identifiable {

    fun clean() {
        wagons.forEach { wagon ->
            wagon.occupied = 0
        }
    }


}