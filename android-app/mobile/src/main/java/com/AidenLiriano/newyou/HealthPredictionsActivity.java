package com.AidenLiriano.newyou;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HealthPredictionsActivity extends AppCompatActivity {

    private LinearLayout predictionsContainer;
    private TextView loadingText;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final int COLOR_BG    = Color.parseColor("#E5E0AA");
    private static final int COLOR_CARD  = Color.parseColor("#FFFADC");
    private static final int COLOR_TEXT  = Color.parseColor("#1C1C1E");
    private static final int COLOR_GREEN = Color.parseColor("#98CD00");
    private static final int COLOR_TREND = Color.parseColor("#1565C0");  // blue = current trend
    private static final int COLOR_REC   = Color.parseColor("#2E7D32");  // dark green = recommended

    private static final String[] MONTH_LABELS =
            {"Now", "1m", "2m", "3m", "4m", "5m", "6m"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_predictions);

        predictionsContainer = findViewById(R.id.predictionsContainer);
        loadingText          = findViewById(R.id.predLoadingText);

        NavHelper.setup(this);
        loadPredictions();
    }

    private void loadPredictions() {
        executor.execute(() -> {
            AppDatabase db  = AppDatabase.getDatabase(getApplicationContext());
            AppDao dao      = db.appDao();

            User user       = dao.getFirstUser();
            int userId      = user != null ? user.userId : 1;

            // Gather current stats
            float[] hrValues = {
                    dao.getAvgHeartRateRunning(userId),
                    dao.getAvgHeartRateSwimming(userId),
                    dao.getAvgHeartRateBiking(userId),
                    dao.getAvgHeartRateWalking(userId),
                    dao.getAvgHeartRateHiking(userId),
                    dao.getAvgHeartRateMeditation(userId),
                    dao.getAvgHeartRateStrength(userId),
                    dao.getAvgHeartRateYoga(userId)
            };
            float hrSum = 0; int hrCount = 0;
            for (float hr : hrValues) { if (hr > 0) { hrSum += hr; hrCount++; } }
            float avgHeartRate = hrCount > 0 ? hrSum / hrCount : 0f;

            // Weekly active minutes
            long totalDurSec =
                    dao.getTotalDurationRunning(userId) +
                            dao.getTotalDurationSwimming(userId) +
                            dao.getTotalDurationBiking(userId) +
                            dao.getTotalDurationWalking(userId) +
                            dao.getTotalDurationHiking(userId) +
                            dao.getTotalDurationMeditation(userId) +
                            dao.getTotalDurationStrength(userId) +
                            dao.getTotalDurationYoga(userId);

            long earliestTime  = dao.getEarliestActivityTime(userId);
            long now           = System.currentTimeMillis();
            long msPerWeek     = 7L * 24 * 60 * 60 * 1000;
            int weeksTracked   = Math.max(1,
                    (int) ((now - earliestTime) / msPerWeek));
            float weeklyActiveMinutes =
                    (totalDurSec / 60f) / weeksTracked;

            // Average calories per session
            int totalSessions = dao.getTotalActivityCount(userId);
            float totalCal =
                    dao.getRunningTotalCalories(userId) +
                            dao.getSwimmingTotalCalories(userId) +
                            dao.getBikingTotalCalories(userId) +
                            dao.getWalkingTotalCalories(userId) +
                            dao.getHikingTotalCalories(userId) +
                            dao.getStrengthTotalCalories(userId) +
                            dao.getYogaTotalCalories(userId);
            float avgCaloriesPerSession =
                    totalSessions > 0 ? totalCal / totalSessions : 0f;

            // Average session duration
            float avgSessionDuration =
                    totalSessions > 0 ? (totalDurSec / (float) totalSessions) : 0f;

            // Weekly session count
            float weeklySessionCount =
                    weeksTracked > 0 ? (float) totalSessions / weeksTracked : 0f;

            // Generate predictions
            HealthPredictionEngine.PredictionSet predictions =
                    HealthPredictionEngine.generate(
                            user,
                            avgHeartRate,
                            weeklyActiveMinutes,
                            avgCaloriesPerSession,
                            avgSessionDuration,
                            weeklySessionCount,
                            totalSessions
                    );

            runOnUiThread(() -> {
                loadingText.setVisibility(View.GONE);
                buildUI(predictions, user);
            });
        });
    }

    private void buildUI(HealthPredictionEngine.PredictionSet p, User user) {

        // --- Header summary card ---
        addCard("📊 Your Health Snapshot", buildSnapshotContent(p, user));

        // --- Weight prediction card ---
        addCardWithChart(
                "⚖️ Weight Prediction (lbs)",
                buildPredictionRows(
                        "Weight",
                        p.currentWeightLbs,
                        p.weight1MonthTrend, p.weight6MonthTrend, p.weight1YearTrend,
                        p.weight1MonthRec,   p.weight6MonthRec,   p.weight1YearRec,
                        "lbs", false
                ),
                buildLineChart(p.weightTrendSeries, p.weightRecSeries, "Weight (lbs)")
        );

        // --- BMI prediction card ---
        addCard("📏 BMI Prediction", buildBMIContent(p));

        // --- Heart rate prediction card ---
        addCardWithChart(
                "❤️ Avg Heart Rate Prediction (bpm)",
                buildPredictionRows(
                        "Heart Rate",
                        p.currentAvgHeartRate,
                        p.hr1MonthTrend, p.hr6MonthTrend, p.hr1YearTrend,
                        p.hr1MonthRec,   p.hr6MonthRec,   p.hr1YearRec,
                        "bpm", false
                ),
                buildLineChart(p.hrTrendSeries, p.hrRecSeries, "Heart Rate (bpm)")
        );

        // --- Active minutes prediction card ---
        addCardWithChart(
                "⏱️ Weekly Active Minutes Prediction",
                buildPredictionRows(
                        "Active Minutes/Week",
                        p.currentWeeklyActiveMinutes,
                        p.activeMin1MonthTrend, p.activeMin6MonthTrend, p.activeMin1YearTrend,
                        p.activeMin1MonthRec,   p.activeMin6MonthRec,   p.activeMin1YearRec,
                        "min", true
                ),
                buildLineChart(
                        p.activeMinTrendSeries, p.activeMinRecSeries,
                        "Active Minutes/Week")
        );

        // --- Calories prediction card ---
        addCardWithChart(
                "🔥 Avg Calories Per Session Prediction",
                buildPredictionRows(
                        "Calories/Session",
                        p.currentCaloriesPerSession,
                        p.calories1MonthTrend, p.calories6MonthTrend, p.calories1YearTrend,
                        p.calories1MonthRec,   p.calories6MonthRec,   p.calories1YearRec,
                        "kcal", true
                ),
                buildLineChart(
                        p.caloriesTrendSeries, p.caloriesRecSeries,
                        "Calories/Session")
        );

        // --- Legend card ---
        addCard("ℹ️ How Predictions Work", buildLegendContent());
    }

    // Content builders
    private LinearLayout buildSnapshotContent(
            HealthPredictionEngine.PredictionSet p, User user) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        if (user != null) {
            addRow(layout, "Name", user.name != null ? user.name : "--");
            addRow(layout, "Age", user.age > 0 ? user.age + " years" : "--");
            int ft = user.heightInches / 12;
            int in = user.heightInches % 12;
            addRow(layout, "Height", ft + "'" + in + "\"");
            addRow(layout, "Weight", String.format("%.1f lbs", user.weightLbs));
        }

        addDivider(layout);
        addRow(layout, "Current BMI",
                String.format("%.1f  (%s)",
                        p.currentBMI,
                        HealthPredictionEngine.bmiCategory(p.currentBMI)));
        addRow(layout, "Avg Heart Rate",
                p.currentAvgHeartRate > 0
                        ? String.format("%.0f bpm  (%s)",
                        p.currentAvgHeartRate,
                        HealthPredictionEngine.heartRateCategory(p.currentAvgHeartRate))
                        : "No data yet");
        addRow(layout, "Weekly Active Minutes",
                p.currentWeeklyActiveMinutes > 0
                        ? String.format("%.0f min/week", p.currentWeeklyActiveMinutes)
                        : "No data yet");
        addRow(layout, "Avg Calories/Session",
                p.currentCaloriesPerSession > 0
                        ? String.format("%.0f kcal", p.currentCaloriesPerSession)
                        : "No data yet");
        addRow(layout, "Weekly Sessions",
                p.currentWeeklySessionCount > 0
                        ? p.currentWeeklySessionCount + " sessions/week"
                        : "No data yet");

        return layout;
    }

    private LinearLayout buildBMIContent(HealthPredictionEngine.PredictionSet p) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        addRow(layout, "Current BMI",
                String.format("%.1f  (%s)",
                        p.currentBMI,
                        HealthPredictionEngine.bmiCategory(p.currentBMI)));

        addDivider(layout);
        addSectionLabel(layout, "📈 Current Trend Scenario");
        addRow(layout, "1 Month",
                String.format("%.1f  (%s)",
                        p.bmi1MonthTrend,
                        HealthPredictionEngine.bmiCategory(p.bmi1MonthTrend)));
        addRow(layout, "6 Months",
                String.format("%.1f  (%s)",
                        p.bmi6MonthTrend,
                        HealthPredictionEngine.bmiCategory(p.bmi6MonthTrend)));
        addRow(layout, "1 Year",
                String.format("%.1f  (%s)",
                        p.bmi1YearTrend,
                        HealthPredictionEngine.bmiCategory(p.bmi1YearTrend)));

        addDivider(layout);
        addSectionLabel(layout, "🎯 Following Recommended Plan");
        addRow(layout, "1 Month",
                String.format("%.1f  (%s)",
                        p.bmi1MonthRec,
                        HealthPredictionEngine.bmiCategory(p.bmi1MonthRec)));
        addRow(layout, "6 Months",
                String.format("%.1f  (%s)",
                        p.bmi6MonthRec,
                        HealthPredictionEngine.bmiCategory(p.bmi6MonthRec)));
        addRow(layout, "1 Year",
                String.format("%.1f  (%s)",
                        p.bmi1YearRec,
                        HealthPredictionEngine.bmiCategory(p.bmi1YearRec)));

        return layout;
    }

    private LinearLayout buildPredictionRows(
            String metricName,
            float currentValue,
            float trend1m, float trend6m, float trend1y,
            float rec1m,   float rec6m,   float rec1y,
            String unit,
            boolean higherIsBetter) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        addRow(layout, "Current",
                currentValue > 0
                        ? String.format("%.1f %s", currentValue, unit)
                        : "No data yet");

        addDivider(layout);
        addSectionLabel(layout, "📈 Current Trend Scenario");
        addPredictionRow(layout, "1 Month",  trend1m, currentValue, unit, higherIsBetter);
        addPredictionRow(layout, "6 Months", trend6m, currentValue, unit, higherIsBetter);
        addPredictionRow(layout, "1 Year",   trend1y, currentValue, unit, higherIsBetter);

        addDivider(layout);
        addSectionLabel(layout, "🎯 Following Recommended Plan");
        addPredictionRow(layout, "1 Month",  rec1m, currentValue, unit, higherIsBetter);
        addPredictionRow(layout, "6 Months", rec6m, currentValue, unit, higherIsBetter);
        addPredictionRow(layout, "1 Year",   rec1y, currentValue, unit, higherIsBetter);

        return layout;
    }

    private LinearLayout buildLegendContent() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        addColorRow(layout, "Blue line", "Current trend — based on your existing activity patterns");
        addColorRow(layout, "Green line", "Recommended plan — projected improvement if you follow the suggested workout plan");
        addDivider(layout);

        TextView note = new TextView(this);
        note.setText("Predictions are estimates based on established exercise science formulas. " +
                "Individual results vary. Consult a healthcare professional for personalized advice.");
        note.setTextSize(12f);
        note.setTextColor(Color.parseColor("#666666"));
        note.setPadding(0, 8, 0, 0);
        layout.addView(note);

        return layout;
    }

    // Chart builder
    private LineChart buildLineChart(float[] trendData, float[] recData, String label) {
        LineChart chart = new LineChart(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 500);
        params.setMargins(0, 16, 0, 0);
        chart.setLayoutParams(params);
        chart.setBackgroundColor(COLOR_CARD);

        List<Entry> trendEntries = new ArrayList<>();
        List<Entry> recEntries   = new ArrayList<>();

        for (int i = 0; i < trendData.length; i++) {
            trendEntries.add(new Entry(i, trendData[i]));
            recEntries.add(new Entry(i, recData[i]));
        }

        // Current trend — blue
        LineDataSet trendSet = new LineDataSet(trendEntries, "Current Trend");
        trendSet.setColor(COLOR_TREND);
        trendSet.setCircleColor(COLOR_TREND);
        trendSet.setLineWidth(2.5f);
        trendSet.setCircleRadius(4f);
        trendSet.setDrawValues(false);
        trendSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        trendSet.setDrawFilled(true);
        trendSet.setFillColor(COLOR_TREND);
        trendSet.setFillAlpha(20);

        // Recommended plan — green
        LineDataSet recSet = new LineDataSet(recEntries, "Recommended Plan");
        recSet.setColor(COLOR_REC);
        recSet.setCircleColor(COLOR_REC);
        recSet.setLineWidth(2.5f);
        recSet.setCircleRadius(4f);
        recSet.setDrawValues(false);
        recSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        recSet.enableDashedLine(10f, 5f, 0f);
        recSet.setDrawFilled(true);
        recSet.setFillColor(COLOR_REC);
        recSet.setFillAlpha(20);

        chart.setData(new LineData(trendSet, recSet));
        chart.getDescription().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setTextColor(COLOR_TEXT);
        legend.setTextSize(11f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(MONTH_LABELS));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(COLOR_TEXT);
        xAxis.setTextSize(10f);

        chart.getAxisLeft().setTextColor(COLOR_TEXT);
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisRight().setEnabled(false);
        chart.animateX(600);
        chart.invalidate();

        return chart;
    }

    // UI helper methods
    private void addCard(String title, LinearLayout content) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);
        card.setRadius(24f);
        card.setCardElevation(6f);
        card.setCardBackgroundColor(COLOR_CARD);

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(32, 24, 32, 24);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(16f);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setTextColor(COLOR_TEXT);
        titleView.setPadding(0, 0, 0, 16);
        wrapper.addView(titleView);
        wrapper.addView(content);

        card.addView(wrapper);
        predictionsContainer.addView(card);
    }

    private void addCardWithChart(String title, LinearLayout content,
                                  LineChart chart) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);
        card.setRadius(24f);
        card.setCardElevation(6f);
        card.setCardBackgroundColor(COLOR_CARD);

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(32, 24, 32, 24);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        titleView.setTextSize(16f);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setTextColor(COLOR_TEXT);
        titleView.setPadding(0, 0, 0, 16);
        wrapper.addView(titleView);
        wrapper.addView(content);
        wrapper.addView(chart);

        card.addView(wrapper);
        predictionsContainer.addView(card);
    }

    private void addRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 4, 0, 4);
        row.setLayoutParams(rowParams);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(14f);
        labelView.setTextColor(Color.parseColor("#666666"));
        labelView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(labelView);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextSize(14f);
        valueView.setTextColor(COLOR_TEXT);
        valueView.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(valueView);

        parent.addView(row);
    }

    private void addPredictionRow(LinearLayout parent, String label,
                                  float predictedValue, float currentValue,
                                  String unit, boolean higherIsBetter) {
        float diff = predictedValue - currentValue;
        String arrow = diff > 0.05f ? " ▲" : diff < -0.05f ? " ▼" : " —";

        // Green = improvement, red = decline, gray = no change
        int arrowColor;
        boolean improved = higherIsBetter ? diff > 0.05f : diff < -0.05f;
        boolean declined = higherIsBetter ? diff < -0.05f : diff > 0.05f;

        if (improved)       arrowColor = COLOR_GREEN;
        else if (declined)  arrowColor = Color.parseColor("#E53935");
        else                arrowColor = Color.parseColor("#888888");

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 4, 0, 4);
        row.setLayoutParams(rowParams);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(14f);
        labelView.setTextColor(Color.parseColor("#666666"));
        labelView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(labelView);

        TextView valueView = new TextView(this);
        valueView.setText(String.format("%.1f %s", predictedValue, unit) + arrow);
        valueView.setTextSize(14f);
        valueView.setTextColor(arrowColor);
        valueView.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(valueView);

        parent.addView(row);
    }

    private void addSectionLabel(LinearLayout parent, String text) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(13f);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        label.setTextColor(COLOR_TEXT);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 4);
        label.setLayoutParams(params);
        parent.addView(label);
    }

    private void addColorRow(LinearLayout parent, String colorLabel, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 4, 0, 4);
        row.setLayoutParams(rowParams);

        TextView colorView = new TextView(this);
        colorView.setText(colorLabel);
        colorView.setTextSize(13f);
        colorView.setTextColor(colorLabel.contains("Blue") ? COLOR_TREND : COLOR_REC);
        colorView.setTypeface(null, android.graphics.Typeface.BOLD);
        colorView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.35f));
        row.addView(colorView);

        TextView descView = new TextView(this);
        descView.setText(desc);
        descView.setTextSize(12f);
        descView.setTextColor(COLOR_TEXT);
        descView.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.65f));
        row.addView(descView);

        parent.addView(row);
    }

    private void addDivider(LinearLayout parent) {
        View divider = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.setMargins(0, 10, 0, 10);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(Color.parseColor("#CCCCAA"));
        parent.addView(divider);
    }
}