package io.fair_acc.financial.samples;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class SCIDReader {

    private static boolean RUNNING = true;

    public static void main(String[] args) throws IOException, InterruptedException {

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");



        File f = new File("/home/lisztian/FXProjects/chart-fx/chartfx-samples/target/classes/io/fair_acc/chartfx/samples/financial/NQ-201609-GLOBEX.scid");
        FileInputStream fis = new FileInputStream(f);

        DataInputStream ds = new DataInputStream(fis);

        byte[] header = new byte[4];
        byte[] osm = new byte[4];
        ds.read(header);
        System.out.println(new String(header));

        ds.read(header);
        System.out.println("Header Size: " + getUInt32(header));

        ds.read(header);
        System.out.println("Record Size: " + getUInt32(header));

        ds.skipBytes(56 - (4 * 3));

        double dt;
        float open;
        float high;
        float low;
        float close;

        long numTrades;
        long totalVolume;
        long bidVolume;
        long askVolume;

        long start = System.currentTimeMillis();
        while (RUNNING) {
            while (ds.available() == 0) {
                // Tail the file
                System.out.print(".");
                Thread.sleep(100);
            }

            long dtt = ds.readLong();

            dt = Double.longBitsToDouble(Long.reverseBytes(dtt));
            open = reverse(ds.readFloat());
            high = reverse(ds.readFloat());
            low = reverse(ds.readFloat());
            close = reverse(ds.readFloat());

            ds.read(osm);
            numTrades = getUInt32(osm);
            ds.read(osm);
            totalVolume = getUInt32(osm);
            ds.read(osm);
            bidVolume = getUInt32(osm);
            ds.read(osm);
            askVolume = getUInt32(osm);



            System.out.println(Instant.ofEpochMilli(convertWindowsTimeToMilliseconds(dt)).atZone(ZoneId.systemDefault()).toLocalDateTime());
            System.out.format(
                    "%.4f Open: %.4f High: %.4f Low: %.4f Close: %.4f Trades: %4d TotalVol: %4d Bid/Ask %3d / %3d\n",
                    dt, open, high, low, close, numTrades, totalVolume, bidVolume, askVolume);

        }
        System.out.println(System.currentTimeMillis() - start);

        ds.close();

    }

    /**
     * Thanks to @see http://stackoverflow.com/a/13203649
     */
    public static long getUInt32(byte[] bytes) {
        long value = ((bytes[0] & 0xFF) << 0) | ((bytes[1] & 0xFF) << 8) | ((bytes[2] & 0xFF) << 16)
                | ((bytes[3] & 0xFF) << 24);
        return value;
    }

    public static float reverse(float x) {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putFloat(x).order(ByteOrder.LITTLE_ENDIAN)
                .getFloat(0);
    }

    /**
     * Thanks to @see
     * http://svn.codehaus.org/groovy/modules/scriptom/branches/SCRIPTOM
     * -1.5.4-ANT/src/com/jacob/com/DateUtilities.java
     */
    static public long convertWindowsTimeToMilliseconds(double comTime) {
        long result = 0;

        comTime = comTime - 25569D;
        Calendar cal = Calendar.getInstance();
        result = Math.round(86400000L * comTime) - cal.get(Calendar.ZONE_OFFSET);
        cal.setTime(new Date(result));
        result -= cal.get(Calendar.DST_OFFSET);

        return result;
    }

}
