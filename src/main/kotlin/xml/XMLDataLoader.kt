package xml

import de_b.StationCSV
import json.JsonDataLoader
import model.Station
import model.Train
import model.timetable.TimeTable
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import javax.xml.parsers.DocumentBuilderFactory

class XMLDataLoader {

    companion object {

        private val stationsLocation = "/stations/D_Bahnhof_2017_09.csv"
        private val wagonOrder = "/wagon_order/"

        fun loadStationsMapping(): Map<String, Station> {
            val result = mutableMapOf<String, Station>()
            val resource = XMLDataLoader::class.java.getResource(stationsLocation)
            println("loading data from file: " + resource)
            if (resource != null) {
                val file = File(resource.toURI())
                val reader = BufferedReader(FileReader(file))
                //read head
                reader.readLine()

                var line = reader.readLine()
                while (line != null) {
                    val tokens = line.split(",")
                    if (tokens.size > 0) {
                        val id = tokens[StationCSV.EVA_NR.index]
                        val name = tokens[StationCSV.NAME.index]
                        val station = Station(name, id.toInt())

                        val ds100 = tokens[StationCSV.DS100.index]
                        result.put(ds100, station)

                    }
                }
            }
            return result
        }


        fun loadTimeTable(): TimeTable? {
            val instance = DocumentBuilderFactory.newInstance()
            val stationsMapping = loadStationsMapping()
            val trains = mutableListOf<Train>()

            val resource = JsonDataLoader::class.java.getResource(wagonOrder)
            println("loading data from file: " + resource)
            if (resource != null) {
                val folder = File(resource.toURI())
                if (folder.isDirectory) {
                    val listFiles = folder.listFiles()
                    listFiles.forEach { file ->
                        val documentBuilder = instance.newDocumentBuilder()
                        val document = documentBuilder.parse(file)
                        document.normalize()

                        val shortCode = document.getElementsByTagName("shortcode")
                        var ds100 = ""
                        if (shortCode.length == 1) {
                            var node = shortCode.item(0)
                            ds100 = node.nodeValue
                        }
                        val station = stationsMapping.get(ds100)
                        if (station == null) {
                            println("no station found for file: " + file.name)
                        }
                    }
                }

            }
            //return TimeTable(trains, stationsMapping.values.toList())
            return null
        }

    }

}