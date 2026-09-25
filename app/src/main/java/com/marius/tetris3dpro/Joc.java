package com.marius.tetris3dpro;

import java.util.Random;

/**
 * Motorul de joc. Coordonate: x = coloana (0 stanga), y = randul (0 jos).
 * Randurile RANDURI..RANDURI+3 sunt ascunse, deasupra tablei.
 *
 * Reguli moderne: generator 7-bag, coada de 5 piese, hold, rotatie SRS cu
 * kick-uri, lock delay cu resetare la miscare, T-spin, combo, back-to-back,
 * perfect clear, moduri Clasic / Sprint (40 linii) / Ultra (2 minute) / Liber.
 */
public class Joc {

    public static final int COLOANE = 10;
    public static final int RANDURI = 20;
    public static final int RANDURI_TOTAL = RANDURI + 4;

    public static final int P_I = 0, P_O = 1, P_T = 2, P_S = 3, P_Z = 4, P_J = 5, P_L = 6;
    public static final int NR_PIESE = 7;
    public static final int COADA = 5;

    public static final float LOCK_DELAY = 0.5f;
    public static final int MAX_RESETARI_LOCK = 15;
    public static final float DURATA_STERGERE = 0.32f;
    public static final float DURATA_APARITIE = 0.06f;

    /** forme de baza, y in sus, relativ la centrul de rotatie */
    private static final int[][][] BAZA = {
        {{-1, 0}, {0, 0}, {1, 0}, {2, 0}},   // I
        {{0, 0}, {1, 0}, {0, 1}, {1, 1}},    // O
        {{-1, 0}, {0, 0}, {1, 0}, {0, 1}},   // T
        {{-1, 0}, {0, 0}, {0, 1}, {1, 1}},   // S
        {{-1, 1}, {0, 1}, {0, 0}, {1, 0}},   // Z
        {{-1, 1}, {-1, 0}, {0, 0}, {1, 0}},  // J
        {{-1, 0}, {0, 0}, {1, 0}, {1, 1}}    // L
    };

    /** FORME[tip][rot][celula][0=x,1=y] */
    public static final int[][][][] FORME = new int[NR_PIESE][4][4][2];

    private static final int[][][] OFFSET_JLSTZ = {
        {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
        {{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}},
        {{0, 0}, {0, 0}, {0, 0}, {0, 0}, {0, 0}},
        {{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}}
    };
    private static final int[][][] OFFSET_I = {
        {{0, 0}, {-1, 0}, {2, 0}, {-1, 0}, {2, 0}},
        {{-1, 0}, {0, 0}, {0, 0}, {0, 1}, {0, -2}},
        {{-1, 1}, {1, 1}, {-2, 1}, {1, 0}, {-2, 0}},
        {{0, 1}, {0, 1}, {0, 1}, {0, -1}, {0, 2}}
    };
    private static final int[][][] OFFSET_O = {
        {{0, 0}}, {{0, -1}}, {{-1, -1}}, {{-1, 0}}
    };
    private static final int[][] KICK_180 = {
        {0, 0}, {0, 1}, {1, 1}, {-1, 1}, {1, 0}, {-1, 0}, {0, -1}
    };

    static {
        for (int t = 0; t < NR_PIESE; t++) {
            for (int c = 0; c < 4; c++) {
                int x = BAZA[t][c][0], y = BAZA[t][c][1];
                for (int r = 0; r < 4; r++) {
                    FORME[t][r][c][0] = x;
                    FORME[t][r][c][1] = y;
                    int nx = y, ny = -x;   // rotatie in sens orar
                    x = nx; y = ny;
                }
            }
        }
    }

    public interface Ascultator {
        void laMutare();
        void laRotire(boolean reusita);
        void laHold();
        void laAsezare(int tip, int[] celuleX, int[] celuleY, boolean trantit);
        void laLinii(int nrLinii, int[] randuri, int puncte, boolean tspin, boolean mini,
                     boolean b2b, int combo, boolean perfect);
        void laNivel(int nivelNou);
        void laFinal(boolean victorie);
    }

    // ---------- stare ----------

    public final int[][] tabla = new int[RANDURI_TOTAL][COLOANE];   // 0 gol, altfel tip+1
    public final float[][] stralucire = new float[RANDURI_TOTAL][COLOANE];

    public int mod = Setari.MOD_CLASIC;
    public int tipCurent = -1;
    public int rotatie = 0;
    public int pieseX, pieseY;
    public boolean piesaActiva = false;

    public int tipHold = -1;
    public boolean holdFolosit = false;
    public boolean holdPermis = true;

    public final int[] coada = new int[COADA];
    private final int[] sac = new int[NR_PIESE];
    private int pozSac = NR_PIESE;
    private final Random rnd = new Random();

    public int scor = 0;
    public int linii = 0;
    public int nivel = 1;
    public int nivelStart = 1;
    public int piese = 0;
    public int tetrisuri = 0;
    public int tspinuri = 0;
    public int combo = -1;
    public int comboMaxim = 0;
    public boolean b2bActiv = false;
    public int perfectClears = 0;
    public float timp = 0f;          // secunde de joc
    public boolean terminat = false;
    public boolean victorie = false;

    public float intervalCadere = 1f;
    private float acumulatorCadere = 0f;
    public boolean coborareRapida = false;

    private float timerLock = 0f;
    private int resetariLock = 0;
    private boolean peSol = false;
    private int ultimaActiuneRotire = 0;   // 0 nu, 1 rotire simpla, 2 rotire cu ultimul kick
    private boolean ultimaMiscareRotire = false;

    public final int[] randuriSterse = new int[4];
    public int nrRanduriSterse = 0;
    public float timerStergere = 0f;
    public float timerAparitie = 0f;

    public int ultimulPunctaj = 0;
    public String ultimulMesaj = "";
    public float timerMesaj = 0f;

    private final int[] tmpX = new int[4], tmpY = new int[4];

    private Ascultator ascultator;

    public void setAscultator(Ascultator a) { ascultator = a; }

    // ---------- pornire ----------

    public void jocNou(int modNou, int nivelDeStart) {
        mod = modNou;
        nivelStart = Math.max(1, nivelDeStart);
        for (int r = 0; r < RANDURI_TOTAL; r++)
            for (int c = 0; c < COLOANE; c++) { tabla[r][c] = 0; stralucire[r][c] = 0f; }

        scor = 0; linii = 0; piese = 0; tetrisuri = 0; tspinuri = 0;
        combo = -1; comboMaxim = 0; b2bActiv = false; perfectClears = 0;
        timp = 0f; terminat = false; victorie = false;
        nivel = (mod == Setari.MOD_CLASIC) ? nivelStart : 1;
        tipHold = -1; holdFolosit = false;
        holdPermis = (mod != Setari.MOD_LIBER);
        nrRanduriSterse = 0; timerStergere = 0f; timerAparitie = 0f;
        ultimulMesaj = ""; timerMesaj = 0f; ultimulPunctaj = 0;
        coborareRapida = false;

        pozSac = NR_PIESE;
        for (int i = 0; i < COADA; i++) coada[i] = scoateDinSac();
        recalculeazaViteza();
        piesaNoua();
    }

    private int scoateDinSac() {
        if (pozSac >= NR_PIESE) {
            for (int i = 0; i < NR_PIESE; i++) sac[i] = i;
            for (int i = NR_PIESE - 1; i > 0; i--) {
                int j = rnd.nextInt(i + 1);
                int t = sac[i]; sac[i] = sac[j]; sac[j] = t;
            }
            pozSac = 0;
        }
        return sac[pozSac++];
    }

    private int urmatoareaDinCoada() {
        int t = coada[0];
        System.arraycopy(coada, 1, coada, 0, COADA - 1);
        coada[COADA - 1] = scoateDinSac();
        return t;
    }

    private void piesaNoua() {
        apare(urmatoareaDinCoada());
    }

    private void apare(int tip) {
        tipCurent = tip;
        rotatie = 0;
        pieseX = 4;
        pieseY = RANDURI;
        acumulatorCadere = 0f;
        timerLock = 0f; resetariLock = 0; peSol = false;
        ultimaActiuneRotire = 0; ultimaMiscareRotire = false;
        piesaActiva = true;
        holdFolosit = false;

        if (!incape(tipCurent, rotatie, pieseX, pieseY)) {
            if (incape(tipCurent, rotatie, pieseX, pieseY + 1)) {
                pieseY++;
            } else {
                piesaActiva = false;
                incheie(false);
            }
        }
    }

    // ---------- interogari ----------

    public boolean incape(int tip, int rot, int px, int py) {
        int[][] f = FORME[tip][rot];
        for (int c = 0; c < 4; c++) {
            int x = px + f[c][0], y = py + f[c][1];
            if (x < 0 || x >= COLOANE || y < 0 || y >= RANDURI_TOTAL) return false;
            if (tabla[y][x] != 0) return false;
        }
        return true;
    }

    public int pozitieFantoma() {
        if (!piesaActiva) return pieseY;
        int y = pieseY;
        while (incape(tipCurent, rotatie, pieseX, y - 1)) y--;
        return y;
    }

    public boolean esteInFazaStergere() { return nrRanduriSterse > 0; }

    public boolean randSeSterge(int r) {
        for (int i = 0; i < nrRanduriSterse; i++) if (randuriSterse[i] == r) return true;
        return false;
    }

    public float progresStergere() {
        return nrRanduriSterse > 0 ? 1f - timerStergere / DURATA_STERGERE : 0f;
    }

    // ---------- actiuni jucator ----------

    public boolean muta(int dir) {
        if (!piesaActiva || terminat) return false;
        if (incape(tipCurent, rotatie, pieseX + dir, pieseY)) {
            pieseX += dir;
            ultimaMiscareRotire = false;
            reseteazaLock();
            if (ascultator != null) ascultator.laMutare();
            return true;
        }
        return false;
    }

    /** dir = +1 sens orar, -1 sens trigonometric, 2 = 180 grade */
    public boolean roteste(int dir) {
        if (!piesaActiva || terminat) return false;
        int rotNoua = (rotatie + (dir == 2 ? 2 : dir) + 4) % 4;

        if (dir == 2) {
            for (int k = 0; k < KICK_180.length; k++) {
                int nx = pieseX + KICK_180[k][0], ny = pieseY + KICK_180[k][1];
                if (incape(tipCurent, rotNoua, nx, ny)) {
                    aplicaRotatie(rotNoua, nx, ny, k == KICK_180.length - 1);
                    return true;
                }
            }
        } else {
            int[][][] of = tipCurent == P_I ? OFFSET_I : tipCurent == P_O ? OFFSET_O : OFFSET_JLSTZ;
            int nrTeste = of[0].length;
            for (int k = 0; k < nrTeste; k++) {
                int kx = of[rotatie][k][0] - of[rotNoua][k][0];
                int ky = of[rotatie][k][1] - of[rotNoua][k][1];
                int nx = pieseX + kx, ny = pieseY + ky;
                if (incape(tipCurent, rotNoua, nx, ny)) {
                    aplicaRotatie(rotNoua, nx, ny, k == nrTeste - 1);
                    return true;
                }
            }
        }
        if (ascultator != null) ascultator.laRotire(false);
        return false;
    }

    private void aplicaRotatie(int rotNoua, int nx, int ny, boolean ultimulKick) {
        rotatie = rotNoua;
        pieseX = nx; pieseY = ny;
        ultimaMiscareRotire = true;
        ultimaActiuneRotire = ultimulKick ? 2 : 1;
        reseteazaLock();
        if (ascultator != null) ascultator.laRotire(true);
    }

    public boolean hold() {
        if (!piesaActiva || terminat || !holdPermis || holdFolosit) return false;
        int vechi = tipCurent;
        int nou = tipHold >= 0 ? tipHold : urmatoareaDinCoada();
        tipHold = vechi;
        apare(nou);
        holdFolosit = true;
        if (ascultator != null) ascultator.laHold();
        return true;
    }

    /**
     * modul liber: la fiecare atingere piesa care cade trece la urmatoarea forma, pe rand prin
     * toate cele 7 (ca in versiunea originala): I culcat -> I in picioare -> O -> T -> S -> Z -> J -> L -> I...
     * Daca forma noua nu incape pe loc, se cauta un loc liber lateral, apoi mai sus; daca nu
     * incape nicaieri, se trece la forma urmatoare, ca schimbarea sa nu se blocheze.
     */
    public boolean schimbaForma() {
        if (!piesaActiva || terminat || mod != Setari.MOD_LIBER) return false;
        int t = tipCurent, r = rotatie;
        for (int incercare = 0; incercare < NR_PIESE + 1; incercare++) {
            if (t == P_I && r == 0) { r = 1; }
            else { t = (t + 1) % NR_PIESE; r = 0; }
            if (aseazaUndeIncape(t, r)) {
                tipCurent = t; rotatie = r;
                ultimaMiscareRotire = false;
                reseteazaLock();
                if (ascultator != null) ascultator.laRotire(true);
                return true;
            }
        }
        return false;
    }

    /** cauta pozitia cea mai apropiata unde incape forma (lateral, apoi in sus) si muta piesa acolo */
    private boolean aseazaUndeIncape(int tip, int rot) {
        for (int dx = 0; dx <= COLOANE; dx++) {
            if (incape(tip, rot, pieseX + dx, pieseY)) { pieseX += dx; return true; }
            if (dx > 0 && incape(tip, rot, pieseX - dx, pieseY)) { pieseX -= dx; return true; }
        }
        for (int dy = 1; pieseY + dy < RANDURI_TOTAL; dy++) {
            for (int dx = 0; dx <= COLOANE; dx++) {
                if (incape(tip, rot, pieseX + dx, pieseY + dy)) { pieseX += dx; pieseY += dy; return true; }
                if (dx > 0 && incape(tip, rot, pieseX - dx, pieseY + dy)) { pieseX -= dx; pieseY += dy; return true; }
            }
        }
        return false;
    }

    public void setCoborareRapida(boolean v) {
        coborareRapida = v;
    }

    public void trantesteJos() {
        if (!piesaActiva || terminat) return;
        int y = pozitieFantoma();
        int dist = pieseY - y;
        pieseY = y;
        if (dist > 0) { scor += dist * 2; ultimaMiscareRotire = false; }
        aseaza(true);
    }

    private void reseteazaLock() {
        if (peSol && resetariLock < MAX_RESETARI_LOCK) {
            timerLock = 0f;
            resetariLock++;
        }
    }

    // ---------- asezare si linii ----------

    private void aseaza(boolean trantit) {
        int[][] f = FORME[tipCurent][rotatie];
        boolean deasupra = true;
        for (int c = 0; c < 4; c++) {
            tmpX[c] = pieseX + f[c][0];
            tmpY[c] = pieseY + f[c][1];
            tabla[tmpY[c]][tmpX[c]] = tipCurent + 1;
            stralucire[tmpY[c]][tmpX[c]] = 1f;
            if (tmpY[c] < RANDURI) deasupra = false;
        }
        piese++;
        piesaActiva = false;

        boolean tspin = false, mini = false;
        if (tipCurent == P_T && ultimaMiscareRotire) {
            int colturi = 0, colturiFata = 0;
            int[][] cx = {{-1, 1}, {1, 1}, {1, -1}, {-1, -1}};
            for (int i = 0; i < 4; i++) {
                int x = pieseX + cx[i][0], y = pieseY + cx[i][1];
                boolean plin = x < 0 || x >= COLOANE || y < 0 || y >= RANDURI_TOTAL || tabla[y][x] != 0;
                if (plin) {
                    colturi++;
                    if (esteColtFata(cx[i][0], cx[i][1])) colturiFata++;
                }
            }
            if (colturi >= 3) {
                tspin = true;
                mini = colturiFata < 2 && ultimaActiuneRotire != 2;
            }
        }

        if (ascultator != null) ascultator.laAsezare(tipCurent, tmpX, tmpY, trantit);

        if (deasupra) {
            incheie(false);
            return;
        }

        nrRanduriSterse = 0;
        for (int r = RANDURI_TOTAL - 1; r >= 0; r--) {
            boolean plin = true;
            for (int c = 0; c < COLOANE; c++) if (tabla[r][c] == 0) { plin = false; break; }
            if (plin && nrRanduriSterse < 4) randuriSterse[nrRanduriSterse++] = r;
        }

        if (nrRanduriSterse > 0) {
            timerStergere = DURATA_STERGERE;
            puncteazaLinii(nrRanduriSterse, tspin, mini);
        } else {
            combo = -1;
            if (tspin) {
                int p = (mini ? 100 : 400) * nivel;
                scor += p;
                b2bActiv = true;
                mesaj(mini ? "T-SPIN MINI" : "T-SPIN", p);
            }
            timerAparitie = DURATA_APARITIE;
        }
    }

    private boolean esteColtFata(int dx, int dy) {
        // partea "din fata" a T-ului este directia in care e orientat varful
        switch (rotatie) {
            case 0: return dy == 1;
            case 1: return dx == 1;
            case 2: return dy == -1;
            default: return dx == -1;
        }
    }

    private void puncteazaLinii(int n, boolean tspin, boolean mini) {
        int baza;
        String nume;
        boolean dificil;
        if (tspin) {
            if (mini) { baza = n == 1 ? 200 : 400; nume = "T-SPIN MINI"; }
            else      { baza = n == 1 ? 800 : n == 2 ? 1200 : 1600; nume = "T-SPIN"; }
            nume += n == 1 ? " SINGLE" : n == 2 ? " DOUBLE" : " TRIPLE";
            dificil = true;
            tspinuri++;
        } else {
            switch (n) {
                case 1:  baza = 100; nume = "SINGLE"; break;
                case 2:  baza = 300; nume = "DOUBLE"; break;
                case 3:  baza = 500; nume = "TRIPLE"; break;
                default: baza = 800; nume = "TETRIS"; tetrisuri++; break;
            }
            dificil = n == 4;
        }

        boolean b2b = dificil && b2bActiv;
        int puncte = baza * nivel;
        if (b2b) puncte = puncte * 3 / 2;

        combo++;
        if (combo > comboMaxim) comboMaxim = combo;
        if (combo > 0) puncte += 50 * combo * nivel;

        boolean perfect = true;
        for (int r = 0; r < RANDURI_TOTAL && perfect; r++) {
            if (randSeSterge(r)) continue;
            for (int c = 0; c < COLOANE; c++) if (tabla[r][c] != 0) { perfect = false; break; }
        }
        if (perfect) {
            puncte += (n == 1 ? 800 : n == 2 ? 1200 : n == 3 ? 1800 : 2000) * nivel;
            perfectClears++;
            nume = "PERFECT CLEAR";
        } else if (b2b) {
            nume = "B2B " + nume;
        }

        if (dificil) b2bActiv = true;
        else b2bActiv = false;

        scor += puncte;
        mesaj(nume, puncte);

        if (ascultator != null)
            ascultator.laLinii(n, randuriSterse, puncte, tspin, mini, b2b, combo, perfect);
    }

    private void mesaj(String m, int puncte) {
        ultimulMesaj = m;
        ultimulPunctaj = puncte;
        timerMesaj = 1.6f;
    }

    private void finalizeazaStergere() {
        int scris = 0;
        for (int r = 0; r < RANDURI_TOTAL; r++) {
            if (randSeSterge(r)) continue;
            if (scris != r) {
                System.arraycopy(tabla[r], 0, tabla[scris], 0, COLOANE);
                System.arraycopy(stralucire[r], 0, stralucire[scris], 0, COLOANE);
            }
            scris++;
        }
        for (int r = scris; r < RANDURI_TOTAL; r++)
            for (int c = 0; c < COLOANE; c++) { tabla[r][c] = 0; stralucire[r][c] = 0f; }

        int n = nrRanduriSterse;
        nrRanduriSterse = 0;
        linii += n;

        if (mod == Setari.MOD_CLASIC) {
            int nivelNou = nivelStart + linii / 10;
            if (nivelNou > nivel) {
                nivel = nivelNou;
                recalculeazaViteza();
                if (ascultator != null) ascultator.laNivel(nivel);
            }
        } else if (mod == Setari.MOD_SPRINT && linii >= Setari.SPRINT_LINII) {
            incheie(true);
            return;
        }
        timerAparitie = DURATA_APARITIE;
    }

    private void incheie(boolean victorieJoc) {
        if (terminat) return;
        terminat = true;
        victorie = victorieJoc;
        piesaActiva = false;
        if (ascultator != null) ascultator.laFinal(victorieJoc);
    }

    public void recalculeazaViteza() {
        int n = Math.max(1, nivel) - 1;
        double baza = 0.8 - n * 0.007;
        if (baza < 0.05) baza = 0.05;
        intervalCadere = (float) Math.pow(baza, n);
        if (intervalCadere < 0.02f) intervalCadere = 0.02f;
        if (mod != Setari.MOD_CLASIC) intervalCadere = Math.max(intervalCadere, 0.55f);
    }

    // ---------- actualizare ----------

    public void actualizeaza(float dt) {
        if (terminat) return;
        timp += dt;
        if (timerMesaj > 0f) timerMesaj -= dt;

        for (int r = 0; r < RANDURI_TOTAL; r++)
            for (int c = 0; c < COLOANE; c++)
                if (stralucire[r][c] > 0f) stralucire[r][c] = Math.max(0f, stralucire[r][c] - dt * 2.2f);

        if (mod == Setari.MOD_ULTRA && timp >= Setari.ULTRA_SECUNDE) {
            timp = Setari.ULTRA_SECUNDE;
            incheie(true);
            return;
        }

        if (nrRanduriSterse > 0) {
            timerStergere -= dt;
            if (timerStergere <= 0f) finalizeazaStergere();
            return;
        }

        if (!piesaActiva) {
            timerAparitie -= dt;
            if (timerAparitie <= 0f) piesaNoua();
            return;
        }

        boolean poateCobori = incape(tipCurent, rotatie, pieseX, pieseY - 1);
        if (poateCobori) {
            peSol = false;
            float interval = coborareRapida ? Math.min(intervalCadere / 20f, 0.05f) : intervalCadere;
            acumulatorCadere += dt;
            while (acumulatorCadere >= interval && piesaActiva) {
                acumulatorCadere -= interval;
                if (incape(tipCurent, rotatie, pieseX, pieseY - 1)) {
                    pieseY--;
                    ultimaMiscareRotire = false;
                    if (coborareRapida) scor += 1;
                } else {
                    break;
                }
            }
        } else {
            if (!peSol) { peSol = true; timerLock = 0f; }
            timerLock += dt;
            if (timerLock >= LOCK_DELAY || coborareRapida && timerLock >= LOCK_DELAY * 0.4f) {
                aseaza(false);
            }
        }
    }

    // ---------- utilitare pentru afisare ----------

    public float timpRamasUltra() {
        return Math.max(0f, Setari.ULTRA_SECUNDE - timp);
    }

    public int liniiRamaseSprint() {
        return Math.max(0, Setari.SPRINT_LINII - linii);
    }

    public static String formatTimp(float secunde) {
        int total = (int) (secunde * 100);
        int m = total / 6000, s = (total / 100) % 60, c = total % 100;
        return String.format("%d:%02d.%02d", m, s, c);
    }
}
