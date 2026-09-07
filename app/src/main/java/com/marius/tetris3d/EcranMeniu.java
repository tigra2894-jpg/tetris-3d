package com.marius.tetris3d;

import android.graphics.Color;

public class EcranMeniu extends Ecran {

    private static final float Y_TITLU  = 0.16f;
    private static final float Y_JOACA  = 0.46f;
    private static final float Y_MODURI = 0.56f;
    private static final float Y_SETARI = 0.65f;
    private static final float Y_STAT   = 0.74f;
    private static final float Y_IESIRE = 0.83f;
    private static final float GROSIME  = 0.055f;

    private int apasat = -1;
    private float stralucire = 0f;

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
            px[i] = (rnd.nextFloat() - 0.5f) * 14f;
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
        d.seteazaProiectie(48f, d.raport, 1f, 90f);

        float leg = timp * 0.22f;
        d.seteazaCamera(
                (float) Math.sin(leg) * 1.0f,
                (float) Math.cos(leg * 0.8f) * 0.6f,
                26f,
                0f, 0f, 0f);

        d.seteazaLumina(
                (float) Math.sin(timp * 0.5f) * 16f,
                14f,
                (float) Math.cos(timp * 0.5f) * 16f + 12f);

        app.stele.deseneaza2(d);
        deseneazaPieseFundal(d);

        // ---------- titlu, text normal ----------
        float apTitlu = intrare(0f, 0.5f);
        float alfaTitlu = (int) (apTitlu * 255) << 24;

        int culTitlu1 = culoare(255, 235, 90, apTitlu);
        int culTitlu2 = culoare(80, 220, 255, apTitlu);

        app.ui.textCentrat("TETRIS", 0.5f, Y_TITLU, 0.11f, culTitlu1);
        app.ui.textCentrat("3D", 0.5f, Y_TITLU + 0.10f, 0.075f, culTitlu2);

        // ---------- optiuni ----------
        optiune("JOACA",      0, Y_JOACA,  0.052f, 100, 255, 140);
        optiune("MODURI",     1, Y_MODURI, 0.044f, 140, 205, 255);
        optiune("SETARI",     2, Y_SETARI, 0.044f, 215, 190, 255);
        optiune("STATISTICI", 3, Y_STAT,   0.036f, 180, 190, 230);
        optiune("IESIRE",     4, Y_IESIRE, 0.036f, 255, 115, 115);
    }

    private float intrare(float intarziere, float durata) {
        float t = (timp - intarziere) / durata;
        if (t < 0f) return 0f;
        if (t > 1f) return 1f;
        return t * t * (3f - 2f * t);
    }

    private void optiune(String s, int index, float yFrac, float marime,
                         int r, int g, int b) {
        float ap = intrare(index * 0.08f, 0.4f);

        float lum = 1f;
        if (apasat == index) lum = 1f + stralucire * 0.7f;

        int culoare = culoare(
                (int) Math.min(255, r * lum),
                (int) Math.min(255, g * lum),
                (int) Math.min(255, b * lum),
                ap);

        app.ui.textCentrat(s, 0.5f, yFrac, marime, culoare);
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
                float cx = px[i] + (forma[k][0] - 1.5f) * 0.75f;
                float cy = py[i] + (forma[k][1] - 2.0f) * 0.75f;

                d.cubRotit(cx, cy, pz[i],
                        rot[i], 0.5f, 1f, 0.3f,
                        cul[0] * 0.55f, cul[1] * 0.55f, cul[2] * 0.55f,
                        0.30f, 0.65f);
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
