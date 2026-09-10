package com.marius.tetris3d;

import android.graphics.Color;

public class EcranJoc extends Ecran {

    public static final int STARE_JOC   = 0;
    public static final int STARE_PAUZA = 1;
    public static final int STARE_FINAL = 2;

    private int stare = STARE_JOC;
    private int mod = Setari.MOD_CLASIC;
    private boolean modLiber = false;

    public Joc joc;
    private Particule particule;

    private float rotatieLumina = 0f;
    private float leganare = 0f;
    private float timpJucat = 0f;
    private int secundeRaportate = 0;

    private float offX, offY;
    private float[][] culori;

    private float startX, startY;
    private long startTimp;
    private boolean gestFacut = false;
    private boolean schimbLaAtingere = false;
    private static final float PRAG = 0.055f;

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

    public EcranJoc(Aplicatie app) {
        super(app);
        particule = new Particule();
        joc = new Joc();
        joc.particule = particule;
        joc.sunet = app.sunet;
    }

    public void pregateste(int modNou, boolean liber) {
        mod = modNou;
        modLiber = liber;
        joc.vitezaInitiala = app.setari.vitezaInitiala();
        joc.record = app.setari.record(mod);
        joc.culori = app.setari.culoriPiese();
        joc.stilSticla = (mod == Setari.MOD_STICLA);
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
        app.setari.setRecord(mod, joc.scor);
        app.setari.raporteazaNivel(joc.nivel);
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);

        rotatieLumina += dt * 6f;
        leganare += dt * 0.28f;

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
        float zgX = (float) Math.sin(timp * 47f) * scut * 0.42f;
        float zgY = (float) Math.cos(timp * 39f) * scut * 0.32f;

        float camX = 3.4f + (float) Math.sin(leganare) * 1.3f + zgX;
        float camY = -2.6f + (float) Math.cos(leganare * 0.7f) * 0.8f + zgY;

        d.seteazaCamera(camX, camY, 30f, 0f, 0.5f, 0f);

        d.seteazaLumina(
                (float) Math.sin(Math.toRadians(rotatieLumina)) * 16f,
                18f,
                (float) Math.cos(Math.toRadians(rotatieLumina)) * 16f + 12f);

        offX = -Joc.COLOANE / 2f + 0.5f;
        offY = -Joc.RANDURI / 2f + 1.0f;

        app.stele.deseneaza2(d);

        stalpi(d);
        podea(d);
        blocuri(d);

        if (stare == STARE_JOC) piesaCurenta(d);

        particule.deseneaza2(d, offX, offY);

        if (!modLiber) {
            piesaUrmatoare(d);
        }
        interfata();

        if (stare == STARE_PAUZA) ecranPauza();
        if (stare == STARE_FINAL) ecranFinal();
    }

    private void stalpi(Desenator d) {
        for (int r = 0; r < Joc.RANDURI; r++) {
            d.cub(offX - 0.85f, offY + r, -1.2f,
                    0.10f, 0.12f, 0.20f, 0.55f, 0.5f);
            d.cub(offX + Joc.COLOANE - 0.15f, offY + r, -1.2f,
                    0.10f, 0.12f, 0.20f, 0.55f, 0.5f);
        }
    }

    private void podea(Desenator d) {
        boolean activ = (stare == STARE_JOC);
        float ap = activ ? joc.apropiere() : 0f;

        for (int c = 0; c < Joc.COLOANE; c++) {
            boolean tinta = activ && joc.coloanaTinta(c);

            float r = 0.20f, g = 0.24f, b = 0.40f, a = 0.85f;

            if (tinta) {
                float caldura = ap * ap;
                r = 0.20f + caldura * 0.80f;
                g = 0.24f + caldura * 0.28f;
                b = 0.40f - caldura * 0.34f;
                a = 0.85f + caldura * 0.15f;
            }
            d.cub(offX + c, offY - 1f, -0.4f, r, g, b, a, 0.92f);
        }
    }

    private void blocuri(Desenator d) {
        boolean activ = (stare == STARE_JOC);
        float ap = activ ? joc.apropiere() : 0f;
        int yFantoma = activ ? joc.pozitieFantoma() : -1;
        float alfaBaza = joc.stilSticla ? 0.62f : 1f;

        for (int r = 0; r < Joc.RANDURI; r++) {
            for (int c = 0; c < Joc.COLOANE; c++) {
                int val = joc.tabla[r][c];
                if (val == 0) continue;

                float[] cul = culori[val - 1];

                float trem = joc.tremurRand[r] + joc.cutremurGlobal * 0.6f;
                boolean simte = activ && joc.coloanaTinta(c) && r <= yFantoma + 1;
                if (simte) trem += ap * 0.55f;

                float dx = (float) Math.sin(timp * 41f + r * 2.1f + c) * trem * 0.10f;
                float dy = (float) Math.cos(timp * 53f + c * 1.7f) * trem * 0.07f;

                float lum = 1f + (simte ? ap * 0.45f : 0f) + trem * 0.20f;

                float rr = Math.min(1f, cul[0] * lum);
                float gg = Math.min(1f, cul[1] * lum);
                float bb = Math.min(1f, cul[2] * lum);
                float alfa = alfaBaza;
                float scara = 1f;

                // in modul sticla, crapaturile decoloreaza si micsoreaza blocul
                if (joc.stilSticla) {
                    int cr = joc.crapaturi[r][c];
                    if (cr > 0) {
                        float uzura = Math.min(1f, cr / 3f);
                        // culoarea se spala spre alb-cenusiu
                        rr = rr + (0.85f - rr) * uzura * 0.7f;
                        gg = gg + (0.85f - gg) * uzura * 0.7f;
                        bb = bb + (0.90f - bb) * uzura * 0.7f;
                        alfa = alfaBaza + uzura * 0.25f;
                        scara = 1f - uzura * 0.10f;

                        // blocul aproape spart tremura singur
                        if (cr >= 2) {
                            dx += (float) Math.sin(timp * 33f + c * 3.1f) * 0.05f;
                            dy += (float) Math.cos(timp * 29f + r * 2.7f) * 0.04f;
                        }
                    }
                }

                d.cub(offX + c + dx, offY + r + dy, 0f, rr, gg, bb, alfa, scara);
            }
        }
    }

    private void piesaCurenta(Desenator d) {
        int[][] forma = joc.formaCurenta();
        float[] cul = culori[joc.tipCurent];
        float alfaBloc = joc.stilSticla ? 0.75f : 1f;

        for (int i = 0; i < 4; i++) {
            d.cub(offX + joc.pieseX + forma[i][0],
                  offY + joc.pieseYVizual() + forma[i][1],
                  0f, cul[0], cul[1], cul[2], alfaBloc, 1f);
        }
    }

    private void piesaUrmatoare(Desenator d) {
        int[][] forma = Joc.formaPiesei(joc.tipUrmator, 0);
        float[] cul = culori[joc.tipUrmator];

        float bazaX = offX + Joc.COLOANE / 2f - 1.1f;
        float bazaY = offY - 3.2f;
        float scara = 0.40f;

        for (int i = 0; i < 4; i++) {
            d.cub(bazaX + forma[i][0] * scara,
                  bazaY + forma[i][1] * scara,
                  -0.3f, cul[0], cul[1], cul[2], 0.95f, scara);
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

            if (modLiber) {
                app.ui.textCentrat("LIBER", 0.5f, 0.088f, 0.016f, Color.rgb(190, 160, 255));
            }
            if (joc.stilSticla && joc.blocuriSparte > 0) {
                app.ui.textCentrat("SPARTE " + joc.blocuriSparte, 0.5f,
                        modLiber ? 0.118f : 0.088f, 0.015f, Color.rgb(150, 220, 240));
            }
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
                pregateste(mod, modLiber);
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
                pregateste(mod, modLiber);
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
        startTimp = System.currentTimeMillis();
        gestFacut = false;

        if (modLiber) {
            schimbLaAtingere = true;
        }
        return true;
    }

    @Override
    public boolean tragere(float x, float y, float dx, float dy) {
        if (stare != STARE_JOC) return true;

        float difX = x - startX;
        float difY = y - startY;

        if (Math.abs(difX) > PRAG && Math.abs(difX) > Math.abs(difY)) {
            joc.muta(difX > 0 ? 1 : -1);
            startX = x;
            startY = y;
            gestFacut = true;
        } else if (difY > PRAG && Math.abs(difY) > Math.abs(difX)) {
            joc.coboaraRapid();
            startY = y;
            gestFacut = true;
        }
        return true;
    }

    @Override
    public boolean ridicare(float x, float y) {
        if (stare != STARE_JOC) return true;

        long durata = System.currentTimeMillis() - startTimp;
        float dx = Math.abs(x - startX);
        float dy = Math.abs(y - startY);

        if (!gestFacut && durata < 250 && dx < PRAG && dy < PRAG) {
            if (schimbLaAtingere) {
                joc.schimbaForma();
            } else {
                joc.roteste();
            }
        } else if ((startY - y) > PRAG * 2.2f) {
            joc.trantesteJos();
        }
        schimbLaAtingere = false;
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
