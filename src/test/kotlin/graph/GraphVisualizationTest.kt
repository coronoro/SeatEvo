package graph

import com.mxgraph.layout.mxCircleLayout
import com.mxgraph.util.mxCellRenderer
import com.mxgraph.view.mxGraph
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.assertTrue


class GraphVisualizationTest {

    var g = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)

    @BeforeEach
    fun init() {


        val x1 = "x1"
        val x2 = "x2"
        val x3 = "x3"

        g.addVertex(x1)
        g.addVertex(x2)
        g.addVertex(x3)

        g.addEdge(x1, x2)
        g.addEdge(x2, x3)
        g.addEdge(x3, x1)
    }

    @Test
    fun test() {
        val graphAdapter = JGraphXAdapter<String, DefaultEdge>(g)
        val layout = mxCircleLayout(graphAdapter as mxGraph?)
        layout.execute(graphAdapter.defaultParent)

        val image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2.0, Color.WHITE, true, null)

        val resource = GraphVisualizationTest::class.java.getResource("/graph.png")
        val imgFile = File(resource.toURI())
        imgFile.createNewFile()
        ImageIO.write(image, "PNG", imgFile)

        assertTrue(imgFile.exists())
    }

}