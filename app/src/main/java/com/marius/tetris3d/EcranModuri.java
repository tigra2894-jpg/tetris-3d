package com.marius.tetris3d;

import android.graphics.Color;

public class EcranModuri extends Ecran {

    private static final String[][] DESCRIERI = {
        {"TETRIS", "OBISNUIT"},
        {"TABLA E UN", "TURN ROTITOR"},
        {"JOSUL SE", "SCHIMBA MEREU"},
        {"CUBURI DE", "STICLA SPARTA"},
        {"TABLA", "RESPIRA"},
        {"CONSTRUIESTI", "IN SPATIU"}
    };

    private static final int[][] CUL_MOD = {
        {100, 255, 140},
        {255, 190, 65},
        {155, 140, 255},
        {115, 240, 255},
        {255, 100, 140},
        {215, 155, 255}
    };

    private int selectat = 0;
    private float derulare = 0f;
    private float derulareTinta = 0f;

    private float stralucire = 0f;
    private int apasat = -1;   // 0 = JOACA, 1 = JOACA LIBER
    private float clipireBlocat = 0f;

    private static final float Y_TITLU  = 0.08f;
    private static final float Y_NUME   = 0.38f;
    private static final float Y_DESC1  = 0.44f;
    private static final float Y_DESC2  = 0.49f;
    private static final float Y_RECL   = 0.57f;
    private static final float Y_RECV   = 0.62f;
    private static final float Y_PUNCTE = 0.69f;
    private static final float Y_JOACA  = 0.78f;
    private static final float Y_LIBER  = 0.86f;
    private static final float Y_INAPOI = 0.94f;

    private float startTragereX = 0f;
    private boolean trage = false;

    public EcranModuri(Aplicatie app) {
        super(app);
    }

    @Override
    public void laIntrare() {
        super.laIntrare();
        selectat = app.setari.ultimulMod();
        derulare = selectat;
        derulareTinta = selectat;
        stralucire = 0f;
        apasat = -1;
        clipireBlocat = 0f;
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);
        float dif = derulareTinta - derulare;
        derulare += dif * Math.min(1f, dt * 9f);
        if (Math.abs(dif) < 0.001f) derulare = derulareTinta;

        stralucire -= dt * 3f;
        if (stralucire < 0f) stralucire = 0f;

        clipireBlocat -= dt * 3.5f;
        if (clipireBlocat < 0f) clipireBlocat = 0f;
    }

    @Override
    public void deseneaza(Desenator d) {
        d.seteazaProiectie(48f, d.raport, 1f, 90f);
        d.seteazaCamera(0f, 0f, 26f, 0f, 0f, 0f);
        d.seteazaLumina(
                (float) Math.sin(timp * 0.6f) * 15f, 14f,
                (float) Math.cos(timp * 0.6f) * 15f + 12f);

        app.stele.deseneaza2(d);

        app.ui.textCentrat("MODURI", 0.5f, Y_TITLU, 0.070f,
                Color.rgb(220, 230, 255));

        int mod = Math.round(derulare);
        if (mod < 0) mod = 0;
        if (mod >= Setari.NR_MODURI) mod = Setari.NR_MODURI - 1;
        boolean disponibil = Setari.MOD_DISPONIBIL[mod];
        int[] cul = CUL_MOD[mod];

        int rNume = disponibil ? cul[0] : 110;
        int gNume = disponibil ? cul[1] : 115;
        int bNume = disponibil ? cul[2] : 130;

        app.ui.textCentrat(Setari.NUME_MODURI[mod], 0.5f, Y_NUME, 0.058f,
                Color.rgb(rNume, gNume, bNume));

        if (disponibil) {
            app.ui.textCentrat(DESCRIERI[mod][0], 0.5f, Y_DESC1, 0.026f,
                    Color.rgb(190, 200, 220));
            app.ui.textCentrat(DESCRIERI[mod][1], 0.5f, Y_DESC2, 0.026f,
                    Color.rgb(190, 200, 220));

            app.ui.textCentrat("RECORD", 0.5f, Y_RECL, 0.022f,
                    Color.rgb(150, 160, 190));
            app.ui.textCentrat(String.valueOf(app.setari.record(mod)), 0.5f, Y_RECV, 0.038f,
                    Color.rgb(255, 225, 130));
        } else {
            float p = 0.6f + 0.4f * puls(2.2f);
            app.ui.textCentrat("IN LUCRU", 0.5f, Y_DESC1 + 0.015f, 0.032f,
                    Color.rgb((int) (255 * p), (int) (140 * p), 60));
            app.ui.textCentrat("REVINE INTR-O ACTUALIZARE", 0.5f, Y_RECL, 0.018f,
                    Color.rgb(140, 145, 165));
        }

        float[][] culori = app.setari.culoriPiese();
        int[][] forma = Joc.formaPiesei(mod % 7, 0);
        float rotP = timp * 34f + mod * 60f;
        float[] culP = culori[mod % 7];
        float alfaPiesa = disponibil ? 1f : 0.35f;
        for (int k = 0; k < 4; k++) {
            float cx = (forma[k][0] - 1.5f) * 0.85f;
            float cy = 5.4f + (forma[k][1] - 2.0f) * 0.85f;
            d.cubRotit(cx, cy, 0f, rotP, 0.4f, 1f, 0.3f,
                    culP[0], culP[1], culP[2], alfaPiesa, 0.44f);
        }

        float xPuncte = 0.5f - (Setari.NR_MODURI - 1) * 0.035f;
        for (int i = 0; i < Setari.NR_MODURI; i++) {
            boolean act = (i == mod);
            boolean disp = Setari.MOD_DISPONIBIL[i];
            int culPunct;
            if (act) culPunct = Color.rgb(255, 255, 255);
            else if (disp) culPunct = Color.rgb(140, 150, 180);
            else culPunct = Color.rgb(70, 72, 85);

            app.ui.textCentrat("*", xPuncte + i * 0.07f, Y_PUNCTE,
                    act ? 0.030f : 0.019f, culPunct);
        }

        if (disponibil) {
            float lumJ = (apasat == 0) ? 1f + stralucire : 1f;
            app.ui.textCentrat("JOACA", 0.5f, Y_JOACA, 0.048f,
                    Color.rgb((int) Math.min(255, 90 * lumJ),
                              (int) Math.min(255, 255 * lumJ),
                              (int) Math.min(255, 140 * lumJ)));

            float lumL = (apasat == 1) ? 1f + stralucire : 1f;
            app.ui.textCentrat("JOACA LIBER", 0.5f, Y_LIBER, 0.032f,
                    Color.rgb((int) Math.min(255, 190 * lumL),
                              (int) Math.min(255, 160 * lumL),
                              (int) Math.min(255, 255 * lumL)));
        } else {
            float lumB = 1f + clipireBlocat * 0.6f;
            app.ui.textCentrat("BLOCAT", 0.5f, Y_JOACA, 0.042f,
                    Color.rgb((int) Math.min(255, 130 * lumB), 90, 90));
        }

        app.ui.textCentrat("INAPOI", 0.5f, Y_INAPOI, 0.028f,
                Color.rgb(150, 160, 190));
    }

    @Override
    public boolean atingere(float x, float y) {
        startTragereX = x;
        trage = false;

        int mod = Math.round(derulare);
        if (mod < 0) mod = 0;
        if (mod >= Setari.NR_MODURI) mod = Setari.NR_MODURI - 1;

        if (inRand(y, Y_JOACA, 0.055f)) {
            if (Setari.MOD_DISPONIBIL[mod]) {
                apasat = 0;
                stralucire = 1f;
                app.sunet.rotire();
                app.setari.setUltimulMod(selectat);
                app.jocNou(selectat, false);
            } else {
                clipireBlocat = 1f;
                app.sunet.mutare();
            }
            return true;
        }

        if (Setari.MOD_DISPONIBIL[mod] && inRand(y, Y_LIBER, 0.05f)) {
            apasat = 1;
            stralucire = 1f;
            app.sunet.rotire();
            app.setari.setUltimulMod(selectat);
            app.jocNou(selectat, true);
            return true;
        }

        if (inRand(y, Y_INAPOI, 0.05f)) {
            app.sunet.mutare();
            app.inapoi();
            return true;
        }

        if (inRand(y, Y_NUME, 0.24f)) {
            if (x < 0.30f) { schimba(-1); return true; }
            if (x > 0.70f) { schimba(1);  return true; }
        }
        return false;
    }

    @Override
    public boolean tragere(float x, float y, float dx, float dy) {
        if (trage) return true;
        float dif = x - startTragereX;
        if (Math.abs(dif) > 0.14f) {
            schimba(dif > 0 ? -1 : 1);
            trage = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean ridicare(float x, float y) {
        trage = false;
        return false;
    }

    private void schimba(int directie) {
        int nou = selectat + directie;
        if (nou < 0 || nou >= Setari.NR_MODURI) return;
        selectat = nou;
        derulareTinta = selectat;
        app.sunet.mutare();
    }

    @Override
    public boolean inapoi() {
        return false;
    }
}
