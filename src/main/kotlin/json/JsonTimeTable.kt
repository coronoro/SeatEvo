package json

import java.time.LocalTime

data class JsonTimeTable (var train: Int, val departures:List<LocalTime>, val stops: List<JsonStationStop>){
}