package model.route

import model.Identifiable

abstract class Route<T>(val stops: List<T>, override val id: Int) : Identifiable{

    private var iterator = stops.iterator()

    fun next(): T {
        return iterator.next()
    }

    fun anew(){
        iterator = stops.iterator()
    }




}