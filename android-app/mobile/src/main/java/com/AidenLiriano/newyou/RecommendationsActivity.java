package com.AidenLiriano.newyou;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.gms.wearable.Wearable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecommendationsActivity extends AppCompatActivity {

    private LinearLayout recommendationsContainer;
    private TextView loadingText;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private String selectedWorkoutName = null;

    private static final int COLOR_BG      = Color.parseColor("#D9D49A");
    private static final int COLOR_CARD    = Color.parseColor("#FFFADC");
    private static final int COLOR_TEXT    = Color.parseColor("#1C1C1E");
    private static final int COLOR_SUBTEXT = Color.parseColor("#555555");
    private static final int COLOR_GREEN   = Color.parseColor("#7DB800");
    private static final int COLOR_REC     = Color.parseColor("#6AA800");
    private static final int COLOR_DIVIDER = Color.parseColor("#DDD8A0");
    private static final int COLOR_BUTTON  = Color.parseColor("#B6F500");
    private static final int COLOR_SELECT  = Color.parseColor("#4A7A00");

    private static final String[] WORKOUT_EMOJIS = {
            "", "🏃", "🏊", "🚴", "🚶", "🥾", "🧘", "🏋️", "🧘"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        recommendationsContainer = findViewById(R.id.recommendationsContainer);
        loadingText              = findViewById(R.id.loadingText);

        NavHelper.setup(this);
        loadRecommendations();
    }

    private void loadRecommendations() {
        executor.execute(() -> {
            AppDatabase db  = AppDatabase.getDatabase(getApplicationContext());
            AppDao dao      = db.appDao();

            User user       = dao.getFirstUser();
            int age         = user != null && user.age > 0 ? user.age : 30;
            float weightLbs = user != null && user.weightLbs > 0 ? user.weightLbs : 154f;
            int userId      = user != null ? user.userId : 1;

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

            int totalSessions = dao.getTotalActivityCount(userId);

            List<WorkoutRecommendationEngine.WorkoutRecommendation> recommendations =
                    WorkoutRecommendationEngine.generateRecommendations(
                            age, weightLbs, avgHeartRate, totalSessions);

            runOnUiThread(() -> {
                loadingText.setVisibility(View.GONE);

                // Profile summary header
                LinearLayout headerLayout = new LinearLayout(this);
                headerLayout.setOrientation(LinearLayout.VERTICAL);
                headerLayout.setBackgroundColor(COLOR_CARD);
                LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                headerParams.setMargins(16, 8, 16, 16);
                headerLayout.setLayoutParams(headerParams);
                headerLayout.setPadding(20, 16, 20, 16);

                TextView headerTitle = new TextView(this);
                headerTitle.setText("Your Recommendations");
                headerTitle.setTextSize(18f);
                headerTitle.setTypeface(null, Typeface.BOLD);
                headerTitle.setTextColor(COLOR_TEXT);
                headerTitle.setPadding(0, 0, 0, 4);
                headerLayout.addView(headerTitle);

                String profileSummary = "Age " + age
                        + "  •  " + String.format("%.0f lbs", weightLbs)
                        + (avgHeartRate > 0
                        ? "  •  Avg HR " + (int) avgHeartRate + " bpm"
                        : "")
                        + "  •  " + totalSessions + " sessions logged";

                TextView summaryView = new TextView(this);
                summaryView.setText(profileSummary);
                summaryView.setTextSize(12f);
                summaryView.setTextColor(COLOR_SUBTEXT);
                headerLayout.addView(summaryView);

                TextView tapHint = new TextView(this);
                tapHint.setText("Tap any workout card to expand. Tap ⭐ Recommended to send to your watch.");
                tapHint.setTextSize(11f);
                tapHint.setTextColor(COLOR_SUBTEXT);
                tapHint.setTypeface(null, Typeface.ITALIC);
                tapHint.setPadding(0, 8, 0, 0);
                headerLayout.addView(tapHint);

                recommendationsContainer.addView(headerLayout);

                for (int i = 0; i < recommendations.size(); i++) {
                    addWorkoutCard(recommendations.get(i), i + 1);
                }
            });
        });
    }

    private void addWorkoutCard(
            WorkoutRecommendationEngine.WorkoutRecommendation recommendation,
            int workoutTypeId) {

        String emoji = workoutTypeId >= 1 && workoutTypeId <= 8
                ? WORKOUT_EMOJIS[workoutTypeId] : "💪";

        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(16, 8, 16, 8);
        card.setLayoutParams(cardParams);
        card.setRadius(16f);
        card.setCardElevation(4f);
        card.setCardBackgroundColor(COLOR_CARD);

        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(20, 18, 20, 18);
        card.addView(cardContent);

        // Header row with workout name and expand icon
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        // Color accent dot
        View dot = new View(this);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(8, 8);
        dotParams.setMargins(0, 0, 10, 0);
        dotParams.gravity = Gravity.CENTER_VERTICAL;
        dot.setLayoutParams(dotParams);
        dot.setBackgroundColor(COLOR_GREEN);
        headerRow.addView(dot);

        TextView workoutTitle = new TextView(this);
        workoutTitle.setText(emoji + "  " + recommendation.workoutName);
        workoutTitle.setTextSize(18f);
        workoutTitle.setTypeface(null, Typeface.BOLD);
        workoutTitle.setTextColor(COLOR_TEXT);
        workoutTitle.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        headerRow.addView(workoutTitle);

        // Recommended badge preview
        WorkoutRecommendationEngine.WorkoutPlan recPlan = recommendation.getRecommended();
        TextView recBadge = new TextView(this);
        recBadge.setText("⭐ " + recPlan.tierName);
        recBadge.setTextSize(11f);
        recBadge.setTextColor(COLOR_SELECT);
        recBadge.setTypeface(null, Typeface.BOLD);
        headerRow.addView(recBadge);

        TextView expandIcon = new TextView(this);
        expandIcon.setText("  ▼");
        expandIcon.setTextSize(13f);
        expandIcon.setTextColor(COLOR_SUBTEXT);
        headerRow.addView(expandIcon);

        cardContent.addView(headerRow);

        // Tiers container hidden by default
        LinearLayout tiersContainer = new LinearLayout(this);
        tiersContainer.setOrientation(LinearLayout.VERTICAL);
        tiersContainer.setVisibility(View.GONE);

        // Divider
        View divider = new View(this);
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        divParams.setMargins(0, 12, 0, 12);
        divider.setLayoutParams(divParams);
        divider.setBackgroundColor(COLOR_DIVIDER);
        tiersContainer.addView(divider);

        List<WorkoutRecommendationEngine.WorkoutPlan> tiers = recommendation.allTiers;
        for (int i = 0; i < tiers.size(); i++) {
            boolean isRecommended = i == recommendation.recommendedTierIndex;
            tiersContainer.addView(
                    buildTierView(tiers.get(i), isRecommended, workoutTypeId));
        }

        cardContent.addView(tiersContainer);

        card.setOnClickListener(v -> {
            if (tiersContainer.getVisibility() == View.GONE) {
                tiersContainer.setVisibility(View.VISIBLE);
                expandIcon.setText("  ▲");
            } else {
                tiersContainer.setVisibility(View.GONE);
                expandIcon.setText("  ▼");
            }
        });

        recommendationsContainer.addView(card);
    }

    private View buildTierView(
            WorkoutRecommendationEngine.WorkoutPlan plan,
            boolean isRecommended,
            int workoutTypeId) {

        LinearLayout tierLayout = new LinearLayout(this);
        tierLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 6, 0, 6);
        tierLayout.setLayoutParams(params);
        tierLayout.setPadding(16, 14, 16, 14);
        tierLayout.setBackgroundColor(isRecommended ? COLOR_REC : COLOR_CARD);

        // Tier name row with badge
        LinearLayout tierHeaderRow = new LinearLayout(this);
        tierHeaderRow.setOrientation(LinearLayout.HORIZONTAL);
        tierHeaderRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tierName = new TextView(this);
        tierName.setText(plan.tierName);
        tierName.setTextSize(15f);
        tierName.setTypeface(null, Typeface.BOLD);
        tierName.setTextColor(COLOR_TEXT);
        tierName.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tierHeaderRow.addView(tierName);

        if (isRecommended) {
            TextView badge = new TextView(this);
            badge.setText("⭐ Recommended for you");
            badge.setTextSize(11f);
            badge.setTextColor(COLOR_TEXT);
            badge.setTypeface(null, Typeface.BOLD);
            tierHeaderRow.addView(badge);
        }
        tierLayout.addView(tierHeaderRow);

        // Description
        TextView description = new TextView(this);
        description.setText(plan.description);
        description.setTextSize(13f);
        description.setTextColor(isRecommended ? COLOR_TEXT : COLOR_SUBTEXT);
        description.setPadding(0, 6, 0, 10);
        description.setLineSpacing(4f, 1f);
        tierLayout.addView(description);

        // Divider before goals
        View goalDivider = new View(this);
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        divParams.setMargins(0, 0, 0, 8);
        goalDivider.setLayoutParams(divParams);
        goalDivider.setBackgroundColor(COLOR_DIVIDER);
        tierLayout.addView(goalDivider);

        // Goals
        for (String goal : plan.goals) {
            LinearLayout goalRow = new LinearLayout(this);
            goalRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams goalParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            goalParams.setMargins(0, 3, 0, 3);
            goalRow.setLayoutParams(goalParams);

            TextView bullet = new TextView(this);
            bullet.setText("•");
            bullet.setTextSize(13f);
            bullet.setTextColor(COLOR_GREEN);
            bullet.setTypeface(null, Typeface.BOLD);
            bullet.setPadding(0, 0, 8, 0);
            goalRow.addView(bullet);

            TextView goalView = new TextView(this);
            goalView.setText(goal);
            goalView.setTextSize(13f);
            goalView.setTextColor(COLOR_TEXT);
            goalView.setLineSpacing(2f, 1f);
            goalRow.addView(goalView);

            tierLayout.addView(goalRow);
        }

        // Select button on recommended tier only
        if (isRecommended) {
            View buttonDivider = new View(this);
            LinearLayout.LayoutParams bdParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            bdParams.setMargins(0, 12, 0, 8);
            buttonDivider.setLayoutParams(bdParams);
            buttonDivider.setBackgroundColor(COLOR_DIVIDER);
            tierLayout.addView(buttonDivider);

            Button selectButton = new Button(this);
            selectButton.setText("▶  Start This Workout on Watch");
            selectButton.setTextSize(13f);
            selectButton.setTextColor(COLOR_TEXT);
            selectButton.setTypeface(null, Typeface.BOLD);
            selectButton.setBackgroundColor(COLOR_BUTTON);
            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.setMargins(0, 4, 0, 0);
            selectButton.setLayoutParams(btnParams);

            selectButton.setOnClickListener(v ->
                    sendWorkoutToWatch(workoutTypeId, plan)
            );

            tierLayout.addView(selectButton);
        }

        return tierLayout;
    }

    // Sends the selected workout plan goals to the watch via Wearable Data Layer
    private void sendWorkoutToWatch(int workoutTypeId,
                                    WorkoutRecommendationEngine.WorkoutPlan plan) {
        // Build a compact goals string to send
        // Format: workoutType|tierName|goal1~goal2~goal3...
        StringBuilder goalsBuilder = new StringBuilder();
        for (int i = 0; i < plan.goals.size(); i++) {
            if (i > 0) goalsBuilder.append("~");
            goalsBuilder.append(plan.goals.get(i));
        }
        String message = workoutTypeId + "|" + plan.tierName + "|"
                + goalsBuilder.toString();

        Wearable.getNodeClient(this)
                .getConnectedNodes()
                .addOnSuccessListener(nodes -> {
                    if (nodes.isEmpty()) {
                        Toast.makeText(this,
                                "Watch not connected. Open the watch app first.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (com.google.android.gms.wearable.Node node : nodes) {
                        Wearable.getMessageClient(this)
                                .sendMessage(node.getId(),
                                        "/guided_workout",
                                        message.getBytes())
                                .addOnSuccessListener(i -> runOnUiThread(() ->
                                        Toast.makeText(this,
                                                "✅ " + plan.tierName + " sent to watch!",
                                                Toast.LENGTH_SHORT).show()))
                                .addOnFailureListener(e -> runOnUiThread(() ->
                                        Toast.makeText(this,
                                                "Failed to send to watch. Try again.",
                                                Toast.LENGTH_SHORT).show()));
                    }
                });
    }
}