package model

data class Train(var wagons: List<Wagon>, override val id: String, val name: String) : Identifiable {

    fun clean() {
        wagons.forEach { wagon ->
            wagon.occupied = 0
        }
    }


}