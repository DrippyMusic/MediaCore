package me.vitormac.mediacore.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Range {

    private int size = -1;

    private final int start;
    private final int end;

    public Range(int start, int end) {
        this.start = start;
        this.end = end > start ? end : 0;
    }

    public Range(String start, String end) {
        this(Integer.parseInt(start), Integer.parseInt(end));
    }

    public Range() {
        this(0, 0);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getLength() {
        return (this.end - this.start) + 1;
    }

    public int getSize() {
        return size;
    }

    public static Range from(String value) {
        if (value != null) {
            Matcher matcher = Pattern.compile("bytes[\\s=](\\d+)?-?(\\d+)?/?(\\d+)?")
                    .matcher(value);

            if (matcher.find() && matcher.group(1) != null) {
                Range range = new Range(matcher.group(1), "0");

                if (matcher.group(2) != null) {
                    range = new Range(matcher.group(1), matcher.group(2));
                }

                if (matcher.group(3) != null) {
                    range.size = Integer.parseInt(matcher.group(3));
                }

                return range;
            }
        }

        return new Range();
    }

    @Override
    public String toString() {
        String value = String.format("bytes=%d-", this.start);

        if (this.end > 0) {
            value += this.end;
        }

        if (this.size > -1) {
            return value.replace('=', ' ') + String.format("/%d", this.size);
        }

        return value;
    }

}
