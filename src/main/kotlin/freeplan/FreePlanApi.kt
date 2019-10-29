package freeplan

import com.beust.klaxon.Klaxon
import org.restlet.representation.Representation
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat


object FreePlanApi : AbstractRestApi() {

    private val V1 = "https://api.deutschebahn.com/freeplan/v1/"
    private val locationEndPoint = "location/"
    private val arrivalBoardEndPoint = "arrivalBoard/"
    private val journeyDetailsEndPoint = "journeyDetails/"
    val dateformatter = SimpleDateFormat("yyyy-MM-dd")

    fun findLocations(name: String): List<Location> {
        var result = listOf<Location>()
        val baseUrl = V1 + locationEndPoint
        var representation: Representation? = null
        var query = name
        // the api gets sometimes weird. example : Fulda -> Nope
        while (!name.isEmpty() && representation == null) {
            try {
                val url = URL(baseUrl + URLEncoder.encode(query.replace(("[^A-Za-z0-9]").toRegex(), ""), "UTF-8"))
                representation = getRepresentation(url)
            } catch (e: Exception) {
                //Ignore Exception
                println("could not find: " + query)
                query = query.dropLast(1)
            }
        }

        if (representation != null) {
            val parse = Klaxon().parseArray<Location>(representation.stream)
            if (parse != null) {
                result = parse
            }
        }

        return result
    }

    fun findLocation(name: String, id: Int): Location? {
        var result: Location? = null
        val locations = findLocations(name)
        val filter = locations.filter { it.id == id }
        if (!filter.isEmpty()) {
            result = filter.get(0)
        }
        return result
    }

    fun getArrivalBoard(location: Location, date: String = "2019-10-03"): List<BoardEntry> {
        var result = listOf<BoardEntry>()
        val baseUrl = V1 + arrivalBoardEndPoint
        //TODO
        val url = URL(baseUrl + location.id + "?date=" + date)
        val representation = getRepresentation(url)
        if (representation != null) {
            val parse = Klaxon().parseArray<BoardEntry>(representation.stream)
            if (parse != null) {
                result = parse
            }
        }
        return result
    }

    fun getJourney(entry: BoardEntry): List<JourneyStop> {
        var result = listOf<JourneyStop>()
        val baseUrl = V1 + journeyDetailsEndPoint
        val url = URL(baseUrl + URLEncoder.encode(entry.detailsId, "UTF-8"))
        val representation = getRepresentation(url)
        if (representation != null) {
            val parse = Klaxon().converter(JourneyStopConverter()).parseArray<JourneyStop>(representation.stream)
            if (parse != null) {
                result = parse
            }
        }

        return result
    }


}