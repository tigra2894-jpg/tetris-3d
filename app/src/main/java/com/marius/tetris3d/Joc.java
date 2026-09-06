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

    public boolean terminat = false;
    public boolean pauza = false;

    public Particule particule;
    public Sunet sunet;

    private final Random rnd = new Random();
    private float ceas = 0f;
    private float vitezaCadere = 0.75f;
    private float alunecare = 0f;

    public final float[] tremurRand = new float[RANDURI];
    public float cutremurGlobal = 0f;

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
        if (ciocnire(pieseX, pieseY, rotatie)) {
            terminat = true;
            if (scor > record) record = scor;
            if (sunet != null) sunet.final_();
        }
    }

    public void jocNou() {
        for (int r = 0; r < RANDURI; r++)
            for (int c = 0; c < COLOANE; c++)
                tabla[r][c] = 0;
        for (int r = 0; r < RANDURI; r++) tremurRand[r] = 0f;
        scor = 0; linii = 0; nivel = 1;
        vitezaCadere = 0.75f;
        terminat = false;
        pauza = false;
        cutremurGlobal = 0f;
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

    private boolean activ() {
        return !terminat && !pauza;
    }

    public void muta(int dir) {
        if (!activ()) return;
        if (!ciocnire(pieseX + dir, pieseY, rotatie)) {
            pieseX += dir;
            if (sunet != null) sunet.mutare();
        }
    }

    public void roteste() {
        if (!activ()) return;
        int nou = (rotatie + 1) % 4;
        boolean reusit = false;

        if (!ciocnire(pieseX, pieseY, nou))          { rotatie = nou; reusit = true; }
        else if (!ciocnire(pieseX - 1, pieseY, nou)) { pieseX--; rotatie = nou; reusit = true; }
        else if (!ciocnire(pieseX + 1, pieseY, nou)) { pieseX++; rotatie = nou; reusit = true; }
        else if (!ciocnire(pieseX - 2, pieseY, nou)) { pieseX -= 2; rotatie = nou; reusit = true; }

        if (reusit && sunet != null) sunet.rotire();
    }

    public void coboaraRapid() {
        if (!activ()) return;
        if (!ciocnire(pieseX, pieseY - 1, rotatie)) {
            pieseY--;
            ceas = 0f;
            alunecare = 0f;
            cutremurGlobal = Math.min(1f, cutremurGlobal + 0.35f);
        }
    }

    public void trantesteJos() {
        if (!activ()) return;
        while (!ciocnire(pieseX, pieseY - 1, rotatie)) pieseY--;
        alunecare = 0f;
        ceas = 0f;
        cutremurGlobal = 1f;
        if (sunet != null) sunet.trantire();
        aseaza(false);
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

    private void aseaza(boolean cuSunet) {
        int[][] f = formaCurenta();
        for (int i = 0; i < 4; i++) {
            int x = pieseX + f[i][0];
            int y = pieseY + f[i][1] - 3;
            if (y >= 0 && y < RANDURI && x >= 0 && x < COLOANE) {
                tabla[y][x] = tipCurent + 1;
                tremurRand[y] = 1f;
                if (y > 0) tremurRand[y - 1] = 0.7f;
            }
        }
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
                case 1: scor += 100 * nivel; break;
                case 2: scor += 300 * nivel; break;
                case 3: scor += 500 * nivel; break;
                default: scor += 800 * nivel; break;
            }

            if (sunet != null) {
                if (sterse >= 4) sunet.tetris();
                else sunet.linie();
            }

            if (scor > record) record = scor;
            nivel = 1 + linii / 10;
            vitezaCadere = Math.max(0.09f, 0.75f - (nivel - 1) * 0.06f);
            cutremurGlobal = Math.min(1f, cutremurGlobal + 0.5f * sterse);

            if (nivel > nivelVechi && sunet != null) sunet.nivel();
        }
    }

    public void actualizeaza(float dt) {
        for (int r = 0; r < RANDURI; r++) {
            tremurRand[r] -= dt * 2.2f;
            if (tremurRand[r] < 0f) tremurRand[r] = 0f;
        }
        cutremurGlobal -= dt * 2.6f;
        if (cutremurGlobal < 0f) cutremurGlobal = 0f;

        if (!activ()) return;

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
                aseaza(true);
            }
        }
    }
    }
