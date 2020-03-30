package json

import java.time.LocalTime

data class JsonTimeTable(var train: String, val departures: MutableList<LocalTime>, val stops: List<JsonStationStop>) {
}