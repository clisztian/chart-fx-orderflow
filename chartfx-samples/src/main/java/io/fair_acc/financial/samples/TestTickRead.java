package io.fair_acc.financial.samples;

import io.fair_acc.dataset.event.AddedDataEvent;
import io.fair_acc.financial.samples.dos.OHLCVItem;
import io.fair_acc.financial.samples.service.SimpleOhlcvReplayDataSet;
import io.fair_acc.financial.samples.service.TickDataFinishedException;
import io.fair_acc.financial.samples.service.TickOhlcvDataProvider;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static ar.com.hjg.pngj.PngHelperInternal.LOGGER;


public class TestTickRead {

    static final DateTimeFormatter dft = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");

    protected AtomicBoolean running = new AtomicBoolean(false);
    protected AtomicBoolean paused = new AtomicBoolean(false);
    protected transient TickOhlcvDataProvider tickOhlcvDataProvider;
    private BufferedReader br;

    protected final transient Object pauseSemaphore = new Object();

    //String filename = "data/CH0012005267_20220209.csv";
    public TestTickRead() {


    }

    //function that reads the tick data from the file line by line and returns the data in a list of OHLCVItem
    public static void main(String[] args) {

        TestTickRead testTickRead = new TestTickRead();

        try {
            testTickRead.createStream("/home/lisztian/FXProjects/chart-fx/chartfx-samples/src/main/resources/data/CH0012005267_20220209.csv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void createStream(String filename) throws FileNotFoundException {

        br = new BufferedReader(new FileReader(filename));

        tickOhlcvDataProvider = createTickDataReplayStream();

        start();

    }



    public TickOhlcvDataProvider createTickDataReplayStream() {


        return new TickOhlcvDataProvider() {
            private OHLCVItem prevItem = null;
            private OHLCVItem item = null;

            @Override
            public OHLCVItem get() throws TickDataFinishedException, IOException {

                long prevTime = prevItem != null ? prevItem.getTimeStamp().getTime() : 0L;
                long time = item != null ? item.getTimeStamp().getTime() : 0L;
                long waitingTime = Math.round((time - prevTime) / 1.0 );

                if(prevItem == null) {
                    waitingTime = 0L;
                }

                LOGGER.info("Waiting time: " + waitingTime);

                waitingTime = Math.max(1, waitingTime);
                try {
                    // waiting to send next sample - simulation of replay processing
                    Thread.sleep(waitingTime);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }

                prevItem = item;
                item = loadOhlcvItemRealtime();

                if (item == null) {
                    throw new TickDataFinishedException("No more data available");
                }

                return item;
            }
        };





    }



    public OHLCVItem loadOhlcvItemRealtime() throws IOException {

        String line = br.readLine();
        if (line == null) {
            return null;
        }

        String[] split = line.split(",");

        var timestamp = convertLocalDateTime(split[0]);
        float open = 0f;
        float high = Float.parseFloat(split[1].trim());
        float low = high;
        float close = high;

        boolean isBid = split[2].contains("RemovedLiquidity");

        long totalVolume = Long.parseLong(split[3].trim());
        long bidVolume = isBid ? totalVolume : 0;
        long askVolume = isBid ? 0 : totalVolume;

        return new OHLCVItem(timestamp, open, high, low, close, totalVolume, 0, askVolume, bidVolume);

    }

    public void start() {
        paused.set(false);
        running.set(true);
        new Thread(getDataUpdateTask()).start();
    }

    protected Runnable getDataUpdateTask() {
        return () -> {
            while (running.get()) {
                try {
                    tick();

                } catch (TickDataFinishedException e) {
                    LOGGER.info("The OHLCV data stream is finished.");
                    stop();
                } catch (ClosedChannelException e) {
                    LOGGER.info("The OHLCV data channel is already closed.");
                } catch (Exception e) { // NOSONAR NOPMD
                    throw new IllegalArgumentException(e);
                }
            }
        };
    }

    protected void tick() throws Exception {
        OHLCVItem increment = tickOhlcvDataProvider.get();

        LOGGER.info(increment.toString());
    }

    public void close() throws IOException {
        br.close();
    }

    public void stop() {
        if (running.get()) {
            running.set(false);
            if (paused.get()) {
                pauseResume();
            }
            try {
                close();
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    public void pauseResume() {
        if (paused.get()) {
            paused.set(false);
            synchronized (pauseSemaphore) {
                pauseSemaphore.notifyAll();
            }
        } else {
            paused.set(true);
        }
    }

    public static Date convertLocalDateTime(String timestamp) {

            LocalDateTime dt = LocalDateTime.parse(timestamp, dft);
            Date date = Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());

            return date;
    }

}
