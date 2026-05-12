package com.AidenLiriano.newyou;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private TextView displayName, displayAge, displayHeight, displayWeight, displayGender;
    private EditText editName, editAge, editHeightFt, editHeightIn, editWeight;
    private LinearLayout editHeightContainer, editGenderContainer;
    private RadioGroup genderRadioGroup;
    private RadioButton radioMale, radioFemale, radioOther;
    private Button editButton, saveButton;

    private boolean isEditing = false;
    private User currentUser = null;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        displayName   = findViewById(R.id.displayName);
        displayAge    = findViewById(R.id.displayAge);
        displayHeight = findViewById(R.id.displayHeight);
        displayWeight = findViewById(R.id.displayWeight);
        displayGender = findViewById(R.id.displayGender);

        editName            = findViewById(R.id.editName);
        editAge             = findViewById(R.id.editAge);
        editHeightFt        = findViewById(R.id.editHeightFt);
        editHeightIn        = findViewById(R.id.editHeightIn);
        editWeight          = findViewById(R.id.editWeight);
        editHeightContainer = findViewById(R.id.editHeightContainer);
        editGenderContainer = findViewById(R.id.editGenderContainer);
        genderRadioGroup    = findViewById(R.id.genderRadioGroup);
        radioMale           = findViewById(R.id.radioMale);
        radioFemale         = findViewById(R.id.radioFemale);
        radioOther          = findViewById(R.id.radioOther);
        editButton          = findViewById(R.id.editButton);
        saveButton          = findViewById(R.id.saveButton);

        editButton.setOnClickListener(v -> enterEditMode());
        saveButton.setOnClickListener(v -> saveChanges());

        NavHelper.setup(this);
        loadUser();
    }

    private void loadUser() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            User user = db.appDao().getFirstUser();
            runOnUiThread(() -> {
                if (user != null) {
                    currentUser = user;
                    displayUser(user);
                }
            });
        });
    }

    private void displayUser(User user) {
        displayName.setText(user.name != null && !user.name.isEmpty()
                ? user.name : "Not set");
        displayAge.setText(user.age > 0 ? user.age + " years" : "Not set");
        displayHeight.setText(formatHeight(user.heightInches));
        displayWeight.setText(user.weightLbs > 0
                ? String.format("%.1f lbs", user.weightLbs) : "Not set");
        displayGender.setText(user.gender != null && !user.gender.isEmpty()
                ? user.gender : "Not set");
    }

    private String formatHeight(int totalInches) {
        if (totalInches <= 0) return "Not set";
        int feet = totalInches / 12;
        int inches = totalInches % 12;
        return feet + "'" + inches + "\"";
    }

    private void enterEditMode() {
        isEditing = true;
        editButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.VISIBLE);

        // Hide display views, show edit views
        displayName.setVisibility(View.GONE);
        displayAge.setVisibility(View.GONE);
        displayHeight.setVisibility(View.GONE);
        displayWeight.setVisibility(View.GONE);
        displayGender.setVisibility(View.GONE);

        editName.setVisibility(View.VISIBLE);
        editAge.setVisibility(View.VISIBLE);
        editHeightContainer.setVisibility(View.VISIBLE);
        editWeight.setVisibility(View.VISIBLE);
        editGenderContainer.setVisibility(View.VISIBLE);

        // Pre-fill current values
        if (currentUser != null) {
            editName.setText(currentUser.name);
            editAge.setText(currentUser.age > 0
                    ? String.valueOf(currentUser.age) : "");
            int feet = currentUser.heightInches / 12;
            int inches = currentUser.heightInches % 12;
            editHeightFt.setText(feet > 0 ? String.valueOf(feet) : "");
            editHeightIn.setText(inches > 0 ? String.valueOf(inches) : "");
            editWeight.setText(currentUser.weightLbs > 0
                    ? String.valueOf(currentUser.weightLbs) : "");

            // Pre-select gender radio
            if (currentUser.gender != null) {
                switch (currentUser.gender) {
                    case "Male":   radioMale.setChecked(true);   break;
                    case "Female": radioFemale.setChecked(true); break;
                    case "Other":  radioOther.setChecked(true);  break;
                }
            }
        }
    }

    private void saveChanges() {
        String name = editName.getText().toString().trim();
        String ageStr = editAge.getText().toString().trim();
        String ftStr = editHeightFt.getText().toString().trim();
        String inStr = editHeightIn.getText().toString().trim();
        String weightStr = editWeight.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            editName.setError("Please enter your name");
            return;
        }
        if (ageStr.isEmpty()) {
            editAge.setError("Please enter your age");
            return;
        }
        if (ftStr.isEmpty()) {
            editHeightFt.setError("Required");
            return;
        }

        int age = Integer.parseInt(ageStr);
        int feet = Integer.parseInt(ftStr);
        int inches = inStr.isEmpty() ? 0 : Integer.parseInt(inStr);
        int totalInches = (feet * 12) + inches;
        float weight = weightStr.isEmpty() ? 0f : Float.parseFloat(weightStr);

        // Get selected gender
        String gender = "Not set";
        int selectedId = genderRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radioMale)        gender = "Male";
        else if (selectedId == R.id.radioFemale) gender = "Female";
        else if (selectedId == R.id.radioOther)  gender = "Other";

        final String finalGender = gender;
        final int finalAge = age;
        final int finalTotalInches = totalInches;
        final float finalWeight = weight;

        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            int userId = currentUser != null ? currentUser.userId : 1;
            db.appDao().updateUser(userId, name, finalAge,
                    finalTotalInches, finalWeight, finalGender);

            // Reload updated user
            User updated = db.appDao().getFirstUser();

            runOnUiThread(() -> {
                currentUser = updated;
                exitEditMode();
                if (updated != null) displayUser(updated);
                Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void exitEditMode() {
        isEditing = false;
        editButton.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.GONE);

        displayName.setVisibility(View.VISIBLE);
        displayAge.setVisibility(View.VISIBLE);
        displayHeight.setVisibility(View.VISIBLE);
        displayWeight.setVisibility(View.VISIBLE);
        displayGender.setVisibility(View.VISIBLE);

        editName.setVisibility(View.GONE);
        editAge.setVisibility(View.GONE);
        editHeightContainer.setVisibility(View.GONE);
        editWeight.setVisibility(View.GONE);
        editGenderContainer.setVisibility(View.GONE);
    }
}