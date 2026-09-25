package com.marius.tetris3dpro;

import java.util.Random;

/** particule cubice pentru explozii de linii si aterizari */
public class Particule {

    private static final int MAX = 600;

    private final float[] x = new float[MAX], y = new float[MAX], z = new float[MAX];
    private final float[] vx = new float[MAX], vy = new float[MAX], vz = new float[MAX];
    private final float[] r = new float[MAX], g = new float[MAX], b = new float[MAX];
    private final float[] viata = new float[MAX], viataMax = new float[MAX];
    private final float[] marime = new float[MAX], unghi = new float[MAX], vUnghi = new float[MAX];
    private final float[] ax = new float[MAX], ay = new float[MAX], az = new float[MAX];
    private int nr = 0;
    private final Random rnd = new Random();

    public void goleste() { nr = 0; }

    public void emite(float px, float py, float pz, float cr, float cg, float cb,
                      int cantitate, float forta, float marimeBaza) {
        for (int i = 0; i < cantitate; i++) {
            if (nr >= MAX) return;
            int k = nr++;
            x[k] = px + (rnd.nextFloat() - 0.5f) * 0.6f;
            y[k] = py + (rnd.nextFloat() - 0.5f) * 0.6f;
            z[k] = pz + (rnd.nextFloat() - 0.5f) * 0.6f;
            float u = rnd.nextFloat() * 6.2832f;
            float w = (rnd.nextFloat() - 0.3f);
            float f = forta * (0.4f + rnd.nextFloat() * 0.9f);
            vx[k] = (float) Math.cos(u) * f;
            vy[k] = (float) Math.abs(Math.sin(u)) * f * 0.9f + forta * 0.5f;
            vz[k] = w * f * 0.8f + 1.5f;
            float lum = 0.75f + rnd.nextFloat() * 0.45f;
            r[k] = Math.min(1f, cr * lum);
            g[k] = Math.min(1f, cg * lum);
            b[k] = Math.min(1f, cb * lum);
            viataMax[k] = viata[k] = 0.55f + rnd.nextFloat() * 0.7f;
            marime[k] = marimeBaza * (0.35f + rnd.nextFloat() * 0.75f);
            unghi[k] = rnd.nextFloat() * 360f;
            vUnghi[k] = (rnd.nextFloat() - 0.5f) * 720f;
            ax[k] = rnd.nextFloat() - 0.5f;
            ay[k] = rnd.nextFloat() - 0.5f;
            az[k] = rnd.nextFloat() - 0.5f;
        }
    }

    public void actualizeaza(float dt) {
        for (int i = 0; i < nr; ) {
            viata[i] -= dt;
            if (viata[i] <= 0f) {
                int u = --nr;
                if (i != u) copiaza(u, i);
                continue;
            }
            vy[i] -= 22f * dt;
            float fr = 1f - 1.8f * dt;
            vx[i] *= fr; vz[i] *= fr;
            x[i] += vx[i] * dt;
            y[i] += vy[i] * dt;
            z[i] += vz[i] * dt;
            unghi[i] += vUnghi[i] * dt;
            i++;
        }
    }

    private void copiaza(int din, int in) {
        x[in] = x[din]; y[in] = y[din]; z[in] = z[din];
        vx[in] = vx[din]; vy[in] = vy[din]; vz[in] = vz[din];
        r[in] = r[din]; g[in] = g[din]; b[in] = b[din];
        viata[in] = viata[din]; viataMax[in] = viataMax[din];
        marime[in] = marime[din]; unghi[in] = unghi[din]; vUnghi[in] = vUnghi[din];
        ax[in] = ax[din]; ay[in] = ay[din]; az[in] = az[din];
    }

    public void deseneaza(Randare rnd) {
        for (int i = 0; i < nr; i++) {
            float t = viata[i] / viataMax[i];
            float alfa = Math.min(1f, t * 1.6f);
            float s = marime[i] * (0.4f + 0.6f * t);
            rnd.cubRotit(x[i], y[i], z[i], unghi[i], ax[i], ay[i], az[i],
                    r[i], g[i], b[i], alfa, s);
        }
    }

    /** varianta cu textura particula.png: scantei luminoase in loc de cuburi */
    public void deseneazaScantei(Scantei sc) {
        for (int i = 0; i < nr; i++) {
            float t = viata[i] / viataMax[i];
            float alfa = Math.min(1f, t * 1.6f);
            float s = marime[i] * (0.4f + 0.6f * t) * 5f;   // sprite-ul include si stralucirea
            sc.adauga(x[i], y[i], z[i], s, unghi[i] * 0.25f, r[i], g[i], b[i], alfa);
        }
    }

    public int numar() { return nr; }
}
