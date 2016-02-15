package org.kirillius.mymusic.core;

/**
 * Created by Kirill on 05.02.2016.
 */
public class DurationFormatter {

    private final static String DELIMITER = ":";
    private final static String DELIMITER_ZERO = ":0";

    public static CharSequence format(StringBuilder sb, int duration) {

        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;

        sb.setLength(0);

        if ( duration >= 3600 ) {
            sb.append(hours).append( minutes < 10 ? DELIMITER_ZERO : DELIMITER );
        }

        sb.append(minutes)
            .append(seconds < 10 ? DELIMITER_ZERO : DELIMITER)
            .append(seconds);

        return sb;
    }

}
