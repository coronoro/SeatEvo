package model.timetable

import model.Station
import model.Track
import java.time.Duration

/**
 * Represents an entry in an timetable. Meaning when a train arrives a the station and when it departes.
 */
data class StationStop(var station: Station, var arrival: Duration?, var departure: Duration?, var track: Track)