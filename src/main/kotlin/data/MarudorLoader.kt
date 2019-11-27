package data

import marudor.MarudorApi
import model.Station
import model.Train
import model.Wagon
import java.util.*

object MarudorLoader {

    fun loadICE(){
        var stations = mutableListOf<Station>()
        var trains = mutableListOf<Train>()

        var stationsQueue = ArrayDeque<String>()
        var processedStations = mutableListOf<String>()
        stationsQueue.add("Leipzig HBF")
        processedStations.add("Leipzig HBF")


        while (!stationsQueue.isEmpty()){
            val search = MarudorApi.searchStations(stationsQueue.pop())
            if (search.isEmpty()) {
                val station = search.get(0)
                stations.add(Station(station.title, station.id))
                val departuresInfo = MarudorApi.getDeparturesInfo(station.id)
                if (departuresInfo != null){
                    departuresInfo.departures.forEach { departure ->
                        if ("ice".equals(departure.train.type.toLowerCase())){
                            // only ICE
                            var wagons = mutableListOf<Wagon>()
                            val trainId = departure.train.number
                            trains.add(Train(wagons, trainId))
                            departure.route.forEach { route ->
                                if (!processedStations.contains(route.name)){

                                    stationsQueue.add(route.name)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}