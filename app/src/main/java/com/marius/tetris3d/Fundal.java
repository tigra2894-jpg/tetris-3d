package com.marius.tetris3d;

/**
 * Fundalul jocului, cu doua atmosfere:
 *   CLASIC - noapte rece, albastru-petrol, calm si ordonat
 *   LIBER  - seara calda, violet-prun cu chihlimbar, mai moale
 * Totul se misca foarte incet, ca sa fie odihnitor la ochi.
 */
public class Fundal {

    /** culoarea de accent, folosita la grila si contur */
    private final float[] accent = new float[]{0.16f, 0.62f, 0.78f};
    /** a doua culoare, pentru ceata si adancime */
    private final float[] adanc  = new float[]{0.10f, 0.30f, 0.52f};

    private boolean cald = false;
    private float timp = 0f;

    /** pete mari de lumina, foarte sterse, care respira incet */
    private static final int NR_LUMINI = 7;

    private final float[] lx = new float[NR_LUMINI];
    private final float[] ly = new float[NR_LUMINI];
    private final float[] lz = new float[NR_LUMINI];
    private final float[] lmar = new float[NR_LUMINI];
    private final float[] lfaza = new float[NR_LUMINI];
    private final float[] lviteza = new float[NR_LUMINI];

    /** firicele de praf care plutesc, foarte putine si discrete */
    private static final int NR_PRAF = 22;

    private final float[] px = new float[NR_PRAF];
    private final float[] py = new float[NR_PRAF];
    private final float[] pz = new float[NR_PRAF];
    private final float[] pmar = new float[NR_PRAF];
    private final float[] pviteza = new float[NR_PRAF];
    private final float[] pfaza = new float[NR_PRAF];

    public Fundal() {
        java.util.Random rnd = new java.util.Random(31);

        for (int i = 0; i < NR_LUMINI; i++) {
            lx[i] = (rnd.nextFloat() - 0.5f) * 30f;
            ly[i] = (rnd.nextFloat() - 0.5f) * 28f;
            lz[i] = -22f - rnd.nextFloat() * 14f;
            lmar[i] = 3.2f + rnd.nextFloat() * 3.6f;
            lfaza[i] = rnd.nextFloat() * 6.28f;
            lviteza[i] = 0.10f + rnd.nextFloat() * 0.12f;
        }

        for (int i = 0; i < NR_PRAF; i++) {
            px[i] = (rnd.nextFloat() - 0.5f) * 24f;
            py[i] = (rnd.nextFloat() - 0.5f) * 26f;
            pz[i] = -6f - rnd.nextFloat() * 12f;
            pmar[i] = 0.05f + rnd.nextFloat() * 0.07f;
            pviteza[i] = 0.18f + rnd.nextFloat() * 0.30f;
            pfaza[i] = rnd.nextFloat() * 6.28f;
        }
    }

    /** CLASIC: noapte rece. LIBER: seara calda. */
    public void seteazaAccent(boolean modLiber) {
        cald = modLiber;

        if (cald) {
            // violet-prun cu chihlimbar
            accent[0] = 0.72f; accent[1] = 0.40f; accent[2] = 0.88f;
            adanc[0]  = 0.34f; adanc[1]  = 0.14f; adanc[2]  = 0.36f;
        } else {
            // albastru-petrol, rece si linistit
            accent[0] = 0.16f; accent[1] = 0.66f; accent[2] = 0.82f;
            adanc[0]  = 0.08f; adanc[1]  = 0.26f; adanc[2]  = 0.46f;
        }
    }

    public float[] accent() {
        return accent;
    }

    public void actualizeaza(float dt) {
        timp += dt;

        for (int i = 0; i < NR_PRAF; i++) {
            py[i] += dt * pviteza[i];
            px[i] += (float) Math.sin(timp * 0.25f + pfaza[i]) * dt * 0.22f;
            if (py[i] > 15f) py[i] = -15f;
        }
    }

    /** lumini mari, difuze, care respira foarte incet */
    public void deseneazaCeata(Desenator d) {
        for (int i = 0; i < NR_LUMINI; i++) {
            float p = 0.5f + 0.5f * (float) Math.sin(timp * lviteza[i] + lfaza[i]);

            float alfa = 0.022f + p * 0.028f;
            float s = lmar[i] * (0.88f + p * 0.24f);

            // amestec intre adanc si accent, dupa respiratie
            float r = adanc[0] + (accent[0] - adanc[0]) * p * 0.45f;
            float g = adanc[1] + (accent[1] - adanc[1]) * p * 0.45f;
            float b = adanc[2] + (accent[2] - adanc[2]) * p * 0.45f;

            d.cub(lx[i], ly[i], lz[i], r, g, b, alfa, s);
        }

        // praf fin care pluteste
        for (int i = 0; i < NR_PRAF; i++) {
            float p = 0.5f + 0.5f * (float) Math.sin(timp * 0.9f + pfaza[i]);
            float alfa = 0.10f + p * 0.16f;

            d.cub(px[i], py[i], pz[i],
                    accent[0] * 0.9f + 0.10f,
                    accent[1] * 0.9f + 0.10f,
                    accent[2] * 0.9f + 0.10f,
                    alfa, pmar[i]);
        }
    }

    /** grila pe podea: la cald e mai difuza, la rece mai clara */
    public void deseneazaGrila(Desenator d, float yPodea, float latimeTabla) {
        float ritm = cald ? 0.55f : 0.95f;
        float pulsatie = 0.68f + 0.32f * (float) Math.sin(timp * ritm);

        float st = -latimeTabla / 2f - 1.6f;
        float dr =  latimeTabla / 2f + 1.6f;

        float tarie = cald ? 0.78f : 1f;
        float pas = cald ? 0.85f : 0.72f;

        // linii transversale
        for (int i = 1; i <= 4; i++) {
            float z = -i * 2.7f;
            float stins = 1f - (i - 1) / 4.6f;
            float alfa = (0.09f + stins * 0.17f * pulsatie) * tarie;

            for (float x = st; x <= dr; x += pas) {
                d.cub(x, yPodea - 0.62f, z,
                        accent[0], accent[1], accent[2],
                        alfa, cald ? 0.075f : 0.06f);
            }
        }

        // linii pe adancime
        for (int i = 0; i <= 4; i++) {
            float x = st + i * ((dr - st) / 4f);

            for (float z = -0.6f; z > -11f; z -= pas) {
                float stins = 1f + z / 11f;
                float alfa = (0.08f + stins * 0.15f * pulsatie) * tarie;
                if (alfa < 0.018f) continue;

                d.cub(x, yPodea - 0.62f, z,
                        accent[0], accent[1], accent[2],
                        alfa, cald ? 0.075f : 0.06f);
            }
        }
    }

    /** conturul luminos al tablei */
    public void deseneazaContur(Desenator d, float offX, float offY,
                                int coloane, int randuri) {
        float ritm = cald ? 1.0f : 1.5f;
        float puls = 0.80f + 0.20f * (float) Math.sin(timp * ritm);

        float st = offX - 0.95f;
        float dr = offX + coloane - 0.05f;
        float jos = offY - 1.05f;
        float sus = offY + randuri - 0.4f;

        float r = accent[0], g = accent[1], b = accent[2];
        float tarie = cald ? 0.88f : 1f;

        for (float y = jos; y <= sus; y += 0.42f) {
            float stins = 1f - Math.max(0f, (y - offY - randuri * 0.6f)) / (randuri * 0.5f);
            if (stins < 0.12f) stins = 0.12f;

            float a = 0.36f * puls * stins * tarie;
            d.cub(st, y, -0.15f, r, g, b, a, 0.13f);
            d.cub(dr, y, -0.15f, r, g, b, a, 0.13f);
        }

        for (float x = st; x <= dr; x += 0.42f) {
            d.cub(x, jos, -0.15f, r, g, b, 0.48f * puls * tarie, 0.14f);
        }

        d.cub(st, jos, -0.15f, r, g, b, 0.70f * puls * tarie, 0.20f);
        d.cub(dr, jos, -0.15f, r, g, b, 0.70f * puls * tarie, 0.20f);
    }

    /** umbra proiectata a unui bloc */
    public void umbra(Desenator d, float x, float y, float z, float putere) {
        if (putere < 0.03f) return;

        d.cub(x - 0.20f, y - 0.28f, z - 0.30f,
                0.02f, 0.02f, 0.05f, 0.32f * putere, 0.98f);
    }

    public void reflexieBloc(Desenator d, float x, float yPodea, float rand, float z,
                             float r, float g, float b, float scara) {
        float inaltime = rand + 1f;
        if (inaltime > 5f) return;

        float yRef = yPodea - 1.1f - inaltime * 0.5f;
        float alfa = Math.max(0f, 0.16f - inaltime * 0.028f);
        if (alfa < 0.015f) return;

        d.cub(x, yRef, z - 0.25f, r, g, b, alfa, scara * 0.90f);
    }

    public void halou(Desenator d, float x, float y, float z,
                      float r, float g, float b, float intensitate) {
        d.cub(x, y, z - 0.35f, r, g, b, 0.08f * intensitate, 1.42f);
    }
}
