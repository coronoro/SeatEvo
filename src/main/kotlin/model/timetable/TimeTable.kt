package model.timetable

import model.Train
import java.time.LocalTime

data class TimeTable(var train: Train, val departures:List<LocalTime>, val stops: List<StationStop>)