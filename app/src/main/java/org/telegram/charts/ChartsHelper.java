package org.telegram.charts;

import org.telegram.charts.model.ChartData;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ChartsHelper {

    public static final int STEP_COUNT = 6;
    public static final float MIN_SHARE = 0.20f;

    public static SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("d MMM", Locale.getDefault());

    static final String CHART_STATES_KEY = "chart_states";

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "K");
        suffixes.put(1_000_000L, "M");
    }

    public static int getYValueStep(ChartData chartData) {
        int diff = chartData.getMaxYValue() - chartData.getMinYValue();
        int maxYWrapValue = chartData.getMinYValue() * 2 + diff;
        int stepNext = (int) ((float) maxYWrapValue / (STEP_COUNT - 1f));
        int stepPrev = (maxYWrapValue - stepNext / 2) / (STEP_COUNT - 1);
        int multiplier = (int) Math.pow(10, Integer.toString(maxYWrapValue).length() - 2);
        return (int) Math.rint((float) stepPrev / multiplier) * multiplier;
    }

    public static String getReadableYValueString(int value) {
        if (value == Integer.MIN_VALUE) return getReadableYValueString(Integer.MIN_VALUE + 1);
        if (value < 1000) return Long.toString(value);

        Map.Entry<Long, String> e = suffixes.floorEntry((long) value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10);
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }
}
