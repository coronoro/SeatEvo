package json

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import java.time.LocalTime

class LocalTimeConverter : Converter {

    override fun canConvert(cls: Class<*>): Boolean {
        return cls == LocalTime::class.java//To change body of created functions use File | Settings | File Templates.
    }

    override fun fromJson(jv: JsonValue): Any? {
        var hour = jv.objInt("hour")
        var minute = jv.objInt("minute")
        return LocalTime.of(hour, minute)
    }

    override fun toJson(value: Any): String {
        var result = ""
        if (value is LocalTime) {
            result = """{"hour" : ${value.hour}, "minute": ${value.minute}}"""
        }
        return result
    }
}