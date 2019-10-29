package freeplan

import com.beust.klaxon.Json

data class Journey(
    @Json(name = "train_locs")
    val stops: List<JourneyStop>
)