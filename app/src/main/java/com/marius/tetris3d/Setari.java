package com.marius.tetris3d;

import android.content.Context;
import android.content.SharedPreferences;

public class Setari {

    public static final int MOD_CLASIC     = 0;
    public static final int MOD_TURN       = 1;
    public static final int MOD_GRAVITATIE = 2;
    public static final int MOD_STICLA     = 3;
    public static final int MOD_VIU        = 4;
    public static final int MOD_SPATIU     = 5;
    public static final int NR_MODURI      = 6;

    public static final String[] NUME_MODURI = {
        "CLASIC", "TURN", "GRAVITATIE", "STICLA", "VIU", "SPATIU"
    };

    public static final int TEMA_NEON    = 0;
    public static final int TEMA_FOC     = 1;
    public static final int TEMA_GHEATA  = 2;
    public static final int TEMA_PADURE  = 3;
    public static final int NR_TEME      = 4;

    public static final String[] NUME_TEME = {
        "APUS", "FOC", "GHEATA", "PADURE"
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
        return p.getInt("tema", TEMA_NEON);
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
        return p.getInt("ultim_mod", MOD_CLASIC);
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

    /** paleta de culori a temei curente — nuante cu caracter, nu culori pure */
    public float[][] culoriPiese() {
        switch (tema()) {

            case TEMA_FOC:
                return new float[][] {
                    {1.00f, 0.78f, 0.12f},   // aur
                    {1.00f, 0.42f, 0.05f},   // portocaliu ars
                    {0.92f, 0.12f, 0.06f},   // rosu jar
                    {1.00f, 0.58f, 0.22f},   // ambra
                    {0.62f, 0.05f, 0.04f},   // jar stins
                    {1.00f, 0.88f, 0.42f},   // flacara galbena
                    {0.82f, 0.24f, 0.02f}    // caramiziu
                };

            case TEMA_GHEATA:
                return new float[][] {
                    {0.55f, 0.92f, 1.00f},   // gheata electrica
                    {0.86f, 0.97f, 1.00f},   // alb-albastrui
                    {0.22f, 0.55f, 0.92f},   // albastru profund
                    {0.50f, 0.78f, 0.90f},   // otel-albastru
                    {0.10f, 0.32f, 0.78f},   // indigo glacial
                    {0.92f, 0.98f, 1.00f},   // alb pur
                    {0.35f, 0.70f, 1.00f}    // azuriu
                };

            case TEMA_PADURE:
                return new float[][] {
                    {0.30f, 0.88f, 0.28f},   // verde crud
                    {0.78f, 0.92f, 0.18f},   // verde-galbui
                    {0.10f, 0.55f, 0.28f},   // verde padure
                    {0.52f, 0.82f, 0.14f},   // lime
                    {0.48f, 0.34f, 0.14f},   // scoarta
                    {0.16f, 0.42f, 0.22f},   // verde adanc
                    {0.88f, 0.82f, 0.28f}    // muschi auriu
                };

            case TEMA_NEON:
            default:
                // apus synthwave: roz electric, ambra, violet, turcoaz
                return new float[][] {
                    {1.00f, 0.16f, 0.56f},   // roz-magenta electric
                    {1.00f, 0.66f, 0.16f},   // ambra apus
                    {0.58f, 0.16f, 0.88f},   // violet neon
                    {0.14f, 0.88f, 0.80f},   // turcoaz electric
                    {0.86f, 0.06f, 0.32f},   // rosu-vin profund
                    {0.22f, 0.48f, 1.00f},   // albastru electric
                    {1.00f, 0.42f, 0.12f}    // portocaliu apus
                };
        }
    }

    public float[] culoareFundal() {
        switch (tema()) {
            case TEMA_FOC:    return new float[] {0.07f, 0.02f, 0.02f};
            case TEMA_GHEATA: return new float[] {0.02f, 0.04f, 0.09f};
            case TEMA_PADURE: return new float[] {0.02f, 0.05f, 0.03f};
            default:          return new float[] {0.05f, 0.02f, 0.09f}; // noapte-mov de apus
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
