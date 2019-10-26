package model.route

import model.Station
import model.Track

class TrainRoute(stops: List<Pair<Station, Track>>, id:Int) : Route<Pair<Station, Track>>(stops, id){

}