package com.AidenLiriano.newyou;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

public class CreateWorkoutActivity extends AppCompatActivity {

    private EditText workoutNameInput;
    private TextView nameCharCount;
    private LinearLayout iconGridRow1;
    private LinearLayout iconGridRow2;
    private TextView selectedIconLabel;
    private ImageView previewIcon;
    private TextView previewName;
    private TextView previewFeatures;

    private SwitchCompat toggleSteps;
    private SwitchCompat toggleDistance;
    private SwitchCompat togglePace;
    private SwitchCompat toggleSpeed;
    private SwitchCompat toggleElevation;
    private SwitchCompat toggleLaps;

    private int selectedIconIndex = 0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Icon resources and names — uses existing workout icons
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
    private static final int COLOR_GREEN    = Color.parseColor("#B6F500");

    private final ImageView[] iconViews = new ImageView[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_workout);

        workoutNameInput  = findViewById(R.id.workoutNameInput);
        nameCharCount     = findViewById(R.id.nameCharCount);
        iconGridRow1      = findViewById(R.id.iconGridRow1);
        iconGridRow2      = findViewById(R.id.iconGridRow2);
        selectedIconLabel = findViewById(R.id.selectedIconLabel);
        previewIcon       = findViewById(R.id.previewIcon);
        previewName       = findViewById(R.id.previewName);
        previewFeatures   = findViewById(R.id.previewFeatures);

        toggleSteps     = findViewById(R.id.toggleSteps);
        toggleDistance  = findViewById(R.id.toggleDistance);
        togglePace      = findViewById(R.id.togglePace);
        toggleSpeed     = findViewById(R.id.toggleSpeed);
        toggleElevation = findViewById(R.id.toggleElevation);
        toggleLaps      = findViewById(R.id.toggleLaps);

        buildIconGrid();
        setupListeners();
        updatePreview();

        // If distance is off, force pace and speed off too
        toggleDistance.setOnCheckedChangeListener((btn, checked) -> {
            if (!checked) {
                togglePace.setChecked(false);
                toggleSpeed.setChecked(false);
            }
            updatePreview();
        });

        findViewById(R.id.createWorkoutButton).setOnClickListener(v -> attemptCreate());
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

            icon.setOnClickListener(v -> {
                selectIcon(index);
            });

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

            // Check if a workout with this name already exists for this user
            List<CustomWorkout> existing = dao.getCustomWorkoutsForUser(userId);
            for (CustomWorkout cw : existing) {
                if (cw.name.equalsIgnoreCase(name)) {
                    runOnUiThread(() -> {
                        workoutNameInput.setError(
                                "You already have a workout named \"" + name + "\"");
                        workoutNameInput.requestFocus();
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

            dao.insertCustomWorkout(workout);

            runOnUiThread(() -> {
                Toast.makeText(this,
                        "\"" + name + "\" created! It will appear on your watch.",
                        Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }
}