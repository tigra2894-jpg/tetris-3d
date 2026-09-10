package com.marius.tetris3d;

import android.graphics.Color;

public class EcranSetari extends Ecran {

    private static final float Y_TITLU = 0.09f;

    private static final float Y_SUNET_L = 0.24f;
    private static final float Y_SUNET_V = 0.31f;

    private static final float Y_VITEZA_L = 0.46f;
    private static final float Y_VITEZA_V = 0.53f;

    private static final float Y_TEMA_L = 0.66f;
    private static final float Y_TEMA_V = 0.72f;

    private static final float Y_INAPOI = 0.93f;

    private int atins = -1;
    private float stralucire = 0f;

    private Fundal fundal;

    public EcranSetari(Aplicatie app) {
        super(app);
        fundal = new Fundal();
    }

    @Override
    public void laIntrare() {
        super.laIntrare();
        atins = -1;
        stralucire = 0f;
        fundal.seteazaAccent(false);
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);
        fundal.actualizeaza(dt);

        stralucire -= dt * 3f;
        if (stralucire < 0f) {
            stralucire = 0f;
            atins = -1;
        }
    }

    @Override
    public void deseneaza(Desenator d) {
        d.seteazaProiectie(48f, d.raport, 1f, 90f);
        d.seteazaCamera(0f, 0f, 26f, 0f, 0f, 0f);
        d.seteazaLumina(
                (float) Math.sin(timp * 0.5f) * 10f, 18f, 18f);

        fundal.deseneazaCeata(d);
        app.stele.deseneaza2(d);

        app.ui.textCentrat("SETARI", 0.5f, Y_TITLU, 0.072f,
                Color.rgb(220, 230, 255));

        // ---------- sunet ----------
        app.ui.textCentrat("SUNET", 0.5f, Y_SUNET_L, 0.026f,
                Color.rgb(150, 160, 190));

        boolean sunetPornit = app.setari.sunetPornit();
        float lumS = (atins == 0) ? 1f + stralucire * 0.7f : 1f;

        if (sunetPornit) {
            app.ui.textCentrat("PORNIT", 0.5f, Y_SUNET_V, 0.046f,
                    Color.rgb((int) Math.min(255, 100 * lumS),
                              (int) Math.min(255, 255 * lumS),
                              (int) Math.min(255, 150 * lumS)));
        } else {
            app.ui.textCentrat("OPRIT", 0.5f, Y_SUNET_V, 0.046f,
                    Color.rgb((int) Math.min(255, 230 * lumS),
                              (int) Math.min(255, 95 * lumS),
                              (int) Math.min(255, 95 * lumS)));
        }

        // ---------- viteza ----------
        app.ui.textCentrat("VITEZA DE START", 0.5f, Y_VITEZA_L, 0.026f,
                Color.rgb(150, 160, 190));

        int v = app.setari.vitezaStart();

        app.ui.textCentrat("<", 0.18f, Y_VITEZA_V, 0.046f, Color.rgb(150, 190, 255));
        app.ui.textCentrat(">", 0.82f, Y_VITEZA_V, 0.046f, Color.rgb(150, 190, 255));

        // cinci trepte, cuburi 3D
        float[][] culori = app.setari.culoriPiese();
        for (int i = 1; i <= 5; i++) {
            boolean plina = i <= v;
            float x = (i - 3f) * 1.5f;
            float y = 0.2f;
            float s = plina ? 0.52f : 0.34f;

            float[] cul = plina ? culori[(i - 1) % culori.length]
                                : new float[]{0.22f, 0.24f, 0.32f};

            float rotc = plina ? timp * 30f + i * 40f : 0f;
            d.cubRotit(x, y, 0f, rotc, 0.4f, 1f, 0.3f,
                    cul[0], cul[1], cul[2], plina ? 1f : 0.55f, s);
        }

        // ---------- tema ----------
        app.ui.textCentrat("TEMA", 0.5f, Y_TEMA_L, 0.026f,
                Color.rgb(150, 160, 190));

        int t = app.setari.tema();
        app.ui.textCentrat(Setari.NUME_TEME[t], 0.5f, Y_TEMA_V, 0.050f,
                Color.rgb(220, 190, 255));

        for (int i = 0; i < 7; i++) {
            float rotP = timp * 26f + i * 40f;
            d.cubRotit((i - 3f) * 1.4f, -6.2f, 0f,
                    rotP, 0.4f, 1f, 0.3f,
                    culori[i][0], culori[i][1], culori[i][2],
                    1f, 0.50f);
        }

        app.ui.textCentrat("INAPOI", 0.5f, Y_INAPOI, 0.030f,
                Color.rgb(150, 160, 190));
    }

    @Override
    public boolean atingere(float x, float y) {

        if (inRand(y, (Y_SUNET_L + Y_SUNET_V) / 2f, 0.10f)) {
            boolean nou = !app.setari.sunetPornit();
            app.setari.setSunet(nou);
            app.sunet.setPornit(nou);
            if (nou) app.sunet.nivel();
            marcheaza(0);
            return true;
        }

        if (inRand(y, (Y_VITEZA_L + Y_VITEZA_V) / 2f, 0.10f)) {
            int v = app.setari.vitezaStart();
            if (x < 0.35f) {
                app.setari.setVitezaStart(v - 1);
                marcheaza(1);
                app.sunet.mutare();
            } else if (x > 0.65f) {
                app.setari.setVitezaStart(v + 1);
                marcheaza(2);
                app.sunet.mutare();
            }
            return true;
        }

        if (inRand(y, (Y_TEMA_L + Y_TEMA_V) / 2f, 0.10f)) {
            int t = (app.setari.tema() + 1) % Setari.NR_TEME;
            app.setari.setTema(t);
            marcheaza(3);
            app.sunet.rotire();
            return true;
        }

        if (inRand(y, Y_INAPOI, 0.05f)) {
            app.sunet.mutare();
            app.inapoi();
            return true;
        }
        return false;
    }

    private void marcheaza(int index) {
        atins = index;
        stralucire = 1f;
    }

    @Override
    public boolean inapoi() {
        return false;
    }
}
