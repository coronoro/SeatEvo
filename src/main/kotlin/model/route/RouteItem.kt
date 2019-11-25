package model.route

import model.Station
import model.Train
import java.time.LocalTime

data class RouteItem(var fromStation: Station, var fromTime: LocalTime, var toStation: Station, var toTime: LocalTime, var train: Train, var wagonNumber: Int)