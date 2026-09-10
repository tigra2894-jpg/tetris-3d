package com.marius.tetris3d;

import android.content.Context;
import android.content.SharedPreferences;

public class Setari {

    /** doar doua moduri: CLASIC si LIBER */
    public static final int MOD_CLASIC = 0;
    public static final int MOD_LIBER  = 1;
    public static final int NR_MODURI  = 2;

    public static final String[] NUME_MODURI = {
        "CLASIC", "LIBER"
    };

    public static final String[][] DESCRIERI_MODURI = {
        {"TETRIS", "CLASIC"},
        {"APESI SI SCHIMBI", "PIESA CARE CADE"}
    };

    public static final int TEMA_VIE     = 0;
    public static final int TEMA_FOC     = 1;
    public static final int TEMA_GHEATA  = 2;
    public static final int TEMA_PADURE  = 3;
    public static final int NR_TEME      = 4;

    public static final String[] NUME_TEME = {
        "VIE", "FOC", "GHEATA", "PADURE"
    };

    private final SharedPreferences p;

    public Setari(Context ctx) {
        p = ctx.getSharedPreferences("tetris3d", Context.MODE_PRIVATE);
    }

    public boolean sunetPornit() {
        return p.getBoolean("sunet", true);
    }

    public void setSunet(boolean v) {
        p.edit().putBoolean("sunet", v).apply();
    }

    public int vitezaStart() {
        return p.getInt("viteza", 1);
    }

    public void setVitezaStart(int v) {
        if (v < 1) v = 1;
        if (v > 5) v = 5;
        p.edit().putInt("viteza", v).apply();
    }

    public int tema() {
        return p.getInt("tema", TEMA_VIE);
    }

    public void setTema(int t) {
        if (t < 0) t = 0;
        if (t >= NR_TEME) t = NR_TEME - 1;
        p.edit().putInt("tema", t).apply();
    }

    public int record(int mod) {
        return p.getInt("record_" + mod, 0);
    }

    public void setRecord(int mod, int scor) {
        if (scor > record(mod)) {
            p.edit().putInt("record_" + mod, scor).apply();
        }
    }

    public int ultimulMod() {
        int m = p.getInt("ultim_mod", MOD_CLASIC);
        if (m < 0 || m >= NR_MODURI) return MOD_CLASIC;
        return m;
    }

    public void setUltimulMod(int mod) {
        p.edit().putInt("ultim_mod", mod).apply();
    }

    public int jocuriJucate() {
        return p.getInt("stat_jocuri", 0);
    }

    public int liniiTotale() {
        return p.getInt("stat_linii", 0);
    }

    public int pieseTotale() {
        return p.getInt("stat_piese", 0);
    }

    public int timpTotalSecunde() {
        return p.getInt("stat_timp", 0);
    }

    public int celMaiBunNivel() {
        return p.getInt("stat_nivel", 1);
    }

    public int tetrisuri() {
        return p.getInt("stat_tetris", 0);
    }

    public void adaugaJoc() {
        p.edit().putInt("stat_jocuri", jocuriJucate() + 1).apply();
    }

    public void adaugaLinii(int n) {
        p.edit().putInt("stat_linii", liniiTotale() + n).apply();
    }

    public void adaugaPiesa() {
        p.edit().putInt("stat_piese", pieseTotale() + 1).apply();
    }

    public void adaugaTimp(int secunde) {
        p.edit().putInt("stat_timp", timpTotalSecunde() + secunde).apply();
    }

    public void raporteazaNivel(int nivel) {
        if (nivel > celMaiBunNivel()) {
            p.edit().putInt("stat_nivel", nivel).apply();
        }
    }

    public void adaugaTetris() {
        p.edit().putInt("stat_tetris", tetrisuri() + 1).apply();
    }

    public void stergeStatistici() {
        p.edit()
         .remove("stat_jocuri")
         .remove("stat_linii")
         .remove("stat_piese")
         .remove("stat_timp")
         .remove("stat_nivel")
         .remove("stat_tetris")
         .apply();
    }

    /** culori vii, saturate, ca la plasticul lucios */
    public float[][] culoriPiese() {
        switch (tema()) {

            case TEMA_FOC:
                return new float[][] {
                    {1.00f, 0.82f, 0.10f},   // galben intens
                    {1.00f, 0.52f, 0.05f},   // portocaliu aprins
                    {1.00f, 0.18f, 0.10f},   // rosu viu
                    {1.00f, 0.68f, 0.15f},   // chihlimbar
                    {0.92f, 0.10f, 0.30f},   // rosu-zmeura
                    {1.00f, 0.90f, 0.35f},   // galben pal
                    {1.00f, 0.38f, 0.02f}    // portocaliu ars
                };

            case TEMA_GHEATA:
                return new float[][] {
                    {0.30f, 0.92f, 1.00f},   // cyan aprins
                    {0.70f, 0.98f, 1.00f},   // alb-cyan
                    {0.20f, 0.55f, 1.00f},   // albastru electric
                    {0.45f, 0.80f, 1.00f},   // azuriu
                    {0.10f, 0.35f, 0.95f},   // indigo
                    {0.85f, 0.95f, 1.00f},   // alb rece
                    {0.35f, 0.72f, 1.00f}    // albastru deschis
                };

            case TEMA_PADURE:
                return new float[][] {
                    {0.35f, 1.00f, 0.25f},   // verde crud
                    {0.75f, 1.00f, 0.15f},   // lime
                    {0.10f, 0.85f, 0.40f},   // verde smarald
                    {0.55f, 0.95f, 0.20f},   // verde-galbui
                    {0.95f, 0.85f, 0.20f},   // auriu
                    {0.15f, 0.70f, 0.45f},   // verde adanc
                    {0.60f, 1.00f, 0.55f}    // verde pal
                };

            case TEMA_VIE:
            default:
                // exact ca in imagini: roz, portocaliu, galben, verde, cyan, albastru, violet
                return new float[][] {
                    {0.10f, 0.85f, 1.00f},   // I - cyan aprins
                    {1.00f, 0.85f, 0.05f},   // O - galben intens
                    {0.85f, 0.15f, 1.00f},   // T - violet-magenta
                    {0.35f, 1.00f, 0.15f},   // S - verde crud
                    {1.00f, 0.10f, 0.55f},   // Z - roz aprins
                    {0.15f, 0.40f, 1.00f},   // J - albastru electric
                    {1.00f, 0.48f, 0.05f}    // L - portocaliu viu
                };
        }
    }

    public float[] culoareFundal() {
        switch (tema()) {
            case TEMA_FOC:    return new float[] {0.045f, 0.010f, 0.008f};
            case TEMA_GHEATA: return new float[] {0.010f, 0.022f, 0.048f};
            case TEMA_PADURE: return new float[] {0.008f, 0.030f, 0.016f};
            default:          return new float[] {0.012f, 0.016f, 0.032f};
        }
    }

    public float vitezaInitiala() {
        switch (vitezaStart()) {
            case 2:  return 0.60f;
            case 3:  return 0.45f;
            case 4:  return 0.32f;
            case 5:  return 0.22f;
            default: return 0.75f;
        }
    }
}
