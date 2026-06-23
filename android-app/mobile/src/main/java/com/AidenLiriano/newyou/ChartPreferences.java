package com.AidenLiriano.newyou;

import android.content.Context;
import android.content.SharedPreferences;

public class ChartPreferences {

    private static final String PREFS_NAME  = "chart_prefs";
    private static final String KEY_RANGE   = "chart_time_range";

    // Range constants
    public static final int RANGE_ALL_TIME  = 0;
    public static final int RANGE_30_DAYS   = 1;
    public static final int RANGE_7_DAYS    = 2;

    public static int getRange(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_RANGE, RANGE_ALL_TIME);
    }

    public static void setRange(Context context, int range) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_RANGE, range)
                .apply();
    }

    // Converts range constant to millisecond cutoff timestamp
    public static long getFromTime(int range) {
        if (range == RANGE_ALL_TIME) return 0L;
        long now = System.currentTimeMillis();
        int days = range == RANGE_7_DAYS ? 7 : 30;
        return now - ((long) days * 24 * 60 * 60 * 1000);
    }
}