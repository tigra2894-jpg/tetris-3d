package com.marius.tetris3d;

/**
 * Ecranul de joc. Contine si pauza si ecranul de final.
 * Modul de joc decide efectele si regulile speciale.
 */
public class EcranJoc extends Ecran {

    public static final int STARE_JOC   = 0;
    public static final int STARE_PAUZA = 1;
    public static final int STARE_FINAL = 2;

    private int stare = STARE_JOC;
    private int mod = Setari.MOD_CLASIC;

    public Joc joc;
    private Particule particule;

    private float rotatieLumina = 0f;
    private float leganare = 0f;
    private float timpJucat = 0f;
    private int secundeRaportate = 0;

    private float offX, offY;
    private float[][] culori;

    // pentru gesturi
    private float startX, startY;
    private long startTimp;
    private boolean gestFacut = false;
    private static final float PRAG = 0.055f;   // fractiune din latimea ecranului

    // zonele de pe ecran
    private static final float Y_PAUZA_BUTON = 0.05f;

    private static final float Y_P_CONTINUA = 0.44f;
    private static final float Y_P_REINCEPE = 0.57f;
    private static final float Y_P_MENIU    = 0.70f;

    private static final float Y_F_DIN_NOU  = 0.66f;
    private static final float Y_F_MENIU    = 0.79f;

    private float stralucireButon = 0f;

    public EcranJoc(Aplicatie app) {
        super(app);
        particule = new Particule();
        joc = new Joc();
        joc.particule = particule;
        joc.sunet = app.sunet;
    }

    /** se cheama inainte de a intra in ecran */
    public void pregateste(int modNou) {
        mod = modNou;
        joc.vitezaInitiala = app.setari.vitezaInitiala();
        joc.record = app.setari.record(mod);
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
        stralucireButon = 0f;
    }

    @Override
    public void laIesire() {
        salveaza();
    }

    /** cand aplicatia trece in fundal */
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

        stralucireButon -= dt * 3f;
        if (stralucireButon < 0f) stralucireButon = 0f;

        particule.actualizeaza(dt);

        if (stare == STARE_JOC) {
            int liniiInainte = joc.linii;
            int tetrisInainte = joc.tetrisuriFacute;
            int pieseInainte = joc.pieseAsezate;

            joc.actualizeaza(dt);

            // statistici
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
            // in pauza si la final, tremurul se stinge oricum
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

        piesaUrmatoare(d);
        interfata(d);

        if (stare == STARE_JOC)   butonPauza(d);
        if (stare == STARE_PAUZA) ecranPauza(d);
        if (stare == STARE_FINAL) ecranFinal(d);
    }

    // ---------- elementele tablei ----------

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

                d.cub(offX + c + dx, offY + r + dy, 0f,
                        Math.min(1f, cul[0] * lum),
                        Math.min(1f, cul[1] * lum),
                        Math.min(1f, cul[2] * lum),
                        1f, 1f);
            }
        }
    }

    private void piesaCurenta(Desenator d) {
        int[][] forma = joc.formaCurenta();
        float[] cul = culori[joc.tipCurent];

        for (int i = 0; i < 4; i++) {
            d.cub(offX + joc.pieseX + forma[i][0],
                  offY + joc.pieseYVizual() + forma[i][1],
                  0f, cul[0], cul[1], cul[2], 1f, 1f);
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

    private void interfata(Desenator d) {
        float sus = offY + Joc.RANDURI + 2.6f;
        float mic = 0.17f;
        float mare = 0.26f;

        d.text("SCOR", offX - 0.8f, sus, mic, 0.60f, 0.66f, 0.85f);
        d.numar(joc.scor, offX - 0.8f, sus - 1.15f, mare, 1.0f, 0.92f, 0.50f);

        d.text("RECORD", offX - 0.8f, sus - 2.9f, mic, 0.60f, 0.66f, 0.85f);
        d.numar(joc.record, offX - 0.8f, sus - 3.9f, mic, 0.85f, 0.75f, 0.95f);

        float dr = offX + Joc.COLOANE - 2.6f;
        d.text("NIVEL", dr, sus, mic, 0.60f, 0.66f, 0.85f);
        d.numar(joc.nivel, dr, sus - 1.15f, mare, 0.45f, 0.95f, 1.0f);

        d.text("LINII", dr, sus - 2.9f, mic, 0.60f, 0.66f, 0.85f);
        d.numar(joc.linii, dr, sus - 3.9f, mic, 0.70f, 0.80f, 1.0f);
    }

    private void butonPauza(Desenator d) {
        float y = offY + Joc.RANDURI + 1.2f;
        float s = 0.24f;
        for (int i = 0; i < 3; i++) {
            d.cub(-0.3f, y - i * s, 0f, 0.75f, 0.82f, 1.0f, 0.85f, s);
            d.cub( 0.3f, y - i * s, 0f, 0.75f, 0.82f, 1.0f, 0.85f, s);
        }
    }

    // ---------- pauza ----------

    private void ecranPauza(Desenator d) {
        val(d, "PAUZA", 0f, ecranLaLume(0.28f), 0.42f, 1.0f, 0.92f, 0.45f);

        buton(d, "CONTINUA", Y_P_CONTINUA, 0.34f, 0.40f, 1.00f, 0.55f);
        buton(d, "REINCEPE", Y_P_REINCEPE, 0.28f, 0.85f, 0.80f, 1.00f);
        buton(d, "MENIU",    Y_P_MENIU,    0.28f, 0.70f, 0.75f, 0.90f);
    }

    // ---------- final ----------

    private void ecranFinal(Desenator d) {
        float clip = 0.75f + 0.25f * puls(3f);
        val(d, "FINAL", 0f, ecranLaLume(0.20f), 0.42f, 1.0f * clip, 0.35f, 0.35f);

        d.textCentrat("SCOR", 0f, ecranLaLume(0.34f), 0.19f, 0.60f, 0.66f, 0.85f);
        d.numarCentrat(joc.scor, 0f, ecranLaLume(0.40f), 0.34f, 1.0f, 0.92f, 0.50f);

        boolean recordNou = joc.scor >= joc.record && joc.scor > 0;
        if (recordNou) {
            float p = 0.6f + 0.4f * puls(5f);
            d.textCentrat("RECORD NOU", 0f, ecranLaLume(0.50f), 0.22f,
                    1.0f * p, 0.85f * p, 0.25f);
        } else {
            d.textCentrat("RECORD", 0f, ecranLaLume(0.49f), 0.16f, 0.55f, 0.62f, 0.80f);
            d.numarCentrat(joc.record, 0f, ecranLaLume(0.545f), 0.22f, 0.85f, 0.75f, 0.95f);
        }

        buton(d, "DIN NOU", Y_F_DIN_NOU, 0.34f, 0.40f, 1.00f, 0.55f);
        buton(d, "MENIU",   Y_F_MENIU,   0.28f, 0.70f, 0.75f, 0.90f);
    }

    private void val(Desenator d, String s, float x, float y, float scara,
                     float r, float g, float b) {
        d.textCentrat(s, x, y, scara, r, g, b);
    }

    private void buton(Desenator d, String s, float yEcran, float scara,
                       float r, float g, float b) {
        d.textCentrat(s, 0f, ecranLaLume(yEcran), scara, r, g, b);
    }

    private float ecranLaLume(float yEcran) {
        return 13f - yEcran * 26f;
    }

    // ---------- atingeri ----------

    @Override
    public boolean atingere(float x, float y) {

        if (stare == STARE_PAUZA) {
            if (inRand(y, Y_P_CONTINUA, 0.08f)) {
                stare = STARE_JOC;
                app.sunet.rotire();
            } else if (inRand(y, Y_P_REINCEPE, 0.08f)) {
                pregateste(mod);
                app.sunet.nivel();
            } else if (inRand(y, Y_P_MENIU, 0.08f)) {
                salveaza();
                app.sunet.mutare();
                app.meniu();
            }
            return true;
        }

        if (stare == STARE_FINAL) {
            if (inRand(y, Y_F_DIN_NOU, 0.08f)) {
                pregateste(mod);
                app.sunet.nivel();
            } else if (inRand(y, Y_F_MENIU, 0.08f)) {
                app.sunet.mutare();
                app.meniu();
            }
            return true;
        }

        // in joc: butonul de pauza, sus in mijloc
        if (y < 0.11f && x > 0.38f && x < 0.62f) {
            stare = STARE_PAUZA;
            app.sunet.mutare();
            return true;
        }

        startX = x;
        startY = y;
        startTimp = System.currentTimeMillis();
        gestFacut = false;
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
            joc.roteste();
        } else if ((startY - y) > PRAG * 2.2f) {
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
        return false;   // la final, lasa aplicatia sa iasa la meniu
    }
  }
