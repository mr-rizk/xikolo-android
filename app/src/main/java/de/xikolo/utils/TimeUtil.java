package de.xikolo.utils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static final String TAG = TimeUtil.class.getSimpleName();

    /**
     * Converts seconds with fraction part to milliseconds.
     * @param seconds with fraction part
     * @return milliseconds
     */
    public static long secondsToMillis(double seconds) {
        return secondsToMillis(String.valueOf(seconds));
    }

    /**
     * Converts seconds with fraction part to milliseconds.
     * @param seconds as String in format 123.321123
     * @return milliseconds
     */
    public static long secondsToMillis(String seconds) {
        long millis = 0;

        // add seconds without fraction
        millis += TimeUnit.SECONDS.toMillis((long) (Double.parseDouble(seconds)));

        // split seconds from fraction
        String[] timeUnits = seconds.split("\\.");

        if (timeUnits.length > 1) {
            // seconds have fraction
            String fraction = timeUnits[1];
            if (fraction.length() >= 3) {
                // fraction greater or equal than 3, cut these off and convert to millis
                long subFraction = Long.parseLong(fraction.substring(0, 3));
                millis += TimeUnit.MILLISECONDS.toMillis(subFraction);
            } else {
                // fraction lower than 3, right pad zeros up to length 3 and convert to millis
                long subFraction = Long.parseLong(String.format("%1$-" + 3 + "s", fraction).replace(' ', '0'));
                millis += TimeUnit.MILLISECONDS.toMillis(subFraction);
            }
        }

        return millis;
    }

    /**
     * Formats milliseconds to human-readable time string.
     * @param millis milliseconds
     * @return String in format "12:21"
     */
    public static String getTimeString(long millis) {
        return String.format(Locale.US, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    /**
     * Formats minutes and seconds to human-readable time string.
     * @param minutes minutes
     * @param seconds seconds
     * @return String in format "12:21"
     */
    public static String getTimeString(String minutes, String seconds) {
        try {
            return String.format(Locale.US, "%02d:%02d",
                    Integer.valueOf(minutes),
                    Integer.valueOf(seconds)
            );
        } catch (Exception e) {
            return "--:--";
        }
    }

}
