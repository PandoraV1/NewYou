package com.AidenLiriano.newyou;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.LinearLayout;

public class LiveWorkoutActivity extends AppCompatActivity {

    public static final String EXTRA_WORKOUT_NAME = "workout_name";
    public static final String EXTRA_WORKOUT_TYPE = "workout_type";

    private TextView tvWorkoutName;
    private TextView tvTimer;
    private TextView tvHeartRate;
    private TextView tvCalories;
    private TextView tvSteps;
    private TextView tvDistance;
    private TextView tvPace;
    private TextView tvSpeed;
    private TextView tvElevation;
    private TextView tvLaps;
    private TextView tvWaiting;

    // Labels so we can hide irrelevant ones per workout type
    private LinearLayout labelSteps;
    private LinearLayout labelDistance;
    private LinearLayout labelPace;
    private LinearLayout labelSpeed;
    private LinearLayout labelElevation;
    private LinearLayout labelLaps;

    private int workoutType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_workout);

        String workoutName = getIntent().getStringExtra(EXTRA_WORKOUT_NAME);
        workoutType = getIntent().getIntExtra(EXTRA_WORKOUT_TYPE, 0);

        tvWorkoutName = findViewById(R.id.liveWorkoutName);
        tvTimer       = findViewById(R.id.liveTimer);
        tvHeartRate   = findViewById(R.id.liveHeartRate);
        tvCalories    = findViewById(R.id.liveCalories);
        tvSteps       = findViewById(R.id.liveSteps);
        tvDistance    = findViewById(R.id.liveDistance);
        tvPace        = findViewById(R.id.livePace);
        tvSpeed       = findViewById(R.id.liveSpeed);
        tvElevation   = findViewById(R.id.liveElevation);
        tvLaps        = findViewById(R.id.liveLaps);
        tvWaiting     = findViewById(R.id.liveWaiting);

        labelSteps     = findViewById(R.id.labelSteps);
        labelDistance  = findViewById(R.id.labelDistance);
        labelPace      = findViewById(R.id.labelPace);
        labelSpeed     = findViewById(R.id.labelSpeed);
        labelElevation = findViewById(R.id.labelElevation);
        labelLaps      = findViewById(R.id.labelLaps);

        tvWorkoutName.setText(workoutName != null ? workoutName : "Workout");

        // Show only relevant stats for this workout type
        configureVisibleStats(workoutType);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register this activity so PhoneListenerService can push updates to it
        PhoneListenerService.liveWorkoutActivity = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister to avoid memory leaks
        if (PhoneListenerService.liveWorkoutActivity == this) {
            PhoneListenerService.liveWorkoutActivity = null;
        }
    }

    private void configureVisibleStats(int type) {
        // Hide all optional stats first
        setVisible(labelSteps,     tvSteps,     false);
        setVisible(labelDistance,  tvDistance,  false);
        setVisible(labelPace,      tvPace,      false);
        setVisible(labelSpeed,     tvSpeed,     false);
        setVisible(labelElevation, tvElevation, false);
        setVisible(labelLaps,      tvLaps,      false);

        switch (type) {
            case 1: // Running
                setVisible(labelSteps,    tvSteps,    true);
                setVisible(labelDistance, tvDistance, true);
                setVisible(labelPace,     tvPace,     true);
                break;
            case 2: // Swimming
                setVisible(labelLaps,     tvLaps,     true);
                setVisible(labelDistance, tvDistance, true);
                setVisible(labelPace,     tvPace,     true);
                break;
            case 3: // Biking
                setVisible(labelDistance, tvDistance, true);
                setVisible(labelSpeed,    tvSpeed,    true);
                break;
            case 4: // Walking
                setVisible(labelSteps,    tvSteps,    true);
                setVisible(labelDistance, tvDistance, true);
                setVisible(labelPace,     tvPace,     true);
                break;
            case 5: // Hiking
                setVisible(labelSteps,     tvSteps,     true);
                setVisible(labelDistance,  tvDistance,  true);
                setVisible(labelPace,      tvPace,      true);
                setVisible(labelElevation, tvElevation, true);
                break;
            // Meditation (6), Strength (7), Yoga (8) only show HR and calories
        }
    }

    private void setVisible(View label, View value, boolean visible) {
        int v = visible ? View.VISIBLE : View.GONE;
        if (label != null) label.setVisibility(v);
        if (value != null) value.setVisibility(v);
    }

    // Called by PhoneListenerService to push live updates to this screen
    public void updateLiveStats(
            long elapsedSeconds,
            int heartRate,
            int calories,
            int steps,
            float distanceKm,
            float pace,
            float speed,
            float elevGain,
            int laps
    ) {
        runOnUiThread(() -> {
            if (tvWaiting != null) tvWaiting.setVisibility(View.GONE);

            long h = elapsedSeconds / 3600;
            long m = (elapsedSeconds % 3600) / 60;
            long s = elapsedSeconds % 60;
            tvTimer.setText(String.format("%02d:%02d:%02d", h, m, s));

            tvHeartRate.setText(heartRate > 0 ? heartRate + " bpm" : "-- bpm");
            tvCalories.setText(calories + " kcal");

            if (tvSteps.getVisibility() == View.VISIBLE)
                tvSteps.setText(steps + " steps");

            if (tvDistance.getVisibility() == View.VISIBLE)
                tvDistance.setText(String.format("%.2f km", distanceKm));

            if (tvPace.getVisibility() == View.VISIBLE)
                tvPace.setText(formatPace(pace));

            if (tvSpeed.getVisibility() == View.VISIBLE)
                tvSpeed.setText(String.format("%.1f km/h", speed));

            if (tvElevation.getVisibility() == View.VISIBLE)
                tvElevation.setText(String.format("%.1f m gain", elevGain));

            if (tvLaps.getVisibility() == View.VISIBLE)
                tvLaps.setText(laps + " laps");
        });
    }

    private String formatPace(float paceMinPerKm) {
        if (paceMinPerKm <= 0) return "--:-- /km";
        int minutes = (int) paceMinPerKm;
        int seconds = (int) ((paceMinPerKm - minutes) * 60);
        return String.format("%d:%02d /km", minutes, seconds);
    }

    // Called by PhoneListenerService when workout stops
    public void finishWorkout() {
        runOnUiThread(this::finish);
    }
}