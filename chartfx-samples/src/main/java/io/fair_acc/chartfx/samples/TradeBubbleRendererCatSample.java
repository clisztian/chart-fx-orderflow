package io.fair_acc.chartfx.samples;


import io.fair_acc.chartfx.XYChart;
import io.fair_acc.chartfx.axes.AxisLabelOverlapPolicy;
import io.fair_acc.chartfx.axes.spi.CategoryAxis;
import io.fair_acc.chartfx.axes.spi.DefaultNumericAxis;
import io.fair_acc.chartfx.plugins.DataPointTooltip;
import io.fair_acc.chartfx.plugins.Zoomer;
import io.fair_acc.chartfx.renderer.ContourType;
import io.fair_acc.chartfx.renderer.ErrorStyle;
import io.fair_acc.chartfx.renderer.LineStyle;
import io.fair_acc.chartfx.renderer.datareduction.DefaultDataReducer;
import io.fair_acc.chartfx.renderer.spi.ContourDataSetRenderer;
import io.fair_acc.chartfx.renderer.spi.ErrorDataSetRenderer;
import io.fair_acc.chartfx.renderer.spi.HistogramRenderer;
import io.fair_acc.chartfx.renderer.spi.utils.ColorGradient;
import io.fair_acc.chartfx.ui.geometry.Side;
import io.fair_acc.chartfx.utils.AxisSynchronizer;
import io.fair_acc.dataset.DataSet;
import io.fair_acc.dataset.spi.DataSetBuilder;
import io.fair_acc.dataset.spi.DefaultDataSet;
import io.fair_acc.dataset.spi.Histogram;
import io.fair_acc.dataset.spi.TransposedDataSet;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class TradeBubbleRendererCatSample extends Application {

    Random rng = new Random(23);
    List<Color> my_colors;
    private DefaultDataSet buySet;

    private DefaultDataSet spreadSet;
    private long now;

    protected Timer timer;

    public static int UPDATE_PERIOD = 60; // [ms]

    //create date formatter
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS");

    public TimerTask getNewData(final XYChart chart) {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    generateData(chart);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void generateData(final XYChart chart) throws ClassNotFoundException, IllegalAccessException {

        final DefaultDataSet buySet = new DefaultDataSet("BUYS");
        final DefaultDataSet sellSet = new DefaultDataSet("SELLS");



        buySet.setStyle("markerSize=5; markerColor=rgb(39, 23, 25); markerType=rectangle;");
        sellSet.setStyle("markerSize=5; markerColor=rgb(24, 23, 45); markerType=rectangle;");
        long now = System.currentTimeMillis();
        Color rand_col = my_colors.get(rng.nextInt(my_colors.size() - 1));
        Color rand_col_2 = my_colors.get(rng.nextInt(my_colors.size() - 1));

        for(int i = 0; i < 15000; i ++) {

            double price = rng.nextGaussian()*10 + 100;
            now += rng.nextInt(50);
            double t = (double)now;
            double markerSize = rng.nextDouble()*2;
            String label = "BUY" + i;

            String size = "markerSize=" + 10 * Math.sqrt(markerSize) + "; index="
                    + i + ";";

            buySet.add(t, price, label);


            int red = (int)(rand_col.getRed()*255);
            int green = (int)(rand_col.getGreen()*255);
            int blue = (int)(rand_col.getBlue()*255);

            //buySet.addDataStyle(i, size + "markerColor=rgb(" + red + "," + green + "," + blue + "); markerType=circle;");
            if(rng.nextFloat() < .2) {
                buySet.addDataStyle(i, size + "markerColor=rgb(" + red + "," + green + "," + blue + "); markerType=rectangle;");
            }

            price = rng.nextGaussian()*10 + 100;
            now += rng.nextInt(50);
            t = (double)now;
            label = "SELL" + i;
            markerSize = rng.nextDouble()*2;
            size = "markerSize=" + 10 * Math.sqrt(markerSize) + "; index=" + i + ";";


            red = (int)(rand_col_2.getRed()*255);
            green = (int)(rand_col_2.getGreen()*255);
            blue = (int)(rand_col_2.getBlue()*255);

            sellSet.add(t, price, label);
            //sellSet.addDataStyle(i, size + "markerColor=rgb(110, 230, 130); markerType=circle;");
            if(rng.nextFloat() < .2) {
                sellSet.addDataStyle(i, size + "markerColor=rgb(" + red + "," + green + "," + blue + "); markerType=rectangle;");
            }


        }

        Platform.runLater(() -> {
            chart.getRenderers().get(1).getDatasets().setAll(buySet, sellSet);
            chart.requestLayout();
        });

    }




    @Override
    public void start(Stage primaryStage) throws Exception {

        my_colors = allColors();
        // create stops
        Stop[] stop = {new Stop(0, Color.rgb(150, 1, 21, .4)),
                new Stop(1, Color.rgb(130, 178, 255, .3))};

        // create a Linear gradient object
        RadialGradient rg = new RadialGradient(0, 0, 0.5, 0.5, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(16, 54, 84)),
                new Stop(1,Color.rgb(2, 9, 18)));



        Background back = new Background(new BackgroundFill(rg, CornerRadii.EMPTY, Insets.EMPTY));


        final BorderPane root = new BorderPane();
        final Scene scene = new Scene(root, 800, 600);

        final CategoryAxis xAxis1 = new CategoryAxis("time");
        //final DefaultNumericAxis xAxis1 = new DefaultNumericAxis("time", "iso");
        xAxis1.setOverlapPolicy(AxisLabelOverlapPolicy.SKIP_ALT);
        final DefaultNumericAxis yAxis1 = new DefaultNumericAxis("y-axis", "a.u.");



        final DefaultNumericAxis zAxis = new DefaultNumericAxis();
        zAxis.setAnimated(false);
        zAxis.setAutoRangeRounding(false);
        zAxis.setName("z Amplitude");
        zAxis.setAutoRanging(true);
        zAxis.setSide(Side.RIGHT);
        zAxis.getProperties().put(Zoomer.ZOOMER_OMIT_AXIS, true);


        //for xAxis1 we want to use a date axis, apply a LocalDateTime formatter to the axis
//        xAxis1.setTickLabelFormatter(new StringConverter<Number>() {
//            @Override
//            public String toString(Number object) {
//
//                LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(object.longValue()), ZoneId.systemDefault());
//                return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
//            }
//
//            @Override
//            public Number fromString(String string) {
//                return 0;
//            }
//        });



        final XYChart chart = new XYChart(xAxis1, yAxis1);
        chart.getAxes().add(zAxis);
        chart.getGridRenderer().getHorizontalMajorGrid().setVisible(false);
        chart.getGridRenderer().getHorizontalMinorGrid().setVisible(false);
        chart.getGridRenderer().getVerticalMajorGrid().setVisible(false);
        chart.getGridRenderer().getVerticalMajorGrid().setVisible(false);



        chart.legendVisibleProperty().set(false);
        chart.getPlugins().add(new Zoomer());
        chart.getPlugins().add(new DataPointTooltip());

        chart.setAnimated(false);
        chart.setBackground(back);

        xAxis1.setAutoRangeRounding(false);
        xAxis1.setTimeAxis(true);
        yAxis1.setAutoRangeRounding(true);
        yAxis1.setSide(Side.RIGHT);
        yAxis1.setForceZeroInRange(false);

//        final DefaultDataSet buySet = new DefaultDataSet("BUYS");
//        final DefaultDataSet sellSet = new DefaultDataSet("SELLS");
        buySet = new DefaultDataSet("BUYS");
        spreadSet = new DefaultDataSet("SPREAD}");


        ArrayList<String> timestamps = new ArrayList<>();
        buySet.setStyle("markerSize=5; markerColor=rgb(139, 123, 225); markerType=circle;");


        LocalDateTime now = LocalDateTime.now();
        double price = rng.nextGaussian()*10 + 100;

        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;

        for(int i = 0; i < 300000; i ++) {

            price += rng.nextGaussian()*10;
            max = Math.max(max, price);
            min = Math.min(min, price);

            int n_millis = rng.nextInt(5000);

            if(rng.nextFloat() < .93) {
                now = now.plus(n_millis, ChronoUnit.MILLIS);
            }




            timestamps.add(now.format(formatter));

            //get number of milliseconds since epoch
            //double t = (double)now.toInstant(ZoneOffset.UTC).toEpochMilli();

            double markerSize = rng.nextDouble()*16;
            String label = "BUY " + i + " " + price + "\n" + now.format(formatter) + "\n" + markerSize + "\n";

            String size = "markerSize=" +  Math.floor(markerSize) + "; index="
                    + i + ";";



            int red = 179;
            int green = 226;
            int blue = 255;

            buySet.add(i, price, label);
            buySet.addDataStyle(i, size + "markerColor=rgb(" + red + ", " + green + ", " + blue + "); markerType=circle;");

            if(rng.nextFloat() < .3) {

                label = "SPREAD" + i;
                spreadSet.add(i, price, "spread " + i);
            }


            if(i%5000 == 0) {
                now = now.plusDays(1);
            }

            if(i%20000 == 0) {
                now = now.plusDays(1);
            }

        }

        xAxis1.setCategories(timestamps);;


        double[] timestamp = new double[buySet.getDataCount()];
        double[] price_heat = new double[1000];
        double[][] z = new double[1000][buySet.getDataCount()];
        double dx = (max - min)/price_heat.length;
        double start = min;
        for(int i = 0; i < price_heat.length; i++) {
            price_heat[i] = start;
            start += dx;
        }

//        for(int i = 0; i < buySet.getDataCount(); i++) {
//            timestamp[i] = buySet.get(DataSet.DIM_X, i);
//            for(int j = 0; j < price_heat.length; j++) {
//                z[j][i] = Math.sin(2.0 * Math.PI * 1.0 * j / price_heat.length) * Math.cos(2.0 * Math.PI * 3.0 * i / timestamp.length);
//            }
//        }
//
//        DataSet heat = new DataSetBuilder("demoDataSet").setValues(DataSet.DIM_X, timestamp).setValues(DataSet.DIM_Y, price_heat).setValues(DataSet.DIM_Z, z).build();



        final ErrorDataSetRenderer tradeRenderer = new ErrorDataSetRenderer();
        tradeRenderer.setMarkerSize(5);

        tradeRenderer.setPolyLineStyle(LineStyle.NONE);
        tradeRenderer.setErrorType(ErrorStyle.NONE);
        tradeRenderer.setDrawMarker(true);
        tradeRenderer.setDrawBubbles(false);
        tradeRenderer.setAssumeSortedData(true); // !! important since DS is likely unsorted
        tradeRenderer.setPointReduction(true);
//        final DefaultDataReducer reductionAlgorithm = (DefaultDataReducer) tradeRenderer.getRendererDataReducer();
//        reductionAlgorithm.setMinPointPixelDistance(1);


        final ErrorDataSetRenderer spreadRenderer = new ErrorDataSetRenderer();
        spreadRenderer.setPolyLineStyle(LineStyle.STAIR_CASE);
        spreadRenderer.setErrorType(ErrorStyle.NONE);
        spreadRenderer.setDrawMarker(false);
        spreadRenderer.setDrawBubbles(false);
        spreadRenderer.setAssumeSortedData(true); // !! important since DS is likely unsorted
        spreadRenderer.setPointReduction(true);


        final DefaultNumericAxis yAxis2 = new DefaultNumericAxis("y-axis", "a.u.");
        yAxis2.setForceZeroInRange(false);
        yAxis2.setSide(Side.LEFT);

//        final ContourDataSetRenderer contourRenderer = new ContourDataSetRenderer();
//        contourRenderer.getAxes().addAll(xAxis1, yAxis2, zAxis);
//        chart.getAxes().add(yAxis2);
//        chart.getRenderers().setAll(contourRenderer);
//        contourRenderer.setContourType(ContourType.HEATMAP); // false: for color gradient map, true: for true contour map
//        contourRenderer.setColorGradient(ColorGradient.BLACK_WHITE);
//        contourRenderer.getDatasets().add(heat);



        chart.getRenderers().setAll(spreadRenderer, tradeRenderer);
        tradeRenderer.getDatasets().addAll(buySet);
        spreadRenderer.getDatasets().addAll(spreadSet);
        //((DefaultNumericAxis)chart.getAxes().get(1)).setForceZeroInRange(false);

        /**
         * building volume renderer
         */
        List<DataSet> dataSets = new ArrayList<>();
        Histogram volSetBuy = new Histogram("Volume",price_heat);
        Histogram volSetSell = new Histogram("VolumeSell",price_heat);
        for(int j = 0; j < price_heat.length; j++) {
            volSetBuy.fill(price_heat[j], rng.nextInt(100));
            volSetSell.fill(price_heat[j], rng.nextInt(100));
        }
        final HistogramRenderer renderer = new HistogramRenderer();
        renderer.setDrawBars(true);
        renderer.setPolyLineStyle(LineStyle.NONE);
        dataSets.add(volSetBuy);
        dataSets.add(volSetSell);
        dataSets.set(0, TransposedDataSet.transpose(dataSets.get(0)));
        dataSets.set(1, TransposedDataSet.transpose(dataSets.get(1)));

        final DefaultNumericAxis volumeAxis = new DefaultNumericAxis("time", "iso");
        volumeAxis.setOverlapPolicy(AxisLabelOverlapPolicy.SKIP_ALT);
        final DefaultNumericAxis price3 = new DefaultNumericAxis("y-axis", "a.u.");
        price3.setForceZeroInRange(false);
        renderer.getDatasets().setAll(dataSets);

        final XYChart volChart;
        volChart = new XYChart(volumeAxis, price3);
        volChart.getRenderers().set(0, renderer);
        volChart.setLegendVisible(false);
        volChart.setPrefWidth(150);

        volChart.getGridRenderer().getHorizontalMajorGrid().setVisible(false);
        volChart.getGridRenderer().getHorizontalMinorGrid().setVisible(false);
        volChart.getGridRenderer().getVerticalMajorGrid().setVisible(false);
        volChart.getGridRenderer().getVerticalMajorGrid().setVisible(false);

        volumeAxis.setSide(Side.LEFT);

        final Button startTimer = new Button("timer");
        startTimer.setOnAction(evt -> {
            if (timer == null) {
                timer = new Timer("sample-update-timer", true);
                timer.scheduleAtFixedRate(getTask(), 0, UPDATE_PERIOD);
            } else {
                timer.cancel();
                timer = null; // NOPMD
            }
        });


//        chart.getXAxis().addListener(event -> {
//            AxisChangeEvent aevent = (AxisChangeEvent) event;
//            DefaultNumericAxis axis = (DefaultNumericAxis) event.getSource();
//            System.out.println(axis.getMax() + " " + axis.getMin());
//        });


        final Button newDataSet = new Button("new DataSet");
        newDataSet.setOnAction(evt -> Platform.runLater(getNewData(chart)));

        chart.setPrefSize(1000, 800);

        AxisSynchronizer sync = new AxisSynchronizer();
        sync.add(chart.getYAxis());
        sync.add(volumeAxis);

        BorderPane pane = new BorderPane();
        pane.setCenter(chart);
        pane.setTop(startTimer);
        pane.setLeft(volChart);

//        StackPane pane = new StackPane(chart);
//        VBox vBox = new VBox(startTimer, pane);
//
//        VBox.setVgrow(pane, Priority.ALWAYS);

        Scene myscene = new Scene(pane, 1000, 800);
        myscene.getStylesheets().add(getClass().getClassLoader().getResource("css/WhiteOnBlack.css").toExternalForm());
        //myscene.getStylesheets().setAll(Objects.requireNonNull(CssStylingSample.class.getResource("css/WhiteOnBlack.css"), "could not load css file: " + "WhiteOnBlack.css").toExternalForm());
        primaryStage.setTitle(this.getClass().getSimpleName());
        primaryStage.setScene(myscene);
        primaryStage.setOnCloseRequest(evt -> Platform.exit());
        primaryStage.show();


    }

    public void addTradeSpread() {


        if(rng.nextFloat() < .2f) {

            int i = buySet.getDataCount();
            double price = rng.nextGaussian()*10 + 100;
            now += rng.nextInt(50);
            double t = (double)now;
            double markerSize = rng.nextDouble()*2;
            String label = "BUY" + i;



            String size = "markerSize=" + 10 * Math.sqrt(markerSize) + "; index="
                    + i + ";";

            buySet.add(t, price, label);

            Color rand_col = Color.CORNFLOWERBLUE;
            int red = (int)(rand_col.getRed()*255);
            int green = (int)(rand_col.getGreen()*255);
            int blue = (int)(rand_col.getBlue()*255);

            buySet.addDataStyle(i, size + "markerColor=rgb(" + red + "," + green + "," + blue + "); markerType=rectangle;");

        }



        if(rng.nextFloat() < .7f) {

            int i = spreadSet.getDataCount();
            double price = rng.nextGaussian() * 10 + 100;
            now += rng.nextInt(50);
            double t = (double) now;
            String label = "SPREAD" + i;
            spreadSet.add(t, price, label);

        }

    }

    protected TimerTask getTask() {
        return new TimerTask() {

            @Override
            public void run() {
                addTradeSpread();
            }
        };
    }


    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        Application.launch(args);
    }

    private static List<Color> allColors() throws ClassNotFoundException, IllegalAccessException {
        List<Color> colors = new ArrayList<>();
        Class clazz = Class.forName("javafx.scene.paint.Color");
        if (clazz != null) {
            Field[] field = clazz.getFields();
            for (int i = 0; i < field.length; i++) {
                Field f = field[i];
                Object obj = f.get(null);
                if(obj instanceof Color){
                    colors.add((Color) obj);
                }

            }
        }
        return colors;
    }
}