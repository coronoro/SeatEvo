package model

import model.route.TravelerRoute

data class Traveler(val route: TravelerRoute, var group: Int? = null) {

}