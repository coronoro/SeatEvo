package freeplan

import org.restlet.representation.Representation
import org.restlet.resource.ClientResource
import java.net.URL

abstract class AbstractRestApi {

    protected fun getRepresentation(url: URL): Representation? {
        var get: Representation? = null
        println("get from: "+url.toString())
        val clientResource = ClientResource(url.toURI())
        val status = clientResource.status
        println(status)
        try {
            get = clientResource.get()
        }catch (e: Exception){
            throw e
        }
        return get
    }
}