package com.AidenLiriano.newyou;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConfettiView extends View {

    private static class Particle {
        float x, y;
        float vx, vy;      // velocity
        float rotation;
        float rotSpeed;
        float size;
        int color;
        float alpha;
        int shape; // 0=rect, 1=circle, 2=triangle
    }

    private final List<Particle> particles = new ArrayList<>();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean running = false;

    private static final int[] COLORS = {
            Color.parseColor("#98CD00"),
            Color.parseColor("#B6F500"),
            Color.parseColor("#EF5350"),
            Color.parseColor("#FFA726"),
            Color.parseColor("#42A5F5"),
            Color.parseColor("#AB47BC"),
            Color.parseColor("#FFEB3B"),
            Color.parseColor("#26C6DA"),
    };

    public ConfettiView(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public ConfettiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public void startConfetti() {
        particles.clear();
        int count = 120;
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x        = random.nextFloat() * getWidth();
            p.y        = -random.nextFloat() * getHeight() * 0.5f;
            p.vx       = (random.nextFloat() - 0.5f) * 8f;
            p.vy       = random.nextFloat() * 8f + 4f;
            p.rotation = random.nextFloat() * 360f;
            p.rotSpeed = (random.nextFloat() - 0.5f) * 10f;
            p.size     = random.nextFloat() * 18f + 8f;
            p.color    = COLORS[random.nextInt(COLORS.length)];
            p.alpha    = 255;
            p.shape    = random.nextInt(3);
            particles.add(p);
        }
        running = true;
        scheduleFrame();
    }

    public void stopConfetti() {
        running = false;
        particles.clear();
        handler.removeCallbacksAndMessages(null);
        invalidate();
    }

    private void scheduleFrame() {
        handler.postDelayed(() -> {
            if (!running) return;
            updateParticles();
            invalidate();
            if (hasVisibleParticles()) {
                scheduleFrame();
            } else {
                running = false;
                particles.clear();
                setVisibility(GONE);
            }
        }, 16); // ~60fps
    }

    private void updateParticles() {
        for (Particle p : particles) {
            p.x        += p.vx;
            p.y        += p.vy;
            p.vy       += 0.2f; // gravity
            p.rotation += p.rotSpeed;
            // Fade out as particles fall below screen
            if (p.y > getHeight() * 0.7f) {
                p.alpha = Math.max(0, p.alpha - 4f);
            }
        }
    }

    private boolean hasVisibleParticles() {
        for (Particle p : particles) {
            if (p.alpha > 0 && p.y < getHeight() + 50) return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Particle p : particles) {
            if (p.alpha <= 0) continue;
            paint.setColor(p.color);
            paint.setAlpha((int) p.alpha);

            canvas.save();
            canvas.translate(p.x, p.y);
            canvas.rotate(p.rotation);

            switch (p.shape) {
                case 0: // Rectangle
                    canvas.drawRect(-p.size / 2, -p.size / 4,
                            p.size / 2, p.size / 4, paint);
                    break;
                case 1: // Circle
                    canvas.drawCircle(0, 0, p.size / 3, paint);
                    break;
                case 2: // Square
                    canvas.drawRect(-p.size / 3, -p.size / 3,
                            p.size / 3, p.size / 3, paint);
                    break;
            }
            canvas.restore();
        }
    }
}