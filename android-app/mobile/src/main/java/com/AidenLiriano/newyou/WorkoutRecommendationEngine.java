package com.AidenLiriano.newyou;

import java.util.ArrayList;
import java.util.List;

public class WorkoutRecommendationEngine {

    public static class WorkoutPlan {
        public final String tierName;
        public final String description;
        public final List<String> goals;
        public final int intensityLevel;

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
        public final List<WorkoutPlan> allTiers;
        public final int recommendedTierIndex;

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

    // --- Main entry point ---
    public static List<WorkoutRecommendation> generateRecommendations(
            int age, float weightLbs, float avgHeartRate, int totalSessions) {

        // Per-activity session counts are derived from total sessions
        int runSessions      = Math.round(totalSessions * 0.25f);
        int swimSessions     = Math.round(totalSessions * 0.10f);
        int bikeSessions     = Math.round(totalSessions * 0.15f);
        int walkSessions     = Math.round(totalSessions * 0.20f);
        int hikeSessions     = Math.round(totalSessions * 0.08f);
        int meditSessions    = Math.round(totalSessions * 0.08f);
        int strengthSessions = Math.round(totalSessions * 0.08f);
        int yogaSessions     = Math.round(totalSessions * 0.06f);

        // Base fitness score from age, weight, and heart rate
        int baseScore = calculateBaseScore(age, weightLbs, avgHeartRate);

        List<WorkoutRecommendation> recommendations = new ArrayList<>();

        // Each workout type gets its own tier recommendation based on base score modified by activity-specific session count
        recommendations.add(buildRunning(
                clampTier(baseScore + sessionBonus(runSessions))));
        recommendations.add(buildSwimming(
                clampTier(baseScore + sessionBonus(swimSessions) - 5)));
        recommendations.add(buildBiking(
                clampTier(baseScore + sessionBonus(bikeSessions) + 5)));
        recommendations.add(buildWalking(
                clampTier(baseScore + sessionBonus(walkSessions) + 10)));
        recommendations.add(buildHiking(
                clampTier(baseScore + sessionBonus(hikeSessions) - 10)));
        recommendations.add(buildMeditation(
                clampTier(baseScore + sessionBonus(meditSessions) + 15)));
        recommendations.add(buildStrengthTraining(
                clampTier(baseScore + sessionBonus(strengthSessions) - 5)));
        recommendations.add(buildYoga(
                clampTier(baseScore + sessionBonus(yogaSessions) + 10)));

        return recommendations;
    }

    // Base score from age, weight, and heart rate, range 0-100
    private static int calculateBaseScore(int age, float weightLbs,
                                          float avgHeartRate) {
        int score = 50;

        // Age factor
        if (age < 20)      score += 15;
        else if (age < 30) score += 10;
        else if (age < 40) score += 5;
        else if (age < 50) score += 0;
        else if (age < 60) score -= 10;
        else               score -= 20;

        // BMI factor
        float bmi = (weightLbs / (67f * 67f)) * 703f;
        if (bmi < 18.5f)    score -= 5;
        else if (bmi < 25f) score += 10;
        else if (bmi < 30f) score += 0;
        else if (bmi < 35f) score -= 10;
        else                score -= 20;

        // Heart rate factor
        if (avgHeartRate <= 0)       score += 0;
        else if (avgHeartRate < 60)  score += 15;
        else if (avgHeartRate < 70)  score += 8;
        else if (avgHeartRate < 80)  score += 3;
        else if (avgHeartRate < 90)  score += 0;
        else if (avgHeartRate < 100) score -= 8;
        else                         score -= 18;

        return Math.max(0, Math.min(100, score));
    }

    // Bonus points based on how many sessions of a specific activity
    private static int sessionBonus(int sessions) {
        if (sessions >= 15) return 20;
        if (sessions >= 8)  return 12;
        if (sessions >= 4)  return 6;
        if (sessions >= 2)  return 2;
        return -5;
    }

    // Map score 0-100 to tier index 0-4
    // Each activity type uses slightly different thresholds so the same user gets different tiers for different workouts
    private static int clampTier(int score) {
        if (score >= 80) return 4;
        if (score >= 62) return 3;
        if (score >= 44) return 2;
        if (score >= 26) return 1;
        return 0;
    }

    // WORKOUT PLAN BUILDERS — 5 tiers each

    private static WorkoutRecommendation buildRunning(int recommended) {
        List<WorkoutPlan> tiers = new ArrayList<>();
        tiers.add(new WorkoutPlan("Beginner Run",
                "A gentle introduction to running with walk breaks.",
                goals("Duration: 20 minutes",
                        "Run/Walk intervals: 1 min run, 2 min walk",
                        "Target heart rate: 50-60% max HR",
                        "Target pace: 12-14 min/km",
                        "Target steps: 2,000-3,000"), 1));
        tiers.add(new WorkoutPlan("Easy Run",
                "A comfortable continuous run at a conversational pace.",
                goals("Duration: 25-30 minutes",
                        "Continuous running",
                        "Target heart rate: 60-65% max HR",
                        "Target pace: 9-11 min/km",
                        "Target steps: 3,500-4,500"), 2));
        tiers.add(new WorkoutPlan("Moderate Run",
                "A steady run that builds endurance and cardiovascular fitness.",
                goals("Duration: 35-40 minutes",
                        "Steady pace throughout",
                        "Target heart rate: 65-75% max HR",
                        "Target pace: 7-9 min/km",
                        "Target steps: 5,000-6,000"), 3));
        tiers.add(new WorkoutPlan("Tempo Run",
                "A comfortably hard run that improves speed and lactate threshold.",
                goals("Duration: 40-45 minutes",
                        "Warm up 10 min, tempo 20 min, cool down 10 min",
                        "Target heart rate: 75-85% max HR",
                        "Target pace: 5-7 min/km",
                        "Target steps: 6,500-8,000"), 4));
        tiers.add(new WorkoutPlan("Advanced Interval Run",
                "High intensity intervals to maximize performance gains.",
                goals("Duration: 45-50 minutes",
                        "6x800m intervals at near max effort",
                        "Target heart rate: 85-95% max HR during intervals",
                        "Target pace: under 5 min/km on intervals",
                        "Target steps: 8,000-10,000"), 5));
        return new WorkoutRecommendation("Running", tiers, recommended);
    }

    private static WorkoutRecommendation buildSwimming(int recommended) {
        List<WorkoutPlan> tiers = new ArrayList<>();
        tiers.add(new WorkoutPlan("Beginner Swim",
                "Get comfortable in the water with short easy sets.",
                goals("Duration: 20 minutes",
                        "Target laps: 4-6 laps (100m)",
                        "Rest 60 seconds between laps",
                        "Target heart rate: 50-60% max HR",
                        "Focus on breathing technique"), 1));
        tiers.add(new WorkoutPlan("Easy Swim",
                "Build basic swim fitness with consistent easy laps.",
                goals("Duration: 25-30 minutes",
                        "Target laps: 8-10 laps (200-250m)",
                        "Rest 30 seconds between sets",
                        "Target heart rate: 60-65% max HR",
                        "Focus on stroke efficiency"), 2));
        tiers.add(new WorkoutPlan("Moderate Swim",
                "A structured swim session mixing distances and rest.",
                goals("Duration: 35 minutes",
                        "Target laps: 14-16 laps (350-400m)",
                        "2x4 lap sets with 30 second rest",
                        "Target heart rate: 65-75% max HR",
                        "Target calories: 200-300 kcal"), 3));
        tiers.add(new WorkoutPlan("Endurance Swim",
                "Long continuous swim to build stamina.",
                goals("Duration: 40-45 minutes",
                        "Target laps: 20-24 laps (500-600m)",
                        "Minimal rest between laps",
                        "Target heart rate: 75-85% max HR",
                        "Target calories: 350-450 kcal"), 4));
        tiers.add(new WorkoutPlan("Advanced Swim",
                "High intensity swim training with sprint intervals.",
                goals("Duration: 50-60 minutes",
                        "Target laps: 30+ laps (750m+)",
                        "8x2 lap sprints with 15 second rest",
                        "Target heart rate: 85-95% max HR",
                        "Target calories: 500+ kcal"), 5));
        return new WorkoutRecommendation("Swimming", tiers, recommended);
    }

    private static WorkoutRecommendation buildBiking(int recommended) {
        List<WorkoutPlan> tiers = new ArrayList<>();
        tiers.add(new WorkoutPlan("Beginner Ride",
                "A gentle flat ride to build comfort and basic fitness.",
                goals("Duration: 20-25 minutes",
                        "Target distance: 5-6 km",
                        "Target speed: 12-15 km/h",
                        "Target heart rate: 50-60% max HR",
                        "Target calories: 100-150 kcal"), 1));
        tiers.add(new WorkoutPlan("Easy Ride",
                "A relaxed ride at a comfortable steady pace.",
                goals("Duration: 30 minutes",
                        "Target distance: 8-10 km",
                        "Target speed: 15-18 km/h",
                        "Target heart rate: 60-65% max HR",
                        "Target calories: 180-220 kcal"), 2));
        tiers.add(new WorkoutPlan("Moderate Ride",
                "A steady ride building endurance and leg strength.",
                goals("Duration: 40 minutes",
                        "Target distance: 12-15 km",
                        "Target speed: 18-22 km/h",
                        "Target heart rate: 65-75% max HR",
                        "Target calories: 280-350 kcal"), 3));
        tiers.add(new WorkoutPlan("Endurance Ride",
                "A longer ride to build sustained cardiovascular endurance.",
                goals("Duration: 50-60 minutes",
                        "Target distance: 18-22 km",
                        "Target speed: 22-26 km/h",
                        "Target heart rate: 75-85% max HR",
                        "Target calories: 400-500 kcal"), 4));
        tiers.add(new WorkoutPlan("Advanced Interval Ride",
                "High intensity cycling intervals to maximize power output.",
                goals("Duration: 60 minutes",
                        "Target distance: 25+ km",
                        "6x3 minute sprints at max effort",
                        "Target heart rate: 85-95% max HR during sprints",
                        "Target calories: 550+ kcal"), 5));
        return new WorkoutRecommendation("Biking", tiers, recommended);
    }

    private static WorkoutRecommendation buildWalking(int recommended) {
        List<WorkoutPlan> tiers = new ArrayList<>();
        tiers.add(new WorkoutPlan("Gentle Walk",
                "A slow comfortable walk ideal for beginners or recovery days.",
                goals("Duration: 20 minutes",
                        "Target steps: 2,000-2,500",
                        "Target distance: 1.5-2 km",
                        "Target pace: 14-16 min/km",
                        "Target heart rate: 45-55% max HR"), 1));
        tiers.add(new WorkoutPlan("Easy Walk",
                "A relaxed walk at a natural comfortable pace.",
                goals("Duration: 25-30 minutes",
                        "Target steps: 3,000-3,500",
                        "Target distance: 2-2.5 km",
                        "Target pace: 12-14 min/km",
                        "Target heart rate: 50-60% max HR"), 2));
        tiers.add(new WorkoutPlan("Brisk Walk",
                "A purposeful walk at an elevated pace for cardio benefits.",
                goals("Duration: 35 minutes",
                        "Target steps: 4,500-5,500",
                        "Target distance: 3-3.5 km",
                        "Target pace: 9-11 min/km",
                        "Target heart rate: 60-70% max HR"), 3));
        tiers.add(new WorkoutPlan("Power Walk",
                "An intense walk with deliberate arm swing and fast pace.",
                goals("Duration: 40-45 minutes",
                        "Target steps: 6,000-7,000",
                        "Target distance: 4-5 km",
                        "Target pace: 7-9 min/km",
                        "Target heart rate: 65-75% max HR"), 4));
        tiers.add(new WorkoutPlan("Advanced Walk",
                "Maximum effort walking with incline or weighted vest.",
                goals("Duration: 50-60 minutes",
                        "Target steps: 8,000-10,000",
                        "Target distance: 6+ km",
                        "Target pace: under 7 min/km",
                        "Target heart rate: 70-80% max HR"), 5));
        return new WorkoutRecommendation("Walking", tiers, recommended);
    }

    private static WorkoutRecommendation buildHiking(int recommended) {
        List<WorkoutPlan> tiers = new ArrayList<>();
        tiers.add(new WorkoutPlan("Beginner Hike",
                "A flat easy trail walk to build hiking confidence.",
                goals("Duration: 30 minutes",
                        "Target distance: 1.5-2 km",
                        "Target elevation gain: under 30 m",
                        "Target steps: 2,500-3,000",
                        "Target heart rate: 50-60% max HR"), 1));
        tiers.add(new WorkoutPlan("Easy Hike",
                "A gentle trail with mild elevation for light fitness.",
                goals("Duration: 40-45 minutes",
                        "Target distance: 2.5-3.5 km",
                        "Target elevation gain: 30-60 m",
                        "Target steps: 3,500-4,500",
                        "Target heart rate: 55-65% max HR"), 2));
        tiers.add(new WorkoutPlan("Moderate Hike",
                "A trail with consistent elevation that builds leg strength.",
                goals("Duration: 60 minutes",
                        "Target distance: 4-6 km",
                        "Target elevation gain: 100-150 m",
                        "Target steps: 6,000-7,500",
                        "Target heart rate: 65-75% max HR"), 3));
        tiers.add(new WorkoutPlan("Challenging Hike",
                "A demanding trail with significant elevation gain.",
                goals("Duration: 90 minutes",
                        "Target distance: 7-10 km",
                        "Target elevation gain: 200-350 m",
                        "Target steps: 10,000-13,000",
                        "Target heart rate: 70-80% max HR"), 4));
        tiers.add(new WorkoutPlan("Advanced Summit Hike",
                "A strenuous long distance hike with major elevation.",
                goals("Duration: 2+ hours",
                        "Target distance: 12+ km",
                        "Target elevation gain: 500+ m",
                        "Target steps: 15,000+",
                        "Target heart rate: 75-85% max HR"), 5));
        return new WorkoutRecommendation("Hiking", tiers, recommended);
    }

    private static WorkoutRecommendation buildMeditation(int recommended) {
        List<WorkoutPlan> tiers = new ArrayList<>();
        tiers.add(new WorkoutPlan("Introduction Session",
                "A very short guided meditation for complete beginners.",
                goals("Duration: 5 minutes",
                        "Focus: deep breathing only",
                        "Target starting heart rate: any",
                        "Target ending heart rate: 5-10% lower than start",
                        "Seated position recommended"), 1));
        tiers.add(new WorkoutPlan("Beginner Session",
                "A short calm session focusing on breath awareness.",
                goals("Duration: 10 minutes",
                        "Focus: breath counting",
                        "Target starting heart rate: any",
                        "Target ending heart rate: 10% lower than start",
                        "Body scan technique"), 2));
        tiers.add(new WorkoutPlan("Intermediate Session",
                "A focused mindfulness session building concentration.",
                goals("Duration: 15-20 minutes",
                        "Focus: mindfulness and body awareness",
                        "Target heart rate reduction: 10-15% during session",
                        "Visualisation techniques",
                        "Consistent breathing rhythm"), 3));
        tiers.add(new WorkoutPlan("Deep Session",
                "An extended meditation session for experienced practitioners.",
                goals("Duration: 25-30 minutes",
                        "Focus: deep mindfulness or mantra",
                        "Target heart rate reduction: 15-20% during session",
                        "Progressive muscle relaxation",
                        "Minimal external distraction"), 4));
        tiers.add(new WorkoutPlan("Advanced Session",
                "A long immersive meditation for maximum mental recovery.",
                goals("Duration: 40-60 minutes",
                        "Focus: sustained deep concentration",
                        "Target heart rate reduction: 20%+ during session",
                        "Advanced breathing techniques",
                        "Complete stillness throughout"), 5));
        return new WorkoutRecommendation("Meditation", tiers, recommended);
    }

    private static WorkoutRecommendation buildStrengthTraining(int recommended) {
        List<WorkoutPlan> tiers = new ArrayList<>();
        tiers.add(new WorkoutPlan("Beginner Strength",
                "Bodyweight basics to build foundational strength safely.",
                goals("Duration: 20 minutes",
                        "Exercises: wall push-ups, chair squats, standing calf raises",
                        "2 sets of 8-10 reps each",
                        "Target heart rate: 50-60% max HR",
                        "Target calories: 80-120 kcal"), 1));
        tiers.add(new WorkoutPlan("Easy Strength",
                "Light resistance training to build muscle and confidence.",
                goals("Duration: 25-30 minutes",
                        "Exercises: push-ups, bodyweight squats, lunges",
                        "2-3 sets of 10-12 reps each",
                        "Target heart rate: 55-65% max HR",
                        "Target calories: 150-200 kcal"), 2));
        tiers.add(new WorkoutPlan("Moderate Strength",
                "A full body session with moderate resistance.",
                goals("Duration: 35-40 minutes",
                        "Exercises: push-ups, squats, rows, planks, dips",
                        "3 sets of 12-15 reps each",
                        "Target heart rate: 60-70% max HR",
                        "Target calories: 250-320 kcal"), 3));
        tiers.add(new WorkoutPlan("Challenging Strength",
                "An intense full body session with compound movements.",
                goals("Duration: 45-50 minutes",
                        "Exercises: burpees, jump squats, push-up variations, pull-ups",
                        "4 sets of 12-15 reps each",
                        "Target heart rate: 70-80% max HR",
                        "Target calories: 380-450 kcal"), 4));
        tiers.add(new WorkoutPlan("Advanced Strength",
                "High volume intense training for experienced athletes.",
                goals("Duration: 55-60 minutes",
                        "Exercises: explosive movements, plyometrics, supersets",
                        "4-5 sets of 15-20 reps each",
                        "Target heart rate: 80-90% max HR",
                        "Target calories: 500+ kcal"), 5));
        return new WorkoutRecommendation("Strength Training", tiers, recommended);
    }

    private static WorkoutRecommendation buildYoga(int recommended) {
        List<WorkoutPlan> tiers = new ArrayList<>();
        tiers.add(new WorkoutPlan("Restorative Yoga",
                "Gentle held poses for relaxation and flexibility.",
                goals("Duration: 20 minutes",
                        "Style: restorative or yin",
                        "Poses: child's pose, supine twist, legs up wall",
                        "Target heart rate: 45-55% max HR",
                        "Target calories: 50-80 kcal"), 1));
        tiers.add(new WorkoutPlan("Beginner Yoga",
                "Simple foundational poses building flexibility and balance.",
                goals("Duration: 25-30 minutes",
                        "Style: hatha",
                        "Poses: mountain, downward dog, warrior I",
                        "Target heart rate: 50-60% max HR",
                        "Target calories: 100-140 kcal"), 2));
        tiers.add(new WorkoutPlan("Moderate Yoga",
                "A flowing sequence connecting breath and movement.",
                goals("Duration: 35-40 minutes",
                        "Style: vinyasa flow",
                        "Poses: sun salutations, warrior series, balancing poses",
                        "Target heart rate: 55-65% max HR",
                        "Target calories: 180-230 kcal"), 3));
        tiers.add(new WorkoutPlan("Active Yoga",
                "A vigorous flowing practice building strength and flexibility.",
                goals("Duration: 45 minutes",
                        "Style: power vinyasa",
                        "Poses: crow, wheel, headstand prep, chaturanga",
                        "Target heart rate: 65-75% max HR",
                        "Target calories: 280-350 kcal"), 4));
        tiers.add(new WorkoutPlan("Advanced Yoga",
                "A challenging practice requiring strength and advanced poses.",
                goals("Duration: 60 minutes",
                        "Style: ashtanga or advanced vinyasa",
                        "Poses: handstand, full wheel, advanced balances",
                        "Target heart rate: 70-80% max HR",
                        "Target calories: 400+ kcal"), 5));
        return new WorkoutRecommendation("Yoga", tiers, recommended);
    }

    private static List<String> goals(String... items) {
        List<String> list = new ArrayList<>();
        for (String item : items) list.add(item);
        return list;
    }
}