package tq.jmhplugin.graphReport;

import java.util.Map;
import javax.swing.BorderFactory;
import com.intellij.ui.JBColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class BarChart {
    Map<String, Double> scoreMap;

    public BarChart(Map<String, Double> scoreMap) {
        this.scoreMap = scoreMap;
    }

    private CategoryDataset createDataset() {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        scoreMap.forEach((key, value) -> dataset.setValue(value, "Benchmark Scores", key));
        return dataset;
    }

    public ChartPanel createChartPanel() {

        CategoryDataset dataset = createDataset();

        JFreeChart barChart = ChartFactory.createBarChart(
                "Benchmark Scores",
                "",
                "Scores",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);
        BarRenderer barrenderer = new BarRenderer();
        barrenderer.setMaximumBarWidth(0.1);
        barrenderer.setMinimumBarLength(0.1);
        CategoryPlot categoryplot = (CategoryPlot) barChart.getPlot();
        categoryplot.setRenderer(barrenderer);
        CategoryAxis axis = categoryplot.getDomainAxis();
        axis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(JBColor.white);
        return chartPanel;


    }

}
