package com.marius.tetris3dpro;

import java.util.Random;

/** stele plutitoare, praf si podea cu grila; comun pentru toate ecranele */
public class Fundal {

    private static final int NR_STELE = 120;
    private final float[] sx = new float[NR_STELE], sy = new float[NR_STELE], sz = new float[NR_STELE];
    private final float[] sv = new float[NR_STELE], ss = new float[NR_STELE], sf = new float[NR_STELE];
    private final float[] sr = new float[NR_STELE], sg = new float[NR_STELE], sb = new float[NR_STELE];

    private float timp = 0f;

    /** podea texturata (assets/texturi/podea.png); setate de Aplicatie la crearea contextului GL */
    public Imagine imagine;
    public int texPodea = 0;
    /** stele cu textura (assets/texturi/stea.png): lotul si textura, setate de Aplicatie */
    public Scantei lotStele;
    public int texStea = 0;
    private final Random rnd = new Random(11);

    public Fundal() {
        for (int i = 0; i < NR_STELE; i++) reseteazaStea(i, true);
    }

    private void reseteazaStea(int i, boolean oriunde) {
        sx[i] = (rnd.nextFloat() - 0.5f) * 60f;
        sy[i] = oriunde ? (rnd.nextFloat() - 0.5f) * 60f : -30f;
        sz[i] = -8f - rnd.nextFloat() * 40f;
        sv[i] = 0.4f + rnd.nextFloat() * 1.4f;
        ss[i] = 0.08f + rnd.nextFloat() * 0.25f;
        sf[i] = rnd.nextFloat() * 6.28f;
        float h = rnd.nextFloat();
        if (h < 0.5f)      { sr[i] = 0.55f; sg[i] = 0.75f; sb[i] = 1.0f; }
        else if (h < 0.8f) { sr[i] = 1.0f;  sg[i] = 0.55f; sb[i] = 0.9f; }
        else               { sr[i] = 1.0f;  sg[i] = 0.95f; sb[i] = 0.8f; }
    }

    public void actualizeaza(float dt) {
        timp += dt;
        for (int i = 0; i < NR_STELE; i++) {
            sy[i] += sv[i] * dt;
            if (sy[i] > 32f) reseteazaStea(i, false);
        }
    }

    public void deseneazaStele(Randare r, float[] culoareTema) {
        boolean texturat = texStea != 0 && lotStele != null;
        for (int i = 0; i < NR_STELE; i++) {
            float puls = 0.6f + 0.4f * (float) Math.sin(timp * 2.1f + sf[i]);
            float ad = 1f - Math.min(1f, (-sz[i] - 8f) / 40f);
            float alfa = (0.25f + 0.55f * ad) * puls;
            if (texturat) {
                lotStele.adauga(sx[i], sy[i], sz[i], ss[i] * 7f, 0f,
                        sr[i] * 0.7f + culoareTema[0] * 0.3f,
                        sg[i] * 0.7f + culoareTema[1] * 0.3f,
                        sb[i] * 0.7f + culoareTema[2] * 0.3f,
                        alfa);
                continue;
            }
            r.cubRotit(sx[i], sy[i], sz[i], timp * 25f + sf[i] * 57f, 1f, 1f, 0.3f,
                    sr[i] * 0.7f + culoareTema[0] * 0.3f,
                    sg[i] * 0.7f + culoareTema[1] * 0.3f,
                    sb[i] * 0.7f + culoareTema[2] * 0.3f,
                    alfa, ss[i]);
        }
    }

    /** podea perspectiva sub tabla */
    public void deseneazaPodea(Randare r, float yPodea, float[] cul) {
        if (texPodea != 0 && imagine != null) {
            // o placa de textura = 6 unitati; se estompeaza intre z=-26 si z=-6
            imagine.podea(texPodea, r.vizProj(), -24f, 24f, -26f, 8f, yPodea,
                    48f / 6f, 34f / 6f, 0.75f, 0.9f, -6f);
            return;
        }
        for (int i = -7; i <= 7; i++) {
            float x = i * 2.4f;
            float alfa = 0.10f + 0.10f * (1f - Math.abs(i) / 7f);
            r.cubIntins(x, yPodea, -6f, 0.05f, 0.05f, 30f, cul[0], cul[1], cul[2], alfa);
        }
        for (int k = 0; k <= 10; k++) {
            float z = 4f - k * 3f;
            float alfa = 0.22f * (1f - k / 10f) + 0.03f;
            r.cubIntins(0f, yPodea, z, 40f, 0.05f, 0.05f, cul[0], cul[1], cul[2], alfa);
        }
    }

    public float timp() { return timp; }
}
