package com.marius.tetris3d;

import android.opengl.Matrix;

import java.util.Random;

public class Stele {

    private static final int NR = 140;

    private final float[] x = new float[NR];
    private final float[] y = new float[NR];
    private final float[] z = new float[NR];
    private final float[] marime = new float[NR];
    private final float[] licarire = new float[NR];
    private final float[] vitezaLicar = new float[NR];
    private final float[] cr = new float[NR];
    private final float[] cg = new float[NR];
    private final float[] cb = new float[NR];

    private final Random rnd = new Random(7);

    public Stele() {
        for (int i = 0; i < NR; i++) {
            x[i] = (rnd.nextFloat() - 0.5f) * 46f;
            y[i] = (rnd.nextFloat() - 0.5f) * 60f;
            z[i] = -14f - rnd.nextFloat() * 22f;

            marime[i] = 0.06f + rnd.nextFloat() * 0.16f;
            licarire[i] = rnd.nextFloat() * 6.28f;
            vitezaLicar[i] = 0.5f + rnd.nextFloat() * 1.8f;

            float t = rnd.nextFloat();
            if (t < 0.55f) {
                cr[i] = 0.75f; cg[i] = 0.82f; cb[i] = 1.00f;   // albastru rece
            } else if (t < 0.85f) {
                cr[i] = 1.00f; cg[i] = 0.95f; cb[i] = 0.85f;   // alb cald
            } else {
                cr[i] = 0.85f; cg[i] = 0.60f; cb[i] = 1.00f;   // mov
            }
        }
    }

    public void actualizeaza(float dt) {
        for (int i = 0; i < NR; i++) {
            licarire[i] += dt * vitezaLicar[i];

            // deriva foarte lenta in sus
            y[i] += dt * 0.35f;
            if (y[i] > 32f) y[i] = -32f;
        }
    }

    public void deseneaza(Cub cub, float[] vizProiectie,
                          float[] model, float[] mvp) {
        for (int i = 0; i < NR; i++) {
            float p = 0.35f + 0.45f * (float) Math.abs(Math.sin(licarire[i]));

            Matrix.setIdentityM(model, 0);
            Matrix.translateM(model, 0, x[i], y[i], z[i]);
            Matrix.rotateM(model, 0, licarire[i] * 18f, 0.4f, 1f, 0.3f);
            Matrix.scaleM(model, 0, marime[i], marime[i], marime[i]);
            Matrix.multiplyMM(mvp, 0, vizProiectie, 0, model, 0);

            cub.deseneaza(mvp, model, cr[i], cg[i], cb[i], p);
        }
    }
}
