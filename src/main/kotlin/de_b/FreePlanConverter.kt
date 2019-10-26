package de_b

import freeplan.Location
import model.Station

object  FreePlanConverter {

    fun convert(location:Location): Station {
        return Station(location.name, location.id)
    }

    fun convertAll(locations:List<Location>): List<Station> {
        return locations.map { location -> convert(location)}
    }

}