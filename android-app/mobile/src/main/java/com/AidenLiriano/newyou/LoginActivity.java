package com.AidenLiriano.newyou;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText loginName, loginAge, loginHeightFt, loginHeightIn, loginWeight;
    private RadioGroup loginGenderGroup;
    private RadioButton loginRadioMale, loginRadioFemale, loginRadioOther;
    private TextView genderError;
    private Button getStartedButton;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginName      = findViewById(R.id.loginName);
        loginAge       = findViewById(R.id.loginAge);
        loginHeightFt  = findViewById(R.id.loginHeightFt);
        loginHeightIn  = findViewById(R.id.loginHeightIn);
        loginWeight    = findViewById(R.id.loginWeight);
        loginGenderGroup  = findViewById(R.id.loginGenderGroup);
        loginRadioMale    = findViewById(R.id.loginRadioMale);
        loginRadioFemale  = findViewById(R.id.loginRadioFemale);
        loginRadioOther   = findViewById(R.id.loginRadioOther);
        genderError       = findViewById(R.id.genderError);
        getStartedButton  = findViewById(R.id.getStartedButton);

        getStartedButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        // Collect inputs
        String name     = loginName.getText().toString().trim();
        String ageStr   = loginAge.getText().toString().trim();
        String ftStr    = loginHeightFt.getText().toString().trim();
        String inStr    = loginHeightIn.getText().toString().trim();
        String weightStr = loginWeight.getText().toString().trim();
        int selectedGenderId = loginGenderGroup.getCheckedRadioButtonId();

        // Validate all fields
        boolean valid = true;

        if (name.isEmpty()) {
            loginName.setError("Please enter your name");
            valid = false;
        }
        if (ageStr.isEmpty()) {
            loginAge.setError("Please enter your age");
            valid = false;
        }
        if (ftStr.isEmpty()) {
            loginHeightFt.setError("Required");
            valid = false;
        }
        if (weightStr.isEmpty()) {
            loginWeight.setError("Please enter your weight");
            valid = false;
        }
        if (selectedGenderId == -1) {
            genderError.setVisibility(android.view.View.VISIBLE);
            valid = false;
        } else {
            genderError.setVisibility(android.view.View.GONE);
        }

        if (!valid) return;

        // Parse values
        int age          = Integer.parseInt(ageStr);
        int feet         = Integer.parseInt(ftStr);
        int inches       = inStr.isEmpty() ? 0 : Integer.parseInt(inStr);
        int totalInches  = (feet * 12) + inches;
        float weight     = Float.parseFloat(weightStr);

        String gender = "Not set";
        if (selectedGenderId == R.id.loginRadioMale)        gender = "Male";
        else if (selectedGenderId == R.id.loginRadioFemale) gender = "Female";
        else if (selectedGenderId == R.id.loginRadioOther)  gender = "Other";

        final String finalGender = gender;
        final int finalAge       = age;
        final int finalInches    = totalInches;
        final float finalWeight  = weight;

        getStartedButton.setEnabled(false);
        getStartedButton.setText("Saving...");

        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());

            // Insert the new user
            User newUser = new User(name, finalAge, finalInches,
                    finalWeight, finalGender);
            db.appDao().insertUser(newUser);

            runOnUiThread(() -> {
                Toast.makeText(this,
                        "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();

                // Go to main screen and clear back stack
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}