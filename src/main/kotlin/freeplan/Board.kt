package freeplan

import com.beust.klaxon.Json

data class Board (
    @Json(name = "boards")
    val entries: List<BoardEntry>)