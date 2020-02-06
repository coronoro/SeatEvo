package model

import model.route.TravelerRoute

data class Traveler(val route: TravelerRoute, val group: Int? = null) {

}