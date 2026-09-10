package com.marius.tetris3d;

import android.graphics.Color;

/**
 * Modul TURN: o baza circulara pe care o invarti cu degetul.
 * Piesa cade in centrul ecranului, pe pozitia din fata.
 */
public class EcranTurn extends Ecran {

    public static final int STARE_JOC   = 0;
    public static final int STARE_PAUZA = 1;
    public static final int STARE_FINAL = 2;

    private int stare = STARE_JOC;
    private boolean modLiber = false;

    public JocTurn joc;
    private Particule particule;
    private float[][] culori;

    private float rotatieLumina = 0f;
    private float timpJucat = 0f;
    private int secundeRaportate = 0;

    /** raza cercului de baza, in unitati de lume */
    private static final float RAZA = 3.4f;
    /** inaltimea unui nivel */
    private static final float PAS_NIVEL = 1.0f;

    private float offY;

    private float startX, startY;
    private float ultimX;
    private long startTimp;
    private boolean gestFacut = false;
    private static final float PRAG = 0.05f;
    private static final float GRADE_PE_ECRAN = 360f;

    private static final float Y_P_TITLU    = 0.30f;
    private static final float Y_P_CONTINUA = 0.50f;
    private static final float Y_P_REINCEPE = 0.60f;
    private static final float Y_P_MENIU    = 0.70f;

    private static final float Y_F_TITLU   = 0.22f;
    private static final float Y_F_SCORL   = 0.36f;
    private static final float Y_F_SCORV   = 0.42f;
    private static final float Y_F_RECL    = 0.52f;
    private static final float Y_F_RECV    = 0.58f;
    private static final float Y_F_DIN_NOU = 0.72f;
    private static final float Y_F_MENIU   = 0.82f;

    public EcranTurn(Aplicatie app) {
        super(app);
        particule = new Particule();
        joc = new JocTurn();
        joc.particule = particule;
        joc.sunet = app.sunet;
    }

    public void pregateste(boolean liber) {
        modLiber = liber;
        joc.vitezaInitiala = app.setari.vitezaInitiala();
        joc.record = app.setari.record(Setari.MOD_TURN);
        joc.culori = app.setari.culoriPiese();
        joc.jocNou();
        stare = STARE_JOC;
        timpJucat = 0f;
        secundeRaportate = 0;
        app.setari.adaugaJoc();
    }

    @Override
    public void laIntrare() {
        super.laIntrare();
        culori = app.setari.culoriPiese();
    }

    @Override
    public void laIesire() {
        salveaza();
    }

    public void laPauzaAplicatie() {
        if (stare == STARE_JOC) stare = STARE_PAUZA;
        salveaza();
    }

    private void salveaza() {
        app.setari.setRecord(Setari.MOD_TURN, joc.scor);
        app.setari.raporteazaNivel(joc.nivel);
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);

        rotatieLumina += dt * 6f;
        particule.actualizeaza(dt);

        if (stare == STARE_JOC) {
            int ineleInainte = joc.inele;
            int pieseInainte = joc.pieseAsezate;

            joc.actualizeaza(dt);

            if (joc.inele > ineleInainte) {
                app.setari.adaugaLinii(joc.inele - ineleInainte);
            }
            if (joc.pieseAsezate > pieseInainte) {
                app.setari.adaugaPiesa();
            }

            timpJucat += dt;
            int sec = (int) timpJucat;
            if (sec > secundeRaportate) {
                app.setari.adaugaTimp(sec - secundeRaportate);
                secundeRaportate = sec;
            }

            if (joc.terminat) {
                stare = STARE_FINAL;
                salveaza();
            }
        } else {
            joc.stingeEfecte(dt);
        }
    }

    @Override
    public void deseneaza(Desenator d) {
        d.seteazaProiectie(46f, d.raport, 1f, 90f);

        float scut = joc.cutremurGlobal;
        float zgX = (float) Math.sin(timp * 47f) * scut * 0.30f;
        float zgY = (float) Math.cos(timp * 39f) * scut * 0.24f;

        // camera priveste turnul putin de sus, ca sa se vada cercul
        d.seteazaCamera(zgX, 3.5f + zgY, 20f, 0f, 1.5f, 0f);

        d.seteazaLumina(
                (float) Math.sin(Math.toRadians(rotatieLumina)) * 12f,
                18f, 16f);

        offY = -5.5f;

        app.stele.deseneaza2(d);

        cercBaza(d);
        turnul(d);

        if (stare == STARE_JOC) {
            fantoma(d);
            piesaCurenta(d);
        }

        particule.deseneaza2(d, 0f, offY);

        interfata();

        if (stare == STARE_PAUZA) ecranPauza();
        if (stare == STARE_FINAL) ecranFinal();
    }

    private float unghiPozitie(float pozitie) {
        return pozitie * JocTurn.PAS_GRADE + joc.unghiBaza;
    }

    private float[] loc(float pozitie, float nivel) {
        float u = (float) Math.toRadians(unghiPozitie(pozitie));
        float x = (float) Math.sin(u) * RAZA;
        float z = (float) Math.cos(u) * RAZA;
        float y = offY + nivel * PAS_NIVEL;
        return new float[]{x, y, z};
    }

    /** 1 = in fata camerei, 0 = in spate */
    private float vizib(float pozitie) {
        float u = (float) Math.toRadians(unghiPozitie(pozitie));
        return Math.max(0f, (float) Math.cos(u));
    }

    private void cercBaza(Desenator d) {
        boolean activ = (stare == STARE_JOC);
        float ap = activ ? joc.apropiere() : 0f;

        int[] pozPiesa = activ ? joc.pozitiiPiesa() : new int[0];

        for (int p = 0; p < JocTurn.POZITII; p++) {
            float viz = vizib(p);

            boolean tinta = false;
            for (int pp : pozPiesa) if (pp == p) tinta = true;

            float r = 0.16f, g = 0.20f, b = 0.34f;
            float a = 0.45f + viz * 0.45f;

            if (tinta) {
                float caldura = ap * ap;
                r = 0.16f + caldura * 0.82f;
                g = 0.20f + caldura * 0.28f;
                b = 0.34f - caldura * 0.28f;
                a = Math.min(1f, a + caldura * 0.3f);
            }

            float[] pz = loc(p, -1f);
            d.cubRotit(pz[0], pz[1], pz[2],
                    -unghiPozitie(p), 0f, 1f, 0f,
                    r, g, b, a, 0.92f);
        }
    }

    private void turnul(Desenator d) {
        boolean activ = (stare == STARE_JOC);
        float ap = activ ? joc.apropiere() : 0f;
        int nivAteriz = activ ? joc.nivelAterizare() : -1;
        int[] pozPiesa = activ ? joc.pozitiiPiesa() : new int[0];

        for (int n = 0; n < JocTurn.NIVELE; n++) {
            for (int p = 0; p < JocTurn.POZITII; p++) {
                int val = joc.turn[n][p];
                if (val == 0) continue;

                float viz = vizib(p);
                float[] cul = culori[(val - 1) % culori.length];

                boolean subPiesa = false;
                for (int pp : pozPiesa) if (pp == p && n <= nivAteriz) subPiesa = true;

                float trem = joc.tremurNivel[n] + joc.cutremurGlobal * 0.5f;
                if (subPiesa) trem += ap * 0.4f;

                float dy = (float) Math.cos(timp * 47f + n * 1.9f + p) * trem * 0.06f;

                float lum = (0.32f + viz * 0.68f)
                        * (1f + (subPiesa ? ap * 0.35f : 0f) + trem * 0.25f);

                float[] pz = loc(p, n);
                d.cubRotit(pz[0], pz[1] + dy, pz[2],
                        -unghiPozitie(p), 0f, 1f, 0f,
                        Math.min(1f, cul[0] * lum),
                        Math.min(1f, cul[1] * lum),
                        Math.min(1f, cul[2] * lum),
                        0.35f + viz * 0.65f, 0.94f);
            }
        }
    }

    private void fantoma(Desenator d) {
        int niv = joc.nivelAterizare();
        int[] poz = joc.pozitiiPiesa();
        float[] cul = culori[joc.tipCurent % culori.length];
        float a = 0.18f + 0.14f * puls(2.8f);

        for (int p : poz) {
            if (p < 0) continue;
            float[] pz = loc(p, niv);
            d.cubRotit(pz[0], pz[1], pz[2],
                    -unghiPozitie(p), 0f, 1f, 0f,
                    cul[0], cul[1], cul[2], a, 0.90f);
        }
    }

    private void piesaCurenta(Desenator d) {
        int[] poz = joc.pozitiiPiesa();
        float[] cul = culori[joc.tipCurent % culori.length];

        for (int p : poz) {
            if (p < 0) continue;
            float[] pz = loc(p, joc.inaltimePiesa);
            d.cubRotit(pz[0], pz[1], pz[2],
                    -unghiPozitie(p), 0f, 1f, 0f,
                    cul[0], cul[1], cul[2], 1f, 0.96f);
        }
    }

    private void interfata() {
        app.ui.text("SCOR", 0.06f, 0.06f, 0.024f, Color.rgb(150, 160, 190));
        app.ui.text(String.valueOf(joc.scor), 0.06f, 0.115f, 0.044f, Color.rgb(255, 225, 130));

        app.ui.text("RECORD", 0.06f, 0.17f, 0.020f, Color.rgb(150, 160, 190));
        app.ui.text(String.valueOf(joc.record), 0.06f, 0.205f, 0.028f, Color.rgb(200, 190, 230));

        app.ui.textDreapta("NIVEL", 0.94f, 0.06f, 0.024f, Color.rgb(150, 160, 190));
        app.ui.textDreapta(String.valueOf(joc.nivel), 0.94f, 0.115f, 0.044f, Color.rgb(120, 220, 255));

        app.ui.textDreapta("INELE", 0.94f, 0.17f, 0.020f, Color.rgb(150, 160, 190));
        app.ui.textDreapta(String.valueOf(joc.inele), 0.94f, 0.205f, 0.028f, Color.rgb(180, 200, 255));

        if (stare == STARE_JOC) {
            app.ui.textCentrat("| |", 0.5f, 0.05f, 0.026f, Color.rgb(190, 200, 230));
            app.ui.textCentrat("TRAGE CA SA INVARTI BAZA", 0.5f, 0.955f, 0.015f,
                    Color.rgb(120, 128, 152));
        }
    }

    private void ecranPauza() {
        app.ui.panou(0.5f - 0.30f, Y_P_TITLU - 0.05f, 0.60f, 0.50f,
                Color.argb(150, 10, 12, 22), 0.03f);

        app.ui.textCentrat("PAUZA", 0.5f, Y_P_TITLU, 0.060f, Color.rgb(255, 225, 110));

        buton("CONTINUA", Y_P_CONTINUA, 0.040f, Color.rgb(100, 255, 140));
        buton("REINCEPE", Y_P_REINCEPE, 0.034f, Color.rgb(140, 205, 255));
        buton("MENIU",    Y_P_MENIU,    0.034f, Color.rgb(180, 190, 230));
    }

    private void ecranFinal() {
        float clip = 0.75f + 0.25f * puls(3f);

        app.ui.panou(0.5f - 0.34f, Y_F_TITLU - 0.06f, 0.68f, 0.68f,
                Color.argb(160, 10, 12, 22), 0.03f);

        app.ui.textCentrat("FINAL", 0.5f, Y_F_TITLU, 0.060f,
                Color.rgb((int) (255 * clip), 90, 90));

        app.ui.textCentrat("SCOR", 0.5f, Y_F_SCORL, 0.022f, Color.rgb(150, 160, 190));
        app.ui.textCentrat(String.valueOf(joc.scor), 0.5f, Y_F_SCORV, 0.052f,
                Color.rgb(255, 225, 130));

        boolean recordNou = joc.scor >= joc.record && joc.scor > 0;
        if (recordNou) {
            float p = 0.6f + 0.4f * puls(5f);
            app.ui.textCentrat("RECORD NOU", 0.5f, Y_F_RECL + 0.02f, 0.028f,
                    Color.rgb(255, (int) (215 * p), (int) (60 * p)));
        } else {
            app.ui.textCentrat("RECORD", 0.5f, Y_F_RECL, 0.020f, Color.rgb(150, 160, 190));
            app.ui.textCentrat(String.valueOf(joc.record), 0.5f, Y_F_RECV, 0.028f,
                    Color.rgb(200, 190, 230));
        }

        buton("DIN NOU", Y_F_DIN_NOU, 0.040f, Color.rgb(100, 255, 140));
        buton("MENIU",   Y_F_MENIU,   0.034f, Color.rgb(180, 190, 230));
    }

    private void buton(String s, float yFrac, float marime, int culoare) {
        app.ui.textCentrat(s, 0.5f, yFrac, marime, culoare);
    }

    @Override
    public boolean atingere(float x, float y) {

        if (stare == STARE_PAUZA) {
            if (inRand(y, Y_P_CONTINUA, 0.06f)) {
                stare = STARE_JOC;
                app.sunet.rotire();
            } else if (inRand(y, Y_P_REINCEPE, 0.06f)) {
                pregateste(modLiber);
                app.sunet.nivel();
            } else if (inRand(y, Y_P_MENIU, 0.06f)) {
                salveaza();
                app.sunet.mutare();
                app.meniu();
            }
            return true;
        }

        if (stare == STARE_FINAL) {
            if (inRand(y, Y_F_DIN_NOU, 0.06f)) {
                pregateste(modLiber);
                app.sunet.nivel();
            } else if (inRand(y, Y_F_MENIU, 0.06f)) {
                app.sunet.mutare();
                app.meniu();
            }
            return true;
        }

        if (y < 0.11f && x > 0.38f && x < 0.62f) {
            stare = STARE_PAUZA;
            app.sunet.mutare();
            return true;
        }

        startX = x;
        startY = y;
        ultimX = x;
        startTimp = System.currentTimeMillis();
        gestFacut = false;
        joc.degetJos();
        return true;
    }

    @Override
    public boolean tragere(float x, float y, float dx, float dy) {
        if (stare != STARE_JOC) return true;

        float difTotalX = x - startX;
        float difTotalY = y - startY;

        if (difTotalY > PRAG * 1.8f && Math.abs(difTotalY) > Math.abs(difTotalX) * 1.6f) {
            joc.coboaraRapid();
            startY = y;
            gestFacut = true;
            ultimX = x;
            return true;
        }

        float deltaX = x - ultimX;
        if (Math.abs(deltaX) > 0.0005f) {
            joc.rotesteContinuu(-deltaX * GRADE_PE_ECRAN);
            ultimX = x;
            if (Math.abs(difTotalX) > 0.03f) gestFacut = true;
        }
        return true;
    }

    @Override
    public boolean ridicare(float x, float y) {
        joc.degetSus();

        if (stare != STARE_JOC) return true;

        float dx = Math.abs(x - startX);
        float dy = startY - y;

        if (dy > PRAG * 2.4f && dx < PRAG * 2f) {
            joc.trantesteJos();
        }
        return true;
    }

    @Override
    public boolean inapoi() {
        if (stare == STARE_JOC) {
            stare = STARE_PAUZA;
            return true;
        }
        if (stare == STARE_PAUZA) {
            stare = STARE_JOC;
            return true;
        }
        return false;
    }
                       }
