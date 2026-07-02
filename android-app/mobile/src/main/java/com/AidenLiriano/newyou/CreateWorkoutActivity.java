package com.AidenLiriano.newyou;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateWorkoutActivity extends AppCompatActivity {

    private EditText workoutNameInput;
    private TextView nameCharCount;
    private LinearLayout iconGridRow1;
    private LinearLayout iconGridRow2;
    private TextView selectedIconLabel;
    private ImageView previewIcon;
    private TextView previewName;
    private TextView previewFeatures;
    private Button createWorkoutButton;

    private SwitchCompat toggleSteps;
    private SwitchCompat toggleDistance;
    private SwitchCompat togglePace;
    private SwitchCompat toggleSpeed;
    private SwitchCompat toggleElevation;
    private SwitchCompat toggleLaps;

    private int selectedIconIndex = 0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private boolean isSubmitting = false;

    private static final int[] ICON_RES = {
            R.drawable.ic_running,
            R.drawable.ic_swimming,
            R.drawable.ic_biking,
            R.drawable.ic_walking,
            R.drawable.ic_hiking,
            R.drawable.ic_meditation,
            R.drawable.ic_strength,
            R.drawable.ic_yoga
    };

    private static final String[] ICON_NAMES = {
            "Running", "Swimming", "Biking", "Walking",
            "Hiking", "Meditation", "Strength", "Yoga"
    };

    private static final int COLOR_SELECTED = Color.parseColor("#98CD00");
    private static final int COLOR_NORMAL   = Color.parseColor("#FFFADC");
    private static final int COLOR_TEXT     = Color.parseColor("#1C1C1E");

    private final ImageView[] iconViews = new ImageView[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        workoutNameInput    = findViewById(R.id.workoutNameInput);
        nameCharCount       = findViewById(R.id.nameCharCount);
        iconGridRow1        = findViewById(R.id.iconGridRow1);
        iconGridRow2        = findViewById(R.id.iconGridRow2);
        selectedIconLabel   = findViewById(R.id.selectedIconLabel);
        previewIcon         = findViewById(R.id.previewIcon);
        previewName         = findViewById(R.id.previewName);
        previewFeatures     = findViewById(R.id.previewFeatures);
        createWorkoutButton = findViewById(R.id.createWorkoutButton);

        toggleSteps     = findViewById(R.id.toggleSteps);
        toggleDistance  = findViewById(R.id.toggleDistance);
        togglePace      = findViewById(R.id.togglePace);
        toggleSpeed     = findViewById(R.id.toggleSpeed);
        toggleElevation = findViewById(R.id.toggleElevation);
        toggleLaps      = findViewById(R.id.toggleLaps);

        buildIconGrid();
        setupListeners();
        updatePreview();

        toggleDistance.setOnCheckedChangeListener((btn, checked) -> {
            if (!checked) {
                togglePace.setChecked(false);
                toggleSpeed.setChecked(false);
            }
            updatePreview();
        });

        createWorkoutButton.setOnClickListener(v -> attemptCreate());
        findViewById(R.id.cancelButton).setOnClickListener(v -> finish());
    }

    private void buildIconGrid() {
        int iconSizePx = (int) (64 * getResources().getDisplayMetrics().density);
        int marginPx   = (int) (8  * getResources().getDisplayMetrics().density);

        for (int i = 0; i < 8; i++) {
            final int index = i;

            ImageView icon = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    iconSizePx, iconSizePx);
            params.setMargins(marginPx, marginPx, marginPx, marginPx);
            icon.setLayoutParams(params);
            icon.setImageResource(ICON_RES[i]);
            icon.setPadding(12, 12, 12, 12);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            icon.setBackgroundColor(i == 0 ? COLOR_SELECTED : COLOR_NORMAL);
            icon.setClickable(true);
            icon.setFocusable(true);
            icon.setOnClickListener(v -> selectIcon(index));

            iconViews[i] = icon;

            if (i < 4) iconGridRow1.addView(icon);
            else        iconGridRow2.addView(icon);
        }
    }

    private void selectIcon(int index) {
        selectedIconIndex = index;
        for (int i = 0; i < iconViews.length; i++) {
            iconViews[i].setBackgroundColor(
                    i == index ? COLOR_SELECTED : COLOR_NORMAL);
        }
        selectedIconLabel.setText("Selected: " + ICON_NAMES[index]);
        updatePreview();
    }

    private void setupListeners() {
        workoutNameInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                nameCharCount.setText(s.length() + " / 30");
                updatePreview();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        toggleSteps.setOnCheckedChangeListener((b, c)     -> updatePreview());
        togglePace.setOnCheckedChangeListener((b, c)      -> updatePreview());
        toggleSpeed.setOnCheckedChangeListener((b, c)     -> updatePreview());
        toggleElevation.setOnCheckedChangeListener((b, c) -> updatePreview());
        toggleLaps.setOnCheckedChangeListener((b, c)      -> updatePreview());
    }

    private void updatePreview() {
        String name = workoutNameInput.getText().toString().trim();
        previewName.setText(name.isEmpty() ? "My Workout" : name);
        previewIcon.setImageResource(ICON_RES[selectedIconIndex]);

        StringBuilder features = new StringBuilder("Tracking: Heart Rate, Calories, Duration");
        if (toggleSteps.isChecked())     features.append(", Steps");
        if (toggleDistance.isChecked())  features.append(", Distance");
        if (togglePace.isChecked())      features.append(", Pace");
        if (toggleSpeed.isChecked())     features.append(", Speed");
        if (toggleElevation.isChecked()) features.append(", Elevation");
        if (toggleLaps.isChecked())      features.append(", Laps");

        previewFeatures.setText(features.toString());
    }

    private void attemptCreate() {
        if (isSubmitting) {
            Log.d("CreateWorkout", "Submission already in progress, ignoring tap");
            return;
        }

        String name = workoutNameInput.getText().toString().trim();

        if (name.isEmpty()) {
            workoutNameInput.setError("Please give your workout a name");
            workoutNameInput.requestFocus();
            return;
        }
        if (name.length() < 2) {
            workoutNameInput.setError("Name must be at least 2 characters");
            workoutNameInput.requestFocus();
            return;
        }

        isSubmitting = true;
        createWorkoutButton.setEnabled(false);
        createWorkoutButton.setAlpha(0.6f);
        createWorkoutButton.setText("Creating...");

        boolean trackSteps     = toggleSteps.isChecked();
        boolean trackDistance  = toggleDistance.isChecked();
        boolean trackPace      = trackDistance && togglePace.isChecked();
        boolean trackSpeed     = trackDistance && toggleSpeed.isChecked();
        boolean trackElevation = toggleElevation.isChecked();
        boolean trackLaps      = toggleLaps.isChecked();

        executor.execute(() -> {
            AppDatabase db   = AppDatabase.getDatabase(getApplicationContext());
            AppDao dao       = db.appDao();
            User user        = dao.getFirstUser();
            int userId       = user != null ? user.userId : 1;

            // Check for duplicate name before inserting
            List<CustomWorkout> existing = dao.getCustomWorkoutsForUser(userId);
            for (CustomWorkout cw : existing) {
                if (cw.name.equalsIgnoreCase(name)) {
                    runOnUiThread(() -> {
                        workoutNameInput.setError(
                                "You already have a workout named \"" + name + "\"");
                        workoutNameInput.requestFocus();
                        resetSubmitState();
                    });
                    return;
                }
            }

            CustomWorkout workout = new CustomWorkout(
                    userId, name, selectedIconIndex,
                    true, true, true,
                    trackSteps, trackDistance,
                    trackPace, trackSpeed,
                    trackElevation, trackLaps
            );

            long newId = dao.insertCustomWorkout(workout);
            Log.d("CreateWorkout", "Saved custom workout with id: " + newId);

            List<CustomWorkout> allWorkouts = dao.getCustomWorkoutsForUser(userId);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < allWorkouts.size(); i++) {
                CustomWorkout cw = allWorkouts.get(i);
                if (i > 0) sb.append(";");
                sb.append(cw.name).append("|")
                        .append(cw.iconIndex).append("|")
                        .append(cw.trackSteps     ? "1" : "0").append("|")
                        .append(cw.trackDistance  ? "1" : "0").append("|")
                        .append(cw.trackElevation ? "1" : "0").append("|")
                        .append(cw.trackLaps      ? "1" : "0").append("|")
                        .append(cw.trackSpeed     ? "1" : "0").append("|")
                        .append(cw.id);
            }
            final String payload = sb.toString();

            Wearable.getNodeClient(getApplicationContext())
                    .getConnectedNodes()
                    .addOnSuccessListener(nodes -> {
                        for (Node node : nodes) {
                            Wearable.getMessageClient(getApplicationContext())
                                    .sendMessage(node.getId(),
                                            "/custom_workouts_data",
                                            payload.getBytes())
                                    .addOnSuccessListener(i ->
                                            Log.d("CreateWorkout",
                                                    "Pushed workout list to watch: "
                                                            + node.getDisplayName()))
                                    .addOnFailureListener(e ->
                                            Log.e("CreateWorkout",
                                                    "Failed to push to watch", e));
                        }
                    })
                    .addOnFailureListener(e ->
                            Log.e("CreateWorkout", "Failed to get connected nodes", e));

            runOnUiThread(() -> {
                Toast.makeText(this,
                        "\"" + name + "\" created! It will appear on your watch.",
                        Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }

    private void resetSubmitState() {
        isSubmitting = false;
        createWorkoutButton.setEnabled(true);
        createWorkoutButton.setAlpha(1f);
        createWorkoutButton.setText("✅  Create Workout");
    }
}