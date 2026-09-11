package com.marius.tetris3d;

/**
 * Fundal discret: lumina difuza foarte slaba in spate
 * si o grila subtire pe podea. Nu trebuie sa fure atentia de la piese.
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

    /** lumina difuza foarte slaba, doar ca fundalul sa nu fie negru mort */
    public void deseneazaCeata(Desenator d) {
        for (int i = 0; i < NR_CEATA; i++) {
            float p = 0.5f + 0.5f * (float) Math.sin(timp * 0.30f + faza[i]);
            float alfa = 0.020f + p * 0.022f;

            d.cub(cx[i], cy[i], cz[i],
                    accent[0] * 0.7f, accent[1] * 0.7f, accent[2],
                    alfa, marime[i]);
        }
    }

    /** grila subtire pe podea: doar cateva linii clare */
    public void deseneazaGrila(Desenator d, float yPodea, float latimeTabla) {
        float pulsatie = 0.65f + 0.35f * (float) Math.sin(timp * 1.0f);

        float st = -latimeTabla / 2f - 1.5f;
        float dr =  latimeTabla / 2f + 1.5f;

        // linii transversale, tot mai sterse spre spate
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

        // linii pe adancime
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

    /** reflexie slaba sub un bloc */
    public void reflexieBloc(Desenator d, float x, float yPodea, float rand, float z,
                             float r, float g, float b, float scara) {
        float inaltime = rand + 1f;
        if (inaltime > 5f) return;

        float yRef = yPodea - 1.1f - inaltime * 0.5f;
        float alfa = Math.max(0f, 0.17f - inaltime * 0.030f);
        if (alfa < 0.015f) return;

        d.cub(x, yRef, z - 0.25f, r, g, b, alfa, scara * 0.90f);
    }

    /** halou slab, folosit doar unde e nevoie */
    public void halou(Desenator d, float x, float y, float z,
                      float r, float g, float b, float intensitate) {
        d.cub(x, y, z - 0.35f, r, g, b, 0.09f * intensitate, 1.42f);
    }
}
