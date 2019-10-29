package model.route

import model.Station
import model.Train
import java.time.LocalTime

data class RouteItem(var station: Station, var time: LocalTime, var train: Train, var wagonNumber: Int)