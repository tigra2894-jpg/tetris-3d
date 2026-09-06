package com.marius.tetris3d;

import android.opengl.Matrix;

import java.util.Random;

public class Particule {

    private static final int MAX = 400;

    private final float[] x = new float[MAX];
    private final float[] y = new float[MAX];
    private final float[] z = new float[MAX];
    private final float[] vx = new float[MAX];
    private final float[] vy = new float[MAX];
    private final float[] vz = new float[MAX];
    private final float[] viata = new float[MAX];
    private final float[] viataMax = new float[MAX];
    private final float[] cr = new float[MAX];
    private final float[] cg = new float[MAX];
    private final float[] cb = new float[MAX];
    private final float[] marime = new float[MAX];

    private final Random rnd = new Random();
    private int urmator = 0;

    public void explozie(int rand, int coloane) {
        for (int c = 0; c < coloane; c++) {
            for (int k = 0; k < 4; k++) {
                naste(c, rand);
            }
        }
    }

    private void naste(float cx, float cy) {
        int i = urmator;
        urmator = (urmator + 1) % MAX;

        x[i] = cx + (rnd.nextFloat() - 0.5f) * 0.8f;
        y[i] = cy + (rnd.nextFloat() - 0.5f) * 0.8f;
        z[i] = (rnd.nextFloat() - 0.5f) * 0.6f;

        vx[i] = (rnd.nextFloat() - 0.5f) * 7f;
        vy[i] = rnd.nextFloat() * 6f + 1.5f;
        vz[i] = (rnd.nextFloat() - 0.5f) * 5f;

        viataMax[i] = 0.7f + rnd.nextFloat() * 0.7f;
        viata[i] = viataMax[i];

        float t = rnd.nextFloat();
        cr[i] = 1.0f;
        cg[i] = 0.55f + t * 0.45f;
        cb[i] = 0.15f + t * 0.55f;

        marime[i] = 0.14f + rnd.nextFloat() * 0.16f;
    }

    public void actualizeaza(float dt) {
        for (int i = 0; i < MAX; i++) {
            if (viata[i] <= 0f) continue;

            viata[i] -= dt;

            x[i] += vx[i] * dt;
            y[i] += vy[i] * dt;
            z[i] += vz[i] * dt;

            vy[i] -= 13f * dt;

            vx[i] *= 0.985f;
            vz[i] *= 0.985f;
        }
    }

    public void deseneaza(Cub cub, float[] vizProiectie, float[] model,
                          float[] mvp, float offX, float offY) {
        for (int i = 0; i < MAX; i++) {
            if (viata[i] <= 0f) continue;

            float p = viata[i] / viataMax[i];
            float s = marime[i] * (0.35f + p * 0.65f);

            Matrix.setIdentityM(model, 0);
            Matrix.translateM(model, 0, offX + x[i], offY + y[i], z[i]);
            Matrix.rotateM(model, 0, (1f - p) * 320f, 0.6f, 1f, 0.4f);
            Matrix.scaleM(model, 0, s, s, s);
            Matrix.multiplyMM(mvp, 0, vizProiectie, 0, model, 0);

            cub.deseneaza(mvp, model, cr[i], cg[i], cb[i], p);
        }
    }
}
