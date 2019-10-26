package freeplan

data class JourneyStop (
    val stopId : Int,
    val stopName: String,
    val lat : String,
    val lon : String,
    var arrTime: String?,
    val depTime: String?,
    val train: String,
    val type: String,
    val  operator: String,
    val notes : List<Note>
){}