package com.marius.tetris3d;

/**
 * Setari: sunet pornit/oprit, viteza de start, tema de culori.
 * Modificarile se salveaza imediat.
 */
public class EcranSetari extends Ecran {

    private static final float Y_TITLU   = 0.09f;

    private static final float Y_SUNET   = 0.26f;
    private static final float Y_VITEZA  = 0.47f;
    private static final float Y_TEMA    = 0.68f;

    private static final float Y_INAPOI  = 0.92f;

    // ce rand a fost atins ultima data, pentru stralucire
    private int atins = -1;
    private float stralucire = 0f;

    public EcranSetari(Aplicatie app) {
        super(app);
    }

    @Override
    public void laIntrare() {
        super.laIntrare();
        atins = -1;
        stralucire = 0f;
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);
        stralucire -= dt * 3f;
        if (stralucire < 0f) {
            stralucire = 0f;
            atins = -1;
        }
    }

    @Override
    public void deseneaza(Desenator d) {
        float ap = aparitie();

        d.seteazaProiectie(48f, d.raport, 1f, 90f);
        d.seteazaCamera(0f, 0f, 26f, 0f, 0f, 0f);
        d.seteazaLumina(
                (float) Math.sin(timp * 0.5f) * 15f, 14f,
                (float) Math.cos(timp * 0.5f) * 15f + 12f);

        app.stele.deseneaza2(d);

        d.textCentrat("SETARI", 0f, ecranLaLume(Y_TITLU) + (1f - ap) * 5f,
                0.42f, 0.85f, 0.90f, 1.00f);

        deseneazaSunet(d, ap);
        deseneazaViteza(d, ap);
        deseneazaTema(d, ap);

        d.textCentrat("INAPOI", 0f, ecranLaLume(Y_INAPOI), 0.24f,
                0.60f, 0.66f, 0.85f);
    }

    // ---------- sunet ----------
    private void deseneazaSunet(Desenator d, float ap) {
        float y = ecranLaLume(Y_SUNET);
        float x = -(1f - ap) * 12f;

        d.textCentrat("SUNET", x, y + 2.6f, 0.24f, 0.60f, 0.66f, 0.85f);

        boolean pornit = app.setari.sunetPornit();
        float lum = (atins == 0) ? 1f + stralucire * 0.8f : 1f;

        if (pornit) {
            d.textCentrat("PORNIT", x, y, 0.36f,
                    Math.min(1f, 0.40f * lum), Math.min(1f, 1.00f * lum), Math.min(1f, 0.55f * lum));
        } else {
            d.textCentrat("OPRIT", x, y, 0.36f,
                    Math.min(1f, 0.85f * lum), Math.min(1f, 0.35f * lum), Math.min(1f, 0.35f * lum));
        }

        // bare de volum, animate cand sunetul e pornit
        for (int i = 0; i < 5; i++) {
            float h = pornit
                    ? 0.25f + 0.55f * (0.5f + 0.5f * (float) Math.sin(timp * 5f + i * 0.9f))
                    : 0.20f;
            float lumB = pornit ? 1f : 0.25f;
            d.cubIntins(x + 5.6f + i * 0.75f, y - 0.4f + h * 0.5f, 0f,
                    0.24f, h, 0.24f,
                    0.45f * lumB, 0.95f * lumB, 0.80f * lumB, 0.9f);
        }
    }

    // ---------- viteza ----------
    private void deseneazaViteza(Desenator d, float ap) {
        float y = ecranLaLume(Y_VITEZA);
        float x = -(1f - ap) * 12f;

        d.textCentrat("VITEZA DE START", x, y + 2.6f, 0.24f, 0.60f, 0.66f, 0.85f);

        int v = app.setari.vitezaStart();

        // sagetile
        float lumS = (atins == 1) ? 1f + stralucire * 0.8f : 1f;
        float lumD = (atins == 2) ? 1f + stralucire * 0.8f : 1f;

        d.textCentrat("V", x - 6.2f, y, 0.32f,
                Math.min(1f, 0.55f * lumS), Math.min(1f, 0.75f * lumS), Math.min(1f, 1.00f * lumS));
        d.textCentrat("V", x + 6.2f, y, 0.32f,
                Math.min(1f, 0.55f * lumD), Math.min(1f, 0.75f * lumD), Math.min(1f, 1.00f * lumD));

        // cinci trepte
        for (int i = 1; i <= 5; i++) {
            boolean plina = i <= v;
            float inaltime = 0.30f + i * 0.22f;
            float r = plina ? 1.00f : 0.20f;
            float g = plina ? 0.90f - i * 0.13f : 0.22f;
            float b = plina ? 0.35f : 0.32f;

            d.cubIntins(x + (i - 3) * 1.5f, y - 0.6f + inaltime * 0.5f, 0f,
                    0.34f, inaltime, 0.34f,
                    r, g, b, plina ? 1.0f : 0.55f);
        }
    }

    // ---------- tema ----------
    private void deseneazaTema(Desenator d, float ap) {
        float y = ecranLaLume(Y_TEMA);
        float x = -(1f - ap) * 12f;

        d.textCentrat("TEMA", x, y + 3.0f, 0.24f, 0.60f, 0.66f, 0.85f);

        int t = app.setari.tema();
        float lum = (atins == 3) ? 1f + stralucire * 0.8f : 1f;

        d.textCentrat(Setari.NUME_TEME[t], x, y + 0.9f, 0.34f,
                Math.min(1f, 0.90f * lum), Math.min(1f, 0.80f * lum), Math.min(1f, 1.00f * lum));

        // proba de culori: cele 7 culori ale temei
        float[][] culori = app.setari.culoriPiese();
        for (int i = 0; i < 7; i++) {
            float rotP = timp * 26f + i * 40f;
            d.cubRotit(x + (i - 3f) * 1.5f, y - 2.4f, 0f,
                    rotP, 0.4f, 1f, 0.3f,
                    culori[i][0], culori[i][1], culori[i][2],
                    1f, 0.52f);
        }
    }

    private float ecranLaLume(float yEcran) {
        return 13f - yEcran * 26f;
    }

    // ---------- atingeri ----------

    @Override
    public boolean atingere(float x, float y) {

        // sunet: oriunde pe randul lui
        if (inRand(y, Y_SUNET, 0.11f)) {
            boolean nou = !app.setari.sunetPornit();
            app.setari.setSunet(nou);
            app.sunet.setPornit(nou);
            if (nou) app.sunet.nivel();
            marcheaza(0);
            return true;
        }

        // viteza: stanga scade, dreapta creste
        if (inRand(y, Y_VITEZA, 0.11f)) {
            int v = app.setari.vitezaStart();
            if (x < 0.35f) {
                app.setari.setVitezaStart(v - 1);
                marcheaza(1);
                app.sunet.mutare();
                return true;
            }
            if (x > 0.65f) {
                app.setari.setVitezaStart(v + 1);
                marcheaza(2);
                app.sunet.mutare();
                return true;
            }
            return true;
        }

        // tema: atingere oriunde trece la urmatoarea
        if (inRand(y, Y_TEMA, 0.13f)) {
            int t = (app.setari.tema() + 1) % Setari.NR_TEME;
            app.setari.setTema(t);
            marcheaza(3);
            app.sunet.rotire();
            return true;
        }

        if (inRand(y, Y_INAPOI, 0.07f)) {
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
