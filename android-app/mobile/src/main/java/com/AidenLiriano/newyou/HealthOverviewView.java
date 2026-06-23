package com.AidenLiriano.newyou;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class HealthOverviewView extends View {

    private Paint trackPaint;
    private Paint ringPaint;
    private Paint textPaint;
    private Paint labelPaint;
    private Paint scorePaint;
    private Paint gradePaint;

    private FitnessScoreEngine.FitnessScore fitnessScore;

    // Ring colors matching app theme
    private static final int[] RING_COLORS = {
            Color.parseColor("#EF5350"), // Cardio — red
            Color.parseColor("#98CD00"), // Activity — green
            Color.parseColor("#42A5F5"), // Consistency — blue
            Color.parseColor("#FFA726"), // BMI — orange
            Color.parseColor("#AB47BC"), // Effort — purple
    };

    private static final String[] RING_LABELS = {
            "Cardio", "Activity", "Consistency", "BMI", "Effort"
    };

    public HealthOverviewView(Context context) {
        super(context);
        init();
    }

    public HealthOverviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#1C1C1E"));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#555555"));
        labelPaint.setTextAlign(Paint.Align.LEFT);
        labelPaint.setTextSize(28f);

        scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scorePaint.setColor(Color.parseColor("#1C1C1E"));
        scorePaint.setTextAlign(Paint.Align.CENTER);
        scorePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        gradePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gradePaint.setTextAlign(Paint.Align.CENTER);
        gradePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    }

    public void setFitnessScore(FitnessScoreEngine.FitnessScore score) {
        this.fitnessScore = score;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (fitnessScore == null) return;

        int width  = getWidth();
        int height = getHeight();
        float cx   = width / 2f;
        float cy   = height / 2f;

        // 5 concentric rings
        // Outermost = cardio, innermost = effort
        float strokeWidth = width * 0.055f;
        float gap         = strokeWidth * 1.3f;
        float outerRadius = (Math.min(width, height) / 2f) - strokeWidth;

        int[] scores = {
                fitnessScore.cardioScore,
                fitnessScore.activityScore,
                fitnessScore.consistencyScore,
                fitnessScore.weightScore,
                fitnessScore.recoveryScore
        };

        trackPaint.setStrokeWidth(strokeWidth);
        ringPaint.setStrokeWidth(strokeWidth);

        for (int i = 0; i < 5; i++) {
            float radius = outerRadius - (i * gap);
            RectF oval = new RectF(
                    cx - radius, cy - radius,
                    cx + radius, cy + radius
            );

            // Track (background arc)
            trackPaint.setColor(adjustAlpha(RING_COLORS[i], 40));
            canvas.drawArc(oval, -90, 360, false, trackPaint);

            // Progress arc
            float sweep = (scores[i] / 100f) * 360f;
            ringPaint.setColor(RING_COLORS[i]);
            canvas.drawArc(oval, -90, sweep, false, ringPaint);
        }

        // Center text — overall score
        float innerRadius = outerRadius - (4 * gap) - strokeWidth;
        scorePaint.setTextSize(innerRadius * 0.55f);
        canvas.drawText(
                String.valueOf(fitnessScore.overallScore),
                cx, cy + (innerRadius * 0.18f), scorePaint);

        // Grade below score
        gradePaint.setTextSize(innerRadius * 0.28f);
        gradePaint.setColor(gradeColor(fitnessScore.overallScore));
        canvas.drawText(
                fitnessScore.overallGrade,
                cx, cy + (innerRadius * 0.55f), gradePaint);

        // Small label above score
        textPaint.setTextSize(innerRadius * 0.20f);
        textPaint.setColor(Color.parseColor("#888888"));
        canvas.drawText("SCORE", cx, cy - (innerRadius * 0.28f), textPaint);
    }

    private int adjustAlpha(int color, int alpha) {
        return Color.argb(alpha,
                Color.red(color), Color.green(color), Color.blue(color));
    }

    private int gradeColor(int score) {
        if (score >= 80) return Color.parseColor("#4CAF50");
        if (score >= 60) return Color.parseColor("#98CD00");
        if (score >= 40) return Color.parseColor("#FFA726");
        return Color.parseColor("#EF5350");
    }
}