package com.AidenLiriano.newyou;

import java.util.ArrayList;
import java.util.List;

/**
 * Workout Recommendation Engine
 *
 * Uses several different features of a user's fitness profile to generate
 * workout recommendations in three categories: low intensity, medium intensity, and high intensity.
 *
 * features evaluated per user:
 *   - Cardio capacity (from avg heart rate)
 *   - Endurance (from avg session duration)
 *   - Activity volume (from weekly active minutes)
 *   - Consistency (from sessions per week)
 *   - BMI / body composition (from weight and height)
 *   - Activity-specific experience (from session count per type)
 */
public class WorkoutRecommendationEngine {

    // Data classes

    public static class WorkoutPlan {
        public final String tierName;
        public final String description;
        public final List<String> goals;
        public final int intensityLevel; // 1=low, 2=medium, 3=high

        public WorkoutPlan(String tierName, String description,
                           List<String> goals, int intensityLevel) {
            this.tierName       = tierName;
            this.description    = description;
            this.goals          = goals;
            this.intensityLevel = intensityLevel;
        }
    }

    public static class WorkoutRecommendation {
        public final String workoutName;
        public final List<WorkoutPlan> allTiers; // always 3: low, medium, high
        public final int recommendedTierIndex;   // 0, 1, or 2

        public WorkoutRecommendation(String workoutName,
                                     List<WorkoutPlan> allTiers,
                                     int recommendedTierIndex) {
            this.workoutName          = workoutName;
            this.allTiers             = allTiers;
            this.recommendedTierIndex = recommendedTierIndex;
        }

        public WorkoutPlan getRecommended() {
            return allTiers.get(recommendedTierIndex);
        }
    }

    private static class Piece {
        final String label;  // shown as a goal line
        final int    minScore; // minimum fitness score for this piece to apply
        final int    maxScore; // maximum fitness score (exclusive)
        final int    dimension; // which fitness dimension this piece targets
        // Dimensions: 0=cardio, 1=endurance, 2=volume, 3=consistency, 4=bmi, 5=experience

        Piece(String label, int minScore, int maxScore, int dimension) {
            this.label     = label;
            this.minScore  = minScore;
            this.maxScore  = maxScore;
            this.dimension = dimension;
        }
    }

    // User fitness profile
    private static class FitnessProfile {
        int cardio;      // based on avg heart rate
        int endurance;   // based on avg session duration
        int volume;      // based on weekly active minutes
        int consistency; // based on sessions per week
        int bmi;         // based on BMI
        int experience;  // based on total sessions for a specific activity

        int[] scores() {
            return new int[]{cardio, endurance, volume, consistency, bmi, experience};
        }
    }

    // Main enter point
    public static List<WorkoutRecommendation> generateRecommendations(
            int age,
            float weightLbs,
            float avgHeartRate,
            int totalSessions) {

        int runSessions  = Math.round(totalSessions * 0.25f);
        int swimSessions = Math.round(totalSessions * 0.10f);
        int bikeSessions = Math.round(totalSessions * 0.15f);
        int walkSessions = Math.round(totalSessions * 0.20f);
        int hikeSessions = Math.round(totalSessions * 0.08f);
        int medSessions  = Math.round(totalSessions * 0.08f);
        int strSessions  = Math.round(totalSessions * 0.08f);
        int yogaSessions = Math.round(totalSessions * 0.06f);

        float avgDurationMinutes    = totalSessions > 0 ? 35f : 0f;
        float weeklyActiveMinutes   = totalSessions > 0
                ? Math.min(300f, totalSessions * 8f) : 0f;
        float weeklySessionCount    = totalSessions > 0
                ? Math.min(7f, totalSessions / 4f) : 0f;

        FitnessProfile profile = buildProfile(age, weightLbs, avgHeartRate,
                avgDurationMinutes, weeklyActiveMinutes,
                weeklySessionCount, 0);

        List<WorkoutRecommendation> recommendations = new ArrayList<>();
        recommendations.add(buildRunning(profile, runSessions));
        recommendations.add(buildSwimming(profile, swimSessions));
        recommendations.add(buildBiking(profile, bikeSessions));
        recommendations.add(buildWalking(profile, walkSessions));
        recommendations.add(buildHiking(profile, hikeSessions));
        recommendations.add(buildMeditation(profile, medSessions));
        recommendations.add(buildStrengthTraining(profile, strSessions));
        recommendations.add(buildYoga(profile, yogaSessions));

        return recommendations;
    }

    // Profile builder
    private static FitnessProfile buildProfile(
            int age, float weightLbs, float avgHeartRate,
            float avgDurationMinutes, float weeklyActiveMinutes,
            float weeklySessionCount, int activitySessions) {

        FitnessProfile p = new FitnessProfile();

        // Cardio score from heart rate
        if      (avgHeartRate <= 0)   p.cardio = 50;
        else if (avgHeartRate < 55)   p.cardio = 95;
        else if (avgHeartRate < 62)   p.cardio = 80;
        else if (avgHeartRate < 70)   p.cardio = 65;
        else if (avgHeartRate < 78)   p.cardio = 50;
        else if (avgHeartRate < 86)   p.cardio = 35;
        else                          p.cardio = 20;

        // Age adjustment to cardio
        if (age < 25)        p.cardio = Math.min(100, p.cardio + 10);
        else if (age > 55)   p.cardio = Math.max(0, p.cardio - 10);

        // Endurance score from avg session duration
        if      (avgDurationMinutes <= 0)  p.endurance = 20;
        else if (avgDurationMinutes >= 60) p.endurance = 90;
        else if (avgDurationMinutes >= 45) p.endurance = 75;
        else if (avgDurationMinutes >= 30) p.endurance = 60;
        else if (avgDurationMinutes >= 20) p.endurance = 40;
        else                               p.endurance = 25;

        // Volume score from weekly active minutes
        if      (weeklyActiveMinutes <= 0)   p.volume = 5;
        else if (weeklyActiveMinutes >= 300)  p.volume = 100;
        else if (weeklyActiveMinutes >= 200)  p.volume = 82;
        else if (weeklyActiveMinutes >= 150)  p.volume = 70;
        else if (weeklyActiveMinutes >= 90)   p.volume = 52;
        else if (weeklyActiveMinutes >= 45)   p.volume = 34;
        else                                  p.volume = 15;

        // Consistency score from sessions per week
        if      (weeklySessionCount <= 0) p.consistency = 5;
        else if (weeklySessionCount >= 5) p.consistency = 100;
        else if (weeklySessionCount >= 4) p.consistency = 82;
        else if (weeklySessionCount >= 3) p.consistency = 65;
        else if (weeklySessionCount >= 2) p.consistency = 48;
        else                              p.consistency = 25;

        // BMI score
        float heightIn = 67f; // assume average if not available
        float bmi = (weightLbs / (heightIn * heightIn)) * 703f;
        if      (bmi < 18.5f) p.bmi = 55;
        else if (bmi < 22f)   p.bmi = 100;
        else if (bmi < 25f)   p.bmi = 85;
        else if (bmi < 28f)   p.bmi = 65;
        else if (bmi < 30f)   p.bmi = 50;
        else if (bmi < 35f)   p.bmi = 30;
        else                  p.bmi = 15;

        // Experience score from activity-specific sessions
        if      (activitySessions >= 20) p.experience = 100;
        else if (activitySessions >= 12) p.experience = 80;
        else if (activitySessions >= 6)  p.experience = 60;
        else if (activitySessions >= 3)  p.experience = 40;
        else if (activitySessions >= 1)  p.experience = 20;
        else                             p.experience = 0;

        return p;
    }

    // Picks the best matching workout piece from a list based on the user's score for that dimension
    private static Piece selectPiece(List<Piece> pieces, FitnessProfile profile) {
        int[] scores = profile.scores();
        for (Piece p : pieces) {
            int userScore = scores[Math.min(p.dimension, scores.length - 1)];
            if (userScore >= p.minScore && userScore < p.maxScore) {
                return p;
            }
        }
        // Fallback to middle piece if no exact match
        return pieces.get(pieces.size() / 2);
    }

    private static Piece selectPieceForIntensity(List<Piece> pieces,
                                                 FitnessProfile profile, int intensity) {
        // Find matching piece based on profile
        int baseIndex = 0;
        int[] scores  = profile.scores();
        for (int i = 0; i < pieces.size(); i++) {
            Piece p = pieces.get(i);
            int userScore = scores[Math.min(p.dimension, scores.length - 1)];
            if (userScore >= p.minScore && userScore < p.maxScore) {
                baseIndex = i;
                break;
            }
        }
        int offset = intensity - 2;
        int index  = Math.max(0, Math.min(pieces.size() - 1, baseIndex + offset));
        return pieces.get(index);
    }

    // Intensity label helper
    private static String intensityLabel(int intensity, String activity) {
        switch (intensity) {
            case 1: return "Low Intensity " + activity;
            case 2: return "Moderate " + activity;
            case 3: return "High Intensity " + activity;
            default: return activity;
        }
    }

    private static String intensityDescription(int intensity, String baseDesc) {
        switch (intensity) {
            case 1: return "A gentle, accessible session. " + baseDesc;
            case 2: return "A balanced, effective session. " + baseDesc;
            case 3: return "A challenging, high-output session. " + baseDesc;
            default: return baseDesc;
        }
    }

    // Recommended tier picker
    private static int pickRecommendedTier(FitnessProfile profile,
                                           int activitySessions) {
        // Overall fitness score as a simple average of key dimensions
        int overall = (profile.cardio + profile.endurance + profile.volume
                + profile.consistency) / 4;

        // Beginners (few sessions) stay at low regardless of overall score
        if (activitySessions < 3 && overall < 60) return 0;
        if (overall >= 70) return 2;
        if (overall >= 45) return 1;
        return 0;
    }

    // RUNNING
    private static WorkoutRecommendation buildRunning(
            FitnessProfile profile, int sessions) {

        List<Piece> warmupPieces = new ArrayList<>();
        warmupPieces.add(new Piece("Warm-up: 3 min slow walk", 0, 35, 0));
        warmupPieces.add(new Piece("Warm-up: 5 min brisk walk", 35, 65, 0));
        warmupPieces.add(new Piece("Warm-up: 5 min easy jog", 65, 101, 0));

        List<Piece> mainPieces = new ArrayList<>();
        mainPieces.add(new Piece("Run/Walk intervals: 1 min run, 3 min walk × 4", 0, 25, 1));
        mainPieces.add(new Piece("Run/Walk intervals: 2 min run, 2 min walk × 5", 25, 45, 1));
        mainPieces.add(new Piece("Easy continuous run for 20 minutes", 45, 60, 1));
        mainPieces.add(new Piece("Steady run for 30 minutes at conversational pace", 60, 75, 1));
        mainPieces.add(new Piece("Tempo run for 35 minutes — comfortably hard pace", 75, 88, 1));
        mainPieces.add(new Piece("6 × 800m intervals with 90 second rest between each", 88, 101, 1));

        List<Piece> targetHRPieces = new ArrayList<>();
        targetHRPieces.add(new Piece("Target heart rate: 50–60% max HR", 0, 35, 0));
        targetHRPieces.add(new Piece("Target heart rate: 60–70% max HR", 35, 60, 0));
        targetHRPieces.add(new Piece("Target heart rate: 70–80% max HR", 60, 78, 0));
        targetHRPieces.add(new Piece("Target heart rate: 80–90% max HR", 78, 101, 0));

        List<Piece> distancePieces = new ArrayList<>();
        distancePieces.add(new Piece("Target distance: 1–2 km", 0, 30, 1));
        distancePieces.add(new Piece("Target distance: 2–4 km", 30, 55, 1));
        distancePieces.add(new Piece("Target distance: 4–6 km", 55, 72, 1));
        distancePieces.add(new Piece("Target distance: 6–9 km", 72, 88, 1));
        distancePieces.add(new Piece("Target distance: 9+ km", 88, 101, 1));

        List<Piece> stepsPieces = new ArrayList<>();
        stepsPieces.add(new Piece("Target steps: 2,000–3,500", 0, 35, 2));
        stepsPieces.add(new Piece("Target steps: 4,000–5,500", 35, 60, 2));
        stepsPieces.add(new Piece("Target steps: 6,000–8,000", 60, 78, 2));
        stepsPieces.add(new Piece("Target steps: 8,500–11,000", 78, 101, 2));

        List<Piece> calPieces = new ArrayList<>();
        calPieces.add(new Piece("Target calories: 120–200 kcal", 0, 35, 4));
        calPieces.add(new Piece("Target calories: 220–340 kcal", 35, 60, 4));
        calPieces.add(new Piece("Target calories: 360–480 kcal", 60, 78, 4));
        calPieces.add(new Piece("Target calories: 500+ kcal", 78, 101, 4));

        List<WorkoutPlan> tiers = new ArrayList<>();
        for (int intensity = 1; intensity <= 3; intensity++) {
            List<String> goals = new ArrayList<>();
            goals.add(selectPieceForIntensity(warmupPieces, profile, intensity).label);
            goals.add(selectPieceForIntensity(mainPieces, profile, intensity).label);
            goals.add(selectPiece(targetHRPieces, profile).label);
            goals.add(selectPieceForIntensity(distancePieces, profile, intensity).label);
            goals.add(selectPieceForIntensity(stepsPieces, profile, intensity).label);
            goals.add(selectPieceForIntensity(calPieces, profile, intensity).label);
            goals.add("Cool-down: 3 min slow walk and light stretch");
            tiers.add(new WorkoutPlan(
                    intensityLabel(intensity, "Run"),
                    intensityDescription(intensity,
                            "Build aerobic fitness and improve your cardiovascular health."),
                    goals, intensity));
        }

        return new WorkoutRecommendation("Running", tiers,
                pickRecommendedTier(profile, sessions));
    }

    // SWIMMING

    private static WorkoutRecommendation buildSwimming(
            FitnessProfile profile, int sessions) {

        List<Piece> lapPieces = new ArrayList<>();
        lapPieces.add(new Piece("Target laps: 4–6 laps with 60 second rest each", 0, 30, 1));
        lapPieces.add(new Piece("Target laps: 8–10 laps with 30 second rest", 30, 50, 1));
        lapPieces.add(new Piece("Target laps: 12–16 laps with 20 second rest", 50, 68, 1));
        lapPieces.add(new Piece("Target laps: 18–24 laps with minimal rest", 68, 82, 1));
        lapPieces.add(new Piece("Target laps: 28+ laps, sprint every 4th lap", 82, 101, 1));

        List<Piece> stylePieces = new ArrayList<>();
        stylePieces.add(new Piece("Stroke: freestyle only, focus on breathing", 0, 40, 5));
        stylePieces.add(new Piece("Stroke: freestyle with backstroke cool-down", 40, 65, 5));
        stylePieces.add(new Piece("Stroke: mix freestyle and breaststroke", 65, 82, 5));
        stylePieces.add(new Piece("Stroke: all four strokes in rotation", 82, 101, 5));

        List<Piece> durationPieces = new ArrayList<>();
        durationPieces.add(new Piece("Duration: 15–20 minutes", 0, 30, 1));
        durationPieces.add(new Piece("Duration: 25–30 minutes", 30, 55, 1));
        durationPieces.add(new Piece("Duration: 35–40 minutes", 55, 72, 1));
        durationPieces.add(new Piece("Duration: 45–55 minutes", 72, 101, 1));

        List<Piece> calPieces = new ArrayList<>();
        calPieces.add(new Piece("Target calories: 80–130 kcal", 0, 30, 4));
        calPieces.add(new Piece("Target calories: 160–220 kcal", 30, 55, 4));
        calPieces.add(new Piece("Target calories: 260–340 kcal", 55, 75, 4));
        calPieces.add(new Piece("Target calories: 380–500 kcal", 75, 101, 4));

        List<WorkoutPlan> tiers = new ArrayList<>();
        for (int intensity = 1; intensity <= 3; intensity++) {
            List<String> goals = new ArrayList<>();
            goals.add(selectPieceForIntensity(durationPieces, profile, intensity).label);
            goals.add(selectPieceForIntensity(lapPieces, profile, intensity).label);
            goals.add(selectPiece(stylePieces, profile).label);
            goals.add(intensity == 1
                    ? "Target heart rate: 50–65% max HR"
                    : intensity == 2
                    ? "Target heart rate: 65–75% max HR"
                    : "Target heart rate: 75–88% max HR");
            goals.add(selectPieceForIntensity(calPieces, profile, intensity).label);
            goals.add("Focus on stroke efficiency and steady breathing rhythm");
            tiers.add(new WorkoutPlan(
                    intensityLabel(intensity, "Swim"),
                    intensityDescription(intensity,
                            "Full body low-impact cardio that builds endurance safely."),
                    goals, intensity));
        }

        return new WorkoutRecommendation("Swimming", tiers,
                pickRecommendedTier(profile, sessions));
    }

    // BIKING
    private static WorkoutRecommendation buildBiking(
            FitnessProfile profile, int sessions) {

        List<Piece> distancePieces = new ArrayList<>();
        distancePieces.add(new Piece("Target distance: 4–6 km", 0, 30, 1));
        distancePieces.add(new Piece("Target distance: 8–12 km", 30, 52, 1));
        distancePieces.add(new Piece("Target distance: 13–18 km", 52, 70, 1));
        distancePieces.add(new Piece("Target distance: 20–28 km", 70, 85, 1));
        distancePieces.add(new Piece("Target distance: 30+ km", 85, 101, 1));

        List<Piece> speedPieces = new ArrayList<>();
        speedPieces.add(new Piece("Target speed: 10–14 km/h", 0, 30, 0));
        speedPieces.add(new Piece("Target speed: 15–18 km/h", 30, 52, 0));
        speedPieces.add(new Piece("Target speed: 19–22 km/h", 52, 70, 0));
        speedPieces.add(new Piece("Target speed: 23–27 km/h", 70, 85, 0));
        speedPieces.add(new Piece("Target speed: 28+ km/h", 85, 101, 0));

        List<Piece> durationPieces = new ArrayList<>();
        durationPieces.add(new Piece("Duration: 20–25 minutes", 0, 30, 1));
        durationPieces.add(new Piece("Duration: 30–35 minutes", 30, 55, 1));
        durationPieces.add(new Piece("Duration: 40–50 minutes", 55, 75, 1));
        durationPieces.add(new Piece("Duration: 55–70 minutes", 75, 101, 1));

        List<Piece> stylePieces = new ArrayList<>();
        stylePieces.add(new Piece("Steady flat terrain at comfortable pace", 0, 40, 5));
        stylePieces.add(new Piece("Mix of flat terrain and gentle hills", 40, 65, 5));
        stylePieces.add(new Piece("Rolling hills with sustained effort", 65, 82, 5));
        stylePieces.add(new Piece("Sprint intervals: 3 min fast, 2 min easy × 6", 82, 101, 5));

        List<Piece> calPieces = new ArrayList<>();
        calPieces.add(new Piece("Target calories: 100–170 kcal", 0, 30, 4));
        calPieces.add(new Piece("Target calories: 200–300 kcal", 30, 55, 4));
        calPieces.add(new Piece("Target calories: 330–440 kcal", 55, 75, 4));
        calPieces.add(new Piece("Target calories: 480–600 kcal", 75, 101, 4));

        List<WorkoutPlan> tiers = new ArrayList<>();
        for (int intensity = 1; intensity <= 3; intensity++) {
            List<String> goals = new ArrayList<>();
            goals.add(selectPieceForIntensity(durationPieces, profile, intensity).label);
            goals.add(selectPiece(stylePieces, profile).label);
            goals.add(selectPieceForIntensity(distancePieces, profile, intensity).label);
            goals.add(selectPieceForIntensity(speedPieces, profile, intensity).label);
            goals.add(intensity == 1
                    ? "Target heart rate: 50–65% max HR"
                    : intensity == 2
                    ? "Target heart rate: 65–75% max HR"
                    : "Target heart rate: 75–90% max HR");
            goals.add(selectPieceForIntensity(calPieces, profile, intensity).label);
            tiers.add(new WorkoutPlan(
                    intensityLabel(intensity, "Ride"),
                    intensityDescription(intensity,
                            "Builds leg strength and cardiovascular endurance."),
                    goals, intensity));
        }

        return new WorkoutRecommendation("Biking", tiers,
                pickRecommendedTier(profile, sessions));
    }

    // WALKING
    private static WorkoutRecommendation buildWalking(
            FitnessProfile profile, int sessions) {

        List<Piece> pacePieces = new ArrayList<>();
        pacePieces.add(new Piece("Pace: slow and comfortable — 16–18 min/km", 0, 30, 0));
        pacePieces.add(new Piece("Pace: relaxed — 13–15 min/km", 30, 50, 0));
        pacePieces.add(new Piece("Pace: brisk — 10–12 min/km", 50, 68, 0));
        pacePieces.add(new Piece("Pace: power walk — 7–9 min/km", 68, 83, 0));
        pacePieces.add(new Piece("Pace: race walk with arm drive — under 7 min/km", 83, 101, 0));

        List<Piece> durationPieces = new ArrayList<>();
        durationPieces.add(new Piece("Duration: 15–20 minutes", 0, 28, 1));
        durationPieces.add(new Piece("Duration: 22–28 minutes", 28, 48, 1));
        durationPieces.add(new Piece("Duration: 30–38 minutes", 48, 65, 1));
        durationPieces.add(new Piece("Duration: 40–50 minutes", 65, 80, 1));
        durationPieces.add(new Piece("Duration: 55–70 minutes", 80, 101, 1));

        List<Piece> stepsPieces = new ArrayList<>();
        stepsPieces.add(new Piece("Target steps: 1,500–2,500", 0, 28, 2));
        stepsPieces.add(new Piece("Target steps: 2,800–3,800", 28, 48, 2));
        stepsPieces.add(new Piece("Target steps: 4,000–5,500", 48, 65, 2));
        stepsPieces.add(new Piece("Target steps: 6,000–7,500", 65, 80, 2));
        stepsPieces.add(new Piece("Target steps: 8,000–10,000", 80, 101, 2));

        List<Piece> distancePieces = new ArrayList<>();
        distancePieces.add(new Piece("Target distance: 1–1.5 km", 0, 30, 1));
        distancePieces.add(new Piece("Target distance: 1.8–2.5 km", 30, 50, 1));
        distancePieces.add(new Piece("Target distance: 2.8–3.8 km", 50, 68, 1));
        distancePieces.add(new Piece("Target distance: 4–5.5 km", 68, 83, 1));
        distancePieces.add(new Piece("Target distance: 6+ km", 83, 101, 1));

        List<Piece> varPieces = new ArrayList<>();
        varPieces.add(new Piece("Walk on flat even ground for stability", 0, 35, 4));
        varPieces.add(new Piece("Include one gentle hill or slight incline", 35, 60, 4));
        varPieces.add(new Piece("Mix flat and moderate hills for added challenge", 60, 80, 4));
        varPieces.add(new Piece("Add incline intervals: 2 min hill, 3 min flat × 5", 80, 101, 4));

        List<WorkoutPlan> tiers = new ArrayList<>();
        for (int intensity = 1; intensity <= 3; intensity++) {
            List<String> goals = new ArrayList<>();
            goals.add(selectPieceForIntensity(durationPieces, profile, intensity).label);
            goals.add(selectPieceForIntensity(pacePieces, profile, intensity).label);
            goals.add(selectPieceForIntensity(distancePieces, profile, intensity).label);
            goals.add(selectPieceForIntensity(stepsPieces, profile, intensity).label);
            goals.add(selectPiece(varPieces, profile).label);
            goals.add(intensity == 1
                    ? "Target heart rate: 45–58% max HR"
                    : intensity == 2
                    ? "Target heart rate: 58–68% max HR"
                    : "Target heart rate: 68–80% max HR");
            tiers.add(new WorkoutPlan(
                    intensityLabel(intensity, "Walk"),
                    intensityDescription(intensity,
                            "Improves circulation, mood, and daily step count."),
                    goals, intensity));
        }

        return new WorkoutRecommendation("Walking", tiers,
                pickRecommendedTier(profile, sessions));
    }

    // HIKING
    private static WorkoutRecommendation buildHiking(
            FitnessProfile profile, int sessions) {

        List<Piece> distancePieces = new ArrayList<>();
        distancePieces.add(new Piece("Target distance: 1.5–2.5 km", 0, 30, 1));
        distancePieces.add(new Piece("Target distance: 3–4.5 km", 30, 52, 1));
        distancePieces.add(new Piece("Target distance: 5–7 km", 52, 70, 1));
        distancePieces.add(new Piece("Target distance: 8–11 km", 70, 85, 1));
        distancePieces.add(new Piece("Target distance: 12+ km", 85, 101, 1));

        List<Piece> elevPieces = new ArrayList<>();
        elevPieces.add(new Piece("Target elevation gain: under 40 m", 0, 30, 0));
        elevPieces.add(new Piece("Target elevation gain: 50–100 m", 30, 52, 0));
        elevPieces.add(new Piece("Target elevation gain: 120–200 m", 52, 70, 0));
        elevPieces.add(new Piece("Target elevation gain: 220–380 m", 70, 85, 0));
        elevPieces.add(new Piece("Target elevation gain: 400+ m", 85, 101, 0));

        List<Piece> durationPieces = new ArrayList<>();
        durationPieces.add(new Piece("Duration: 25–35 minutes", 0, 30, 1));
        durationPieces.add(new Piece("Duration: 40–55 minutes", 30, 52, 1));
        durationPieces.add(new Piece("Duration: 60–80 minutes", 52, 70, 1));
        durationPieces.add(new Piece("Duration: 90–120 minutes", 70, 85, 1));
        durationPieces.add(new Piece("Duration: 2+ hours", 85, 101, 1));

        List<Piece> terrainPieces = new ArrayList<>();
        terrainPieces.add(new Piece("Terrain: flat or gently rolling trail", 0, 38, 5));
        terrainPieces.add(new Piece("Terrain: moderate trail with some elevation", 38, 60, 5));
        terrainPieces.add(new Piece("Terrain: steady ascent and descent trail", 60, 78, 5));
        terrainPieces.add(new Piece("Terrain: technical trail with significant elevation", 78, 101, 5));

        List<Piece> stepsPieces = new ArrayList<>();
        stepsPieces.add(new Piece("Target steps: 2,500–4,000", 0, 35, 2));
        stepsPieces.add(new Piece("Target steps: 5,000–7,000", 35, 58, 2));
        stepsPieces.add(new Piece("Target steps: 8,000–11,000", 58, 75, 2));
        stepsPieces.add(new Piece("Target steps: 12,000–16,000", 75, 101, 2));

        List<WorkoutPlan> tiers = new ArrayList<>();
        for (int intensity = 1; intensity <= 3; intensity++) {
            List<String> goals = new ArrayList<>();
            goals.add(selectPieceForIntensity(durationPieces, profile, intensity).label);
            goals.add(selectPiece(terrainPieces, profile).label);
            goals.add(selectPieceForIntensity(distancePieces, profile, intensity).label);
            goals.add(selectPieceForIntensity(elevPieces, profile, intensity).label);
            goals.add(selectPieceForIntensity(stepsPieces, profile, intensity).label);
            goals.add(intensity == 1
                    ? "Target heart rate: 50–62% max HR"
                    : intensity == 2
                    ? "Target heart rate: 62–74% max HR"
                    : "Target heart rate: 74–85% max HR");
            goals.add("Bring water and take breaks as needed");
            tiers.add(new WorkoutPlan(
                    intensityLabel(intensity, "Hike"),
                    intensityDescription(intensity,
                            "Builds lower body strength, balance, and mental resilience."),
                    goals, intensity));
        }

        return new WorkoutRecommendation("Hiking", tiers,
                pickRecommendedTier(profile, sessions));
    }

    // MEDITATION

    private static WorkoutRecommendation buildMeditation(
            FitnessProfile profile, int sessions) {

        List<Piece> durationPieces = new ArrayList<>();
        durationPieces.add(new Piece("Duration: 5 minutes", 0, 30, 5));
        durationPieces.add(new Piece("Duration: 8–10 minutes", 30, 52, 5));
        durationPieces.add(new Piece("Duration: 12–18 minutes", 52, 70, 5));
        durationPieces.add(new Piece("Duration: 20–30 minutes", 70, 85, 5));
        durationPieces.add(new Piece("Duration: 35–50 minutes", 85, 101, 5));

        List<Piece> techniquePieces = new ArrayList<>();
        techniquePieces.add(new Piece("Technique: deep belly breathing only", 0, 28, 5));
        techniquePieces.add(new Piece("Technique: 4-7-8 breath counting", 28, 50, 5));
        techniquePieces.add(new Piece("Technique: body scan with breath awareness", 50, 68, 5));
        techniquePieces.add(new Piece("Technique: mindfulness with visualization", 68, 84, 5));
        techniquePieces.add(new Piece("Technique: advanced mantra or open monitoring", 84, 101, 5));

        List<Piece> hrGoalPieces = new ArrayList<>();
        hrGoalPieces.add(new Piece("Goal: reduce heart rate by 5% during session", 0, 40, 0));
        hrGoalPieces.add(new Piece("Goal: reduce heart rate by 10% during session", 40, 65, 0));
        hrGoalPieces.add(new Piece("Goal: reduce heart rate by 15% during session", 65, 82, 0));
        hrGoalPieces.add(new Piece("Goal: reduce heart rate by 20%+ during session", 82, 101, 0));

        List<Piece> envPieces = new ArrayList<>();
        envPieces.add(new Piece("Environment: comfortable seated position, eyes closed", 0, 40, 5));
        envPieces.add(new Piece("Environment: quiet space, use guided audio if helpful", 40, 65, 5));
        envPieces.add(new Piece("Environment: dedicated space with no distractions", 65, 101, 5));

        List<WorkoutPlan> tiers = new ArrayList<>();
        for (int intensity = 1; intensity <= 3; intensity++) {
            List<String> goals = new ArrayList<>();
            goals.add(selectPieceForIntensity(durationPieces, profile, intensity).label);
            goals.add(selectPieceForIntensity(techniquePieces, profile, intensity).label);
            goals.add(selectPiece(hrGoalPieces, profile).label);
            goals.add(selectPiece(envPieces, profile).label);
            goals.add(intensity == 1
                    ? "Focus: complete mental rest and tension release"
                    : intensity == 2
                    ? "Focus: sustained concentration and mindful breathing"
                    : "Focus: deep immersive awareness and emotional regulation");
            tiers.add(new WorkoutPlan(
                    intensity == 1 ? "Relaxation Session"
                            : intensity == 2 ? "Mindfulness Session"
                            : "Deep Meditation Session",
                    intensityDescription(intensity,
                            "Reduces stress, lowers heart rate, and improves mental clarity."),
                    goals, intensity));
        }

        return new WorkoutRecommendation("Meditation", tiers,
                pickRecommendedTier(profile, sessions));
    }

    // STRENGTH TRAINING

    private static WorkoutRecommendation buildStrengthTraining(
            FitnessProfile profile, int sessions) {

        List<Piece> mainMovementPieces = new ArrayList<>();
        mainMovementPieces.add(new Piece("Exercises: wall push-ups, chair squats, standing rows", 0, 25, 5));
        mainMovementPieces.add(new Piece("Exercises: push-ups, bodyweight squats, reverse lunges", 25, 45, 5));
        mainMovementPieces.add(new Piece("Exercises: push-ups, split squats, rows, plank", 45, 62, 5));
        mainMovementPieces.add(new Piece("Exercises: push-up variations, jump squats, pull-ups, dips", 62, 80, 5));
        mainMovementPieces.add(new Piece("Exercises: plyometrics, explosive movements, supersets", 80, 101, 5));

        List<Piece> setRepPieces = new ArrayList<>();
        setRepPieces.add(new Piece("Sets and reps: 2 sets × 8 reps, rest 90 sec", 0, 28, 5));
        setRepPieces.add(new Piece("Sets and reps: 2–3 sets × 10 reps, rest 60 sec", 28, 48, 5));
        setRepPieces.add(new Piece("Sets and reps: 3 sets × 12 reps, rest 45 sec", 48, 65, 5));
        setRepPieces.add(new Piece("Sets and reps: 4 sets × 12 reps, rest 30 sec", 65, 80, 5));
        setRepPieces.add(new Piece("Sets and reps: 4–5 sets × 15 reps, minimal rest", 80, 101, 5));

        List<Piece> durationPieces = new ArrayList<>();
        durationPieces.add(new Piece("Duration: 18–22 minutes", 0, 30, 1));
        durationPieces.add(new Piece("Duration: 25–32 minutes", 30, 52, 1));
        durationPieces.add(new Piece("Duration: 35–42 minutes", 52, 70, 1));
        durationPieces.add(new Piece("Duration: 45–55 minutes", 70, 101, 1));

        List<Piece> calPieces = new ArrayList<>();
        calPieces.add(new Piece("Target calories: 80–130 kcal", 0, 30, 4));
        calPieces.add(new Piece("Target calories: 150–220 kcal", 30, 52, 4));
        calPieces.add(new Piece("Target calories: 250–340 kcal", 52, 70, 4));
        calPieces.add(new Piece("Target calories: 380–500 kcal", 70, 101, 4));

        List<Piece> focusPieces = new ArrayList<>();
        focusPieces.add(new Piece("Focus area: full body with emphasis on core stability", 0, 40, 5));
        focusPieces.add(new Piece("Focus area: upper and lower body balanced", 40, 65, 5));
        focusPieces.add(new Piece("Focus area: compound movements and muscle endurance", 65, 82, 5));
        focusPieces.add(new Piece("Focus area: power, speed, and muscular strength", 82, 101, 5));

        List<WorkoutPlan> tiers = new ArrayList<>();
        for (int intensity = 1; intensity <= 3; intensity++) {
            List<String> goals = new ArrayList<>();
            goals.add(selectPieceForIntensity(durationPieces, profile, intensity).label);
            goals.add(selectPiece(focusPieces, profile).label);
            goals.add(selectPieceForIntensity(mainMovementPieces, profile, intensity).label);
            goals.add(selectPieceForIntensity(setRepPieces, profile, intensity).label);
            goals.add(intensity == 1
                    ? "Target heart rate: 50–62% max HR"
                    : intensity == 2
                    ? "Target heart rate: 62–72% max HR"
                    : "Target heart rate: 72–85% max HR");
            goals.add(selectPieceForIntensity(calPieces, profile, intensity).label);
            tiers.add(new WorkoutPlan(
                    intensityLabel(intensity, "Strength"),
                    intensityDescription(intensity,
                            "Builds functional strength, muscle tone, and metabolic rate."),
                    goals, intensity));
        }

        return new WorkoutRecommendation("Strength Training", tiers,
                pickRecommendedTier(profile, sessions));
    }

    // YOGA

    private static WorkoutRecommendation buildYoga(
            FitnessProfile profile, int sessions) {

        List<Piece> stylePieces = new ArrayList<>();
        stylePieces.add(new Piece("Style: restorative yoga — long held passive poses", 0, 28, 5));
        stylePieces.add(new Piece("Style: hatha yoga — basic foundational poses", 28, 48, 5));
        stylePieces.add(new Piece("Style: gentle vinyasa flow — linked breath and movement", 48, 65, 5));
        stylePieces.add(new Piece("Style: power vinyasa — dynamic and strength focused", 65, 82, 5));
        stylePieces.add(new Piece("Style: ashtanga — structured demanding sequence", 82, 101, 5));

        List<Piece> posesPieces = new ArrayList<>();
        posesPieces.add(new Piece("Poses: child's pose, supine twist, cat-cow, corpse pose", 0, 28, 5));
        posesPieces.add(new Piece("Poses: mountain, warrior I, downward dog, tree pose", 28, 48, 5));
        posesPieces.add(new Piece("Poses: sun salutations, warrior series, triangle, half moon", 48, 65, 5));
        posesPieces.add(new Piece("Poses: crow pose, wheel, headstand prep, chaturanga flows", 65, 82, 5));
        posesPieces.add(new Piece("Poses: handstand, full wheel, advanced arm balances", 82, 101, 5));

        List<Piece> durationPieces = new ArrayList<>();
        durationPieces.add(new Piece("Duration: 15–20 minutes", 0, 30, 1));
        durationPieces.add(new Piece("Duration: 25–30 minutes", 30, 52, 1));
        durationPieces.add(new Piece("Duration: 35–42 minutes", 52, 70, 1));
        durationPieces.add(new Piece("Duration: 45–55 minutes", 70, 101, 1));

        List<Piece> breathPieces = new ArrayList<>();
        breathPieces.add(new Piece("Breathing: natural breath, no specific technique", 0, 35, 5));
        breathPieces.add(new Piece("Breathing: ujjayi — ocean breath through nose", 35, 62, 5));
        breathPieces.add(new Piece("Breathing: full pranayama integration with poses", 62, 101, 5));

        List<Piece> calPieces = new ArrayList<>();
        calPieces.add(new Piece("Target calories: 50–90 kcal", 0, 30, 4));
        calPieces.add(new Piece("Target calories: 100–150 kcal", 30, 52, 4));
        calPieces.add(new Piece("Target calories: 170–240 kcal", 52, 70, 4));
        calPieces.add(new Piece("Target calories: 260–380 kcal", 70, 101, 4));

        List<WorkoutPlan> tiers = new ArrayList<>();
        for (int intensity = 1; intensity <= 3; intensity++) {
            List<String> goals = new ArrayList<>();
            goals.add(selectPieceForIntensity(durationPieces, profile, intensity).label);
            goals.add(selectPiece(stylePieces, profile).label);
            goals.add(selectPiece(posesPieces, profile).label);
            goals.add(selectPiece(breathPieces, profile).label);
            goals.add(intensity == 1
                    ? "Target heart rate: 45–58% max HR"
                    : intensity == 2
                    ? "Target heart rate: 58–68% max HR"
                    : "Target heart rate: 68–78% max HR");
            goals.add(selectPieceForIntensity(calPieces, profile, intensity).label);
            tiers.add(new WorkoutPlan(
                    intensity == 1 ? "Restorative Yoga"
                            : intensity == 2 ? "Flow Yoga"
                            : "Power Yoga",
                    intensityDescription(intensity,
                            "Improves flexibility, balance, strength, and mindfulness."),
                    goals, intensity));
        }

        return new WorkoutRecommendation("Yoga", tiers,
                pickRecommendedTier(profile, sessions));
    }

    private static List<String> goals(String... items) {
        List<String> list = new ArrayList<>();
        for (String item : items) list.add(item);
        return list;
    }
}