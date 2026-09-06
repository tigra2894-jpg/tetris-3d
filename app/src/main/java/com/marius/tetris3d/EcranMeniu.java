package com.marius.tetris3d;

/**
 * Ecranul principal: titlul si optiunile.
 * In fundal se rotesc lent cateva piese de tetris.
 */
public class EcranMeniu extends Ecran {

    // pozitiile randurilor pe ecran, 0..1 de sus in jos
    private static final float Y_JOACA  = 0.44f;
    private static final float Y_MODURI = 0.56f;
    private static final float Y_SETARI = 0.68f;
    private static final float Y_STAT   = 0.80f;
    private static final float Y_IESIRE = 0.92f;
    private static final float GROSIME  = 0.09f;

    // ce rand e apasat acum, -1 = niciunul
    private int apasat = -1;
    private float stralucire = 0f;

    // piesele care plutesc in fundal
    private static final int NR_PIESE = 5;
    private final float[] px = new float[NR_PIESE];
    private final float[] py = new float[NR_PIESE];
    private final float[] pz = new float[NR_PIESE];
    private final float[] rot = new float[NR_PIESE];
    private final float[] vitRot = new float[NR_PIESE];
    private final float[] vitY = new float[NR_PIESE];
    private final int[] tip = new int[NR_PIESE];

    public EcranMeniu(Aplicatie app) {
        super(app);

        java.util.Random rnd = new java.util.Random(11);
        for (int i = 0; i < NR_PIESE; i++) {
            px[i] = (rnd.nextFloat() - 0.5f) * 22f;
            py[i] = (rnd.nextFloat() - 0.5f) * 26f;
            pz[i] = -10f - rnd.nextFloat() * 12f;
            rot[i] = rnd.nextFloat() * 360f;
            vitRot[i] = 8f + rnd.nextFloat() * 16f;
            vitY[i] = 0.5f + rnd.nextFloat() * 1.1f;
            tip[i] = rnd.nextInt(7);
        }
    }

    @Override
    public void laIntrare() {
        super.laIntrare();
        apasat = -1;
        stralucire = 0f;
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);

        for (int i = 0; i < NR_PIESE; i++) {
            rot[i] += dt * vitRot[i];
            py[i] += dt * vitY[i];
            if (py[i] > 16f) {
                py[i] = -16f;
                tip[i] = (tip[i] + 3) % 7;
            }
        }

        stralucire -= dt * 3.2f;
        if (stralucire < 0f) {
            stralucire = 0f;
            apasat = -1;
        }
    }

    @Override
    public void deseneaza(Desenator d) {
        float ap = aparitie();

        d.seteazaProiectie(48f, d.raport, 1f, 90f);

        float leg = timp * 0.22f;
        d.seteazaCamera(
                (float) Math.sin(leg) * 1.2f,
                (float) Math.cos(leg * 0.8f) * 0.7f,
                26f,
                0f, 0f, 0f);

        d.seteazaLumina(
                (float) Math.sin(timp * 0.5f) * 16f,
                14f,
                (float) Math.cos(timp * 0.5f) * 16f + 12f);

        app.stele.deseneaza2(d);
        deseneazaPieseFundal(d);

        // ---------- titlul ----------
        float pulsTitlu = 0.85f + 0.15f * puls(1.6f);
        float yTitlu = 11.0f - (1f - ap) * 4f;

        d.textCentrat("TETRIS", 0f, yTitlu, 0.62f,
                1.0f * pulsTitlu, 0.85f * pulsTitlu, 0.30f);
        d.textCentrat("3D", 0f, yTitlu - 4.2f, 0.62f,
                0.35f, 0.90f * pulsTitlu, 1.0f * pulsTitlu);

        // ---------- optiunile ----------
        deseneazaOptiune(d, "JOACA",       0, Y_JOACA,  0.44f, ap, 0.40f, 1.00f, 0.55f);
        deseneazaOptiune(d, "MODURI",      1, Y_MODURI, 0.34f, ap, 0.55f, 0.80f, 1.00f);
        deseneazaOptiune(d, "SETARI",      2, Y_SETARI, 0.34f, ap, 0.85f, 0.75f, 1.00f);
        deseneazaOptiune(d, "STATISTICI",  3, Y_STAT,   0.26f, ap, 0.70f, 0.75f, 0.90f);
        deseneazaOptiune(d, "IESIRE",      4, Y_IESIRE, 0.26f, ap, 1.00f, 0.45f, 0.45f);
    }

    private void deseneazaOptiune(Desenator d, String s, int index,
                                  float yEcran, float scara, float ap,
                                  float r, float g, float b) {
        // transforma pozitia de pe ecran (0..1) in pozitie in lume
        float y = ecranLaLume(yEcran);

        // intarziere la aparitie, unul dupa altul
        float apLocal = ap - index * 0.10f;
        if (apLocal < 0f) apLocal = 0f;
        if (apLocal > 1f) apLocal = 1f;

        float x = -(1f - apLocal) * 14f;

        float lum = 1f;
        if (apasat == index) {
            lum = 1f + stralucire * 0.9f;
            x += stralucire * 0.35f;
        }

        d.textCentrat(s, x, y, scara,
                Math.min(1f, r * lum),
                Math.min(1f, g * lum),
                Math.min(1f, b * lum));
    }

    /** 0 sus, 1 jos pe ecran -> coordonata verticala in lume */
    private float ecranLaLume(float yEcran) {
        // camera vede aproximativ de la +13 la -13 pe verticala
        return 13f - yEcran * 26f;
    }

    private void deseneazaPieseFundal(Desenator d) {
        float[][] culori = app.setari.culoriPiese();

        for (int i = 0; i < NR_PIESE; i++) {
            int[][] forma = Joc.formaPiesei(tip[i], 0);
            float[] cul = culori[tip[i]];

            for (int k = 0; k < 4; k++) {
                float cx = px[i] + (forma[k][0] - 1.5f) * 0.75f;
                float cy = py[i] + (forma[k][1] - 2.0f) * 0.75f;

                d.cubRotit(cx, cy, pz[i],
                        rot[i], 0.5f, 1f, 0.3f,
                        cul[0] * 0.55f, cul[1] * 0.55f, cul[2] * 0.55f,
                        0.35f, 0.72f);
            }
        }
    }

    @Override
    public boolean atingere(float x, float y) {

        if (inRand(y, Y_JOACA, GROSIME)) {
            apasa(0);
            app.jocNou(app.setari.ultimulMod());
            return true;
        }
        if (inRand(y, Y_MODURI, GROSIME)) {
            apasa(1);
            app.mergiLa(app.ecranModuri);
            return true;
        }
        if (inRand(y, Y_SETARI, GROSIME)) {
            apasa(2);
            app.mergiLa(app.ecranSetari);
            return true;
        }
        if (inRand(y, Y_STAT, GROSIME)) {
            apasa(3);
            app.mergiLa(app.ecranStatistici);
            return true;
        }
        if (inRand(y, Y_IESIRE, GROSIME)) {
            apasa(4);
            app.iesire();
            return true;
        }
        return false;
    }

    private void apasa(int index) {
        apasat = index;
        stralucire = 1f;
        app.sunet.rotire();
    }

    @Override
    public boolean inapoi() {
        app.iesire();
        return true;
    }
}
