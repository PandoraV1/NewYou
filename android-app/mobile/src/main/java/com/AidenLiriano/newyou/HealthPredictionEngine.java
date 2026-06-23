package com.AidenLiriano.newyou;

/**
 * Calculates health predictions based on current activity trends.
 * Two scenarios are computed for each metric:
 *   - Current trend: extrapolated from what the user has been doing
 *   - Recommended trend: projected improvement if they follow recommendations
 */
public class HealthPredictionEngine {

    // Prediction result container
    public static class PredictionSet {
        // Current values
        public float currentBMI;
        public float currentWeightLbs;
        public float currentAvgHeartRate;
        public float currentWeeklyActiveMinutes;
        public float currentCaloriesPerSession;
        public float currentAvgSessionDuration;
        public int   currentWeeklySessionCount;

        // Predicted values — current trend
        public float bmi1MonthTrend;
        public float bmi6MonthTrend;
        public float bmi1YearTrend;

        public float weight1MonthTrend;
        public float weight6MonthTrend;
        public float weight1YearTrend;

        public float hr1MonthTrend;
        public float hr6MonthTrend;
        public float hr1YearTrend;

        public float activeMin1MonthTrend;
        public float activeMin6MonthTrend;
        public float activeMin1YearTrend;

        public float calories1MonthTrend;
        public float calories6MonthTrend;
        public float calories1YearTrend;

        // Predicted values, recommended plan
        public float bmi1MonthRec;
        public float bmi6MonthRec;
        public float bmi1YearRec;

        public float weight1MonthRec;
        public float weight6MonthRec;
        public float weight1YearRec;

        public float hr1MonthRec;
        public float hr6MonthRec;
        public float hr1YearRec;

        public float activeMin1MonthRec;
        public float activeMin6MonthRec;
        public float activeMin1YearRec;

        public float calories1MonthRec;
        public float calories6MonthRec;
        public float calories1YearRec;

        // Series data for line charts (7 points: now + 6 monthly)
        public float[] weightTrendSeries    = new float[7];
        public float[] weightRecSeries      = new float[7];
        public float[] hrTrendSeries        = new float[7];
        public float[] hrRecSeries          = new float[7];
        public float[] activeMinTrendSeries = new float[7];
        public float[] activeMinRecSeries   = new float[7];
        public float[] caloriesTrendSeries  = new float[7];
        public float[] caloriesRecSeries    = new float[7];
    }

    /**
     * Generates all predictions.
     *
     * @param user                  the user's profile
     * @param avgHeartRate          average HR across all sessions
     * @param weeklyActiveMinutes   average active minutes per week
     * @param avgCaloriesPerSession average calories per session
     * @param avgSessionDuration    average session duration in seconds
     * @param weeklySessionCount    average sessions per week
     * @param totalSessions         total sessions ever logged
     */
    public static PredictionSet generate(
            User user,
            float avgHeartRate,
            float weeklyActiveMinutes,
            float avgCaloriesPerSession,
            float avgSessionDuration,
            float weeklySessionCount,
            int totalSessions) {

        PredictionSet p = new PredictionSet();

        float weightLbs = user != null && user.weightLbs > 0 ? user.weightLbs : 154f;
        int age         = user != null && user.age > 0 ? user.age : 30;
        int heightIn    = user != null && user.heightInches > 0 ? user.heightInches : 67;

        // Current BMI
        float bmi = (weightLbs / ((float) heightIn * heightIn)) * 703f;
        p.currentBMI                 = bmi;
        p.currentWeightLbs           = weightLbs;
        p.currentAvgHeartRate        = avgHeartRate > 0 ? avgHeartRate : 75f;
        p.currentWeeklyActiveMinutes = weeklyActiveMinutes > 0 ? weeklyActiveMinutes : 0f;
        p.currentCaloriesPerSession  = avgCaloriesPerSession > 0 ? avgCaloriesPerSession : 0f;
        p.currentAvgSessionDuration  = avgSessionDuration > 0 ? avgSessionDuration : 0f;
        p.currentWeeklySessionCount  = weeklySessionCount > 0 ? (int) weeklySessionCount : 0;


        // TREND SCENARIO — based on current activity level

        // Weight change rate based on weekly active minutes
        // WHO recommends 150 min/week for weight maintenance
        // Each 30 min/week above maintenance ≈ 0.05 lbs/week loss
        float weeklyWeightChangeTrend = 0f;
        if (weeklyActiveMinutes >= 150) {
            float excessMinutes = weeklyActiveMinutes - 150f;
            weeklyWeightChangeTrend = -(excessMinutes / 30f) * 0.05f;
        } else if (weeklyActiveMinutes > 0) {
            // Below recommended — slight gain tendency
            weeklyWeightChangeTrend = 0.05f;
        } else {
            // No activity — moderate gain tendency
            weeklyWeightChangeTrend = 0.15f;
        }

        float w1mTrend  = weightLbs + (weeklyWeightChangeTrend * 4.3f);
        float w6mTrend  = weightLbs + (weeklyWeightChangeTrend * 26f);
        float w1yTrend  = weightLbs + (weeklyWeightChangeTrend * 52f);

        p.weight1MonthTrend = Math.max(80f, w1mTrend);
        p.weight6MonthTrend = Math.max(80f, w6mTrend);
        p.weight1YearTrend  = Math.max(80f, w1yTrend);
        p.bmi1MonthTrend    = bmiFrom(p.weight1MonthTrend, heightIn);
        p.bmi6MonthTrend    = bmiFrom(p.weight6MonthTrend, heightIn);
        p.bmi1YearTrend     = bmiFrom(p.weight1YearTrend, heightIn);

        // Heart rate improvement from consistent cardio
        // Each session/week above 2 lowers avg HR by ~0.3 bpm/month
        float currentHR = p.currentAvgHeartRate;
        float hrMonthlyChangeTrend = 0f;
        if (weeklySessionCount >= 3) {
            hrMonthlyChangeTrend = -0.6f;
        } else if (weeklySessionCount >= 2) {
            hrMonthlyChangeTrend = -0.3f;
        } else if (weeklySessionCount >= 1) {
            hrMonthlyChangeTrend = -0.1f;
        } else {
            hrMonthlyChangeTrend = 0.2f; // deconditioning
        }

        p.hr1MonthTrend  = Math.max(45f, currentHR + hrMonthlyChangeTrend);
        p.hr6MonthTrend  = Math.max(45f, currentHR + (hrMonthlyChangeTrend * 6));
        p.hr1YearTrend   = Math.max(45f, currentHR + (hrMonthlyChangeTrend * 12));

        // Active minutes trend, slightly improving if already active
        float activeMinMonthlyChangeTrend = weeklyActiveMinutes > 0 ? 5f : 0f;
        p.activeMin1MonthTrend = weeklyActiveMinutes + activeMinMonthlyChangeTrend;
        p.activeMin6MonthTrend = weeklyActiveMinutes + (activeMinMonthlyChangeTrend * 6);
        p.activeMin1YearTrend  = weeklyActiveMinutes + (activeMinMonthlyChangeTrend * 12);

        // Calories per session trend, slight improvement from fitness gains
        float calMonthlyChangeTrend = avgCaloriesPerSession > 0 ? 5f : 0f;
        p.calories1MonthTrend = avgCaloriesPerSession + calMonthlyChangeTrend;
        p.calories6MonthTrend = avgCaloriesPerSession + (calMonthlyChangeTrend * 6);
        p.calories1YearTrend  = avgCaloriesPerSession + (calMonthlyChangeTrend * 12);

        // RECOMMENDED SCENARIO, following recommended workout plan
        // Assumes 4 sessions/week, 45 min each, moderate intensity

        float recWeeklySessions      = 4f;
        float recWeeklyActiveMinutes = 180f;

        // Weight, following recommendations burns more, loses more
        float weeklyWeightChangeRec = -0.25f; // ~1 lb/month loss
        if (bmi < 18.5f) weeklyWeightChangeRec = 0f; // underweight — maintain
        if (bmi < 25f)   weeklyWeightChangeRec = -0.1f; // healthy — slight improvement

        float w1mRec = weightLbs + (weeklyWeightChangeRec * 4.3f);
        float w6mRec = weightLbs + (weeklyWeightChangeRec * 26f);
        float w1yRec = weightLbs + (weeklyWeightChangeRec * 52f);

        p.weight1MonthRec = Math.max(80f, w1mRec);
        p.weight6MonthRec = Math.max(80f, w6mRec);
        p.weight1YearRec  = Math.max(80f, w1yRec);
        p.bmi1MonthRec    = bmiFrom(p.weight1MonthRec, heightIn);
        p.bmi6MonthRec    = bmiFrom(p.weight6MonthRec, heightIn);
        p.bmi1YearRec     = bmiFrom(p.weight1YearRec, heightIn);

        // Heart rate, following plan gives stronger improvement
        float hrMonthlyChangeRec = -1.2f;
        p.hr1MonthRec = Math.max(45f, currentHR + hrMonthlyChangeRec);
        p.hr6MonthRec = Math.max(45f, currentHR + (hrMonthlyChangeRec * 6));
        p.hr1YearRec  = Math.max(45f, currentHR + (hrMonthlyChangeRec * 12));

        // Active minutes, following plan brings up to recommended level
        p.activeMin1MonthRec = Math.max(weeklyActiveMinutes, recWeeklyActiveMinutes * 0.7f);
        p.activeMin6MonthRec = Math.max(weeklyActiveMinutes, recWeeklyActiveMinutes);
        p.activeMin1YearRec  = Math.max(weeklyActiveMinutes, recWeeklyActiveMinutes * 1.1f);

        // Calories per session, following plan improves output
        float baseCalRec = Math.max(avgCaloriesPerSession, 250f);
        p.calories1MonthRec = baseCalRec + 20f;
        p.calories6MonthRec = baseCalRec + 60f;
        p.calories1YearRec  = baseCalRec + 100f;

        // CHART SERIES, monthly points from now to 12 months
        // Index 0 = now, 1 = 1 month, 2 = 2 months ... 6 = 6 months

        for (int i = 0; i <= 6; i++) {
            float monthFactor = i;

            p.weightTrendSeries[i] = Math.max(80f,
                    weightLbs + (weeklyWeightChangeTrend * 4.3f * monthFactor));
            p.weightRecSeries[i] = Math.max(80f,
                    weightLbs + (weeklyWeightChangeRec * 4.3f * monthFactor));

            p.hrTrendSeries[i] = Math.max(45f,
                    currentHR + (hrMonthlyChangeTrend * monthFactor));
            p.hrRecSeries[i] = Math.max(45f,
                    currentHR + (hrMonthlyChangeRec * monthFactor));

            p.activeMinTrendSeries[i] = weeklyActiveMinutes
                    + (activeMinMonthlyChangeTrend * monthFactor);
            p.activeMinRecSeries[i] = monthFactor == 0
                    ? weeklyActiveMinutes
                    : Math.max(weeklyActiveMinutes,
                    recWeeklyActiveMinutes * Math.min(1f, 0.5f + (monthFactor * 0.1f)));

            p.caloriesTrendSeries[i] = avgCaloriesPerSession
                    + (calMonthlyChangeTrend * monthFactor);
            p.caloriesRecSeries[i] = monthFactor == 0
                    ? avgCaloriesPerSession
                    : Math.max(avgCaloriesPerSession, baseCalRec + (15f * monthFactor));
        }

        return p;
    }

    private static float bmiFrom(float weightLbs, int heightInches) {
        if (heightInches <= 0) return 0f;
        return (weightLbs / ((float) heightInches * heightInches)) * 703f;
    }

    public static String bmiCategory(float bmi) {
        if (bmi < 18.5f) return "Underweight";
        if (bmi < 25f)   return "Healthy Weight";
        if (bmi < 30f)   return "Overweight";
        if (bmi < 35f)   return "Obese Class I";
        return "Obese Class II+";
    }

    public static String heartRateCategory(float hr) {
        if (hr < 60f)  return "Athlete";
        if (hr < 70f)  return "Excellent";
        if (hr < 80f)  return "Good";
        if (hr < 90f)  return "Average";
        if (hr < 100f) return "Below Average";
        return "Poor";
    }
}