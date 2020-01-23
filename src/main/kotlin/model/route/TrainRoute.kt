package model.route

import model.Station
import model.track.Track

class TrainRoute(stops: List<Pair<Station, Track>>, id: String) : Route<Pair<Station, Track>>(stops, id) {

}