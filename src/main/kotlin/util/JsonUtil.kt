package util

import com.beust.klaxon.Klaxon
import json.DurationConverter
import json.LocalTimeConverter

object JsonUtil {

    val klaxon = Klaxon().converter(LocalTimeConverter()).converter(DurationConverter())

}