package model.route

import model.Station
import model.Train
import model.Wagon

class TravelerRoute(stops: List<RouteItem>, id: Int) : Route<RouteItem>(stops, id) {
}