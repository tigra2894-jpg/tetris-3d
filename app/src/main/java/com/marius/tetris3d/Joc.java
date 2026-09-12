package com.marius.tetris3d;

import java.util.Random;

public class Joc {

    public static final int COLOANE = 10;
    public static final int RANDURI = 20;

    public int[][] tabla = new int[RANDURI][COLOANE];
    public int[][] crapaturi = new int[RANDURI][COLOANE];

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
    public int pieseX;
    public int pieseY;

    public int scor = 0;
    public int record = 0;
    public int linii = 0;
    public int nivel = 1;

    public int pieseAsezate = 0;
    public int tetrisuriFacute = 0;
    public int blocuriSparte = 0;

    public boolean terminat = false;

    public Particule particule;
    public Sunet sunet;

    public float vitezaInitiala = 0.75f;

    public float[][] culori;
    public boolean stilSticla = false;

    private final Random rnd = new Random();
    private float ceas = 0f;
    private float vitezaCadere = 0.75f;
    private float alunecare = 0f;

    private static final int LINII_PE_NIVEL = 8;

    public final float[] tremurRand = new float[RANDURI];
    public float cutremurGlobal = 0f;

    public float animRotire = 0f;
    public int directieRotire = 1;

    public int randValLumina = -1;
    public float valLumina = 0f;

    /** cate randuri s-au stins la final; merge de sus in jos */
    public float stingereFinal = 0f;
    /** true cat timp ruleaza animatia de final */
    public boolean seStinge = false;

    public Joc() {
        tipUrmator = rnd.nextInt(7);
        pieseNoua();
    }

    private void pieseNoua() {
        tipCurent = tipUrmator;
        tipUrmator = rnd.nextInt(7);
        rotatie = 0;
        pieseX = 3;
        pieseY = RANDURI - 1;
        alunecare = 0f;
        ceas = 0f;
        animRotire = 0f;
        if (ciocnire(pieseX, pieseY, rotatie)) {
            terminat = true;
            seStinge = true;
            stingereFinal = 0f;
            if (scor > record) record = scor;
            if (sunet != null) sunet.final_();
        }
    }

    public void jocNou() {
        for (int r = 0; r < RANDURI; r++) {
            for (int c = 0; c < COLOANE; c++) {
                tabla[r][c] = 0;
                crapaturi[r][c] = 0;
            }
            tremurRand[r] = 0f;
        }

        scor = 0; linii = 0; nivel = 1;
        pieseAsezate = 0;
        tetrisuriFacute = 0;
        blocuriSparte = 0;
        vitezaCadere = vitezaInitiala;
        terminat = false;
        seStinge = false;
        stingereFinal = 0f;
        cutremurGlobal = 0f;
        animRotire = 0f;
        valLumina = 0f;
        randValLumina = -1;
        tipUrmator = rnd.nextInt(7);
        pieseNoua();
    }

    public int[][] formaCurenta() {
        return PIESE[tipCurent][rotatie];
    }

    private boolean ciocnire(int px, int py, int rot) {
        int[][] f = PIESE[tipCurent][rot];
        for (int i = 0; i < 4; i++) {
            int x = px + f[i][0];
            int y = py + f[i][1] - 3;
            if (x < 0 || x >= COLOANE) return true;
            if (y < 0) return true;
            if (y < RANDURI && tabla[y][x] != 0) return true;
        }
        return false;
    }

    public void muta(int dir) {
        if (terminat) return;
        if (!ciocnire(pieseX + dir, pieseY, rotatie)) {
            pieseX += dir;
            if (sunet != null) sunet.mutare();
        }
    }

    public void roteste() {
        if (terminat) return;
        int nou = (rotatie + 1) % 4;
        boolean reusit = false;

        if (!ciocnire(pieseX, pieseY, nou))          { rotatie = nou; reusit = true; }
        else if (!ciocnire(pieseX - 1, pieseY, nou)) { pieseX--; rotatie = nou; reusit = true; }
        else if (!ciocnire(pieseX + 1, pieseY, nou)) { pieseX++; rotatie = nou; reusit = true; }
        else if (!ciocnire(pieseX - 2, pieseY, nou)) { pieseX -= 2; rotatie = nou; reusit = true; }

        if (reusit) {
            animRotire = 1f;
            directieRotire = 1;
            if (sunet != null) sunet.rotire();
        }
    }

    public void schimbaForma() {
        if (terminat) return;

        int vechiTip = tipCurent;
        int vechiRot = rotatie;
        int vechiX = pieseX;
        int vechiY = pieseY;

        int nouTip, nouRot;
        if (tipCurent == 0 && rotatie == 0) {
            nouTip = 0;
            nouRot = 1;
        } else {
            nouTip = (tipCurent + 1) % 7;
            nouRot = 0;
        }

        tipCurent = nouTip;
        rotatie = nouRot;

        if (gasesteLocValid()) {
            animRotire = 1f;
            directieRotire = -1;
            if (sunet != null) sunet.rotire();
        } else {
            tipCurent = vechiTip;
            rotatie = vechiRot;
            pieseX = vechiX;
            pieseY = vechiY;
        }
    }

    private boolean gasesteLocValid() {
        int yOriginal = pieseY;

        for (int dx = 0; dx <= COLOANE; dx++) {
            int xDreapta = pieseX + dx;
            int xStanga = pieseX - dx;

            if (dx == 0) {
                if (!ciocnire(pieseX, yOriginal, rotatie)) {
                    pieseY = yOriginal;
                    return true;
                }
            } else {
                if (xDreapta < COLOANE && !ciocnire(xDreapta, yOriginal, rotatie)) {
                    pieseX = xDreapta;
                    pieseY = yOriginal;
                    return true;
                }
                if (xStanga >= 0 && !ciocnire(xStanga, yOriginal, rotatie)) {
                    pieseX = xStanga;
                    pieseY = yOriginal;
                    return true;
                }
            }
        }

        for (int dy = 1; dy <= 6; dy++) {
            int yNou = yOriginal + dy;
            if (!ciocnire(3, yNou, rotatie)) {
                pieseX = 3;
                pieseY = yNou;
                return true;
            }
        }

        return false;
    }

    public void coboaraRapid() {
        if (terminat) return;
        if (!ciocnire(pieseX, pieseY - 1, rotatie)) {
            pieseY--;
            ceas = 0f;
            alunecare = 0f;
            cutremurGlobal = Math.min(1f, cutremurGlobal + 0.35f);
        }
    }

    public void trantesteJos() {
        if (terminat) return;
        while (!ciocnire(pieseX, pieseY - 1, rotatie)) pieseY--;
        alunecare = 0f;
        ceas = 0f;
        cutremurGlobal = 1f;
        if (sunet != null) sunet.trantire();
        aseaza(false, true);
    }

    public int pozitieFantoma() {
        int y = pieseY;
        while (!ciocnire(pieseX, y - 1, rotatie)) y--;
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
            if (pieseX + f[i][0] == c) return true;
        }
        return false;
    }

    private void aseaza(boolean cuSunet, boolean impactTare) {
        int[][] f = formaCurenta();

        for (int i = 0; i < 4; i++) {
            int x = pieseX + f[i][0];
            int y = pieseY + f[i][1] - 3;
            if (y >= 0 && y < RANDURI && x >= 0 && x < COLOANE) {
                tabla[y][x] = tipCurent + 1;
                crapaturi[y][x] = 0;
                tremurRand[y] = 1f;
                if (y > 0) tremurRand[y - 1] = 0.7f;
            }
        }

        cutremurGlobal = Math.max(cutremurGlobal, impactTare ? 1f : 0.55f);
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
                randValLumina = r;
                valLumina = 1f;

                if (particule != null) {
                    if (stilSticla && culori != null) {
                        particule.explozieColorata(r, COLOANE, tabla[r], culori);
                    } else {
                        particule.explozie(r, COLOANE);
                    }
                }
                for (int rr = r; rr < RANDURI - 1; rr++) {
                    System.arraycopy(tabla[rr + 1], 0, tabla[rr], 0, COLOANE);
                    System.arraycopy(crapaturi[rr + 1], 0, crapaturi[rr], 0, COLOANE);
                }
                for (int c = 0; c < COLOANE; c++) {
                    tabla[RANDURI - 1][c] = 0;
                    crapaturi[RANDURI - 1][c] = 0;
                }
                sterse++;
                r--;
            }
        }

        if (sterse > 0) {
            int nivelVechi = nivel;
            linii += sterse;

            switch (sterse) {
                case 1: scor += 100 * nivel; break;
                case 2: scor += 300 * nivel; break;
                case 3: scor += 500 * nivel; break;
                default:
                    scor += 800 * nivel;
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
        for (int i = 1; i < nivel; i++) {
            v *= 0.82f;
        }
        vitezaCadere = Math.max(0.075f, v);
    }

    public void actualizeaza(float dt) {
        stingeEfecte(dt);

        if (terminat) return;

        ceas += dt;

        if (!ciocnire(pieseX, pieseY - 1, rotatie)) {
            alunecare = -(ceas / vitezaCadere);
            if (alunecare < -1f) alunecare = -1f;
        } else {
            alunecare = 0f;
        }

        if (ceas >= vitezaCadere) {
            ceas = 0f;
            alunecare = 0f;
            if (!ciocnire(pieseX, pieseY - 1, rotatie)) {
                pieseY--;
            } else {
                aseaza(true, false);
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

        animRotire -= dt * 5.5f;
        if (animRotire < 0f) animRotire = 0f;

        valLumina -= dt * 2.0f;
        if (valLumina < 0f) {
            valLumina = 0f;
            randValLumina = -1;
        }

        // stingerea de final: rand cu rand, de sus in jos
        if (seStinge) {
            stingereFinal += dt * 16f;
            if (stingereFinal >= RANDURI + 3) {
                stingereFinal = RANDURI + 3;
                seStinge = false;
            }
        }
    }

    /** cat de stins e un rand la final: 0 = intact, 1 = complet stins */
    public float stinsRand(int rand) {
        if (stingereFinal <= 0f) return 0f;

        float prag = RANDURI - stingereFinal;
        float dif = rand - prag;

        if (dif > 1.4f) return 0f;
        if (dif < 0f) return 1f;
        return 1f - dif / 1.4f;
    }
}
