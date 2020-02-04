package json

import model.Station
import model.Train
import model.timetable.TimeTable
import util.JsonUtil
import java.io.File

object JsonDataWriter {

    private val stationsLocation = "/stations/stations-SNAP.json"
    private val trainsLocation = "/trains/trains-SNAP.json"
    private val timetableLocation = "/timetable/timetable-SNAP.json"

    fun writeTrains(list: List<Train>){
        val data = JsonUtil.klaxon.toJsonString(list)
        writeFile(data, trainsLocation)
    }

    fun writeStations(list: List<Station>){
        val data = JsonUtil.klaxon.toJsonString(list)
        writeFile(data, stationsLocation)
    }

    fun writeFile(data: String, location:String){
        val resource = JsonDataWriter::class.java.getResource(location)
        if (resource != null) {
            val file = File(resource.toURI())
            file.bufferedWriter().use { out -> out.write(data) }
        }
    }

    fun writeTimeTables(list: List<TimeTable>){
        val data = JsonUtil.klaxon.toJsonString(list)
        writeFile(data, timetableLocation)
    }

    fun writeJSonTimeTables(list: List<JsonTimeTable>){
        val data = JsonUtil.klaxon.toJsonString(list)
        writeFile(data, timetableLocation)
    }


}