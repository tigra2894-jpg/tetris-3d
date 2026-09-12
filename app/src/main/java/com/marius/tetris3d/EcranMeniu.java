package com.marius.tetris3d;

import android.graphics.Color;

public class EcranMeniu extends Ecran {

    private static final float Y_TITLU  = 0.16f;
    private static final float Y_SUBT   = 0.235f;

    private static final float Y_JOACA  = 0.46f;
    private static final float Y_MODURI = 0.56f;
    private static final float Y_SETARI = 0.65f;
    private static final float Y_STAT   = 0.74f;
    private static final float Y_DESPRE = 0.83f;
    private static final float Y_IESIRE = 0.91f;
    private static final float GROSIME  = 0.050f;

    private int apasat = -1;
    private float stralucire = 0f;

    /** piesele care plutesc in fundal */
    private static final int NR_PIESE = 7;
    private final float[] px = new float[NR_PIESE];
    private final float[] py = new float[NR_PIESE];
    private final float[] pz = new float[NR_PIESE];
    private final float[] rot = new float[NR_PIESE];
    private final float[] vitRot = new float[NR_PIESE];
    private final float[] vitY = new float[NR_PIESE];
    private final int[] tip = new int[NR_PIESE];

    /** piese care se construiesc singure jos, ca un mic turn viu */
    private static final int NR_TURN = 9;
    private final int[] turnTip = new int[NR_TURN];
    private final float[] turnFaza = new float[NR_TURN];

    private Fundal fundal;

    public EcranMeniu(Aplicatie app) {
        super(app);
        fundal = new Fundal();

        java.util.Random rnd = new java.util.Random(11);
        for (int i = 0; i < NR_PIESE; i++) {
            px[i] = (rnd.nextFloat() - 0.5f) * 17f;
            py[i] = (rnd.nextFloat() - 0.5f) * 26f;
            pz[i] = -7f - rnd.nextFloat() * 11f;
            rot[i] = rnd.nextFloat() * 360f;
            vitRot[i] = 10f + rnd.nextFloat() * 20f;
            vitY[i] = 0.4f + rnd.nextFloat() * 0.9f;
            tip[i] = rnd.nextInt(7);
        }

        for (int i = 0; i < NR_TURN; i++) {
            turnTip[i] = rnd.nextInt(7);
            turnFaza[i] = rnd.nextFloat() * 6.28f;
        }
    }

    @Override
    public void laIntrare() {
        super.laIntrare();
        apasat = -1;
        stralucire = 0f;
        fundal.seteazaAccent(false);
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);
        fundal.actualizeaza(dt);

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
        d.seteazaProiectie(48f, d.raport, 1f, 90f);

        float leg = timp * 0.20f;
        d.seteazaCamera(
                (float) Math.sin(leg) * 0.9f,
                (float) Math.cos(leg * 0.8f) * 0.5f,
                26f,
                0f, 0f, 0f);

        fundal.deseneazaCeata(d);
        app.stele.deseneaza2(d);

        // podea cu grila, jos de tot
        fundal.deseneazaGrila(d, -11.5f, 14f);

        deseneazaPieseFundal(d);
        deseneazaTurnulViu(d);
        deseneazaContururi(d);

        float apTitlu = intrare(0f, 0.5f);
        float pulsTitlu = 0.90f + 0.10f * puls(1.5f);

        app.ui.textCentrat("TETRIS", 0.5f, Y_TITLU, 0.105f,
                culoare((int) (255 * pulsTitlu), (int) (215 * pulsTitlu), 60, apTitlu));

        app.ui.textCentrat("3D", 0.5f, Y_SUBT, 0.070f,
                culoare(60, (int) (225 * pulsTitlu), 255, apTitlu));

        optiune("JOACA",      0, Y_JOACA,  0.056f, 110, 255, 150);
        optiune("MODURI",     1, Y_MODURI, 0.046f, 140, 205, 255);
        optiune("SETARI",     2, Y_SETARI, 0.046f, 220, 180, 255);
        optiune("STATISTICI", 3, Y_STAT,   0.038f, 180, 190, 230);
        optiune("DESPRE",     4, Y_DESPRE, 0.034f, 255, 205, 140);
        optiune("IESIRE",     5, Y_IESIRE, 0.034f, 255, 120, 120);
    }

    private float intrare(float intarziere, float durata) {
        float t = (timp - intarziere) / durata;
        if (t < 0f) return 0f;
        if (t > 1f) return 1f;
        return t * t * (3f - 2f * t);
    }

    private void optiune(String s, int index, float yFrac, float marime,
                         int r, int g, int b) {
        float ap = intrare(index * 0.07f, 0.4f);

        float lum = 1f;
        if (apasat == index) lum = 1f + stralucire * 0.7f;

        int cul = culoare(
                (int) Math.min(255, r * lum),
                (int) Math.min(255, g * lum),
                (int) Math.min(255, b * lum),
                ap);

        app.ui.textCentrat(s, 0.5f, yFrac, marime, cul);
    }

    private int culoare(int r, int g, int b, float alfa) {
        int a = (int) (Math.max(0f, Math.min(1f, alfa)) * 255);
        return Color.argb(a, r, g, b);
    }

    private void deseneazaPieseFundal(Desenator d) {
        float[][] culori = app.setari.culoriPiese();

        for (int i = 0; i < NR_PIESE; i++) {
            int[][] forma = Joc.formaPiesei(tip[i], 0);
            float[] cul = culori[tip[i]];

            for (int k = 0; k < 4; k++) {
                float cx = px[i] + (forma[k][0] - 1.5f) * 0.82f;
                float cy = py[i] + (forma[k][1] - 2.0f) * 0.82f;

                d.cubRotit(cx, cy, pz[i],
                        rot[i], 0.5f, 1f, 0.3f,
                        cul[0] * 0.78f, cul[1] * 0.78f, cul[2] * 0.78f,
                        0.36f, 0.72f);
            }
        }
    }

    /** un mic turn care creste si se micsoreaza singur, jos pe podea */
    private void deseneazaTurnulViu(Desenator d) {
        float[][] culori = app.setari.culoriPiese();
        float bazaY = -11.0f;

        for (int i = 0; i < NR_TURN; i++) {
            float p = 0.5f + 0.5f * (float) Math.sin(timp * 0.55f + turnFaza[i]);
            int inaltime = 1 + (int) (p * 3.4f);

            float x = (i - (NR_TURN - 1) / 2f) * 1.15f;
            float[] cul = culori[turnTip[i]];

            for (int n = 0; n < inaltime; n++) {
                float alfa = 0.65f - n * 0.09f;
                if (alfa < 0.15f) alfa = 0.15f;

                d.cub(x, bazaY + n * 1.0f, -2.5f,
                        cul[0] * 0.9f, cul[1] * 0.9f, cul[2] * 0.9f,
                        alfa, 0.90f);
            }
        }
    }

    /** doua linii de lumina verticale, in lateral, ca la conturul tablei */
    private void deseneazaContururi(Desenator d) {
        float[] acc = fundal.accent();
        float puls = 0.70f + 0.30f * (float) Math.sin(timp * 1.4f);

        float st = -8.4f;
        float dr =  8.4f;

        for (float y = -11.5f; y <= 9.5f; y += 0.55f) {
            float stins = 1f - Math.abs(y + 1f) / 13f;
            if (stins < 0.10f) stins = 0.10f;

            float a = 0.26f * puls * stins;

            d.cub(st, y, -3.5f, acc[0], acc[1], acc[2], a, 0.11f);
            d.cub(dr, y, -3.5f, acc[0], acc[1], acc[2], a, 0.11f);
        }
    }

    @Override
    public boolean atingere(float x, float y) {

        if (inRand(y, Y_JOACA, GROSIME)) {
            apasa(0);
            int m = app.setari.ultimulMod();
            app.jocNou(m, m == Setari.MOD_LIBER);
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
        if (inRand(y, Y_DESPRE, GROSIME)) {
            apasa(4);
            app.mergiLa(app.ecranDespre);
            return true;
        }
        if (inRand(y, Y_IESIRE, GROSIME)) {
            apasa(5);
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
