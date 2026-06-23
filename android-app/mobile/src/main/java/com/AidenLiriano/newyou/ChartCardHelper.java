package com.AidenLiriano.newyou;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChartCardHelper {

    // App theme colors
    private static final int COLOR_BACKGROUND = Color.parseColor("#E5E0AA");
    private static final int COLOR_CARD       = Color.parseColor("#FFFADC");
    private static final int COLOR_TEXT       = Color.parseColor("#1C1C1E");
    private static final int COLOR_GREEN      = Color.parseColor("#98CD00");

    // Workout colors
    private static final int[] WORKOUT_COLORS = {
            0,
            Color.rgb(239, 83,  80),   // 1 Running — red
            Color.rgb(41,  182, 246),  // 2 Swimming — blue
            Color.rgb(102, 187, 106),  // 3 Biking — green
            Color.rgb(255, 167, 38),   // 4 Walking — orange
            Color.rgb(141, 110, 99),   // 5 Hiking — brown
            Color.rgb(171, 71,  188),  // 6 Meditation — purple
            Color.rgb(66,  165, 245),  // 7 Strength — light blue
            Color.rgb(38,  198, 218),  // 8 Yoga — teal
    };

    private static final String[] WORKOUT_NAMES = {
            "", "Running", "Swimming", "Biking", "Walking",
            "Hiking", "Meditation", "Strength", "Yoga"
    };

    public static CardView buildChartCard(Context context, String title,
                                          android.view.View chartView) {
        CardView card = new CardView(context);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 32);
        card.setLayoutParams(cardParams);
        card.setRadius(24f);
        card.setCardElevation(6f);
        card.setCardBackgroundColor(COLOR_CARD);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(32, 24, 32, 24);
        card.addView(content);

        TextView titleView = new TextView(context);
        titleView.setText(title);
        titleView.setTextSize(15f);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setTextColor(COLOR_TEXT);
        titleView.setPadding(0, 0, 0, 16);
        content.addView(titleView);

        content.addView(chartView);
        return card;
    }

    // CHART 1: Calories by activity — Bar Chart
    public static CardView buildCaloriesByActivityChart(Context context,
                                                        int[] caloriesByType) {

        BarChart chart = new BarChart(context);
        chart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600));
        chart.setBackgroundColor(COLOR_CARD);

        List<BarEntry> entries = new ArrayList<>();
        List<Integer> colors  = new ArrayList<>();
        List<String> labels   = new ArrayList<>();

        int index = 0;
        for (int i = 1; i <= 8; i++) {
            if (caloriesByType[i] > 0) {
                entries.add(new BarEntry(index, caloriesByType[i]));
                colors.add(WORKOUT_COLORS[i]);
                labels.add(WORKOUT_NAMES[i]);
                index++;
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "Calories");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(COLOR_TEXT);

        chart.setData(new BarData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setFitBars(true);
        chart.animateY(800);
        chart.getAxisLeft().setTextColor(COLOR_TEXT);
        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(COLOR_TEXT);
        chart.invalidate();

        return buildChartCard(context, "🔥 Total Calories by Activity", chart);
    }

    // CHART 2: Heart rate trend — Line Chart
    public static CardView buildHeartRateTrendChart(Context context,
                                                    List<long[]> heartRateOverTime) {

        LineChart chart = new LineChart(context);
        chart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600));
        chart.setBackgroundColor(COLOR_CARD);

        List<Entry> entries   = new ArrayList<>();
        List<String> dateLabels = new ArrayList<>();
        SimpleDateFormat sdf  = new SimpleDateFormat("M/d", Locale.getDefault());

        for (int i = 0; i < heartRateOverTime.size(); i++) {
            long[] pair = heartRateOverTime.get(i);
            entries.add(new Entry(i, pair[1]));
            dateLabels.add(sdf.format(new Date(pair[0])));
        }

        if (entries.isEmpty()) return buildEmptyCard(context, "❤️ Heart Rate Trend");

        LineDataSet dataSet = new LineDataSet(entries, "Heart Rate");
        dataSet.setColor(Color.rgb(239, 83, 80));
        dataSet.setCircleColor(Color.rgb(239, 83, 80));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.rgb(239, 83, 80));
        dataSet.setFillAlpha(30);

        chart.setData(new LineData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateX(800);
        chart.getAxisLeft().setTextColor(COLOR_TEXT);
        chart.getAxisLeft().setAxisMinimum(40f);
        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(9f);
        xAxis.setTextColor(COLOR_TEXT);
        xAxis.setLabelRotationAngle(-30f);
        chart.invalidate();

        return buildChartCard(context, "❤️ Heart Rate Trend", chart);
    }

    // CHART 3: Session count — Bar Chart
    public static CardView buildSessionCountChart(Context context,
                                                  int[] sessionCounts) {

        BarChart chart = new BarChart(context);
        chart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600));
        chart.setBackgroundColor(COLOR_CARD);

        List<BarEntry> entries = new ArrayList<>();
        List<Integer> colors  = new ArrayList<>();
        List<String> labels   = new ArrayList<>();

        int index = 0;
        for (int i = 1; i <= 8; i++) {
            if (sessionCounts[i] > 0) {
                entries.add(new BarEntry(index, sessionCounts[i]));
                colors.add(WORKOUT_COLORS[i]);
                labels.add(WORKOUT_NAMES[i]);
                index++;
            }
        }

        if (entries.isEmpty()) return buildEmptyCard(context, "📊 Sessions per Activity");

        BarDataSet dataSet = new BarDataSet(entries, "Sessions");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(COLOR_TEXT);

        chart.setData(new BarData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setFitBars(true);
        chart.animateY(800);
        chart.getAxisLeft().setTextColor(COLOR_TEXT);
        chart.getAxisLeft().setGranularity(1f);
        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(COLOR_TEXT);
        chart.invalidate();

        return buildChartCard(context, "📊 Sessions per Activity", chart);
    }

    // CHART 4: Running distance — Line Chart
    public static CardView buildRunningDistanceChart(Context context,
                                                     List<RunningData> runningData) {

        LineChart chart = new LineChart(context);
        chart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600));
        chart.setBackgroundColor(COLOR_CARD);

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("M/d", Locale.getDefault());

        for (int i = 0; i < runningData.size(); i++) {
            RunningData d = runningData.get(i);
            if (d.distance > 0) {
                entries.add(new Entry(i, d.distance));
                labels.add(sdf.format(new Date(d.timestamp)));
            }
        }

        if (entries.isEmpty()) return buildEmptyCard(context, "🏃 Running Distance");

        LineDataSet dataSet = new LineDataSet(entries, "Distance (km)");
        dataSet.setColor(WORKOUT_COLORS[1]);
        dataSet.setCircleColor(WORKOUT_COLORS[1]);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(9f);
        dataSet.setValueTextColor(COLOR_TEXT);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(WORKOUT_COLORS[1]);
        dataSet.setFillAlpha(30);

        chart.setData(new LineData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateX(800);
        chart.getAxisLeft().setTextColor(COLOR_TEXT);
        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(9f);
        xAxis.setTextColor(COLOR_TEXT);
        chart.invalidate();

        return buildChartCard(context, "🏃 Running Distance Over Time (km)", chart);
    }

    // CHART 5: Active minutes — Bar Chart
    public static CardView buildActiveMinutesChart(Context context,
                                                   long[] durationByType) {

        BarChart chart = new BarChart(context);
        chart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600));
        chart.setBackgroundColor(COLOR_CARD);

        List<BarEntry> entries = new ArrayList<>();
        List<Integer> colors  = new ArrayList<>();
        List<String> labels   = new ArrayList<>();

        int index = 0;
        for (int i = 1; i <= 8; i++) {
            if (durationByType[i] > 0) {
                float minutes = durationByType[i] / 60f;
                entries.add(new BarEntry(index, minutes));
                colors.add(WORKOUT_COLORS[i]);
                labels.add(WORKOUT_NAMES[i]);
                index++;
            }
        }

        if (entries.isEmpty()) return buildEmptyCard(context, "⏱️ Active Minutes");

        BarDataSet dataSet = new BarDataSet(entries, "Minutes");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(COLOR_TEXT);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "m";
            }
        });

        chart.setData(new BarData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setFitBars(true);
        chart.animateY(800);
        chart.getAxisLeft().setTextColor(COLOR_TEXT);
        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(COLOR_TEXT);
        chart.invalidate();

        return buildChartCard(context, "⏱️ Total Active Minutes by Activity", chart);
    }

    // CHART 6: HR vs Calories — Scatter Plot
    public static CardView buildHRvsCaloriesScatter(Context context,
                                                    List<float[]> hrCalPairs, int[] workoutTypes) {

        BarChart chart = new BarChart(context);
        chart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600));
        chart.setBackgroundColor(COLOR_CARD);

        if (hrCalPairs == null || hrCalPairs.isEmpty())
            return buildEmptyCard(context, "❤️ Avg Heart Rate by Activity");

        // Average heart rate per workout type
        float[] totalHR  = new float[9];
        int[]   countHR  = new int[9];
        for (int i = 0; i < hrCalPairs.size(); i++) {
            int type = workoutTypes[i];
            if (type >= 1 && type <= 8) {
                totalHR[type]  += hrCalPairs.get(i)[0]; // index 0 = heart rate
                countHR[type]++;
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        List<Integer>  colors  = new ArrayList<>();
        List<String>   labels  = new ArrayList<>();
        int index = 0;
        for (int i = 1; i <= 8; i++) {
            if (countHR[i] > 0) {
                float avg = totalHR[i] / countHR[i];
                entries.add(new BarEntry(index, avg));
                colors.add(WORKOUT_COLORS[i]);
                labels.add(WORKOUT_NAMES[i]);
                index++;
            }
        }

        if (entries.isEmpty())
            return buildEmptyCard(context, "❤️ Avg Heart Rate by Activity");

        BarDataSet dataSet = new BarDataSet(entries, "Avg Heart Rate");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(COLOR_TEXT);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + " bpm";
            }
        });

        chart.setData(new BarData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setFitBars(true);
        chart.animateY(800);
        chart.getAxisLeft().setTextColor(COLOR_TEXT);
        chart.getAxisLeft().setAxisMinimum(40f);
        chart.getAxisRight().setEnabled(false);
        chart.setExtraOffsets(10f, 10f, 10f, 10f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(COLOR_TEXT);
        chart.invalidate();

        return buildChartCard(context,
                "❤️ Avg Heart Rate by Activity (bpm)", chart);
    }

    // CHART 7: Walking steps — Line Chart
    public static CardView buildWalkingStepsChart(Context context,
                                                  List<WalkingData> walkingData) {

        LineChart chart = new LineChart(context);
        chart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 600));
        chart.setBackgroundColor(COLOR_CARD);

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("M/d", Locale.getDefault());

        for (int i = 0; i < walkingData.size(); i++) {
            WalkingData d = walkingData.get(i);
            if (d.stepCount > 0) {
                entries.add(new Entry(i, d.stepCount));
                labels.add(sdf.format(new Date(d.timestamp)));
            }
        }

        if (entries.isEmpty()) return buildEmptyCard(context, "🚶 Walking Steps");

        LineDataSet dataSet = new LineDataSet(entries, "Steps");
        dataSet.setColor(WORKOUT_COLORS[4]);
        dataSet.setCircleColor(WORKOUT_COLORS[4]);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(9f);
        dataSet.setValueTextColor(COLOR_TEXT);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(WORKOUT_COLORS[4]);
        dataSet.setFillAlpha(30);

        chart.setData(new LineData(dataSet));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.animateX(800);
        chart.getAxisLeft().setTextColor(COLOR_TEXT);
        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(9f);
        xAxis.setTextColor(COLOR_TEXT);
        chart.invalidate();

        return buildChartCard(context, "🚶 Walking Steps Over Time", chart);
    }

    // Empty card
    private static CardView buildEmptyCard(Context context, String title) {
        TextView empty = new TextView(context);
        empty.setText("No data available yet");
        empty.setTextSize(13f);
        empty.setTextColor(COLOR_TEXT);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(0, 32, 0, 32);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 200);
        empty.setLayoutParams(params);
        return buildChartCard(context, title, empty);
    }
}