package com.marius.tetris3d;

import android.graphics.Color;

/**
 * Modul TURN: piesa cade mereu in fata camerei,
 * iar tu rotesti turnul dedesubt cu degetul.
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

    private static final float RAZA = 5.2f;
    private float offY;

    private float startX, startY;
    private float ultimX;
    private long startTimp;
    private boolean gestFacut = false;
    private static final float PRAG = 0.05f;

    /** cat de multe grade roteste o miscare pe toata latimea ecranului */
    private static final float GRADE_PE_ECRAN = 420f;

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
            int liniiInainte = joc.linii;
            int tetrisInainte = joc.tetrisuriFacute;
            int pieseInainte = joc.pieseAsezate;

            joc.actualizeaza(dt);

            if (joc.linii > liniiInainte) {
                app.setari.adaugaLinii(joc.linii - liniiInainte);
            }
            if (joc.tetrisuriFacute > tetrisInainte) {
                app.setari.adaugaTetris();
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
        float zgX = (float) Math.sin(timp * 47f) * scut * 0.35f;
        float zgY = (float) Math.cos(timp * 39f) * scut * 0.28f;

        d.seteazaCamera(zgX, -0.8f + zgY, 21f, 0f, 0f, 0f);

        d.seteazaLumina(
                (float) Math.sin(Math.toRadians(rotatieLumina)) * 14f,
                16f,
                18f);

        offY = -JocTurn.RANDURI / 2f + 1.0f;

        app.stele.deseneaza2(d);

        podeaTurn(d);
        blocuriTurn(d);

        if (stare == STARE_JOC) {
            fantoma(d);
            piesaCurenta(d);
        }

        particule.deseneaza2(d, 0f, offY);

        interfata();

        if (stare == STARE_PAUZA) ecranPauza();
        if (stare == STARE_FINAL) ecranFinal();
    }

    /** unghiul, in grade, la care se afla o coloana pe cilindru */
    private float unghiColoana(float coloana) {
        return coloana * JocTurn.PAS_GRADE + joc.unghiTurn;
    }

    private float[] pozitie(float coloana, float rand) {
        float u = (float) Math.toRadians(unghiColoana(coloana));
        float x = (float) Math.sin(u) * RAZA;
        float z = (float) Math.cos(u) * RAZA - RAZA;
        float y = offY + rand;
        return new float[]{x, y, z};
    }

    /** 1 = fix in fata, 0 = in spate */
    private float vizibilitate(float coloana) {
        float u = (float) Math.toRadians(unghiColoana(coloana));
        return Math.max(0f, (float) Math.cos(u));
    }

    private void podeaTurn(Desenator d) {
        boolean activ = (stare == STARE_JOC);
        float ap = activ ? joc.apropiere() : 0f;

        for (int c = 0; c < JocTurn.COLOANE; c++) {
            float viz = vizibilitate(c);
            if (viz < 0.03f) continue;

            boolean tinta = activ && joc.coloanaTinta(c);

            float r = 0.18f, g = 0.22f, b = 0.38f;
            float a = 0.25f + viz * 0.65f;

            if (tinta) {
                float caldura = ap * ap;
                r = 0.18f + caldura * 0.80f;
                g = 0.22f + caldura * 0.26f;
                b = 0.38f - caldura * 0.32f;
                a = Math.min(1f, a + caldura * 0.25f);
            }

            float[] p = pozitie(c, -1f);
            d.cubRotit(p[0], p[1], p[2],
                    -unghiColoana(c), 0f, 1f, 0f,
                    r, g, b, a, 0.86f);
        }
    }

    private void blocuriTurn(Desenator d) {
        boolean activ = (stare == STARE_JOC);
        float ap = activ ? joc.apropiere() : 0f;
        int yFantoma = activ ? joc.pozitieFantoma() : -1;

        for (int r = 0; r < JocTurn.RANDURI; r++) {
            for (int c = 0; c < JocTurn.COLOANE; c++) {
                int val = joc.tabla[r][c];
                if (val == 0) continue;

                float viz = vizibilitate(c);
                if (viz < 0.03f) continue;

                float[] cul = culori[val - 1];

                float trem = joc.tremurRand[r] + joc.cutremurGlobal * 0.6f;
                boolean simte = activ && joc.coloanaTinta(c) && r <= yFantoma + 1;
                if (simte) trem += ap * 0.5f;

                float dy = (float) Math.cos(timp * 53f + c * 1.7f) * trem * 0.07f;

                float lum = (0.30f + viz * 0.70f)
                        * (1f + (simte ? ap * 0.4f : 0f) + trem * 0.2f);

                float[] p = pozitie(c, r);
                d.cubRotit(p[0], p[1] + dy, p[2],
                        -unghiColoana(c), 0f, 1f, 0f,
                        Math.min(1f, cul[0] * lum),
                        Math.min(1f, cul[1] * lum),
                        Math.min(1f, cul[2] * lum),
                        0.30f + viz * 0.70f, 0.94f);
            }
        }
    }

    private void fantoma(Desenator d) {
        int[][] forma = joc.formaCurenta();
        int yF = joc.pozitieFantoma();
        float[] cul = culori[joc.tipCurent];
        float p = 0.16f + 0.12f * puls(2.6f);

        for (int i = 0; i < 4; i++) {
            int c = joc.coloanaPiesei(forma[i][0]);
            float[] pz = pozitie(c, yF + forma[i][1]);

            d.cubRotit(pz[0], pz[1], pz[2],
                    -unghiColoana(c), 0f, 1f, 0f,
                    cul[0], cul[1], cul[2], p, 0.90f);
        }
    }

    private void piesaCurenta(Desenator d) {
        int[][] forma = joc.formaCurenta();
        float[] cul = culori[joc.tipCurent];

        for (int i = 0; i < 4; i++) {
            int c = joc.coloanaPiesei(forma[i][0]);
            float rand = joc.pieseYVizual() + forma[i][1];
            float[] p = pozitie(c, rand);

            d.cubRotit(p[0], p[1], p[2],
                    -unghiColoana(c), 0f, 1f, 0f,
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

        app.ui.textDreapta("LINII", 0.94f, 0.17f, 0.020f, Color.rgb(150, 160, 190));
        app.ui.textDreapta(String.valueOf(joc.linii), 0.94f, 0.205f, 0.028f, Color.rgb(180, 200, 255));

        if (stare == STARE_JOC) {
            app.ui.textCentrat("| |", 0.5f, 0.05f, 0.026f, Color.rgb(190, 200, 230));
            app.ui.textCentrat("TRAGE STANGA-DREAPTA CA SA ROTESTI TURNUL",
                    0.5f, 0.955f, 0.014f, Color.rgb(120, 128, 152));
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

        // tragere in jos, clara: coboara piesa
        if (difTotalY > PRAG * 1.6f && Math.abs(difTotalY) > Math.abs(difTotalX) * 1.5f) {
            joc.coboaraRapid();
            startY = y;
            gestFacut = true;
            ultimX = x;
            return true;
        }

        // orice miscare laterala roteste turnul, continuu, urmarind degetul
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

        long durata = System.currentTimeMillis() - startTimp;
        float dx = Math.abs(x - startX);
        float dy = Math.abs(y - startY);

        if (!gestFacut && durata < 250 && dx < PRAG && dy < PRAG) {
            joc.roteste();
        } else if ((startY - y) > PRAG * 2.4f && dx < PRAG * 2f) {
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
