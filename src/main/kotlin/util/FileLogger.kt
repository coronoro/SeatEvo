package util

import java.io.File
import java.io.FileWriter
import java.io.IOException

object FileLogger {

    private val file = File("C:\\Users\\Coronoro\\Desktop\\log.txt")

    fun write(message:String){
        try {
            val fw = FileWriter(file, true)
            fw.write(message)
            fw.close()
        } catch (e: IOException) {
        }
    }

}