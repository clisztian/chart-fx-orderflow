package io.fair_acc.financial.samples.service;

import io.fair_acc.financial.samples.dos.Interval;
import io.fair_acc.financial.samples.dos.OHLCVItem;
import javafx.beans.property.DoubleProperty;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import static ar.com.hjg.pngj.PngHelperInternal.LOGGER;

/**
 * Provides a service for footprint heatmaps for actual buy side tick data of the form
 * timestamp, price, liquidity, volume
 *
 * where price is the price of the tick, liquidity is the liquidity of the tick (Removed is maker)
 */
public class TickDataService implements AutoCloseable {

    static final DateTimeFormatter dft = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS");

    protected AtomicBoolean running = new AtomicBoolean(false);
    protected AtomicBoolean paused = new AtomicBoolean(false);
    protected transient TickOhlcvDataProvider tickOhlcvDataProvider;
    private BufferedReader br;

    protected final transient Object pauseSemaphore = new Object();

    public void openNewChannel(String resource) throws IOException {
        br = new BufferedReader(new FileReader(resource));
    }


    /**
     * Create instance of tick ohlcv data provider for replay stream
     *
     * @param requiredTimestamps [from, to] interval
     * @param replayStarTime     Date - point of replay timing start
     * @param replaySpeed        multiply of replay simulation (with real timing!)
     * @return tick data provider
     */
    public TickOhlcvDataProvider createTickDataReplayStream(@NotNull final Interval<Calendar> requiredTimestamps,
                                                            @NotNull final Date replayStarTime,
                                                            DoubleProperty replaySpeed) {


        //puts the file reader in the correct position
        ensureNearestTimestampPosition(requiredTimestamps.from.getTime());

        System.out.println("Replay start time: " + replayStarTime);
        System.out.println("Required timestamps: " + requiredTimestamps.from.getTime() + " - " + requiredTimestamps.to.getTime());

        return new TickOhlcvDataProvider() {
            private OHLCVItem prevItem = null;
            private OHLCVItem item = null;

            @Override
            public OHLCVItem get() throws TickDataFinishedException, IOException {

                OHLCVItem newItem = loadOhlcvItemRealtime();
                if (newItem == null)  {
                    throw new TickDataFinishedException("No more data available");
                }

                // waiting to send next sample - simulation of replay processing
                if(newItem.getTimeStamp().after(replayStarTime)) {

                    long prevTime = prevItem != null ? prevItem.getTimeStamp().getTime() : 0L;
                    long time = item != null ? item.getTimeStamp().getTime() : 0L;
                    long waitingTime = Math.round((time - prevTime) / replaySpeed.get());

                    if (prevItem == null) {
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
                }

                prevItem = item;
                item = newItem;

                if (item == null || item.getTimeStamp().after(requiredTimestamps.to.getTime()))  {
                    throw new TickDataFinishedException("No more data available");
                }

                return item;
            }
        };





    }


    /**
     * cycle through the data until the timestamp is reached
     * @param timestamp
     */
    public OHLCVItem ensureNearestTimestampPosition(Date timestamp) {

        OHLCVItem item = null;
        try {
            while (item == null || item.getTimeStamp().before(timestamp)) {
                item = loadOhlcvItemRealtime();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return item;
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



    public void stop() {
        if (running.get()) {
            running.set(false);
            if (paused.get()) {
                pauseResume();
            }
            try {
                close();
            } catch (Exception e) {
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







    @Override
    public void close() throws Exception {
        br.close();
    }

    public void closeActualChannel() {
        try {
            close();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
