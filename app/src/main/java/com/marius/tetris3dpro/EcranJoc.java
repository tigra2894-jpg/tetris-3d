package com.marius.tetris3dpro;

public class EcranJoc extends Ecran implements Joc.Ascultator {

    public static final int STARE_JOC = 0, STARE_PAUZA = 1, STARE_FINAL = 2, STARE_START = 3;

    private static final float CAM_TINTA_Y = 8.5f;
    private static final float CAM_INALTIME = 34f;
    private static final float Y_BUTOANE = 0.855f;

    // butoane
    private static final int B_STANGA = 0, B_DREAPTA = 1, B_ROT_L = 2, B_ROT_R = 3,
            B_JOS = 4, B_TRANTIRE = 5, B_HOLD = 6, NR_BUTOANE = 7;
    private static final String[] ETICHETE = {"\u25C0", "\u25B6", "\u21BA", "\u21BB", "\u25BC", "\u2B07", "H"};
    private final float[] bX = new float[NR_BUTOANE];
    private final float[] bApasat = new float[NR_BUTOANE];
    private final int[] bPointer = new int[NR_BUTOANE];

    private final Joc joc;
    private int stare = STARE_START;
    private float timpStart = 0f;
    private float timpFinal = 0f;

    private float camZ = 40f;
    private float latimeVizibila = 16f;
    private float tremur = 0f, tremurX = 0f, tremurY = 0f;
    private float miscareX = 0f;   // legănare camera
    private float scaraTabla = 1f;

    // gest pe tabla
    private int gestPointer = -1;
    private float gestX0, gestY0, gestXAcum, gestYAcum, gestTimp;
    private boolean gestMutat = false;
    private boolean gestSoft = false;
    private int degeteJos = 0;
    private boolean doiDegete = false;

    // DAS pentru butoane
    private int dasDir = 0;
    private float dasTimer = 0f;
    private boolean dasRepeta = false;

    private final int[] ultimaAsezareX = new int[4], ultimaAsezareY = new int[4];
    private float pulsAsezare = 0f;
    private float flashLinie = 0f;
    private float pulsNivel = 0f;
    private float timpJoc = 0f;
    private boolean recordNou = false;
    private boolean recordSprintNou = false;
    private float[][] culori;
    private int scorAfisat = 0;

    public EcranJoc(Aplicatie app) {
        super(app);
        joc = app.joc;
        joc.setAscultator(this);
        for (int i = 0; i < NR_BUTOANE; i++) bPointer[i] = -1;
    }

    public void porneste(int mod) {
        culori = app.culoriPiese();
        joc.jocNou(mod, app.setari.nivelStart());
        app.particule.goleste();
        stare = STARE_START;
        timpStart = 0f;
        timpJoc = 0f;
        recordNou = false;
        recordSprintNou = false;
        scorAfisat = 0;
        tremur = 0f;
        gestPointer = -1; degeteJos = 0; dasDir = 0;
        app.setari.setUltimulMod(mod);
    }

    @Override
    public void laIntrare() {
        super.laIntrare();
        culori = app.culoriPiese();
    }

    @Override
    public void laPauzaAplicatie() {
        if (stare == STARE_JOC) stare = STARE_PAUZA;
    }

    @Override
    public boolean inapoi() {
        if (stare == STARE_JOC) { stare = STARE_PAUZA; return true; }
        if (stare == STARE_PAUZA) { stare = STARE_JOC; return true; }
        app.schimbaEcran(app.ecranMeniu);
        return true;
    }

    // ---------- actualizare ----------

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);
        latimeVizibila = CAM_INALTIME * app.raport;

        if (tremur > 0f) {
            tremur = Math.max(0f, tremur - dt * 3.2f);
            tremurX = (float) (Math.sin(app.timp * 90f) * tremur * 0.35f);
            tremurY = (float) (Math.cos(app.timp * 73f) * tremur * 0.25f);
        } else { tremurX = tremurY = 0f; }
        pulsAsezare = Math.max(0f, pulsAsezare - dt * 4f);
        flashLinie = Math.max(0f, flashLinie - dt * 2.5f);
        pulsNivel = Math.max(0f, pulsNivel - dt * 1.2f);
        miscareX = (float) Math.sin(app.timp * 0.35f) * 0.6f;

        if (scorAfisat < joc.scor) {
            int dif = joc.scor - scorAfisat;
            scorAfisat += Math.max(1, (int) (dif * Math.min(1f, dt * 8f)));
        }

        for (int i = 0; i < NR_BUTOANE; i++)
            if (bPointer[i] < 0) bApasat[i] = Math.max(0f, bApasat[i] - dt * 6f);

        if (stare == STARE_START) {
            timpStart += dt;
            if (timpStart > 0.9f) stare = STARE_JOC;
            return;
        }
        if (stare == STARE_PAUZA) return;
        if (stare == STARE_FINAL) { timpFinal += dt; return; }

        timpJoc += dt;

        if (dasDir != 0) {
            dasTimer += dt;
            float prag = dasRepeta ? 0.045f : 0.16f;
            if (dasTimer >= prag) {
                dasTimer = 0f; dasRepeta = true;
                joc.muta(dasDir);
            }
        }

        joc.actualizeaza(dt);
    }

    // ---------- evenimente joc ----------

    @Override public void laMutare() { app.sunet.reda(Sunet.MUTARE); }

    @Override public void laRotire(boolean ok) {
        app.sunet.reda(ok ? Sunet.ROTIRE : Sunet.BLOCAT);
    }

    @Override public void laHold() { app.sunet.reda(Sunet.HOLD); app.vibratii.scurt(); }

    @Override
    public void laAsezare(int tip, int[] cx, int[] cy, boolean trantit) {
        System.arraycopy(cx, 0, ultimaAsezareX, 0, 4);
        System.arraycopy(cy, 0, ultimaAsezareY, 0, 4);
        pulsAsezare = 1f;
        float[] c = culori[tip];
        if (trantit) {
            tremur = 0.7f;
            app.sunet.reda(Sunet.TRANTIRE);
            app.vibratii.mediu();
            for (int i = 0; i < 4; i++)
                app.particule.emite(cx[i] - 4.5f, cy[i] + 0.5f, 0.3f, c[0], c[1], c[2], 4, 5f, 0.18f);
        } else {
            app.sunet.reda(Sunet.ATERIZARE);
            app.vibratii.scurt();
        }
    }

    @Override
    public void laLinii(int n, int[] randuri, int puncte, boolean tspin, boolean mini,
                        boolean b2b, int combo, boolean perfect) {
        flashLinie = n >= 4 || tspin ? 1f : 0.5f;
        tremur = Math.max(tremur, n >= 4 ? 1.3f : 0.4f + n * 0.2f);
        for (int i = 0; i < n; i++) {
            int r = randuri[i];
            for (int col = 0; col < Joc.COLOANE; col++) {
                int t = joc.tabla[r][col] - 1;
                if (t < 0) continue;
                float[] c = culori[t];
                app.particule.emite(col - 4.5f, r + 0.5f, 0.2f, c[0], c[1], c[2],
                        n >= 4 ? 5 : 3, 6f + n * 1.5f, 0.22f);
            }
        }
        if (perfect || n >= 4) { app.sunet.reda(Sunet.TETRIS); app.vibratii.lung(); }
        else if (tspin)        { app.sunet.reda(Sunet.TSPIN); app.vibratii.mediu(); }
        else                   { app.sunet.reda(Sunet.LINIE); app.vibratii.mediu(); }
        if (combo >= 2) app.sunet.reda(Sunet.COMBO);
    }

    @Override
    public void laNivel(int nivelNou) {
        pulsNivel = 1f;
        app.sunet.reda(Sunet.NIVEL);
        app.setari.raporteazaNivel(nivelNou);
    }

    @Override
    public void laFinal(boolean victorie) {
        stare = STARE_FINAL;
        timpFinal = 0f;
        dasDir = 0;
        Setari s = app.setari;
        s.adaugaJoc();
        s.adaugaLinii(joc.linii);
        s.adaugaPiese(joc.piese);
        s.adaugaTimp((int) joc.timp);
        s.adaugaTetris(joc.tetrisuri);
        s.adaugaTspin(joc.tspinuri);
        s.adaugaPerfect(joc.perfectClears);
        s.raporteazaCombo(joc.comboMaxim);
        s.raporteazaNivel(joc.nivel);

        if (joc.mod == Setari.MOD_SPRINT) {
            if (victorie) {
                recordSprintNou = s.setRecordSprintMs((int) (joc.timp * 1000));
            }
        } else {
            recordNou = joc.scor > s.record(joc.mod) && joc.scor > 0;
            s.setRecord(joc.mod, joc.scor);
        }

        if (victorie) { app.sunet.reda(Sunet.VICTORIE); app.vibratii.lung(); }
        else { app.sunet.reda(Sunet.FINAL); app.vibratii.lung(); tremur = 1.5f; }
    }

    // ---------- intrare ----------

    private boolean butoaneActive() {
        return app.setari.butoanePornite() && stare == STARE_JOC;
    }

    private int butonLa(float x, float y) {
        if (y < Y_BUTOANE) return -1;
        float lat = 1f / NR_BUTOANE;
        int i = (int) (x / lat);
        if (i < 0 || i >= NR_BUTOANE) return -1;
        if (i == B_HOLD && !joc.holdPermis) return -1;
        return i;
    }

    @Override
    public void apasare(int p, float x, float y) {
        degeteJos++;
        if (stare == STARE_START) return;

        if (stare == STARE_PAUZA) { apasarePauza(x, y); return; }
        if (stare == STARE_FINAL) { apasareFinal(x, y); return; }

        // buton pauza in coltul dreapta-sus
        if (x > 0.86f && y < 0.075f) { stare = STARE_PAUZA; app.sunet.reda(Sunet.MENIU); return; }

        if (butoaneActive()) {
            int b = butonLa(x, y);
            if (b >= 0) { apasaButon(b, p); return; }
        }

        if (degeteJos >= 2 && gestPointer >= 0) {
            doiDegete = true;
            return;
        }
        gestPointer = p;
        gestX0 = gestXAcum = x; gestY0 = gestYAcum = y;
        gestTimp = 0f;
        gestMutat = false; gestSoft = false; doiDegete = false;
        gestTimp = app.timp;
    }

    private void apasaButon(int b, int p) {
        bPointer[b] = p;
        bApasat[b] = 1f;
        switch (b) {
            case B_STANGA:  joc.muta(-1); dasDir = -1; dasTimer = 0f; dasRepeta = false; break;
            case B_DREAPTA: joc.muta(1);  dasDir = 1;  dasTimer = 0f; dasRepeta = false; break;
            case B_ROT_L:   if (joc.mod == Setari.MOD_LIBER) joc.schimbaForma(); else joc.roteste(-1); break;
            case B_ROT_R:   joc.roteste(1); break;
            case B_JOS:     joc.setCoborareRapida(true); break;
            case B_TRANTIRE: joc.trantesteJos(); break;
            case B_HOLD:    joc.hold(); break;
        }
    }

    @Override
    public void miscare(int p, float x, float y) {
        if (stare != STARE_JOC || p != gestPointer) return;
        float cellW = scaraTabla / latimeVizibila;
        float cellH = scaraTabla / CAM_INALTIME;

        float dx = x - gestXAcum;
        while (dx > cellW * 0.85f)  { joc.muta(1);  gestXAcum += cellW; dx -= cellW; gestMutat = true; }
        while (dx < -cellW * 0.85f) { joc.muta(-1); gestXAcum -= cellW; dx += cellW; gestMutat = true; }

        float dy = y - gestY0;
        if (dy > cellH * 1.3f && Math.abs(x - gestX0) < cellW * 2.5f) {
            if (!gestSoft) { gestSoft = true; joc.setCoborareRapida(true); }
            gestMutat = true;
        } else if (gestSoft && dy < cellH * 0.6f) {
            gestSoft = false; joc.setCoborareRapida(false);
        }
        gestYAcum = y;
    }

    @Override
    public void ridicare(int p, float x, float y) {
        degeteJos = Math.max(0, degeteJos - 1);

        for (int i = 0; i < NR_BUTOANE; i++) {
            if (bPointer[i] == p) {
                bPointer[i] = -1;
                if (i == B_JOS) joc.setCoborareRapida(false);
                if ((i == B_STANGA && dasDir == -1) || (i == B_DREAPTA && dasDir == 1)) dasDir = 0;
                return;
            }
        }

        if (p != gestPointer) return;
        gestPointer = -1;
        if (stare != STARE_JOC) return;

        float durata = app.timp - gestTimp;
        float dx = x - gestX0, dy = y - gestY0;
        float cellH = scaraTabla / CAM_INALTIME;

        if (gestSoft) { gestSoft = false; joc.setCoborareRapida(false); }

        if (doiDegete) { doiDegete = false; joc.hold(); return; }

        if (dy > 0.10f && durata < 0.28f && Math.abs(dx) < 0.08f) {
            joc.trantesteJos();
            return;
        }
        if (dy < -cellH * 1.5f && Math.abs(dx) < 0.1f) {
            joc.hold();
            return;
        }
        if (!gestMutat && durata < 0.3f && Math.abs(dx) < 0.03f && Math.abs(dy) < 0.03f) {
            if (joc.mod == Setari.MOD_LIBER && x < 0.5f) joc.schimbaForma();
            else joc.roteste(x < 0.5f ? -1 : 1);
        }
    }

    private void apasarePauza(float x, float y) {
        if (y > 0.40f && y < 0.49f) { stare = STARE_JOC; app.sunet.reda(Sunet.MENIU); }
        else if (y > 0.51f && y < 0.60f) { porneste(joc.mod); app.sunet.reda(Sunet.MENIU); }
        else if (y > 0.62f && y < 0.71f) { app.sunet.reda(Sunet.MENIU); app.schimbaEcran(app.ecranMeniu); }
    }

    private void apasareFinal(float x, float y) {
        if (timpFinal < 0.8f) return;
        if (y > 0.70f && y < 0.79f) { porneste(joc.mod); app.sunet.reda(Sunet.MENIU); }
        else if (y > 0.81f && y < 0.90f) { app.sunet.reda(Sunet.MENIU); app.schimbaEcran(app.ecranMeniu); }
    }

    // ---------- desen 3D ----------

    private float ecranX(float wx) { return 0.5f + wx / latimeVizibila; }
    private float ecranY(float wy) { return 0.5f - (wy - CAM_TINTA_Y) / CAM_INALTIME; }

    @Override
    public void deseneaza3D(Randare r) {
        float d = (CAM_INALTIME / 2f) / (float) Math.tan(Math.toRadians(23f));
        camZ = d;
        r.seteazaCamera(miscareX * 0.15f + tremurX, CAM_TINTA_Y + tremurY, d,
                        tremurX * 0.5f, CAM_TINTA_Y + tremurY * 0.5f, 0f);

        float[] fundal = app.setari.culoareFundal();
        float[] accent = {0.35f + fundal[0] * 3f, 0.55f + fundal[1] * 3f, 0.9f};
        app.fundal.deseneazaStele(r, accent);
        app.fundal.deseneazaPodea(r, -0.55f, accent);

        deseneazaCadru(r, accent);
        deseneazaTabla(r);
        if (stare != STARE_FINAL || timpFinal < 0.6f) {
            deseneazaPiesaCurenta(r);
        }
        deseneazaHoldSiNext(r);
        if (app.texParticula != 0) app.particule.deseneazaScantei(app.scantei);
        else app.particule.deseneaza(r);
    }

    private void deseneazaCadru(Randare r, float[] a) {
        float g = 0.16f;
        float alfa = 0.85f;
        float h = Joc.RANDURI;
        if (app.texRama != 0) {
            // rama texturata, mai groasa: corpul e un cub inchis, fata din fata primeste imaginea
            float gr = 0.5f, fata = 0.51f;
            float c = 0.06f;
            r.cubIntins(-5f - gr / 2f, (h - gr) / 2f, 0f, gr, h + gr, 1f, c, c, c * 1.4f, 1f);
            r.cubIntins( 5f + gr / 2f, (h - gr) / 2f, 0f, gr, h + gr, 1f, c, c, c * 1.4f, 1f);
            r.cubIntins(0f, -gr / 2f, 0f, 10f, gr, 1f, c, c, c * 1.4f, 1f);
            // fasia 1:8 pe o latime de gr acopera 8*gr unitati; se repeta pe lungime
            float rep = (h + gr) / (8f * gr);
            app.imagine.bara(app.texRama, r.vizProj(), -5f - gr, -gr, -5f, h, fata, rep, false);
            app.imagine.bara(app.texRama, r.vizProj(), 5f, -gr, 5f + gr, h, fata, rep, false);
            app.imagine.bara(app.texRama, r.vizProj(), -5f, -gr, 5f, 0f, fata, 10f / (8f * gr), true);
        } else {
            // laterale
            r.cubIntins(-5f - g / 2f, h / 2f, 0f, g, h + g, 1f, a[0], a[1], a[2], alfa);
            r.cubIntins( 5f + g / 2f, h / 2f, 0f, g, h + g, 1f, a[0], a[1], a[2], alfa);
            r.cubIntins(0f, -g / 2f, 0f, 10f + g * 2f, g, 1f, a[0], a[1], a[2], alfa);
        }
        // perete din spate cu grila; cu imaginea panou.png grila ramane doar ca urma fina
        float grila = 1f;
        if (app.texPanou != 0) {
            app.imagine.panou(app.texPanou, r.vizProj(), -5f, 0f, 5f, h, -0.72f, 1f, 1f, 1f, 1f);
            grila = 0.4f;
        } else {
            r.cubIntins(0f, h / 2f, -0.75f, 10f, h, 0.06f, 0.02f, 0.03f, 0.07f, 0.92f);
        }
        for (int c = 1; c < Joc.COLOANE; c++)
            r.cubIntins(c - 5f, h / 2f, -0.70f, 0.03f, h, 0.02f, a[0], a[1], a[2], 0.12f * grila);
        for (int row = 1; row < Joc.RANDURI; row++)
            r.cubIntins(0f, row, -0.70f, 10f, 0.03f, 0.02f, a[0], a[1], a[2], 0.10f * grila);
        // linie de pericol
        r.cubIntins(0f, Joc.RANDURI - 2f, -0.68f, 10f, 0.05f, 0.02f, 1f, 0.3f, 0.3f, 0.18f);
    }

    private void deseneazaTabla(Randare r) {
        boolean stergere = joc.esteInFazaStergere();
        float prog = joc.progresStergere();
        float cadereFinal = stare == STARE_FINAL ? neted(Math.min(1f, timpFinal / 1.2f)) : 0f;

        for (int row = 0; row < Joc.RANDURI_TOTAL; row++) {
            boolean seSterge = stergere && joc.randSeSterge(row);
            for (int col = 0; col < Joc.COLOANE; col++) {
                int t = joc.tabla[row][col];
                if (t == 0) continue;
                float[] c = culori[t - 1];
                float x = col - 4.5f, y = row + 0.5f;
                float s = 1f, alfa = 1f;
                float cr = c[0], cg = c[1], cb = c[2];
                float lum = joc.stralucire[row][col];
                if (lum > 0f) { cr = Math.min(1f, cr + lum * 0.6f); cg = Math.min(1f, cg + lum * 0.6f); cb = Math.min(1f, cb + lum * 0.6f); }
                if (seSterge) {
                    float fz = prog;
                    s = 1f - fz * 0.9f;
                    float alb = Math.min(1f, fz * 2f);
                    cr = cr + (1f - cr) * alb; cg = cg + (1f - cg) * alb; cb = cb + (1f - cb) * alb;
                    x += (col - 4.5f) * fz * 0.25f;
                    alfa = 1f - fz * 0.7f;
                }
                if (cadereFinal > 0f) {
                    float gri = 0.45f;
                    cr = cr * (1f - cadereFinal) + gri * cadereFinal;
                    cg = cg * (1f - cadereFinal) + gri * cadereFinal;
                    cb = cb * (1f - cadereFinal) + gri * cadereFinal;
                    y -= cadereFinal * cadereFinal * (row * 0.6f + 1f) * 0f;
                }
                r.cub(x, y, 0f, cr, cg, cb, alfa, s);
            }
        }
    }

    private void deseneazaPiesaCurenta(Randare r) {
        if (!joc.piesaActiva) return;
        float[] c = culori[joc.tipCurent];
        int[][] f = Joc.FORME[joc.tipCurent][joc.rotatie];

        if (app.setari.fantomaPornita()) {
            int gy = joc.pozitieFantoma();
            if (gy != joc.pieseY) {
                for (int i = 0; i < 4; i++) {
                    int x = joc.pieseX + f[i][0], y = gy + f[i][1];
                    if (y >= Joc.RANDURI) continue;
                    r.cub(x - 4.5f, y + 0.5f, 0f, c[0], c[1], c[2], 0.28f, 0.92f);
                }
            }
        }
        float puls = 0.5f + 0.5f * (float) Math.sin(app.timp * 6f);
        for (int i = 0; i < 4; i++) {
            int x = joc.pieseX + f[i][0], y = joc.pieseY + f[i][1];
            if (y >= Joc.RANDURI + 2) continue;
            float lum = 0.10f + puls * 0.08f;
            r.cub(x - 4.5f, y + 0.5f, 0.02f,
                  Math.min(1f, c[0] + lum), Math.min(1f, c[1] + lum), Math.min(1f, c[2] + lum), 1f, 1f);
        }
    }

    private void deseneazaPiesaMica(Randare r, int tip, float cx, float cy, float scara, float alfa) {
        if (tip < 0) return;
        float[] c = culori[tip];
        int[][] f = Joc.FORME[tip][0];
        float mx = 0f, my = 0f;
        for (int i = 0; i < 4; i++) { mx += f[i][0]; my += f[i][1]; }
        mx /= 4f; my /= 4f;
        for (int i = 0; i < 4; i++) {
            r.cub(cx + (f[i][0] - mx) * scara, cy + (f[i][1] - my) * scara, 0f,
                  c[0], c[1], c[2], alfa, scara);
        }
    }

    private float holdX() { return -latimeVizibila / 2f + 2.2f; }
    private float nextX0() { return 5.9f + Math.max(0f, latimeVizibila / 2f - 7.8f) * 0.5f; }
    private static final float Y_SUS = 22.8f;

    private void deseneazaHoldSiNext(Randare r) {
        float s = Math.min(0.55f, latimeVizibila / 30f);
        if (joc.holdPermis) {
            float alfa = joc.holdFolosit ? 0.35f : 1f;
            deseneazaPiesaMica(r, joc.tipHold, holdX(), Y_SUS, s, alfa);
        }
        float x = nextX0();
        float pas = 2.35f * s / 0.55f;
        int vizibile = Math.min(4, Math.max(2, (int) ((latimeVizibila / 2f - 5.9f) / pas) + 1));
        if (latimeVizibila / 2f - 5.9f < 2.2f) {
            // ecran ingust: coada sta deasupra tablei, orizontal
            x = -2.2f; pas = 2.3f * s / 0.55f; vizibile = 4;
            for (int i = 0; i < vizibile; i++)
                deseneazaPiesaMica(r, joc.coada[i], x + i * pas, Y_SUS, i == 0 ? s : s * 0.8f, i == 0 ? 1f : 0.75f);
        } else {
            for (int i = 0; i < vizibile; i++)
                deseneazaPiesaMica(r, joc.coada[i], x, Y_SUS - i * pas, i == 0 ? s : s * 0.8f, i == 0 ? 1f : 0.75f);
        }
    }

    // ---------- desen UI ----------

    /** cutiile din jurul pieselor HOLD / NEXT; aceeasi asezare ca in deseneazaHoldSiNext */
    private void deseneazaCutii(CapaUI ui, float fade) {
        if (!ui.areCutie()) return;
        float s = Math.min(0.55f, latimeVizibila / 30f);
        float pas = 2.35f * s / 0.55f;
        boolean ingust = latimeVizibila / 2f - 5.9f < 2.2f;
        if (ingust) pas = 2.3f * s / 0.55f;

        if (joc.holdPermis) {
            float a = joc.holdFolosit ? 0.35f : 0.85f;
            ui.cutie(ecranX(holdX()), ecranY(Y_SUS), pas * 0.98f / latimeVizibila, argb(fade * a, 0xB9C2D6));
        }
        if (ingust) {
            for (int i = 0; i < 4; i++) {
                float latura = (i == 0 ? 0.98f : 0.9f) * pas;
                ui.cutie(ecranX(-2.2f + i * pas), ecranY(Y_SUS), latura / latimeVizibila,
                        argb(fade * (i == 0 ? 0.9f : 0.55f), 0x4DE1FF));
            }
        } else {
            float x = nextX0();
            int vizibile = Math.min(4, Math.max(2, (int) ((latimeVizibila / 2f - 5.9f) / pas) + 1));
            // cutia nu intra peste stalpul din dreapta tablei
            float max = 2f * (x - 5.6f);
            for (int i = 0; i < vizibile; i++) {
                float latura = Math.min((i == 0 ? 0.98f : 0.9f) * pas, max);
                ui.cutie(ecranX(x), ecranY(Y_SUS - i * pas), latura / latimeVizibila,
                        argb(fade * (i == 0 ? 0.9f : 0.55f), 0x4DE1FF));
            }
        }
    }

    private static final int ALB = 0xFFFFFFFF, GRI = 0xFFB9C2D6, CIAN = 0xFF4DE1FF, GALBEN = 0xFFFFD84D,
            ROZ = 0xFFFF5FA8, VERDE = 0xFF7CFF6B, PANOU = 0xC80B1020, PANOU_INCHIS = 0xE0070B18;

    @Override
    public void deseneazaUI(CapaUI ui) {
        boolean ingust = latimeVizibila / 2f - 5.9f < 2.2f;
        float fade = tranzitie;

        deseneazaCutii(ui, fade);

        // etichete hold / next
        if (joc.holdPermis)
            ui.textCentrat("HOLD", ecranX(holdX()), ecranY(Y_SUS + 1.9f), 0.020f, argb(fade * 0.8f, 0xB9C2D6));
        ui.textCentrat("NEXT", ingust ? ecranX(0.3f) : ecranX(nextX0()), ecranY(Y_SUS + 1.9f), 0.020f, argb(fade * 0.8f, 0xB9C2D6));

        // mod si pauza
        ui.text(Setari.NUME_MODURI[joc.mod], 0.03f, 0.045f, 0.022f, argb(fade * 0.9f, 0x4DE1FF));
        if (stare == STARE_JOC || stare == STARE_START) {
            ui.panou(0.885f, 0.018f, 0.09f, 0.048f, argb(fade * 0.55f, 0x0B1020), 0.012f);
            ui.panou(0.912f, 0.028f, 0.012f, 0.028f, argb(fade, 0xFFFFFF), 0.003f);
            ui.panou(0.936f, 0.028f, 0.012f, 0.028f, argb(fade, 0xFFFFFF), 0.003f);
        }

        // rand HUD sub tabla
        float yHud = ecranY(-1.1f);
        boolean areBara = ui.areBaraHud();
        if (areBara) {
            // bara coboara putin ca sa nu atinga baza tablei
            yHud += 0.018f;
            ui.baraHud(0.005f, yHud - 0.027f, 0.99f, 0.080f, argb(fade * 0.7f, 0x4DE1FF));
        }
        float yHud2 = yHud + 0.030f;
        ui.textCentrat("SCOR", 0.17f, yHud, 0.018f, argb(fade * 0.7f, 0xB9C2D6));
        ui.textCentrat(String.valueOf(scorAfisat), 0.17f, yHud2, 0.030f, argb(fade, 0xFFFFFF));

        switch (joc.mod) {
            case Setari.MOD_SPRINT:
                ui.textCentrat("LINII", 0.5f, yHud, 0.018f, argb(fade * 0.7f, 0xB9C2D6));
                ui.textCentrat(joc.liniiRamaseSprint() + "", 0.5f, yHud2, 0.030f, argb(fade, 0xFFD84D));
                ui.textCentrat("TIMP", 0.83f, yHud, 0.018f, argb(fade * 0.7f, 0xB9C2D6));
                ui.textCentrat(Joc.formatTimp(joc.timp), 0.83f, yHud2, 0.030f, argb(fade, 0xFFFFFF));
                break;
            case Setari.MOD_ULTRA: {
                float rest = joc.timpRamasUltra();
                int cul = rest < 10f ? 0xFF5F5F : 0xFFFFFF;
                ui.textCentrat("LINII", 0.5f, yHud, 0.018f, argb(fade * 0.7f, 0xB9C2D6));
                ui.textCentrat(joc.linii + "", 0.5f, yHud2, 0.030f, argb(fade, 0xFFD84D));
                ui.textCentrat("TIMP", 0.83f, yHud, 0.018f, argb(fade * 0.7f, 0xB9C2D6));
                ui.textCentrat(Joc.formatTimp(rest), 0.83f, yHud2, 0.030f, argb(fade, cul));
                break;
            }
            default: {
                float p = pulsNivel;
                ui.textCentrat("NIVEL", 0.5f, yHud, 0.018f, argb(fade * 0.7f, 0xB9C2D6));
                ui.textCentrat(joc.nivel + "", 0.5f, yHud2, 0.030f + p * 0.012f, argb(fade, p > 0.3f ? 0x7CFF6B : 0xFFD84D));
                ui.textCentrat("LINII", 0.83f, yHud, 0.018f, argb(fade * 0.7f, 0xB9C2D6));
                ui.textCentrat(joc.linii + "", 0.83f, yHud2, 0.030f, argb(fade, 0xFFFFFF));
                if (joc.mod == Setari.MOD_CLASIC) {
                    float prog = (joc.linii % 10) / 10f;
                    // cu bara HUD, progresul spre nivelul urmator sta in compartimentul NIVEL
                    if (areBara) ui.bara(0.39f, yHud2 + 0.010f, 0.22f, 0.006f, prog, argb(fade * 0.4f, 0x2A3350), argb(fade, 0x4DE1FF));
                    else ui.bara(0.12f, yHud2 + 0.014f, 0.76f, 0.006f, prog, argb(fade * 0.4f, 0x2A3350), argb(fade, 0x4DE1FF));
                }
            }
        }

        // combo / b2b
        if (joc.combo >= 1 && stare == STARE_JOC) {
            ui.textCentrat("COMBO x" + joc.combo, ecranX(0f), ecranY(-0.3f) - 0.0f, 0.024f, argb(fade, 0xFF5FA8));
        }
        if (joc.b2bActiv && stare == STARE_JOC) {
            ui.textCentrat("B2B", 0.03f + 0.03f, 0.085f, 0.018f, argb(fade * 0.85f, 0xFFD84D));
        }

        // mesaj de puncte
        if (joc.timerMesaj > 0f && !joc.ultimulMesaj.isEmpty()) {
            float t = joc.timerMesaj / 1.6f;
            float a = Math.min(1f, t * 3f);
            float urc = (1f - t) * 0.04f;
            int cul = joc.ultimulMesaj.startsWith("PERFECT") ? 0xFFD84D
                    : joc.ultimulMesaj.contains("T-SPIN") ? 0xFF5FA8
                    : joc.ultimulMesaj.contains("TETRIS") ? 0x4DE1FF : 0xFFFFFF;
            ui.textCentrat(joc.ultimulMesaj, 0.5f, ecranY(12f) - urc, 0.034f, argb(a * fade, cul));
            ui.textCentrat("+" + joc.ultimulPunctaj, 0.5f, ecranY(12f) - urc + 0.036f, 0.026f, argb(a * fade, 0xFFFFFF));
        }

        // flash la linii
        if (flashLinie > 0f) ui.panou(0f, 0f, 1f, 1f, argb(flashLinie * 0.12f, 0xFFFFFF), 0f);

        // butoane
        if (app.setari.butoanePornite() && (stare == STARE_JOC || stare == STARE_START)) {
            float lat = 1f / NR_BUTOANE;
            for (int i = 0; i < NR_BUTOANE; i++) {
                if (i == B_HOLD && !joc.holdPermis) continue;
                float x = i * lat + lat / 2f;
                float ap = bApasat[i];
                int fundal = argb(fade * (0.30f + ap * 0.35f), i == B_TRANTIRE ? 0x4DE1FF : i == B_HOLD ? 0xFF5FA8 : 0x2A3350);
                int contur = argb(fade * (0.35f + ap * 0.5f), 0x8FA3C8);
                ui.panou(x - lat / 2f + 0.008f, Y_BUTOANE + 0.012f, lat - 0.016f, 0.118f, fundal, 0.018f);
                ui.chenar(x - lat / 2f + 0.008f, Y_BUTOANE + 0.012f, lat - 0.016f, 0.118f, contur, 0.018f, 0.0018f);
                String et = ETICHETE[i];
                if (i == B_ROT_L && joc.mod == Setari.MOD_LIBER) et = "\u21C4";
                ui.textCentrat(et, x, Y_BUTOANE + 0.084f, 0.040f, argb(fade, 0xFFFFFF));
            }
        }

        if (stare == STARE_START) deseneazaStart(ui);
        if (stare == STARE_PAUZA) deseneazaPauza(ui);
        if (stare == STARE_FINAL) deseneazaFinal(ui);
    }

    private void deseneazaStart(CapaUI ui) {
        float t = timpStart / 0.9f;
        float a = t < 0.7f ? 1f : 1f - (t - 0.7f) / 0.3f;
        float s = 0.08f + (1f - neted(Math.min(1f, t * 2f))) * 0.06f;
        ui.textCentrat("START", 0.5f, ecranY(10f) + s * 0.35f, s, argb(a, 0xFFFFFF));
        String d = Setari.DESCRIERI_MODURI[joc.mod][0];
        ui.textCentrat(d, 0.5f, ecranY(7.5f), 0.022f, argb(a * 0.85f, 0x4DE1FF));
    }

    private void deseneazaPauza(CapaUI ui) {
        ui.panou(0f, 0f, 1f, 1f, argb(0.62f, 0x04060E), 0f);
        ui.textCentrat("PAUZA", 0.5f, 0.33f, 0.070f, ALB);
        buton(ui, "CONTINUA", 0.445f, 0x2A3350, 0x4DE1FF);
        buton(ui, "RESTART", 0.555f, 0x2A3350, 0xFFD84D);
        buton(ui, "MENIU", 0.665f, 0x2A3350, 0xFF5FA8);
    }

    private void buton(CapaUI ui, String text, float yc, int fundal, int contur) {
        ui.buton(text, 0.5f, yc, 0.56f, 0.075f, argb(0.85f, fundal), argb(0.9f, contur), ALB, 0.030f);
    }

    private void deseneazaFinal(CapaUI ui) {
        float a = neted(Math.min(1f, timpFinal / 0.8f));
        ui.panou(0f, 0f, 1f, 1f, argb(0.70f * a, 0x04060E), 0f);
        boolean v = joc.victorie;
        String titlu = v ? (joc.mod == Setari.MOD_SPRINT ? "GATA!" : "TIMP EXPIRAT") : "GAME OVER";
        int culT = v ? 0x7CFF6B : 0xFF5F5F;
        ui.textCentrat(titlu, 0.5f, 0.24f, 0.065f, argb(a, culT));

        float y = 0.33f;
        ui.fereastra(0.14f, y - 0.02f, 0.72f, 0.31f, argb(0.75f * a, 0x0B1020), argb(0.6f * a, 0x4DE1FF));

        if (joc.mod == Setari.MOD_SPRINT) {
            rand(ui, "TIMP", Joc.formatTimp(joc.timp), y + 0.035f, a, recordSprintNou ? 0xFFD84D : 0xFFFFFF);
            rand(ui, "LINII", joc.linii + " / " + Setari.SPRINT_LINII, y + 0.085f, a, 0xFFFFFF);
            rand(ui, "PIESE", joc.piese + "", y + 0.135f, a, 0xFFFFFF);
            rand(ui, "PPS", String.format("%.2f", joc.piese / Math.max(0.1f, joc.timp)), y + 0.185f, a, 0xFFFFFF);
            int rec = app.setari.recordSprintMs();
            rand(ui, "RECORD", rec > 0 ? Joc.formatTimp(rec / 1000f) : "-", y + 0.235f, a, 0xFFD84D);
            if (recordSprintNou) ui.textCentrat("RECORD NOU!", 0.5f, 0.30f, 0.026f, argb(a, 0xFFD84D));
        } else {
            rand(ui, "SCOR", joc.scor + "", y + 0.035f, a, recordNou ? 0xFFD84D : 0xFFFFFF);
            rand(ui, "LINII", joc.linii + "", y + 0.085f, a, 0xFFFFFF);
            rand(ui, joc.mod == Setari.MOD_ULTRA ? "PIESE" : "NIVEL",
                 joc.mod == Setari.MOD_ULTRA ? joc.piese + "" : joc.nivel + "", y + 0.135f, a, 0xFFFFFF);
            rand(ui, "TETRIS / T-SPIN", joc.tetrisuri + " / " + joc.tspinuri, y + 0.185f, a, 0xFFFFFF);
            rand(ui, "RECORD", app.setari.record(joc.mod) + "", y + 0.235f, a, 0xFFD84D);
            if (recordNou) ui.textCentrat("RECORD NOU!", 0.5f, 0.30f, 0.026f, argb(a, 0xFFD84D));
        }

        if (timpFinal > 0.8f) {
            buton(ui, "DIN NOU", 0.745f, 0x2A3350, 0x4DE1FF);
            buton(ui, "MENIU", 0.855f, 0x2A3350, 0xFF5FA8);
        }
    }

    private void rand(CapaUI ui, String et, String val, float y, float a, int cul) {
        ui.text(et, 0.19f, y, 0.022f, argb(a * 0.75f, 0xB9C2D6));
        ui.textDreapta(val, 0.81f, y, 0.026f, argb(a, cul));
    }
}
