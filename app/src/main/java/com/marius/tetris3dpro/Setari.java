package com.marius.tetris3dpro;

import android.content.Context;
import android.content.SharedPreferences;

public class Setari {

    public static final int MOD_CLASIC = 0;
    public static final int MOD_SPRINT = 1;
    public static final int MOD_ULTRA  = 2;
    public static final int MOD_LIBER  = 3;
    public static final int NR_MODURI  = 4;

    public static final String[] NUME_MODURI = {
        "CLASIC", "SPRINT", "ULTRA", "LIBER"
    };

    public static final String[][] DESCRIERI_MODURI = {
        {"MARATON FARA SFARSIT", "NIVELUL CRESTE LA 10 LINII"},
        {"CURATA 40 DE LINII", "CAT MAI REPEDE"},
        {"AI 2 MINUTE", "FA CAT MAI MULTE PUNCTE"},
        {"APESI SI SCHIMBI", "PIESA CARE CADE"}
    };

    public static final int TEMA_VIE     = 0;
    public static final int TEMA_FOC     = 1;
    public static final int TEMA_GHEATA  = 2;
    public static final int TEMA_PADURE  = 3;
    public static final int TEMA_NEON    = 4;
    public static final int NR_TEME      = 5;

    public static final String[] NUME_TEME = {
        "VIE", "FOC", "GHEATA", "PADURE", "NEON"
    };

    public static final int SPRINT_LINII = 40;
    public static final int ULTRA_SECUNDE = 120;

    private final SharedPreferences p;

    public Setari(Context ctx) {
        p = ctx.getSharedPreferences("tetris3dpro", Context.MODE_PRIVATE);
    }

    // ---------- optiuni ----------

    public boolean sunetPornit()       { return p.getBoolean("sunet", true); }
    public void setSunet(boolean v)    { p.edit().putBoolean("sunet", v).apply(); }

    public boolean vibratiePornita()   { return p.getBoolean("vibratie", true); }
    public void setVibratie(boolean v) { p.edit().putBoolean("vibratie", v).apply(); }

    // implicit jocul se controleaza doar din gesturi; butoanele se pot porni din SETARI
    public boolean butoanePornite()    { return p.getBoolean("butoane_ecran", false); }
    public void setButoane(boolean v)  { p.edit().putBoolean("butoane_ecran", v).apply(); }

    public boolean fantomaPornita()    { return p.getBoolean("fantoma", true); }
    public void setFantoma(boolean v)  { p.edit().putBoolean("fantoma", v).apply(); }

    public boolean holdPornit()        { return p.getBoolean("hold", true); }
    public void setHold(boolean v)     { p.edit().putBoolean("hold", v).apply(); }

    /** nivelul de la care porneste jocul clasic, 1..15 */
    public int nivelStart() {
        return Math.max(1, Math.min(15, p.getInt("nivel_start", 1)));
    }

    public void setNivelStart(int v) {
        if (v < 1) v = 1;
        if (v > 15) v = 15;
        p.edit().putInt("nivel_start", v).apply();
    }

    public int tema() {
        int t = p.getInt("tema", TEMA_VIE);
        if (t < 0 || t >= NR_TEME) return TEMA_VIE;
        return t;
    }

    public void setTema(int t) {
        if (t < 0) t = 0;
        if (t >= NR_TEME) t = NR_TEME - 1;
        p.edit().putInt("tema", t).apply();
    }

    // ---------- recorduri ----------

    public int record(int mod) {
        return p.getInt("record_" + mod, 0);
    }

    public void setRecord(int mod, int scor) {
        if (scor > record(mod)) {
            p.edit().putInt("record_" + mod, scor).apply();
        }
    }

    /** cel mai bun timp la sprint, in milisecunde; 0 = niciunul */
    public int recordSprintMs() {
        return p.getInt("record_sprint_ms", 0);
    }

    public boolean setRecordSprintMs(int ms) {
        int vechi = recordSprintMs();
        if (vechi == 0 || ms < vechi) {
            p.edit().putInt("record_sprint_ms", ms).apply();
            return true;
        }
        return false;
    }

    public int ultimulMod() {
        int m = p.getInt("ultim_mod", MOD_CLASIC);
        if (m < 0 || m >= NR_MODURI) return MOD_CLASIC;
        return m;
    }

    public void setUltimulMod(int mod) {
        p.edit().putInt("ultim_mod", mod).apply();
    }

    // ---------- statistici ----------

    public int jocuriJucate()     { return p.getInt("stat_jocuri", 0); }
    public int liniiTotale()      { return p.getInt("stat_linii", 0); }
    public int pieseTotale()      { return p.getInt("stat_piese", 0); }
    public int timpTotalSecunde() { return p.getInt("stat_timp", 0); }
    public int celMaiBunNivel()   { return p.getInt("stat_nivel", 1); }
    public int tetrisuri()        { return p.getInt("stat_tetris", 0); }
    public int tspinuri()         { return p.getInt("stat_tspin", 0); }
    public int comboMaxim()       { return p.getInt("stat_combo", 0); }
    public int curatariPerfecte() { return p.getInt("stat_perfect", 0); }

    public void adaugaJoc()          { p.edit().putInt("stat_jocuri", jocuriJucate() + 1).apply(); }
    public void adaugaLinii(int n)   { p.edit().putInt("stat_linii", liniiTotale() + n).apply(); }
    public void adaugaPiese(int n)   { p.edit().putInt("stat_piese", pieseTotale() + n).apply(); }
    public void adaugaTimp(int s)    { p.edit().putInt("stat_timp", timpTotalSecunde() + s).apply(); }
    public void adaugaTetris(int n)  { p.edit().putInt("stat_tetris", tetrisuri() + n).apply(); }
    public void adaugaTspin(int n)   { p.edit().putInt("stat_tspin", tspinuri() + n).apply(); }
    public void adaugaPerfect(int n) { p.edit().putInt("stat_perfect", curatariPerfecte() + n).apply(); }

    public void raporteazaNivel(int nivel) {
        if (nivel > celMaiBunNivel()) p.edit().putInt("stat_nivel", nivel).apply();
    }

    public void raporteazaCombo(int combo) {
        if (combo > comboMaxim()) p.edit().putInt("stat_combo", combo).apply();
    }

    public void stergeStatistici() {
        p.edit()
         .remove("stat_jocuri").remove("stat_linii").remove("stat_piese")
         .remove("stat_timp").remove("stat_nivel").remove("stat_tetris")
         .remove("stat_tspin").remove("stat_combo").remove("stat_perfect")
         .apply();
    }

    public void stergeRecorduri() {
        SharedPreferences.Editor e = p.edit();
        for (int m = 0; m < NR_MODURI; m++) e.remove("record_" + m);
        e.remove("record_sprint_ms");
        e.apply();
    }

    // ---------- teme ----------

    public float[][] culoriPiese() {
        return culoriTema(tema());
    }

    /** ordinea pieselor: I, O, T, S, Z, J, L */
    public static float[][] culoriTema(int tema) {
        switch (tema) {
            case TEMA_FOC:
                return new float[][] {
                    {1.00f, 0.82f, 0.10f}, {1.00f, 0.52f, 0.05f}, {1.00f, 0.18f, 0.10f},
                    {1.00f, 0.68f, 0.15f}, {0.92f, 0.10f, 0.30f}, {1.00f, 0.90f, 0.35f},
                    {1.00f, 0.38f, 0.02f}
                };
            case TEMA_GHEATA:
                return new float[][] {
                    {0.30f, 0.92f, 1.00f}, {0.70f, 0.98f, 1.00f}, {0.20f, 0.55f, 1.00f},
                    {0.45f, 0.80f, 1.00f}, {0.10f, 0.35f, 0.95f}, {0.85f, 0.95f, 1.00f},
                    {0.35f, 0.72f, 1.00f}
                };
            case TEMA_PADURE:
                return new float[][] {
                    {0.35f, 1.00f, 0.25f}, {0.75f, 1.00f, 0.15f}, {0.10f, 0.85f, 0.40f},
                    {0.55f, 0.95f, 0.20f}, {0.95f, 0.85f, 0.20f}, {0.15f, 0.70f, 0.45f},
                    {0.60f, 1.00f, 0.55f}
                };
            case TEMA_NEON:
                return new float[][] {
                    {0.00f, 1.00f, 0.90f}, {1.00f, 1.00f, 0.20f}, {1.00f, 0.20f, 1.00f},
                    {0.30f, 1.00f, 0.30f}, {1.00f, 0.25f, 0.40f}, {0.30f, 0.45f, 1.00f},
                    {1.00f, 0.60f, 0.10f}
                };
            case TEMA_VIE:
            default:
                return new float[][] {
                    {0.10f, 0.85f, 1.00f}, {1.00f, 0.85f, 0.05f}, {0.85f, 0.15f, 1.00f},
                    {0.35f, 1.00f, 0.15f}, {1.00f, 0.10f, 0.55f}, {0.15f, 0.40f, 1.00f},
                    {1.00f, 0.48f, 0.05f}
                };
        }
    }

    public float[] culoareFundal() {
        switch (tema()) {
            case TEMA_FOC:    return new float[] {0.045f, 0.010f, 0.008f};
            case TEMA_GHEATA: return new float[] {0.010f, 0.022f, 0.048f};
            case TEMA_PADURE: return new float[] {0.008f, 0.030f, 0.016f};
            case TEMA_NEON:   return new float[] {0.020f, 0.005f, 0.035f};
            default:          return new float[] {0.012f, 0.016f, 0.032f};
        }
    }
}
