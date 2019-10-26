package json

import model.Track
import java.time.Duration

data class JsonStationStop (var station: Int, var arrival: Duration? = null, var departure: Duration? = null, var track: Track){
}