package freeplan

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.KlaxonException

class JourneyStopConverter : Converter {
    override fun canConvert(cls: Class<*>): Boolean {
        return cls == JourneyStop::class.java
    }

    override fun fromJson(jv: JsonValue): Any? {
        val stopId = jv.objInt("stopId")
        val stopName = jv.objString("stopName")
        val lat = jv.objString("lat")
        val lon = jv.objString("lon")
        var arrTime: String? = null

        try {
            arrTime = jv.objString("arrTime")
        } catch (e: KlaxonException) {

        }
        var depTime: String? = null
        try {
            depTime = jv.objString("depTime")
        } catch (e: KlaxonException) {

        }


        val train = jv.objString("train")
        val type = jv.objString("type")
        val operator = jv.objString("operator")

        var notes: List<Note> = emptyList()
        val array = jv.obj?.array<Note>("notes")
        if (array != null) {
            notes = array.toList()
        }
        return JourneyStop(stopId, stopName, lat, lon, arrTime, depTime, train, type, operator, notes)
    }

    override fun toJson(value: Any): String {
        TODO()
    }
}