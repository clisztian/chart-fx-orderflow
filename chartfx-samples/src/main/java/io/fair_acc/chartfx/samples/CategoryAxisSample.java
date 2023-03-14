package io.fair_acc.chartfx.samples;

import java.text.DateFormatSymbols;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import io.fair_acc.chartfx.plugins.DataPointTooltip;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.AxisLabelOverlapPolicy;
import io.fair_acc.chartfx.axes.spi.CategoryAxis;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.plugins.EditAxis;
import io.fair_acc.chartfx.plugins.ParameterMeasurements;
import io.fair_acc.chartfx.plugins.Zoomer;
import io.fair_acc.chartfx.renderer.LineStyle;
import io.fair_acc.chartfx.renderer.spi.ErrorDataSetRenderer;
import io.fair_acc.dataset.spi.DefaultErrorDataSet;
import io.fair_acc.dataset.testdata.spi.RandomDataGenerator;
import javafx.util.StringConverter;

/**
 * @author rstein
 */
public class CategoryAxisSample extends Application {
    private static final int N_SAMPLES = 400000;

    @Override
    public void start(final Stage primaryStage) {

        Random rng = new Random(23);

        final StackPane root = new StackPane();
        final CategoryAxis xAxis = new CategoryAxis("months");
        // xAxis.setTickLabelRotation(90);
        // alt:
        xAxis.setOverlapPolicy(AxisLabelOverlapPolicy.SKIP_ALT);
        xAxis.setMaxMajorTickLabelCount(N_SAMPLES + 1);
        final DefaultNumericAxis yAxis = new DefaultNumericAxis("yAxis");

        final XYChart lineChartPlot = new XYChart(xAxis, yAxis);
        // set them false to make the plot faster
        lineChartPlot.setAnimated(false);
        lineChartPlot.getRenderers().clear();
        // lineChartPlot.getRenderers().add(new ReducingLineRenderer());
        final ErrorDataSetRenderer renderer = new ErrorDataSetRenderer();
        renderer.setPolyLineStyle(LineStyle.NORMAL);
        renderer.setPolyLineStyle(LineStyle.HISTOGRAM);
        lineChartPlot.getRenderers().add(renderer);
        lineChartPlot.legendVisibleProperty().set(true);

        lineChartPlot.getPlugins().add(new ParameterMeasurements());
        lineChartPlot.getPlugins().add(new EditAxis());
        final Zoomer zoomer = new Zoomer();
        final DataPointTooltip tooltip = new DataPointTooltip();

        // zoomer.setSliderVisible(false);
        // zoomer.setAddButtonsToToolBar(false);
        lineChartPlot.getPlugins().add(zoomer);
        lineChartPlot.getPlugins().add(tooltip);

        final DefaultErrorDataSet dataSet = new DefaultErrorDataSet("myData");
        final DefaultErrorDataSet dataSet2 = new DefaultErrorDataSet("myData2");

        final Scene scene = new Scene(root, 800, 600);

        final DateFormatSymbols dfs = new DateFormatSymbols(Locale.ENGLISH);
        final List<String> categories = new ArrayList<>(Arrays.asList(Arrays.copyOf(dfs.getShortMonths(), 12)));
        for (int i = categories.size(); i < CategoryAxisSample.N_SAMPLES; i++) {
            categories.add("Month" + (i + 1));
        }

        // setting the category via axis forces the axis' category
        // N.B. disable this if you want to use the data set's categories
        xAxis.setCategories(categories);

        double y = 0;
        for (int n = 0; n < CategoryAxisSample.N_SAMPLES; n++) {
            y += RandomDataGenerator.random() - 0.5;
            final double ex = 0.0;
            final double ey = 0.1;

            if(rng.nextFloat() < .3) {
                dataSet.add(n, y, ex, ey);
                dataSet.addDataLabel(n, "my second data point at " + n + "\n now I need to put \n a bunch of stuff here \n to see if it's all rendered" + n);
            }
            else {
                dataSet2.add(n, y, ex, ey);
                dataSet2.addDataLabel(n, "my data point at " + n + "\n now I need to put \n a bunch of stuff here \n to see if it's all rendered" + n);
            }

        }

        // setting the axis categories to null forces the first data set's
        // category
        // enable this if you want to use the data set's categories
        // xAxis.setCategories(null);

        lineChartPlot.getDatasets().addAll(dataSet, dataSet2);
        root.getChildren().add(lineChartPlot);




        primaryStage.setTitle(this.getClass().getSimpleName());
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(evt -> Platform.exit());
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }
}