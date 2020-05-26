package model

import model.track.Track

data class Station(
    val name: String,
    var tracks: List<Track>,
    override val id: String) : Identifiable {
}