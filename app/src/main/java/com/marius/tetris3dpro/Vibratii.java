package com.marius.tetris3dpro;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class Vibratii {

    private final Vibrator v;
    private boolean pornit = true;

    public Vibratii(Context ctx) {
        v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void setPornit(boolean p) { pornit = p; }

    public void scurt()  { vibreaza(12, 90); }
    public void mediu()  { vibreaza(25, 160); }
    public void lung()   { vibreaza(60, 255); }

    private void vibreaza(long ms, int amplitudine) {
        if (!pornit || v == null || !v.hasVibrator()) return;
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(ms, amplitudine));
            } else {
                v.vibrate(ms);
            }
        } catch (Exception ignored) { }
    }
}
