package json

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import java.time.Duration

class DurationConverter : Converter {

    override fun canConvert(cls: Class<*>): Boolean {
        return cls == Duration::class.java//To change body of created functions use File | Settings | File Templates.
    }

    override fun fromJson(jv: JsonValue): Any? {
        var minutes = jv.obj?.long("minutes") ?: 0
        return Duration.ofMinutes(minutes)
    }

    override fun toJson(value: Any): String {
        var result = ""
        if (value is Duration) {
            result = """{"minutes": ${value.toMinutes()}}"""
        }
        return result
    }
}