package com.marius.tetris3d;

/**
 * Alegerea modului de joc.
 * Cele 6 moduri, fiecare cu recordul lui si o descriere scurta.
 */
public class EcranModuri extends Ecran {

    // descrierea fiecarui mod, doua randuri scurte
    private static final String[][] DESCRIERI = {
        {"TETRIS", "OBISNUIT"},
        {"TABLA E UN", "TURN ROTITOR"},
        {"JOSUL SE", "SCHIMBA MEREU"},
        {"CUBURI DE", "STICLA SPARTA"},
        {"TABLA", "RESPIRA"},
        {"CONSTRUIESTI", "IN SPATIU"}
    };

    // culoarea fiecarui mod
    private static final float[][] CULORI_MOD = {
        {0.40f, 1.00f, 0.55f},
        {1.00f, 0.75f, 0.25f},
        {0.60f, 0.55f, 1.00f},
        {0.45f, 0.95f, 1.00f},
        {1.00f, 0.40f, 0.55f},
        {0.85f, 0.60f, 1.00f}
    };

    private int selectat = 0;
    private float derulare = 0f;        // pozitia curenta, in moduri
    private float derulareTinta = 0f;

    private float stralucire = 0f;
    private boolean apasatJoaca = false;

    // zonele de pe ecran
    private static final float Y_TITLU  = 0.10f;
    private static final float Y_LISTA  = 0.46f;   // centrul cardului
    private static final float Y_JOACA  = 0.83f;
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

        // alunecare lina spre modul selectat
        float dif = derulareTinta - derulare;
        derulare += dif * Math.min(1f, dt * 9f);
        if (Math.abs(dif) < 0.001f) derulare = derulareTinta;

        stralucire -= dt * 3f;
        if (stralucire < 0f) stralucire = 0f;
    }

    @Override
    public void deseneaza(Desenator d) {
        float ap = aparitie();

        d.seteazaProiectie(48f, d.raport, 1f, 90f);
        d.seteazaCamera(0f, 0f, 26f, 0f, 0f, 0f);
        d.seteazaLumina(
                (float) Math.sin(timp * 0.6f) * 15f, 14f,
                (float) Math.cos(timp * 0.6f) * 15f + 12f);

        app.stele.deseneaza2(d);

        // ---------- titlu ----------
        d.textCentrat("MODURI", 0f, ecranLaLume(Y_TITLU) + (1f - ap) * 5f,
                0.42f, 0.85f, 0.90f, 1.00f);

        // ---------- carduri ----------
        for (int i = 0; i < Setari.NR_MODURI; i++) {
            float dist = i - derulare;
            if (Math.abs(dist) > 1.6f) continue;
            deseneazaCard(d, i, dist, ap);
        }

        // ---------- punctele de jos, arata unde esti ----------
        float yPuncte = ecranLaLume(0.72f);
        for (int i = 0; i < Setari.NR_MODURI; i++) {
            float marime = (i == selectat) ? 0.22f : 0.13f;
            float lum = (i == selectat) ? 1.0f : 0.35f;
            d.cub((i - 2.5f) * 1.1f, yPuncte, 0f,
                    0.8f * lum, 0.85f * lum, 1.0f * lum, 0.9f, marime);
        }

        // ---------- butoane ----------
        float lumJoaca = apasatJoaca ? 1f + stralucire : 1f;
        d.textCentrat("JOACA", 0f, ecranLaLume(Y_JOACA), 0.44f,
                Math.min(1f, 0.40f * lumJoaca),
                Math.min(1f, 1.00f * lumJoaca),
                Math.min(1f, 0.55f * lumJoaca));

        d.textCentrat("INAPOI", 0f, ecranLaLume(Y_INAPOI), 0.24f,
                0.60f, 0.66f, 0.85f);
    }

    private void deseneazaCard(Desenator d, int mod, float dist, float ap) {
        float x = dist * 13f;
        float apropiere = 1f - Math.min(1f, Math.abs(dist));
        float z = -6f + apropiere * 6f;
        float alfa = 0.25f + apropiere * 0.75f;
        float scara = 0.55f + apropiere * 0.45f;

        float[] cul = CULORI_MOD[mod];
        float yBaza = ecranLaLume(Y_LISTA);

        // numele modului
        d.textCentrat(Setari.NUME_MODURI[mod], x, yBaza + 3.4f, z,
                0.50f * scara, cul[0], cul[1], cul[2], alfa);

        // descrierea, doua randuri
        d.textCentrat(DESCRIERI[mod][0], x, yBaza + 0.6f, z,
                0.20f * scara, 0.70f, 0.75f, 0.90f, alfa * 0.9f);
        d.textCentrat(DESCRIERI[mod][1], x, yBaza - 0.9f, z,
                0.20f * scara, 0.70f, 0.75f, 0.90f, alfa * 0.9f);

        // recordul
        int rec = app.setari.record(mod);
        d.textCentrat("RECORD", x, yBaza - 3.4f, z,
                0.17f * scara, 0.55f, 0.60f, 0.78f, alfa * 0.8f);
        d.textCentrat(String.valueOf(rec), x, yBaza - 5.0f, z,
                0.30f * scara, 1.00f, 0.92f, 0.50f, alfa);

        // o piesa care se roteste, deasupra numelui
        float[][] culori = app.setari.culoriPiese();
        int[][] forma = Joc.formaPiesei(mod % 7, 0);
        float rotP = timp * 34f + mod * 60f;
        float[] culP = culori[mod % 7];

        for (int k = 0; k < 4; k++) {
            float cx = x + (forma[k][0] - 1.5f) * 0.85f;
            float cy = yBaza + 7.2f + (forma[k][1] - 2.0f) * 0.85f;
            d.cubRotit(cx, cy, z,
                    rotP, 0.4f, 1f, 0.3f,
                    culP[0], culP[1], culP[2],
                    alfa, 0.42f * scara);
        }
    }

    private float ecranLaLume(float yEcran) {
        return 13f - yEcran * 26f;
    }

    // ---------- atingeri ----------

    @Override
    public boolean atingere(float x, float y) {
        startTragereX = x;
        trage = false;

        if (inRand(y, Y_JOACA, 0.08f)) {
            apasatJoaca = true;
            stralucire = 1f;
            app.sunet.rotire();
            app.setari.setUltimulMod(selectat);
            app.jocNou(selectat);
            return true;
        }

        if (inRand(y, Y_INAPOI, 0.07f)) {
            app.sunet.mutare();
            app.inapoi();
            return true;
        }

        // atingere in stanga sau dreapta cardului: schimba modul
        if (inRand(y, Y_LISTA, 0.42f)) {
            if (x < 0.28f) { schimba(-1); return true; }
            if (x > 0.72f) { schimba(1);  return true; }
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
        return false;   // lasa Aplicatia sa se intoarca la meniu
    }
}
