package com.AidenLiriano.newyou;

/**
 * Calculates a fitness score and category scores for the health overview rings.
 * Each category scores 0-100 and the overall score is a weighted average.
 */
public class FitnessScoreEngine {

    public static class FitnessScore {
        // Overall score 0-100
        public int overallScore;
        public String overallGrade; // A, B, C, D, F

        // Category scores 0-100
        public int cardioScore;
        public int activityScore;
        public int consistencyScore;
        public int weightScore;
        public int recoveryScore;

        // Category labels
        public String cardioLabel;
        public String activityLabel;
        public String consistencyLabel;
        public String weightLabel;
        public String recoveryLabel;

        // Diagnostic messages
        public String overallMessage;
        public String cardioMessage;
        public String activityMessage;
        public String consistencyMessage;
        public String weightMessage;
        public String recoveryMessage;
    }

    public static FitnessScore calculate(
            User user,
            float avgHeartRate,
            float weeklyActiveMinutes,
            float weeklySessionCount,
            int totalSessions,
            float avgCaloriesPerSession) {

        FitnessScore score = new FitnessScore();

        int age        = user != null && user.age > 0 ? user.age : 30;
        float weightLbs = user != null && user.weightLbs > 0 ? user.weightLbs : 154f;
        int heightIn   = user != null && user.heightInches > 0 ? user.heightInches : 67;

        // --- Cardio score (based on avg heart rate) ---
        int cardio = 50;
        if (avgHeartRate <= 0) {
            cardio = 40;
            score.cardioMessage = "No heart rate data yet. Start a workout to track.";
        } else if (avgHeartRate < 55) {
            cardio = 95;
            score.cardioMessage = "Excellent cardiovascular fitness.";
        } else if (avgHeartRate < 62) {
            cardio = 82;
            score.cardioMessage = "Very good cardiovascular health.";
        } else if (avgHeartRate < 70) {
            cardio = 70;
            score.cardioMessage = "Good heart health. Keep it up!";
        } else if (avgHeartRate < 78) {
            cardio = 58;
            score.cardioMessage = "Average. More cardio will lower your resting HR.";
        } else if (avgHeartRate < 86) {
            cardio = 44;
            score.cardioMessage = "Below average. Try 3+ cardio sessions per week.";
        } else {
            cardio = 28;
            score.cardioMessage = "High heart rate. Consult a doctor if concerned.";
        }
        score.cardioScore = cardio;
        score.cardioLabel = scoreToGrade(cardio) + "  Cardio";

        // --- Activity score (based on weekly active minutes vs WHO 150 min target) ---
        int activity = 0;
        if (weeklyActiveMinutes <= 0) {
            activity = 10;
            score.activityMessage = "No activity logged yet. Start moving!";
        } else if (weeklyActiveMinutes >= 300) {
            activity = 100;
            score.activityMessage = "Exceeding WHO guidelines. Outstanding!";
        } else if (weeklyActiveMinutes >= 150) {
            activity = 80;
            score.activityMessage = "Meeting WHO 150 min/week guideline. Great work!";
        } else if (weeklyActiveMinutes >= 90) {
            activity = 60;
            score.activityMessage = "Getting close to 150 min/week goal. Keep pushing!";
        } else if (weeklyActiveMinutes >= 45) {
            activity = 40;
            score.activityMessage = "Below target. Aim for 150 active minutes per week.";
        } else {
            activity = 20;
            score.activityMessage = "Very low activity. Try adding short walks daily.";
        }
        score.activityScore = activity;
        score.activityLabel = scoreToGrade(activity) + "  Activity";

        // --- Consistency score (based on sessions per week) ---
        int consistency = 0;
        if (weeklySessionCount <= 0) {
            consistency = 5;
            score.consistencyMessage = "No sessions logged yet.";
        } else if (weeklySessionCount >= 5) {
            consistency = 100;
            score.consistencyMessage = "Extremely consistent. Elite level dedication!";
        } else if (weeklySessionCount >= 4) {
            consistency = 85;
            score.consistencyMessage = "Very consistent. Excellent routine!";
        } else if (weeklySessionCount >= 3) {
            consistency = 70;
            score.consistencyMessage = "Good consistency. Try adding one more session.";
        } else if (weeklySessionCount >= 2) {
            consistency = 50;
            score.consistencyMessage = "Moderate consistency. 3-4 sessions/week is ideal.";
        } else {
            consistency = 25;
            score.consistencyMessage = "Low consistency. Try to workout more regularly.";
        }
        score.consistencyScore = consistency;
        score.consistencyLabel = scoreToGrade(consistency) + "  Consistency";

        // --- Weight/BMI score ---
        float bmi = (weightLbs / ((float) heightIn * heightIn)) * 703f;
        int weightScore = 0;
        if (bmi < 18.5f) {
            weightScore = 55;
            score.weightMessage = "Underweight. Consider increasing calorie intake.";
        } else if (bmi < 22f) {
            weightScore = 100;
            score.weightMessage = "Ideal weight range. Excellent!";
        } else if (bmi < 25f) {
            weightScore = 85;
            score.weightMessage = "Healthy weight range. Well done!";
        } else if (bmi < 28f) {
            weightScore = 65;
            score.weightMessage = "Slightly above ideal. More activity can help.";
        } else if (bmi < 30f) {
            weightScore = 50;
            score.weightMessage = "Overweight range. Regular exercise is key.";
        } else if (bmi < 35f) {
            weightScore = 30;
            score.weightMessage = "Obese Class I. Consistent activity will help.";
        } else {
            weightScore = 15;
            score.weightMessage = "High BMI. Consider speaking with a healthcare provider.";
        }
        score.weightScore = weightScore;
        score.weightLabel = scoreToGrade(weightScore) + "  BMI";

        // --- Recovery score (based on calories burned, proxy for effort/recovery balance) ---
        int recovery = 50;
        if (avgCaloriesPerSession <= 0) {
            recovery = 40;
            score.recoveryMessage = "No calorie data yet.";
        } else if (avgCaloriesPerSession >= 400) {
            recovery = 90;
            score.recoveryMessage = "High energy output. Great intensity level!";
        } else if (avgCaloriesPerSession >= 280) {
            recovery = 75;
            score.recoveryMessage = "Good energy expenditure per session.";
        } else if (avgCaloriesPerSession >= 180) {
            recovery = 60;
            score.recoveryMessage = "Moderate effort. Try increasing intensity.";
        } else {
            recovery = 40;
            score.recoveryMessage = "Low calorie burn. Push a little harder each session.";
        }
        score.recoveryScore = recovery;
        score.recoveryLabel = scoreToGrade(recovery) + "  Effort";

        // --- Overall score (weighted average) ---
        score.overallScore = (int) (
                cardio * 0.30f +
                        activity * 0.25f +
                        consistency * 0.20f +
                        weightScore * 0.15f +
                        recovery * 0.10f
        );

        score.overallGrade = scoreToGrade(score.overallScore);

        if (score.overallScore >= 85) {
            score.overallMessage = "Outstanding health! You are in excellent shape.";
        } else if (score.overallScore >= 70) {
            score.overallMessage = "Great health! Keep up the good work.";
        } else if (score.overallScore >= 55) {
            score.overallMessage = "Good foundation. A few improvements will make a big difference.";
        } else if (score.overallScore >= 40) {
            score.overallMessage = "Room to improve. Consistent effort will get you there.";
        } else {
            score.overallMessage = "Just getting started. Every workout counts!";
        }

        return score;
    }

    private static String scoreToGrade(int score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B+";
        if (score >= 60) return "B";
        if (score >= 50) return "C+";
        if (score >= 40) return "C";
        if (score >= 30) return "D";
        return "F";
    }
}