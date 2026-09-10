package com.marius.tetris3d;

/**
 * Fundalul scenei: ceata difuza in spate, grila luminoasa pe podea
 * si o reflexie slaba sub piese.
 * Culoarea se schimba dupa mod: cyan la Clasic, violet la Mod Liber.
 */
public class Fundal {

    /** culoarea de accent: [r, g, b] */
    private final float[] accent = new float[]{0.15f, 0.75f, 0.95f};

    private float timp = 0f;

    /** cate linii are grila pe fiecare directie */
    private static final int LINII_X = 13;
    private static final int LINII_Z = 9;

    /** cate pete de ceata plutesc in spate */
    private static final int NR_CEATA = 26;

    private final float[] cx = new float[NR_CEATA];
    private final float[] cy = new float[NR_CEATA];
    private final float[] cz = new float[NR_CEATA];
    private final float[] marime = new float[NR_CEATA];
    private final float[] faza = new float[NR_CEATA];
    private final float[] viteza = new float[NR_CEATA];

    public Fundal() {
        java.util.Random rnd = new java.util.Random(31);
        for (int i = 0; i < NR_CEATA; i++) {
            cx[i] = (rnd.nextFloat() - 0.5f) * 34f;
            cy[i] = (rnd.nextFloat() - 0.5f) * 30f;
            cz[i] = -16f - rnd.nextFloat() * 14f;
            marime[i] = 2.2f + rnd.nextFloat() * 3.4f;
            faza[i] = rnd.nextFloat() * 6.28f;
            viteza[i] = 0.10f + rnd.nextFloat() * 0.22f;
        }
    }

    /** cyan pentru Clasic, violet pentru Mod Liber */
    public void seteazaAccent(boolean modLiber) {
        if (modLiber) {
            accent[0] = 0.72f; accent[1] = 0.26f; accent[2] = 0.98f;
        } else {
            accent[0] = 0.15f; accent[1] = 0.78f; accent[2] = 0.98f;
        }
    }

    public float[] accent() {
        return accent;
    }

    public void actualizeaza(float dt) {
        timp += dt;
        for (int i = 0; i < NR_CEATA; i++) {
            cy[i] += dt * viteza[i];
            if (cy[i] > 18f) cy[i] = -18f;
        }
    }

    /** ceata difuza din spate; se deseneaza prima, in adancime */
    public void deseneazaCeata(Desenator d) {
        for (int i = 0; i < NR_CEATA; i++) {
            float p = 0.5f + 0.5f * (float) Math.sin(timp * 0.35f + faza[i]);
            float alfa = 0.030f + p * 0.045f;
            float s = marime[i] * (0.85f + p * 0.25f);

            d.cub(cx[i], cy[i], cz[i],
                    accent[0] * 0.55f, accent[1] * 0.55f, accent[2] * 0.70f,
                    alfa, s);
        }
    }

    /**
     * grila luminoasa pe podea, care se pierde in departare.
     * yPodea = inaltimea podelei in lume.
     */
    public void deseneazaGrila(Desenator d, float yPodea) {
        float pulsatie = 0.60f + 0.40f * (float) Math.sin(timp * 1.1f);

        float latime = 13f;
        float adancime = 16f;

        // linii pe adancime (merg spre orizont)
        for (int i = 0; i <= LINII_X; i++) {
            float x = -latime / 2f + i * (latime / LINII_X);

            for (float z = -1f; z > -adancime; z -= 0.85f) {
                float depart = -z / adancime;
                float stins = (1f - depart) * (1f - depart);
                float alfa = 0.10f + stins * 0.30f * pulsatie;
                if (alfa < 0.012f) continue;

                d.cub(x, yPodea - 0.55f, z,
                        accent[0], accent[1], accent[2],
                        alfa, 0.055f + stins * 0.045f);
            }
        }

        // linii pe latime (transversale)
        for (float z = -1f; z > -adancime; z -= (adancime / LINII_Z)) {
            float depart = -z / adancime;
            float stins = (1f - depart) * (1f - depart);
            float alfa = 0.12f + stins * 0.32f * pulsatie;
            if (alfa < 0.012f) continue;

            for (float x = -latime / 2f; x <= latime / 2f; x += 0.62f) {
                d.cub(x, yPodea - 0.55f, z,
                        accent[0], accent[1], accent[2],
                        alfa, 0.055f + stins * 0.045f);
            }
        }
    }

    /**
     * reflexia unui bloc pe podea: acelasi bloc, rasturnat si stins.
     * yPodea = nivelul podelei; rand = pozitia blocului fata de podea.
     */
    public void reflexieBloc(Desenator d, float x, float yPodea, float rand, float z,
                             float r, float g, float b, float scara) {
        float inaltime = rand + 1f;
        if (inaltime > 7f) return;

        float yRef = yPodea - 0.9f - inaltime * 0.55f;
        float alfa = Math.max(0f, 0.22f - inaltime * 0.028f);
        if (alfa < 0.012f) return;

        d.cub(x, yRef, z - 0.35f, r, g, b, alfa, scara * 0.92f);
    }

    /** halou slab in jurul unei piese aprinse, ca sa para ca lumineaza */
    public void halou(Desenator d, float x, float y, float z,
                      float r, float g, float b, float intensitate) {
        d.cub(x, y, z - 0.5f, r, g, b, 0.10f * intensitate, 1.55f);
        d.cub(x, y, z - 0.5f, r, g, b, 0.06f * intensitate, 2.20f);
    }
}
