package model.route

import model.Identifiable

abstract class Route<T>(val waypoints: List<T>, override val id: String) : Identifiable {

    private var iterator = waypoints.iterator()

    fun next(): T {
        return iterator.next()
    }

    fun anew() {
        iterator = waypoints.iterator()
    }


}