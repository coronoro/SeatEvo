package evo

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.BasicStroke
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JFrame


class EvoAnalysis: JFrame() {

    val minDataSet = XYSeries("min")
    val maxDataSet = XYSeries("max")
    val minAverageTravelDistance = XYSeries("minAvgTravelDistance")
    val maxAverageTravelDistance = XYSeries("maxAvgTravelDistance")
    val maxWagonOverload = XYSeries("wagonOverloadMax")
    val minWagonOverload = XYSeries("wagonOverloadMin")

    fun showChart(title:String = "Population overview"){
        var dataset = XYSeriesCollection();
        dataset.addSeries(minDataSet);
        dataset.addSeries(maxDataSet);

        val chart = ChartFactory.createXYLineChart(title, "cycle", "fitness", dataset)

        val plot = chart.xyPlot
        var renderer = XYLineAndShapeRenderer()
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesStroke(0, BasicStroke(0.5f));
        renderer.setSeriesPaint(1, Color.BLUE);
        renderer.setSeriesStroke(1, BasicStroke(0.5f));

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.white);


        val chartPanel = ChartPanel(chart)
        chartPanel.border = BorderFactory.createEmptyBorder(15, 15, 15, 15)
        chartPanel.background = Color.white

        add(chartPanel)

        pack()
        setTitle("Line chart")
        setLocationRelativeTo(null)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)


    }

}