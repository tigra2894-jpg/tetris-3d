package com.marius.tetris3d;

/**
 * Fundal, contur de tabla si umbre proiectate.
 */
public class Fundal {

    private final float[] accent = new float[]{0.15f, 0.78f, 0.98f};

    private float timp = 0f;

    private static final int NR_CEATA = 10;

    private final float[] cx = new float[NR_CEATA];
    private final float[] cy = new float[NR_CEATA];
    private final float[] cz = new float[NR_CEATA];
    private final float[] marime = new float[NR_CEATA];
    private final float[] faza = new float[NR_CEATA];

    public Fundal() {
        java.util.Random rnd = new java.util.Random(31);
        for (int i = 0; i < NR_CEATA; i++) {
            cx[i] = (rnd.nextFloat() - 0.5f) * 26f;
            cy[i] = (rnd.nextFloat() - 0.5f) * 24f;
            cz[i] = -26f - rnd.nextFloat() * 10f;
            marime[i] = 1.4f + rnd.nextFloat() * 1.8f;
            faza[i] = rnd.nextFloat() * 6.28f;
        }
    }

    public void seteazaAccent(boolean modLiber) {
        if (modLiber) {
            accent[0] = 0.70f; accent[1] = 0.30f; accent[2] = 0.98f;
        } else {
            accent[0] = 0.15f; accent[1] = 0.78f; accent[2] = 0.98f;
        }
    }

    public float[] accent() {
        return accent;
    }

    public void actualizeaza(float dt) {
        timp += dt;
    }

    public void deseneazaCeata(Desenator d) {
        for (int i = 0; i < NR_CEATA; i++) {
            float p = 0.5f + 0.5f * (float) Math.sin(timp * 0.30f + faza[i]);
            float alfa = 0.020f + p * 0.022f;

            d.cub(cx[i], cy[i], cz[i],
                    accent[0] * 0.7f, accent[1] * 0.7f, accent[2],
                    alfa, marime[i]);
        }
    }

    public void deseneazaGrila(Desenator d, float yPodea, float latimeTabla) {
        float pulsatie = 0.65f + 0.35f * (float) Math.sin(timp * 1.0f);

        float st = -latimeTabla / 2f - 1.5f;
        float dr =  latimeTabla / 2f + 1.5f;

        for (int i = 1; i <= 4; i++) {
            float z = -i * 2.6f;
            float stins = 1f - (i - 1) / 4.5f;
            float alfa = 0.10f + stins * 0.18f * pulsatie;

            for (float x = st; x <= dr; x += 0.75f) {
                d.cub(x, yPodea - 0.62f, z,
                        accent[0], accent[1], accent[2],
                        alfa, 0.06f);
            }
        }

        for (int i = 0; i <= 4; i++) {
            float x = st + i * ((dr - st) / 4f);

            for (float z = -0.6f; z > -11f; z -= 0.75f) {
                float stins = 1f + z / 11f;
                float alfa = 0.09f + stins * 0.16f * pulsatie;
                if (alfa < 0.02f) continue;

                d.cub(x, yPodea - 0.62f, z,
                        accent[0], accent[1], accent[2],
                        alfa, 0.06f);
            }
        }
    }

    /**
     * conturul neon al tablei: doua bare verticale si una jos,
     * de culoarea accentului, pulsand incet.
     */
    public void deseneazaContur(Desenator d, float offX, float offY,
                                int coloane, int randuri) {
        float puls = 0.78f + 0.22f * (float) Math.sin(timp * 1.6f);

        float st = offX - 0.95f;
        float dr = offX + coloane - 0.05f;
        float jos = offY - 1.05f;
        float sus = offY + randuri - 0.4f;

        float r = accent[0], g = accent[1], b = accent[2];

        // stalpii laterali, din segmente mici si luminoase
        for (float y = jos; y <= sus; y += 0.42f) {
            float stins = 1f - Math.max(0f, (y - offY - randuri * 0.6f)) / (randuri * 0.5f);
            if (stins < 0.12f) stins = 0.12f;

            float a = 0.40f * puls * stins;
            d.cub(st, y, -0.15f, r, g, b, a, 0.13f);
            d.cub(dr, y, -0.15f, r, g, b, a, 0.13f);
        }

        // bara de jos, mai puternica
        for (float x = st; x <= dr; x += 0.42f) {
            d.cub(x, jos, -0.15f, r, g, b, 0.52f * puls, 0.14f);
        }

        // colturile de jos, mai aprinse
        d.cub(st, jos, -0.15f, r, g, b, 0.75f * puls, 0.20f);
        d.cub(dr, jos, -0.15f, r, g, b, 0.75f * puls, 0.20f);
    }

    /**
     * umbra proiectata a unui bloc: o pata intunecata,
     * deplasata in jos si intr-o parte, dupa directia luminii.
     */
    public void umbra(Desenator d, float x, float y, float z, float putere) {
        if (putere < 0.03f) return;

        d.cub(x - 0.20f, y - 0.28f, z - 0.30f,
                0.02f, 0.02f, 0.06f, 0.36f * putere, 0.98f);
    }

    public void reflexieBloc(Desenator d, float x, float yPodea, float rand, float z,
                             float r, float g, float b, float scara) {
        float inaltime = rand + 1f;
        if (inaltime > 5f) return;

        float yRef = yPodea - 1.1f - inaltime * 0.5f;
        float alfa = Math.max(0f, 0.17f - inaltime * 0.030f);
        if (alfa < 0.015f) return;

        d.cub(x, yRef, z - 0.25f, r, g, b, alfa, scara * 0.90f);
    }

    public void halou(Desenator d, float x, float y, float z,
                      float r, float g, float b, float intensitate) {
        d.cub(x, y, z - 0.35f, r, g, b, 0.09f * intensitate, 1.42f);
    }
}
