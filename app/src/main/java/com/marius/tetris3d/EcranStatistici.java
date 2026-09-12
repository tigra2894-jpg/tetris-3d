package com.marius.tetris3d;

import android.graphics.Color;

public class EcranStatistici extends Ecran {

    private static final float Y_TITLU  = 0.08f;

    private static final float Y_R1 = 0.20f;
    private static final float Y_R2 = 0.32f;
    private static final float Y_R3 = 0.44f;

    private static final float X_ST = 0.28f;
    private static final float X_DR = 0.72f;

    private static final float Y_REC_TITLU = 0.58f;
    private static final float Y_REC_PRIM  = 0.65f;

    private static final float Y_STERGE = 0.85f;
    private static final float Y_INAPOI = 0.94f;

    private float tinutSterge = 0f;
    private boolean seTine = false;
    private float clipireStergere = 0f;

    private Fundal fundal;

    public EcranStatistici(Aplicatie app) {
        super(app);
        fundal = new Fundal();
    }

    @Override
    public void laIntrare() {
        super.laIntrare();
        tinutSterge = 0f;
        seTine = false;
        clipireStergere = 0f;
        fundal.seteazaAccent(false);
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);
        fundal.actualizeaza(dt);

        if (seTine) {
            tinutSterge += dt;
            if (tinutSterge >= 1.4f) {
                app.setari.stergeStatistici();
                app.sunet.final_();
                clipireStergere = 1f;
                seTine = false;
                tinutSterge = 0f;
            }
        } else {
            tinutSterge -= dt * 2.5f;
            if (tinutSterge < 0f) tinutSterge = 0f;
        }

        clipireStergere -= dt * 1.6f;
        if (clipireStergere < 0f) clipireStergere = 0f;
    }

    @Override
    public void deseneaza(Desenator d) {
        d.seteazaProiectie(48f, d.raport, 1f, 90f);

        float leg = timp * 0.15f;
        d.seteazaCamera(
                (float) Math.sin(leg) * 0.6f,
                (float) Math.cos(leg * 0.8f) * 0.35f,
                26f, 0f, 0f, 0f);

        fundal.deseneazaCeata(d);
        app.stele.deseneaza2(d);
        fundal.deseneazaGrila(d, -11.0f, 14f);
        contururiLaterale(d);

        app.ui.textCentrat("STATISTICI", 0.5f, Y_TITLU, 0.062f,
                Color.rgb(220, 230, 255));

        Setari s = app.setari;

        celula("JOCURI", String.valueOf(s.jocuriJucate()),
                X_ST, Y_R1, Color.rgb(255, 225, 130));
        celula("LINII", String.valueOf(s.liniiTotale()),
                X_DR, Y_R1, Color.rgb(120, 220, 255));

        celula("PIESE", String.valueOf(s.pieseTotale()),
                X_ST, Y_R2, Color.rgb(180, 200, 255));
        celula("TETRIS", String.valueOf(s.tetrisuri()),
                X_DR, Y_R2, Color.rgb(255, 150, 90));

        celula("NIVEL MAX", String.valueOf(s.celMaiBunNivel()),
                X_ST, Y_R3, Color.rgb(210, 190, 255));
        celula("TIMP", timpScris(s.timpTotalSecunde()),
                X_DR, Y_R3, Color.rgb(150, 255, 170));

        app.ui.textCentrat("RECORDURI", 0.5f, Y_REC_TITLU, 0.026f,
                Color.rgb(150, 160, 190));

        for (int m = 0; m < Setari.NR_MODURI; m++) {
            float y = Y_REC_PRIM + m * 0.075f;
            app.ui.text(Setari.NUME_MODURI[m], 0.16f, y, 0.026f,
                    Color.rgb(160, 170, 200));
            app.ui.textDreapta(String.valueOf(s.record(m)), 0.84f, y, 0.034f,
                    Color.rgb(255, 220, 130));
        }

        float progres = Math.min(1f, tinutSterge / 1.4f);

        if (clipireStergere > 0f) {
            app.ui.textCentrat("STERS", 0.5f, Y_STERGE, 0.032f,
                    Color.rgb(100, 255, 140));
        } else {
            int rS = (int) (185 + progres * 70);
            int gS = (int) Math.max(35, 95 - progres * 60);
            app.ui.textCentrat("TINE APASAT PENTRU STERGERE", 0.5f, Y_STERGE, 0.019f,
                    Color.rgb(rS, gS, 65));

            if (progres > 0.02f) {
                app.ui.bara(0.28f, Y_STERGE + 0.022f, 0.44f, 0.012f, progres,
                        Color.argb(120, 60, 30, 30), Color.rgb(235, 75, 60));
            }
        }

        app.ui.textCentrat("INAPOI", 0.5f, Y_INAPOI, 0.030f,
                Color.rgb(150, 160, 190));
    }

    private void contururiLaterale(Desenator d) {
        float[] acc = fundal.accent();
        float puls = 0.70f + 0.30f * (float) Math.sin(timp * 1.3f);

        float st = -8.6f;
        float dr =  8.6f;

        for (float y = -11.0f; y <= 9.5f; y += 0.55f) {
            float stins = 1f - Math.abs(y + 1f) / 13f;
            if (stins < 0.10f) stins = 0.10f;

            float a = 0.24f * puls * stins;

            d.cub(st, y, -3.5f, acc[0], acc[1], acc[2], a, 0.11f);
            d.cub(dr, y, -3.5f, acc[0], acc[1], acc[2], a, 0.11f);
        }
    }

    private void celula(String eticheta, String valoare,
                        float xFrac, float yFrac, int culoare) {
        app.ui.textCentrat(eticheta, xFrac, yFrac, 0.019f,
                Color.rgb(140, 148, 172));
        app.ui.textCentrat(valoare, xFrac, yFrac + 0.048f, 0.038f, culoare);
    }

    private String timpScris(int secunde) {
        int ore = secunde / 3600;
        int minute = (secunde % 3600) / 60;
        if (ore > 0) return ore + "H " + minute + "M";
        return minute + "M";
    }

    @Override
    public boolean atingere(float x, float y) {

        if (inRand(y, Y_STERGE, 0.05f)) {
            seTine = true;
            return true;
        }

        if (inRand(y, Y_INAPOI, 0.05f)) {
            app.sunet.mutare();
            app.inapoi();
            return true;
        }
        return false;
    }

    @Override
    public boolean ridicare(float x, float y) {
        seTine = false;
        return false;
    }

    @Override
    public boolean inapoi() {
        return false;
    }
}
