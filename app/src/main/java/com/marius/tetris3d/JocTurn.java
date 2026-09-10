package com.marius.tetris3d;

import java.util.Random;

/**
 * Modul TURN, varianta A:
 * piesa cade mereu in fata camerei, iar tu rotesti turnul dedesubt
 * ca sa alegi pe ce fata aterizeaza.
 * O linie se sterge doar cand e completa pe toate cele 12 coloane.
 */
public class JocTurn {

    public static final int COLOANE = 12;
    public static final int RANDURI = 18;

    public int[][] tabla = new int[RANDURI][COLOANE];

    private static final int[][][][] PIESE = {
        {{{0,2},{1,2},{2,2},{3,2}}, {{2,0},{2,1},{2,2},{2,3}},
         {{0,1},{1,1},{2,1},{3,1}}, {{1,0},{1,1},{1,2},{1,3}}},
        {{{1,1},{2,1},{1,2},{2,2}}, {{1,1},{2,1},{1,2},{2,2}},
         {{1,1},{2,1},{1,2},{2,2}}, {{1,1},{2,1},{1,2},{2,2}}},
        {{{1,1},{0,2},{1,2},{2,2}}, {{1,1},{1,2},{2,2},{1,3}},
         {{0,2},{1,2},{2,2},{1,3}}, {{1,1},{0,2},{1,2},{1,3}}},
        {{{1,1},{2,1},{0,2},{1,2}}, {{1,1},{1,2},{2,2},{2,3}},
         {{1,2},{2,2},{0,3},{1,3}}, {{0,1},{0,2},{1,2},{1,3}}},
        {{{0,1},{1,1},{1,2},{2,2}}, {{2,1},{1,2},{2,2},{1,3}},
         {{0,2},{1,2},{1,3},{2,3}}, {{1,1},{0,2},{1,2},{0,3}}},
        {{{0,1},{0,2},{1,2},{2,2}}, {{1,1},{2,1},{1,2},{1,3}},
         {{0,2},{1,2},{2,2},{2,3}}, {{1,1},{1,2},{0,3},{1,3}}},
        {{{2,1},{0,2},{1,2},{2,2}}, {{1,1},{1,2},{1,3},{2,3}},
         {{0,2},{1,2},{2,2},{0,3}}, {{0,1},{1,1},{1,2},{1,3}}}
    };

    public static int[][] formaPiesei(int tip, int rot) {
        return PIESE[tip][rot];
    }

    public int tipCurent;
    public int tipUrmator;
    public int rotatie;
    public int pieseY;

    public int scor = 0;
    public int record = 0;
    public int linii = 0;
    public int nivel = 1;

    public int pieseAsezate = 0;
    public int tetrisuriFacute = 0;

    public boolean terminat = false;

    public Particule particule;
    public Sunet sunet;

    public float vitezaInitiala = 0.75f;
    public float[][] culori;

    /** unghiul turnului in grade; degetul il schimba direct */
    public float unghiTurn = 0f;

    /** cat de repede se opreste turnul dupa ce ridici degetul */
    private float vitezaUnghi = 0f;
    private boolean degetPeEcran = false;

    private final Random rnd = new Random();
    private float ceas = 0f;
    private float vitezaCadere = 0.75f;
    private float alunecare = 0f;

    private static final int LINII_PE_NIVEL = 6;
    public static final float PAS_GRADE = 360f / COLOANE;

    public final float[] tremurRand = new float[RANDURI];
    public float cutremurGlobal = 0f;

    public JocTurn() {
        tipUrmator = rnd.nextInt(7);
        pieseNoua();
    }

    public static int normX(int x) {
        int n = x % COLOANE;
        if (n < 0) n += COLOANE;
        return n;
    }

    /** coloana aflata acum exact in fata camerei */
    public int coloanaDinFata() {
        int c = Math.round(-unghiTurn / PAS_GRADE);
        return normX(c);
    }

    /** coloana pe care sta un patratel al piesei; piesa e mereu in fata */
    public int coloanaPiesei(int offsetX) {
        return normX(coloanaDinFata() + offsetX - 1);
    }

    private void pieseNoua() {
        tipCurent = tipUrmator;
        tipUrmator = rnd.nextInt(7);
        rotatie = 0;
        pieseY = RANDURI - 1;
        alunecare = 0f;
        ceas = 0f;
        if (ciocnire(pieseY, rotatie)) {
            terminat = true;
            if (scor > record) record = scor;
            if (sunet != null) sunet.final_();
        }
    }

    public void jocNou() {
        for (int r = 0; r < RANDURI; r++) {
            for (int c = 0; c < COLOANE; c++) tabla[r][c] = 0;
            tremurRand[r] = 0f;
        }
        scor = 0; linii = 0; nivel = 1;
        pieseAsezate = 0;
        tetrisuriFacute = 0;
        vitezaCadere = vitezaInitiala;
        terminat = false;
        cutremurGlobal = 0f;
        unghiTurn = 0f;
        vitezaUnghi = 0f;
        tipUrmator = rnd.nextInt(7);
        pieseNoua();
    }

    public int[][] formaCurenta() {
        return PIESE[tipCurent][rotatie];
    }

    /** verifica daca piesa incape la inaltimea py, cu rotatia rot */
    private boolean ciocnire(int py, int rot) {
        int[][] f = PIESE[tipCurent][rot];
        for (int i = 0; i < 4; i++) {
            int x = coloanaPiesei(f[i][0]);
            int y = py + f[i][1] - 3;
            if (y < 0) return true;
            if (y < RANDURI && tabla[y][x] != 0) return true;
        }
        return false;
    }

    // ---------- rotirea turnului cu degetul ----------

    public void degetJos() {
        degetPeEcran = true;
        vitezaUnghi = 0f;
    }

    /** deltaGrade: cat s-a miscat degetul, transformat in grade */
    public void rotesteContinuu(float deltaGrade) {
        if (terminat) return;
        unghiTurn += deltaGrade;
        vitezaUnghi = deltaGrade;
    }

    public void degetSus() {
        degetPeEcran = false;
    }

    /** aduce turnul la cea mai apropiata fata, ca piesele sa stea aliniate */
    private void alinieazaTurn(float dt) {
        if (degetPeEcran) return;

        // inertie scurta dupa ce ridici degetul
        if (Math.abs(vitezaUnghi) > 0.05f) {
            unghiTurn += vitezaUnghi;
            vitezaUnghi *= 0.88f;
            return;
        }
        vitezaUnghi = 0f;

        float tinta = Math.round(unghiTurn / PAS_GRADE) * PAS_GRADE;
        float dif = tinta - unghiTurn;
        unghiTurn += dif * Math.min(1f, dt * 12f);
        if (Math.abs(dif) < 0.05f) unghiTurn = tinta;
    }

    public void roteste() {
        if (terminat) return;
        int nou = (rotatie + 1) % 4;
        if (!ciocnire(pieseY, nou)) {
            rotatie = nou;
            if (sunet != null) sunet.rotire();
        }
    }

    public void coboaraRapid() {
        if (terminat) return;
        if (!ciocnire(pieseY - 1, rotatie)) {
            pieseY--;
            ceas = 0f;
            alunecare = 0f;
            cutremurGlobal = Math.min(1f, cutremurGlobal + 0.35f);
        }
    }

    public void trantesteJos() {
        if (terminat) return;
        while (!ciocnire(pieseY - 1, rotatie)) pieseY--;
        alunecare = 0f;
        ceas = 0f;
        cutremurGlobal = 1f;
        if (sunet != null) sunet.trantire();
        aseaza(false);
    }

    public int pozitieFantoma() {
        int y = pieseY;
        while (!ciocnire(y - 1, rotatie)) y--;
        return y - 3;
    }

    public float pieseYVizual() {
        return (pieseY - 3) + alunecare;
    }

    public float apropiere() {
        int yAteriz = pozitieFantoma();
        float dist = pieseYVizual() - yAteriz;
        if (dist < 0f) dist = 0f;
        float p = 1f - (dist / 9f);
        if (p < 0f) p = 0f;
        if (p > 1f) p = 1f;
        return p;
    }

    public boolean coloanaTinta(int c) {
        int[][] f = formaCurenta();
        for (int i = 0; i < 4; i++) {
            if (coloanaPiesei(f[i][0]) == c) return true;
        }
        return false;
    }

    private void aseaza(boolean cuSunet) {
        int[][] f = formaCurenta();
        for (int i = 0; i < 4; i++) {
            int x = coloanaPiesei(f[i][0]);
            int y = pieseY + f[i][1] - 3;
            if (y >= 0 && y < RANDURI) {
                tabla[y][x] = tipCurent + 1;
                tremurRand[y] = 1f;
                if (y > 0) tremurRand[y - 1] = 0.7f;
            }
        }
        cutremurGlobal = Math.max(cutremurGlobal, 0.55f);
        pieseAsezate++;
        if (cuSunet && sunet != null) sunet.aterizare();
        verificaLinii();
        pieseNoua();
    }

    private void verificaLinii() {
        int sterse = 0;
        for (int r = 0; r < RANDURI; r++) {
            boolean plin = true;
            for (int c = 0; c < COLOANE; c++) {
                if (tabla[r][c] == 0) { plin = false; break; }
            }
            if (plin) {
                if (particule != null) particule.explozie(r, COLOANE);
                for (int rr = r; rr < RANDURI - 1; rr++) {
                    System.arraycopy(tabla[rr + 1], 0, tabla[rr], 0, COLOANE);
                }
                for (int c = 0; c < COLOANE; c++) tabla[RANDURI - 1][c] = 0;
                sterse++;
                r--;
            }
        }

        if (sterse > 0) {
            int nivelVechi = nivel;
            linii += sterse;

            switch (sterse) {
                case 1: scor += 250 * nivel; break;
                case 2: scor += 700 * nivel; break;
                case 3: scor += 1300 * nivel; break;
                default:
                    scor += 2200 * nivel;
                    tetrisuriFacute++;
                    break;
            }

            if (sunet != null) {
                if (sterse >= 4) sunet.tetris();
                else sunet.linie();
            }

            if (scor > record) record = scor;
            nivel = 1 + linii / LINII_PE_NIVEL;
            recalculeazaViteza();
            cutremurGlobal = Math.min(1f, cutremurGlobal + 0.5f * sterse);

            if (nivel > nivelVechi && sunet != null) sunet.nivel();
        }
    }

    private void recalculeazaViteza() {
        float v = vitezaInitiala;
        for (int i = 1; i < nivel; i++) v *= 0.84f;
        vitezaCadere = Math.max(0.09f, v);
    }

    public void actualizeaza(float dt) {
        stingeEfecte(dt);
        alinieazaTurn(dt);

        if (terminat) return;

        ceas += dt;

        if (!ciocnire(pieseY - 1, rotatie)) {
            alunecare = -(ceas / vitezaCadere);
            if (alunecare < -1f) alunecare = -1f;
        } else {
            alunecare = 0f;
        }

        if (ceas >= vitezaCadere) {
            ceas = 0f;
            alunecare = 0f;
            if (!ciocnire(pieseY - 1, rotatie)) {
                pieseY--;
            } else {
                aseaza(true);
            }
        }
    }

    public void stingeEfecte(float dt) {
        for (int r = 0; r < RANDURI; r++) {
            tremurRand[r] -= dt * 2.2f;
            if (tremurRand[r] < 0f) tremurRand[r] = 0f;
        }
        cutremurGlobal -= dt * 2.6f;
        if (cutremurGlobal < 0f) cutremurGlobal = 0f;
    }

    /** cate coloane sunt ocupate pe un rand, pentru afisaj */
    public int coloanePline(int rand) {
        int n = 0;
        for (int c = 0; c < COLOANE; c++) if (tabla[rand][c] != 0) n++;
        return n;
    }
}
