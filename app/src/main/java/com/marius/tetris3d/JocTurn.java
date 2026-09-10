package com.marius.tetris3d;

import java.util.Random;

/**
 * Modul TURN: o baza circulara cu 8 pozitii, pe care o invarti cu degetul.
 * Piesa cade mereu in centrul ecranului, pe pozitia din fata.
 * Cand un inel complet (toate cele 8 pozitii de pe un nivel) e plin, dispare.
 */
public class JocTurn {

    /** cate pozitii are cercul de baza */
    public static final int POZITII = 8;
    /** cat de inalt poate creste turnul */
    public static final int NIVELE = 16;

    /** turnul: [nivel][pozitie] = culoarea blocului, 0 = gol */
    public int[][] turn = new int[NIVELE][POZITII];

    /** formele posibile ale pieselor, ca latime pe cerc (1, 2 sau 3 pozitii) */
    private static final int[][] FORME = {
        {1},           // un bloc
        {1, 1},        // doua alaturate
        {1, 1, 1},     // trei alaturate
        {1, 0, 1},     // doua cu gol la mijloc
        {1, 1},        // doua alaturate
        {1},           // un bloc
        {1, 1, 1}      // trei alaturate
    };

    /** cate blocuri pune fiecare forma */
    private static int latimeForma(int tip) {
        return FORME[tip].length;
    }

    public int tipCurent;
    public int tipUrmator;
    public float inaltimePiesa;   // inaltimea la care e piesa acum, in nivele

    public int scor = 0;
    public int record = 0;
    public int inele = 0;         // cate inele complete ai facut
    public int nivel = 1;

    public int pieseAsezate = 0;
    public boolean terminat = false;

    public Particule particule;
    public Sunet sunet;

    public float vitezaInitiala = 0.75f;
    public float[][] culori;

    /** unghiul bazei, in grade; degetul il schimba direct */
    public float unghiBaza = 0f;
    private float vitezaUnghi = 0f;
    private boolean degetPeEcran = false;

    private final Random rnd = new Random();
    private float vitezaCadere = 0.75f;

    public static final float PAS_GRADE = 360f / POZITII;

    /** tremurul fiecarui nivel, pentru efect de impact */
    public final float[] tremurNivel = new float[NIVELE];
    public float cutremurGlobal = 0f;

    public JocTurn() {
        tipUrmator = rnd.nextInt(FORME.length);
        pieseNoua();
    }

    public static int normP(int p) {
        int n = p % POZITII;
        if (n < 0) n += POZITII;
        return n;
    }

    /** pozitia aflata acum exact in fata camerei */
    public int pozitiaDinFata() {
        int p = Math.round(-unghiBaza / PAS_GRADE);
        return normP(p);
    }

    /** pozitiile pe care le va ocupa piesa curenta */
    public int[] pozitiiPiesa() {
        int[] forma = FORME[tipCurent];
        int centru = pozitiaDinFata();
        int start = centru - forma.length / 2;

        int[] rez = new int[forma.length];
        for (int i = 0; i < forma.length; i++) {
            rez[i] = (forma[i] == 1) ? normP(start + i) : -1;
        }
        return rez;
    }

    private void pieseNoua() {
        tipCurent = tipUrmator;
        tipUrmator = rnd.nextInt(FORME.length);
        inaltimePiesa = NIVELE - 1;

        if (nivelBlocat(NIVELE - 1)) {
            terminat = true;
            if (scor > record) record = scor;
            if (sunet != null) sunet.final_();
        }
    }

    /** verifica daca piesa se loveste de ceva la un anumit nivel */
    private boolean nivelBlocat(int niv) {
        if (niv < 0) return true;
        if (niv >= NIVELE) return false;

        int[] poz = pozitiiPiesa();
        for (int p : poz) {
            if (p < 0) continue;
            if (turn[niv][p] != 0) return true;
        }
        return false;
    }

    public void jocNou() {
        for (int n = 0; n < NIVELE; n++) {
            for (int p = 0; p < POZITII; p++) turn[n][p] = 0;
            tremurNivel[n] = 0f;
        }
        scor = 0; inele = 0; nivel = 1;
        pieseAsezate = 0;
        vitezaCadere = vitezaInitiala;
        terminat = false;
        cutremurGlobal = 0f;
        unghiBaza = 0f;
        vitezaUnghi = 0f;
        tipUrmator = rnd.nextInt(FORME.length);
        pieseNoua();
    }

    // ---------- rotirea bazei cu degetul ----------

    public void degetJos() {
        degetPeEcran = true;
        vitezaUnghi = 0f;
    }

    public void rotesteContinuu(float deltaGrade) {
        if (terminat) return;
        unghiBaza += deltaGrade;
        vitezaUnghi = deltaGrade;
    }

    public void degetSus() {
        degetPeEcran = false;
    }

    private void alinieazaBaza(float dt) {
        if (degetPeEcran) return;

        if (Math.abs(vitezaUnghi) > 0.06f) {
            unghiBaza += vitezaUnghi;
            vitezaUnghi *= 0.86f;
            return;
        }
        vitezaUnghi = 0f;

        float tinta = Math.round(unghiBaza / PAS_GRADE) * PAS_GRADE;
        float dif = tinta - unghiBaza;
        unghiBaza += dif * Math.min(1f, dt * 14f);
        if (Math.abs(dif) < 0.05f) unghiBaza = tinta;
    }

    public void coboaraRapid() {
        if (terminat) return;
        inaltimePiesa -= 1f;
        if (inaltimePiesa < 0f) inaltimePiesa = 0f;
        cutremurGlobal = Math.min(1f, cutremurGlobal + 0.3f);
    }

    public void trantesteJos() {
        if (terminat) return;
        int niv = (int) Math.floor(inaltimePiesa);
        while (niv > 0 && !nivelBlocat(niv - 1)) niv--;
        inaltimePiesa = niv;
        cutremurGlobal = 1f;
        if (sunet != null) sunet.trantire();
        aseaza(false);
    }

    /** unde va ateriza piesa */
    public int nivelAterizare() {
        int niv = (int) Math.floor(inaltimePiesa);
        while (niv > 0 && !nivelBlocat(niv - 1)) niv--;
        return niv;
    }

    public float apropiere() {
        float dist = inaltimePiesa - nivelAterizare();
        if (dist < 0f) dist = 0f;
        float p = 1f - (dist / 8f);
        if (p < 0f) p = 0f;
        if (p > 1f) p = 1f;
        return p;
    }

    private void aseaza(boolean cuSunet) {
        int niv = (int) Math.floor(inaltimePiesa);
        if (niv < 0) niv = 0;
        if (niv >= NIVELE) niv = NIVELE - 1;

        int[] poz = pozitiiPiesa();
        for (int p : poz) {
            if (p < 0) continue;
            turn[niv][p] = tipCurent + 1;
        }

        tremurNivel[niv] = 1f;
        if (niv > 0) tremurNivel[niv - 1] = 0.7f;
        cutremurGlobal = Math.max(cutremurGlobal, 0.6f);

        pieseAsezate++;
        if (cuSunet && sunet != null) sunet.aterizare();

        verificaInele();
        pieseNoua();
    }

    private void verificaInele() {
        int sterse = 0;

        for (int n = 0; n < NIVELE; n++) {
            boolean plin = true;
            for (int p = 0; p < POZITII; p++) {
                if (turn[n][p] == 0) { plin = false; break; }
            }

            if (plin) {
                if (particule != null) particule.explozie(n, POZITII);

                for (int nn = n; nn < NIVELE - 1; nn++) {
                    System.arraycopy(turn[nn + 1], 0, turn[nn], 0, POZITII);
                }
                for (int p = 0; p < POZITII; p++) turn[NIVELE - 1][p] = 0;

                sterse++;
                n--;
            }
        }

        if (sterse > 0) {
            int nivelVechi = nivel;
            inele += sterse;

            switch (sterse) {
                case 1: scor += 300 * nivel; break;
                case 2: scor += 900 * nivel; break;
                default: scor += 2000 * nivel; break;
            }

            if (sunet != null) {
                if (sterse >= 2) sunet.tetris();
                else sunet.linie();
            }

            if (scor > record) record = scor;
            nivel = 1 + inele / 4;
            recalculeazaViteza();
            cutremurGlobal = 1f;

            if (nivel > nivelVechi && sunet != null) sunet.nivel();
        }
    }

    private void recalculeazaViteza() {
        float v = vitezaInitiala;
        for (int i = 1; i < nivel; i++) v *= 0.85f;
        vitezaCadere = Math.max(0.12f, v);
    }

    public void actualizeaza(float dt) {
        stingeEfecte(dt);
        alinieazaBaza(dt);

        if (terminat) return;

        // piesa coboara lin, continuu
        inaltimePiesa -= dt / vitezaCadere;

        int nivAteriz = nivelAterizare();
        if (inaltimePiesa <= nivAteriz) {
            inaltimePiesa = nivAteriz;
            aseaza(true);
        }
    }

    public void stingeEfecte(float dt) {
        for (int n = 0; n < NIVELE; n++) {
            tremurNivel[n] -= dt * 2.2f;
            if (tremurNivel[n] < 0f) tremurNivel[n] = 0f;
        }
        cutremurGlobal -= dt * 2.6f;
        if (cutremurGlobal < 0f) cutremurGlobal = 0f;
    }

    /** cate pozitii sunt ocupate pe un nivel, pentru afisaj */
    public int pozitiiPline(int niv) {
        int n = 0;
        for (int p = 0; p < POZITII; p++) if (turn[niv][p] != 0) n++;
        return n;
    }

    /** inaltimea maxima a turnului */
    public int inaltimeTurn() {
        for (int n = NIVELE - 1; n >= 0; n--) {
            if (pozitiiPline(n) > 0) return n + 1;
        }
        return 0;
    }
}
