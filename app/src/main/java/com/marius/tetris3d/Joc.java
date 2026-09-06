package com.marius.tetris3d;

import java.util.Random;

public class Joc {

    public static final int COLOANE = 10;
    public static final int RANDURI = 20;

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

    public int tipCurent;
    public int rotatie;
    public int pieseX;
    public int pieseY;

    public int scor = 0;
    public int linii = 0;
    public int nivel = 1;
    public boolean terminat = false;

    public Particule particule;

    private final Random rnd = new Random();
    private float ceas = 0f;
    private float vitezaCadere = 0.75f;

    private float alunecare = 0f;
    private final float[] scaraRanduri = new float[RANDURI];

    public Joc() {
        for (int i = 0; i < RANDURI; i++) scaraRanduri[i] = 1f;
        pieseNoua();
    }

    private void pieseNoua() {
        tipCurent = rnd.nextInt(7);
        rotatie = 0;
        pieseX = 3;
        pieseY = RANDURI - 1;
        alunecare = 0f;
        ceas = 0f;
        if (ciocnire(pieseX, pieseY, rotatie)) {
            terminat = true;
            reseteaza();
        }
    }

    private void reseteaza() {
        for (int r = 0; r < RANDURI; r++)
            for (int c = 0; c < COLOANE; c++)
                tabla[r][c] = 0;
        scor = 0; linii = 0; nivel = 1;
        vitezaCadere = 0.75f;
        terminat = false;
        tipCurent = rnd.nextInt(7);
        rotatie = 0; pieseX = 3; pieseY = RANDURI - 1;
        alunecare = 0f;
        ceas = 0f;
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
        }
    }

    public void roteste() {
        if (terminat) return;
        int nou = (rotatie + 1) % 4;
        if (!ciocnire(pieseX, pieseY, nou))     { rotatie = nou; return; }
        if (!ciocnire(pieseX - 1, pieseY, nou)) { pieseX--; rotatie = nou; return; }
        if (!ciocnire(pieseX + 1, pieseY, nou)) { pieseX++; rotatie = nou; return; }
        if (!ciocnire(pieseX - 2, pieseY, nou)) { pieseX -= 2; rotatie = nou; }
    }

    public void coboaraRapid() {
        if (terminat) return;
        if (!ciocnire(pieseX, pieseY - 1, rotatie)) {
            pieseY--;
            scor += 1;
            ceas = 0f;
            alunecare = 0f;
        }
    }

    public void trantesteJos() {
        if (terminat) return;
        while (!ciocnire(pieseX, pieseY - 1, rotatie)) {
            pieseY--;
            scor += 2;
        }
        alunecare = 0f;
        ceas = 0f;
        aseaza();
    }

    public int pozitieFantoma() {
        int y = pieseY;
        while (!ciocnire(pieseX, y - 1, rotatie)) y--;
        return y - 3;
    }

    public float pieseYVizual() {
        return (pieseY - 3) + alunecare;
    }

    public float scaraRand(int r) {
        return scaraRanduri[r];
    }

    private void aseaza() {
        int[][] f = formaCurenta();
        for (int i = 0; i < 4; i++) {
            int x = pieseX + f[i][0];
            int y = pieseY + f[i][1] - 3;
            if (y >= 0 && y < RANDURI && x >= 0 && x < COLOANE) {
                tabla[y][x] = tipCurent + 1;
            }
        }
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
            linii += sterse;
            switch (sterse) {
                case 1: scor += 100 * nivel; break;
                case 2: scor += 300 * nivel; break;
                case 3: scor += 500 * nivel; break;
                default: scor += 800 * nivel; break;
            }
            nivel = 1 + linii / 10;
            vitezaCadere = Math.max(0.09f, 0.75f - (nivel - 1) * 0.06f);
        }
    }

    public void actualizeaza(float dt) {
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
                aseaza();
            }
        }
    }
    }
