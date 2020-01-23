package json

import model.timetable.DrivingDirection
import model.track.Track
import java.time.Duration

data class JsonStationStop(
    var station: String,
    var arrival: Duration? = null,
    var departure: Duration? = null,
    var track: JsonTrack,
    var offset: Int,
    var direction: DrivingDirection
) {
}