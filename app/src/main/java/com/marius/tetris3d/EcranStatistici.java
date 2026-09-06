package com.marius.tetris3d;

/**
 * Statisticile jucatorului: cate jocuri, linii, piese,
 * timp jucat, cel mai bun nivel, cate tetrisuri.
 * Jos, recordurile pe fiecare mod.
 */
public class EcranStatistici extends Ecran {

    private static final float Y_TITLU  = 0.07f;
    private static final float Y_INAPOI = 0.93f;
    private static final float Y_STERGE = 0.85f;

    // apasare lunga pe STERGE, ca sa nu se piarda din greseala
    private float tinutSterge = 0f;
    private boolean seTine = false;
    private float clipireStergere = 0f;

    public EcranStatistici(Aplicatie app) {
        super(app);
    }

    @Override
    public void laIntrare() {
        super.laIntrare();
        tinutSterge = 0f;
        seTine = false;
        clipireStergere = 0f;
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);

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
        float ap = aparitie();

        d.seteazaProiectie(48f, d.raport, 1f, 90f);
        d.seteazaCamera(0f, 0f, 26f, 0f, 0f, 0f);
        d.seteazaLumina(
                (float) Math.sin(timp * 0.45f) * 15f, 14f,
                (float) Math.cos(timp * 0.45f) * 15f + 12f);

        app.stele.deseneaza2(d);

        d.textCentrat("STATISTICI", 0f, ecranLaLume(Y_TITLU) + (1f - ap) * 5f,
                0.34f, 0.85f, 0.90f, 1.00f);

        // ---------- randurile de statistici ----------
        Setari s = app.setari;

        rand(d, 0, ap, "JOCURI",  String.valueOf(s.jocuriJucate()),  1.00f, 0.92f, 0.50f);
        rand(d, 1, ap, "LINII",   String.valueOf(s.liniiTotale()),   0.45f, 0.95f, 1.00f);
        rand(d, 2, ap, "PIESE",   String.valueOf(s.pieseTotale()),   0.70f, 0.80f, 1.00f);
        rand(d, 3, ap, "TETRIS",  String.valueOf(s.tetrisuri()),     1.00f, 0.55f, 0.35f);
        rand(d, 4, ap, "NIVEL",   String.valueOf(s.celMaiBunNivel()),0.85f, 0.75f, 1.00f);
        rand(d, 5, ap, "TIMP",    timpScris(s.timpTotalSecunde()),   0.60f, 1.00f, 0.70f);

        // ---------- recordurile pe moduri ----------
        float yRec = ecranLaLume(0.62f);
        d.textCentrat("RECORDURI", 0f, yRec + 1.4f, 0.20f, 0.55f, 0.62f, 0.80f);

        for (int m = 0; m < Setari.NR_MODURI; m++) {
            int col = m % 2;
            int linie = m / 2;

            float x = (col == 0) ? -6.2f : 2.2f;
            float y = yRec - 1.2f - linie * 2.3f;

            d.text(Setari.NUME_MODURI[m], x, y, 0.15f, 0.60f, 0.66f, 0.85f);
            d.text(String.valueOf(s.record(m)), x, y - 1.0f, 0.19f,
                    1.00f, 0.90f, 0.55f);
        }

        // ---------- sterge ----------
        float progres = Math.min(1f, tinutSterge / 1.4f);
        float rSterge = 0.75f + progres * 0.25f;
        float gSterge = 0.30f - progres * 0.20f;

        if (clipireStergere > 0f) {
            d.textCentrat("STERS", 0f, ecranLaLume(Y_STERGE), 0.24f,
                    0.40f, 1.00f, 0.55f);
        } else {
            d.textCentrat("TINE PENTRU STERGERE", 0f, ecranLaLume(Y_STERGE), 0.15f,
                    rSterge, gSterge, 0.30f);

            // bara de progres cat tii apasat
            if (progres > 0.02f) {
                int n = (int) (progres * 16);
                for (int i = 0; i < n; i++) {
                    d.cub(-3.6f + i * 0.48f, ecranLaLume(Y_STERGE) - 1.1f, 0f,
                            1.0f, 0.35f - progres * 0.2f, 0.25f, 0.95f, 0.20f);
                }
            }
        }

        d.textCentrat("INAPOI", 0f, ecranLaLume(Y_INAPOI), 0.24f,
                0.60f, 0.66f, 0.85f);
    }

    private void rand(Desenator d, int index, float ap,
                      String eticheta, String valoare,
                      float r, float g, float b) {

        float apLocal = ap - index * 0.07f;
        if (apLocal < 0f) apLocal = 0f;
        if (apLocal > 1f) apLocal = 1f;

        float y = ecranLaLume(0.17f + index * 0.068f);
        float x = -(1f - apLocal) * 14f;

        d.text(eticheta, x - 7.6f, y, 0.18f, 0.55f, 0.62f, 0.80f);

        float lat = d.latimeText(valoare, 0.24f);
        d.text(valoare, x + 7.4f - lat, y, 0.24f, r, g, b);
    }

    /** secunde -> "12H 34M" sau "34M" */
    private String timpScris(int secunde) {
        int ore = secunde / 3600;
        int minute = (secunde % 3600) / 60;
        if (ore > 0) return ore + "H " + minute + "M";
        return minute + "M";
    }

    private float ecranLaLume(float yEcran) {
        return 13f - yEcran * 26f;
    }

    // ---------- atingeri ----------

    @Override
    public boolean atingere(float x, float y) {

        if (inRand(y, Y_STERGE, 0.07f)) {
            seTine = true;
            return true;
        }

        if (inRand(y, Y_INAPOI, 0.07f)) {
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
