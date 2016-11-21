package com.amosyuen.videorecorder.demo;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utilities
 */
public final class Util {
    private Util() {}

    // Taken from https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    public static String getHumanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format(Locale.US, "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String getHumanReadableBitrate(long bytesPerSecond, boolean si) {
        return getHumanReadableByteCount(bytesPerSecond, true) + "ps";
    }

    public static String getHumanReadableDuration(long microSeconds) {
        float seconds = (float) microSeconds / TimeUnit.SECONDS.toMicros(1);
        if (seconds < TimeUnit.MINUTES.toSeconds(1)) {
            return String.format(Locale.US, "%.3fs", seconds);
        } else if (seconds < TimeUnit.HOURS.toSeconds(1)) {
            return String.format(Locale.US, "%.3fm", seconds / TimeUnit.MINUTES.toSeconds(1));
        } else if (seconds < TimeUnit.DAYS.toSeconds(1)) {
            return String.format(Locale.US, "%.3fh", seconds / TimeUnit.HOURS.toSeconds(1));
        } else {
            return String.format(Locale.US, "%.3fd", seconds / TimeUnit.DAYS.toSeconds(1));
        }
    }

    public static String getHumanReadableDate(long millis) {
        return SimpleDateFormat.getDateTimeInstance().format(new Date(millis));
    }
}
