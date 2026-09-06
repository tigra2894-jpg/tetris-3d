package com.marius.tetris3d;

import android.opengl.Matrix;

import java.util.Random;

/**
 * Fundalul: cubulete mici care licaresc, se rotesc
 * si deriva lent in sus. Se folosesc pe toate ecranele.
 */
public class Stele {

    private static final int NR = 150;

    private final float[] x = new float[NR];
    private final float[] y = new float[NR];
    private final float[] z = new float[NR];
    private final float[] marime = new float[NR];
    private final float[] licarire = new float[NR];
    private final float[] vitezaLicar = new float[NR];
    private final float[] vitezaRot = new float[NR];
    private final float[] cr = new float[NR];
    private final float[] cg = new float[NR];
    private final float[] cb = new float[NR];

    private float timp = 0f;

    private final Random rnd = new Random(7);

    public Stele() {
        for (int i = 0; i < NR; i++) {
            x[i] = (rnd.nextFloat() - 0.5f) * 50f;
            y[i] = (rnd.nextFloat() - 0.5f) * 64f;
            z[i] = -14f - rnd.nextFloat() * 24f;

            marime[i] = 0.06f + rnd.nextFloat() * 0.17f;
            licarire[i] = rnd.nextFloat() * 6.28f;
            vitezaLicar[i] = 0.5f + rnd.nextFloat() * 1.9f;
            vitezaRot[i] = 8f + rnd.nextFloat() * 22f;

            float t = rnd.nextFloat();
            if (t < 0.55f) {
                cr[i] = 0.75f; cg[i] = 0.82f; cb[i] = 1.00f;
            } else if (t < 0.85f) {
                cr[i] = 1.00f; cg[i] = 0.95f; cb[i] = 0.85f;
            } else {
                cr[i] = 0.85f; cg[i] = 0.60f; cb[i] = 1.00f;
            }
        }
    }

    public void actualizeaza(float dt) {
        timp += dt;

        for (int i = 0; i < NR; i++) {
            licarire[i] += dt * vitezaLicar[i];
            y[i] += dt * 0.38f;
            if (y[i] > 34f) {
                y[i] = -34f;
                x[i] = (rnd.nextFloat() - 0.5f) * 50f;
            }
        }
    }

    /** desenare prin Desenator, folosita de ecrane */
    public void deseneaza2(Desenator d) {
        for (int i = 0; i < NR; i++) {
            float p = 0.30f + 0.45f * (float) Math.abs(Math.sin(licarire[i]));

            d.cubRotit(x[i], y[i], z[i],
                    timp * vitezaRot[i], 0.4f, 1f, 0.3f,
                    cr[i], cg[i], cb[i],
                    p, marime[i]);
        }
    }

    /** varianta veche, desenare directa cu matrici */
    public void deseneaza(Cub cub, float[] vizProiectie,
                          float[] model, float[] mvp) {
        for (int i = 0; i < NR; i++) {
            float p = 0.30f + 0.45f * (float) Math.abs(Math.sin(licarire[i]));

            Matrix.setIdentityM(model, 0);
            Matrix.translateM(model, 0, x[i], y[i], z[i]);
            Matrix.rotateM(model, 0, timp * vitezaRot[i], 0.4f, 1f, 0.3f);
            Matrix.scaleM(model, 0, marime[i], marime[i], marime[i]);
            Matrix.multiplyMM(mvp, 0, vizProiectie, 0, model, 0);

            cub.deseneaza(mvp, model, cr[i], cg[i], cb[i], p);
        }
    }
}
