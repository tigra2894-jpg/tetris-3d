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
    private final float[] vitezaRot = new float[MAX];
    private final float[] axaRotX = new float[MAX];
    private final float[] axaRotY = new float[MAX];
    private final float[] axaRotZ = new float[MAX];
    private final float[] gravitate = new float[MAX];
    private final float[] frecare = new float[MAX];

    private final Random rnd = new Random();
    private int urmator = 0;

    public void explozie(int rand, int coloane) {
        for (int c = 0; c < coloane; c++) {
            nasteBucata(c, rand, 0.98f, 0.55f, 0.20f);
            nasteBucata(c, rand, 0.98f, 0.55f, 0.20f);
            nastePraf(c, rand, 1.0f, 0.65f, 0.20f);
        }
    }

    public void explozieColorata(int rand, int coloane, int[] valRand, float[][] culori) {
        for (int c = 0; c < coloane; c++) {
            int val = valRand[c];
            float r = 1f, g = 1f, b = 1f;
            if (val > 0 && culori != null && val - 1 < culori.length) {
                float[] cul = culori[val - 1];
                r = cul[0]; g = cul[1]; b = cul[2];
            }
            nasteBucataColorata(c, rand, r, g, b);
            nasteBucataColorata(c, rand, r, g, b);
            nastePrafColorat(c, rand, r, g, b);
        }
    }

    /** bucata mare, cade lent si se rostogoleste incet — ca o cioburi de zid */
    private void nasteBucata(float cx, float cy, float culR, float culG, float culB) {
        int i = urmator;
        urmator = (urmator + 1) % MAX;

        x[i] = cx + (rnd.nextFloat() - 0.5f) * 0.7f;
        y[i] = cy + (rnd.nextFloat() - 0.5f) * 0.7f;
        z[i] = (rnd.nextFloat() - 0.5f) * 0.5f;

        vx[i] = (rnd.nextFloat() - 0.5f) * 4.0f;
        vy[i] = rnd.nextFloat() * 3.2f + 1.0f;
        vz[i] = (rnd.nextFloat() - 0.5f) * 4.0f;

        gravitate[i] = 6.5f;
        frecare[i] = 0.985f;

        viataMax[i] = 2.2f + rnd.nextFloat() * 1.1f;
        viata[i] = viataMax[i];

        float t = rnd.nextFloat();
        cr[i] = Math.min(1f, culR + t * 0.15f);
        cg[i] = Math.min(1f, culG + t * 0.10f);
        cb[i] = culB;

        marime[i] = 0.24f + rnd.nextFloat() * 0.20f;
        vitezaRot[i] = 40f + rnd.nextFloat() * 90f;
        axaRotX[i] = rnd.nextFloat();
        axaRotY[i] = rnd.nextFloat();
        axaRotZ[i] = rnd.nextFloat();
    }

    /** particule mici de praf, mai rapide, se sting mai devreme */
    private void nastePraf(float cx, float cy, float culR, float culG, float culB) {
        int i = urmator;
        urmator = (urmator + 1) % MAX;

        x[i] = cx + (rnd.nextFloat() - 0.5f) * 0.9f;
        y[i] = cy + (rnd.nextFloat() - 0.5f) * 0.9f;
        z[i] = (rnd.nextFloat() - 0.5f) * 0.6f;

        vx[i] = (rnd.nextFloat() - 0.5f) * 6.5f;
        vy[i] = rnd.nextFloat() * 4.5f + 1.5f;
        vz[i] = (rnd.nextFloat() - 0.5f) * 6.5f;

        gravitate[i] = 8.5f;
        frecare[i] = 0.975f;

        viataMax[i] = 0.9f + rnd.nextFloat() * 0.7f;
        viata[i] = viataMax[i];

        cr[i] = culR; cg[i] = culG; cb[i] = culB;

        marime[i] = 0.06f + rnd.nextFloat() * 0.07f;
        vitezaRot[i] = 200f + rnd.nextFloat() * 220f;
        axaRotX[i] = rnd.nextFloat();
        axaRotY[i] = rnd.nextFloat();
        axaRotZ[i] = rnd.nextFloat();
    }

    private void nasteBucataColorata(float cx, float cy, float r, float g, float b) {
        nasteBucata(cx, cy, r, g, b);
    }

    private void nastePrafColorat(float cx, float cy, float r, float g, float b) {
        nastePraf(cx, cy, Math.min(1f, r + 0.2f), Math.min(1f, g + 0.2f), Math.min(1f, b + 0.2f));
    }

    public void actualizeaza(float dt) {
        for (int i = 0; i < MAX; i++) {
            if (viata[i] <= 0f) continue;

            viata[i] -= dt;

            x[i] += vx[i] * dt;
            y[i] += vy[i] * dt;
            z[i] += vz[i] * dt;

            vy[i] -= gravitate[i] * dt;

            vx[i] *= frecare[i];
            vz[i] *= frecare[i];
        }
    }

    public void deseneaza(Cub cub, float[] vizProiectie, float[] model,
                          float[] mvp, float offX, float offY) {
        for (int i = 0; i < MAX; i++) {
            if (viata[i] <= 0f) continue;

            float p = viata[i] / viataMax[i];
            float s = marime[i] * (0.5f + p * 0.5f);
            float alfa = Math.min(1f, p * 1.6f);

            Matrix.setIdentityM(model, 0);
            Matrix.translateM(model, 0, offX + x[i], offY + y[i], z[i]);
            Matrix.rotateM(model, 0, (1f - p) * vitezaRot[i],
                    axaRotX[i], axaRotY[i], axaRotZ[i]);
            Matrix.scaleM(model, 0, s, s, s);
            Matrix.multiplyMM(mvp, 0, vizProiectie, 0, model, 0);

            cub.deseneaza(mvp, model, cr[i], cg[i], cb[i], alfa);
        }
    }

    public void deseneaza2(Desenator d, float offX, float offY) {
        for (int i = 0; i < MAX; i++) {
            if (viata[i] <= 0f) continue;

            float p = viata[i] / viataMax[i];
            float s = marime[i] * (0.5f + p * 0.5f);
            float alfa = Math.min(1f, p * 1.6f);

            d.cubRotit(offX + x[i], offY + y[i], z[i],
                    (1f - p) * vitezaRot[i], axaRotX[i], axaRotY[i], axaRotZ[i],
                    cr[i], cg[i], cb[i], alfa, s);
        }
    }
        }
