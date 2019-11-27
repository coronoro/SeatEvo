package json

import java.time.LocalTime

data class JsonTimeTable(var train: String, val departures: List<LocalTime>, val stops: List<JsonStationStop>) {
}