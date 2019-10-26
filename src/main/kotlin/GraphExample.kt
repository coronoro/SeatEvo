/*
 * (C) Copyright 2003-2018, by Barak Naveh and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */

import org.jgrapht.Graph
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleGraph
import org.jgrapht.io.ComponentNameProvider
import org.jgrapht.io.DOTExporter
import org.jgrapht.traverse.DepthFirstIterator
import java.io.StringWriter
import java.net.URI
import java.net.URISyntaxException
import java.rmi.server.ExportException


/**
 * A simple introduction to using JGraphT.
 *
 * @author Barak Naveh
 */
object HelloJGraphT {

    /**
     * The starting point for the demo.
     *
     * @param args ignored.
     *
     * @throws URISyntaxException if invalid URI is constructed.
     * @throws ExportException if graph cannot be exported.
     */
    @Throws(URISyntaxException::class, ExportException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val stringGraph = createStringGraph()

        // note undirected edges are printed as: {<v1>,<v2>}
        println("-- toString output")
        println(stringGraph.toString())
        println()


        // create a graph based on URI objects
        val hrefGraph = createHrefGraph()

        // find the vertex corresponding to www.jgrapht.org
        val start = hrefGraph
            .vertexSet().stream().filter { uri -> uri.host == "www.jgrapht.org" }.findAny()
            .get()


        // perform a graph traversal starting from that vertex
        println("-- traverseHrefGraph output")
        traverseHrefGraph(hrefGraph, start)
        println()

        println("-- renderHrefGraph output")
        renderHrefGraph(hrefGraph)
        println()
    }

    /**
     * Creates a toy directed graph based on URI objects that represents link structure.
     *
     * @return a graph based on URI objects.
     */
    @Throws(URISyntaxException::class)
    private fun createHrefGraph(): Graph<URI, DefaultEdge> {

        val g = DefaultDirectedGraph<URI, DefaultEdge>(DefaultEdge::class.java)

        val google = URI("http://www.google.com")
        val wikipedia = URI("http://www.wikipedia.org")
        val jgrapht = URI("http://www.jgrapht.org")

        // add the vertices
        g.addVertex(google)
        g.addVertex(wikipedia)
        g.addVertex(jgrapht)

        // add edges to create linking structure
        g.addEdge(jgrapht, wikipedia)
        g.addEdge(google, jgrapht)
        g.addEdge(google, wikipedia)
        g.addEdge(wikipedia, google)


        return g
    }

    /**
     * Traverse a graph in depth-first order and print the vertices.
     *
     * @param hrefGraph a graph based on URI objects
     *
     * @param start the vertex where the traversal should start
     */
    private fun traverseHrefGraph(hrefGraph: Graph<URI, DefaultEdge>, start: URI) {
        val iterator = DepthFirstIterator(hrefGraph, start)
        while (iterator.hasNext()) {
            val uri = iterator.next()
            println(uri)
        }
    }

    /**
     * Render a graph in DOT format.
     *
     * @param hrefGraph a graph based on URI objects
     */
    @Throws(ExportException::class)
    private fun renderHrefGraph(hrefGraph: Graph<URI, DefaultEdge>) {

        // use helper classes to define how vertices should be rendered,
        // adhering to the DOT language restrictions
        val vertexIdProvider =  ComponentNameProvider<URI>() {
                it.host.replace('.', '_')
        }
        val vertexLabelProvider = ComponentNameProvider<URI>() {
                it.toString()
        }
        val exporter = DOTExporter<URI, DefaultEdge>(vertexIdProvider, vertexLabelProvider, null)
        val writer = StringWriter()
        exporter.exportGraph(hrefGraph, writer)
        println(writer.toString())
    }

    /**
     * Create a toy graph based on String objects.
     *
     * @return a graph based on String objects.
     */
    private fun createStringGraph(): Graph<String, DefaultEdge> {
        val g = SimpleGraph<String, DefaultEdge>(DefaultEdge::class.java)

        val v1 = "v1"
        val v2 = "v2"
        val v3 = "v3"
        val v4 = "v4"

        // add the vertices
        g.addVertex(v1)
        g.addVertex(v2)
        g.addVertex(v3)
        g.addVertex(v4)

        // add edges to create a circuit
        g.addEdge(v1, v2)
        g.addEdge(v2, v3)
        g.addEdge(v3, v4)
        g.addEdge(v4, v1)

        return g
    }
}// ensure non-instantiability.