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
        "NEON", "FOC", "GHEATA", "PADURE"
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

    public float[][] culoriPiese() {
        switch (tema()) {

            case TEMA_FOC:
                return new float[][] {
                    {1.00f, 0.85f, 0.20f},
                    {1.00f, 0.60f, 0.10f},
                    {0.95f, 0.30f, 0.10f},
                    {1.00f, 0.45f, 0.25f},
                    {0.85f, 0.15f, 0.10f},
                    {1.00f, 0.75f, 0.35f},
                    {0.90f, 0.40f, 0.05f}
                };

            case TEMA_GHEATA:
                return new float[][] {
                    {0.55f, 0.90f, 1.00f},
                    {0.75f, 0.95f, 1.00f},
                    {0.40f, 0.70f, 0.98f},
                    {0.60f, 0.85f, 0.95f},
                    {0.30f, 0.55f, 0.90f},
                    {0.85f, 0.95f, 1.00f},
                    {0.45f, 0.80f, 1.00f}
                };

            case TEMA_PADURE:
                return new float[][] {
                    {0.35f, 0.85f, 0.35f},
                    {0.75f, 0.90f, 0.30f},
                    {0.20f, 0.65f, 0.35f},
                    {0.50f, 0.80f, 0.25f},
                    {0.60f, 0.45f, 0.20f},
                    {0.25f, 0.55f, 0.30f},
                    {0.85f, 0.80f, 0.35f}
                };

            case TEMA_NEON:
            default:
                return new float[][] {
                    {0.15f, 0.85f, 0.95f},
                    {0.95f, 0.85f, 0.15f},
                    {0.70f, 0.25f, 0.90f},
                    {0.20f, 0.85f, 0.35f},
                    {0.95f, 0.20f, 0.25f},
                    {0.20f, 0.35f, 0.95f},
                    {0.98f, 0.55f, 0.10f}
                };
        }
    }

    public float[] culoareFundal() {
        switch (tema()) {
            case TEMA_FOC:    return new float[] {0.09f, 0.03f, 0.02f};
            case TEMA_GHEATA: return new float[] {0.02f, 0.05f, 0.10f};
            case TEMA_PADURE: return new float[] {0.02f, 0.06f, 0.03f};
            default:          return new float[] {0.03f, 0.04f, 0.09f};
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
