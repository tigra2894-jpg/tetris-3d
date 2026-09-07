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
    private boolean apasatJoaca = false;

    private static final float Y_TITLU  = 0.09f;
    private static final float Y_NUME   = 0.42f;
    private static final float Y_DESC1  = 0.48f;
    private static final float Y_DESC2  = 0.53f;
    private static final float Y_RECL   = 0.62f;
    private static final float Y_RECV   = 0.67f;
    private static final float Y_PUNCTE = 0.75f;
    private static final float Y_JOACA  = 0.85f;
    private static final float Y_INAPOI = 0.93f;

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
        apasatJoaca = false;
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);
        float dif = derulareTinta - derulare;
        derulare += dif * Math.min(1f, dt * 9f);
        if (Math.abs(dif) < 0.001f) derulare = derulareTinta;

        stralucire -= dt * 3f;
        if (stralucire < 0f) stralucire = 0f;
    }

    @Override
    public void deseneaza(Desenator d) {
        d.seteazaProiectie(48f, d.raport, 1f, 90f);
        d.seteazaCamera(0f, 0f, 26f, 0f, 0f, 0f);
        d.seteazaLumina(
                (float) Math.sin(timp * 0.6f) * 15f, 14f,
                (float) Math.cos(timp * 0.6f) * 15f + 12f);

        app.stele.deseneaza2(d);

        app.ui.textCentrat("MODURI", 0.5f, Y_TITLU, 0.075f,
                Color.rgb(220, 230, 255));

        int mod = Math.round(derulare);
        if (mod < 0) mod = 0;
        if (mod >= Setari.NR_MODURI) mod = Setari.NR_MODURI - 1;
        int[] cul = CUL_MOD[mod];

        app.ui.textCentrat(Setari.NUME_MODURI[mod], 0.5f, Y_NUME, 0.062f,
                Color.rgb(cul[0], cul[1], cul[2]));

        app.ui.textCentrat(DESCRIERI[mod][0], 0.5f, Y_DESC1, 0.028f,
                Color.rgb(190, 200, 220));
        app.ui.textCentrat(DESCRIERI[mod][1], 0.5f, Y_DESC2, 0.028f,
                Color.rgb(190, 200, 220));

        app.ui.textCentrat("RECORD", 0.5f, Y_RECL, 0.024f,
                Color.rgb(150, 160, 190));
        app.ui.textCentrat(String.valueOf(app.setari.record(mod)), 0.5f, Y_RECV, 0.042f,
                Color.rgb(255, 225, 130));

        // piesa rotitoare deasupra numelui
        float[][] culori = app.setari.culoriPiese();
        int[][] forma = Joc.formaPiesei(mod % 7, 0);
        float rotP = timp * 34f + mod * 60f;
        float[] culP = culori[mod % 7];
        for (int k = 0; k < 4; k++) {
            float cx = (forma[k][0] - 1.5f) * 0.95f;
            float cy = 5.6f + (forma[k][1] - 2.0f) * 0.95f;
            d.cubRotit(cx, cy, 0f, rotP, 0.4f, 1f, 0.3f,
                    culP[0], culP[1], culP[2], 1f, 0.50f);
        }

        // puncte de navigare
        float xPuncte = 0.5f - (Setari.NR_MODURI - 1) * 0.035f;
        for (int i = 0; i < Setari.NR_MODURI; i++) {
            boolean act = (i == mod);
            app.ui.textCentrat("*", xPuncte + i * 0.07f, Y_PUNCTE,
                    act ? 0.032f : 0.020f,
                    act ? Color.rgb(255, 255, 255) : Color.rgb(90, 95, 120));
        }

        float lumJ = apasatJoaca ? 1f + stralucire : 1f;
        app.ui.textCentrat("JOACA", 0.5f, Y_JOACA, 0.052f,
                Color.rgb((int) Math.min(255, 90 * lumJ),
                          (int) Math.min(255, 255 * lumJ),
                          (int) Math.min(255, 140 * lumJ)));

        app.ui.textCentrat("INAPOI", 0.5f, Y_INAPOI, 0.030f,
                Color.rgb(150, 160, 190));

        app.ui.textCentrat("< >", 0.5f, Y_DESC2 + 0.10f, 0.022f,
                Color.rgb(90, 95, 120));
    }

    @Override
    public boolean atingere(float x, float y) {
        startTragereX = x;
        trage = false;

        if (inRand(y, Y_JOACA, 0.06f)) {
            apasatJoaca = true;
            stralucire = 1f;
            app.sunet.rotire();
            app.setari.setUltimulMod(selectat);
            app.jocNou(selectat);
            return true;
        }

        if (inRand(y, Y_INAPOI, 0.05f)) {
            app.sunet.mutare();
            app.inapoi();
            return true;
        }

        if (inRand(y, Y_NUME, 0.30f)) {
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
