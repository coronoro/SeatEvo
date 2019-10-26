package util

import com.mxgraph.layout.mxCircleLayout
import com.mxgraph.util.mxCellRenderer
import com.mxgraph.view.mxGraph
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.AbstractBaseGraph
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import java.awt.Color
import java.io.File
import java.net.URI
import javax.imageio.ImageIO

object GraphUtil {

    fun <V,E> visualize(graph: AbstractBaseGraph<V, E>, picturePath: URI){

        val graphAdapter = JGraphXAdapter<V, E>(graph)
        val layout = mxCircleLayout(graphAdapter as mxGraph?)
        layout.execute(graphAdapter.defaultParent)
        val image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2.0, Color.WHITE, true, null)

        val imgFile = File(picturePath)
        imgFile.createNewFile()
        ImageIO.write(image, "PNG", imgFile)
    }

    fun <V,E>visualize(graph: DefaultDirectedGraph<V, E>){
        val resource = GraphUtil::class.java.getResource("/graph.png")
        val uri = resource.toURI()
        if (uri != null){
            visualize(graph, uri)
        }
    }

}