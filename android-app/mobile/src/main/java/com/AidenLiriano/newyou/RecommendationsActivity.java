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

    private static final int COLOR_CARD    = Color.parseColor("#FFFADC");
    private static final int COLOR_TEXT    = Color.parseColor("#1C1C1E");
    private static final int COLOR_SUBTEXT = Color.parseColor("#555555");
    private static final int COLOR_GREEN   = Color.parseColor("#7DB800");
    private static final int COLOR_DIVIDER = Color.parseColor("#DDD8A0");
    private static final int COLOR_BUTTON  = Color.parseColor("#B6F500");

    // Tier background colors: low=soft blue, medium=soft green, high=soft orange
    private static final int COLOR_LOW_BG  = Color.parseColor("#E3F2FD");
    private static final int COLOR_MED_BG  = Color.parseColor("#F1F8E9");
    private static final int COLOR_HIGH_BG = Color.parseColor("#FFF3E0");

    // Tier accent colors for labels
    private static final int COLOR_LOW_ACC  = Color.parseColor("#1565C0");
    private static final int COLOR_MED_ACC  = Color.parseColor("#4A7A00");
    private static final int COLOR_HIGH_ACC = Color.parseColor("#E65100");

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

                // Profile summary header card
                CardView headerCard = new CardView(this);
                LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                headerParams.setMargins(16, 8, 16, 16);
                headerCard.setLayoutParams(headerParams);
                headerCard.setRadius(16f);
                headerCard.setCardElevation(4f);
                headerCard.setCardBackgroundColor(COLOR_CARD);

                LinearLayout headerContent = new LinearLayout(this);
                headerContent.setOrientation(LinearLayout.VERTICAL);
                headerContent.setPadding(20, 16, 20, 16);
                headerCard.addView(headerContent);

                TextView headerTitle = new TextView(this);
                headerTitle.setText("Your Personalised Workouts");
                headerTitle.setTextSize(18f);
                headerTitle.setTypeface(null, Typeface.BOLD);
                headerTitle.setTextColor(COLOR_TEXT);
                headerTitle.setPadding(0, 0, 0, 6);
                headerContent.addView(headerTitle);

                TextView profileLine = new TextView(this);
                profileLine.setText("Age " + age
                        + "  •  " + String.format("%.0f lbs", weightLbs)
                        + (avgHeartRate > 0
                        ? "  •  Avg HR " + (int) avgHeartRate + " bpm" : "")
                        + "  •  " + totalSessions + " sessions logged");
                profileLine.setTextSize(12f);
                profileLine.setTextColor(COLOR_SUBTEXT);
                profileLine.setPadding(0, 0, 0, 6);
                headerContent.addView(profileLine);

                TextView hintLine = new TextView(this);
                hintLine.setText("Each workout is built from your data. "
                        + "Three intensities are shown — tap a card to expand. "
                        + "Your recommended level is highlighted.");
                hintLine.setTextSize(11f);
                hintLine.setTextColor(COLOR_SUBTEXT);
                hintLine.setTypeface(null, Typeface.ITALIC);
                headerContent.addView(hintLine);

                recommendationsContainer.addView(headerCard);

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
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(16, 8, 16, 8);
        card.setLayoutParams(cardParams);
        card.setRadius(16f);
        card.setCardElevation(4f);
        card.setCardBackgroundColor(COLOR_CARD);

        LinearLayout cardContent = new LinearLayout(this);
        cardContent.setOrientation(LinearLayout.VERTICAL);
        cardContent.setPadding(20, 18, 20, 18);
        card.addView(cardContent);

        // Header row
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

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

        // Show which tier is recommended as a small badge
        WorkoutRecommendationEngine.WorkoutPlan recPlan = recommendation.getRecommended();
        int recTierIdx = recommendation.recommendedTierIndex;
        int badgeColor = recTierIdx == 0 ? COLOR_LOW_ACC
                : recTierIdx == 1 ? COLOR_MED_ACC : COLOR_HIGH_ACC;
        TextView recBadge = new TextView(this);
        recBadge.setText("⭐ " + recPlan.tierName);
        recBadge.setTextSize(10f);
        recBadge.setTextColor(badgeColor);
        recBadge.setTypeface(null, Typeface.BOLD);
        headerRow.addView(recBadge);

        TextView expandIcon = new TextView(this);
        expandIcon.setText("  ▼");
        expandIcon.setTextSize(13f);
        expandIcon.setTextColor(COLOR_SUBTEXT);
        headerRow.addView(expandIcon);

        cardContent.addView(headerRow);

        // Tiers container
        LinearLayout tiersContainer = new LinearLayout(this);
        tiersContainer.setOrientation(LinearLayout.VERTICAL);
        tiersContainer.setVisibility(View.GONE);

        View topDivider = makeDivider();
        LinearLayout.LayoutParams tdp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        tdp.setMargins(0, 12, 0, 8);
        topDivider.setLayoutParams(tdp);
        tiersContainer.addView(topDivider);

        List<WorkoutRecommendationEngine.WorkoutPlan> tiers = recommendation.allTiers;
        for (int i = 0; i < tiers.size(); i++) {
            boolean isRecommended = i == recommendation.recommendedTierIndex;
            tiersContainer.addView(buildTierView(tiers.get(i), isRecommended,
                    workoutTypeId, i));
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
            int workoutTypeId,
            int tierIndex) {

        // Pick background and accent color by tier
        int bgColor     = tierIndex == 0 ? COLOR_LOW_BG
                : tierIndex == 1 ? COLOR_MED_BG : COLOR_HIGH_BG;
        int accentColor = tierIndex == 0 ? COLOR_LOW_ACC
                : tierIndex == 1 ? COLOR_MED_ACC : COLOR_HIGH_ACC;
        String tierIcon = tierIndex == 0 ? "🔵" : tierIndex == 1 ? "🟢" : "🟠";

        LinearLayout tierLayout = new LinearLayout(this);
        tierLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 6, 0, 6);
        tierLayout.setLayoutParams(params);
        tierLayout.setPadding(14, 14, 14, 14);
        tierLayout.setBackgroundColor(bgColor);

        // Tier header row
        LinearLayout tierHeaderRow = new LinearLayout(this);
        tierHeaderRow.setOrientation(LinearLayout.HORIZONTAL);
        tierHeaderRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tierName = new TextView(this);
        tierName.setText(tierIcon + "  " + plan.tierName);
        tierName.setTextSize(15f);
        tierName.setTypeface(null, Typeface.BOLD);
        tierName.setTextColor(accentColor);
        tierName.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tierHeaderRow.addView(tierName);

        if (isRecommended) {
            TextView badge = new TextView(this);
            badge.setText("⭐ Recommended for you");
            badge.setTextSize(10f);
            badge.setTextColor(accentColor);
            badge.setTypeface(null, Typeface.BOLD);
            tierHeaderRow.addView(badge);
        }
        tierLayout.addView(tierHeaderRow);

        // Description
        TextView description = new TextView(this);
        description.setText(plan.description);
        description.setTextSize(12f);
        description.setTextColor(COLOR_SUBTEXT);
        description.setPadding(0, 6, 0, 8);
        description.setLineSpacing(3f, 1f);
        tierLayout.addView(description);

        // Divider before goals
        View divider = makeDivider();
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        dp.setMargins(0, 0, 0, 8);
        divider.setLayoutParams(dp);
        tierLayout.addView(divider);

        // Goals — each on its own labeled row
        for (String goal : plan.goals) {
            LinearLayout goalRow = new LinearLayout(this);
            goalRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            gp.setMargins(0, 3, 0, 3);
            goalRow.setLayoutParams(gp);

            TextView bullet = new TextView(this);
            bullet.setText("•");
            bullet.setTextSize(13f);
            bullet.setTextColor(accentColor);
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

        // Select button on every tier (not just recommended)
        View buttonDivider = makeDivider();
        LinearLayout.LayoutParams bdp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        bdp.setMargins(0, 10, 0, 8);
        buttonDivider.setLayoutParams(bdp);
        tierLayout.addView(buttonDivider);

        Button selectButton = new Button(this);
        String buttonLabel = tierIndex == 0 ? "▶  Start Low Intensity on Watch"
                : tierIndex == 1 ? "▶  Start Moderate Intensity on Watch"
                : "▶  Start High Intensity on Watch";
        selectButton.setText(buttonLabel);
        selectButton.setTextSize(12f);
        selectButton.setTextColor(COLOR_TEXT);
        selectButton.setTypeface(null, Typeface.BOLD);

        // Button color matches tier
        int btnColor = tierIndex == 0
                ? Color.parseColor("#BBDEFB")
                : tierIndex == 1 ? COLOR_BUTTON
                : Color.parseColor("#FFE0B2");
        selectButton.setBackgroundColor(btnColor);

        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 2, 0, 0);
        selectButton.setLayoutParams(btnParams);
        selectButton.setOnClickListener(v ->
                sendWorkoutToWatch(workoutTypeId, plan));
        tierLayout.addView(selectButton);

        return tierLayout;
    }

    private View makeDivider() {
        View d = new View(this);
        d.setBackgroundColor(COLOR_DIVIDER);
        return d;
    }

    private void sendWorkoutToWatch(int workoutTypeId,
                                    WorkoutRecommendationEngine.WorkoutPlan plan) {
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