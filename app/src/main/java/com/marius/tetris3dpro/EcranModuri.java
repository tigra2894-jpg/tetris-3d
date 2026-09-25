package com.marius.tetris3dpro;

public class EcranModuri extends EcranMeniuBaza {

    private static final int[] CULORI = {CIAN, GALBEN, ROZ, VERDE};

    public EcranModuri(Aplicatie app) {
        super(app,
              new String[]{"CLASIC", "SPRINT", "ULTRA", "LIBER", "INAPOI"},
              new int[]{CIAN, GALBEN, ROZ, VERDE, GRI},
              0.40f, 0.098f);
    }

    @Override
    public void deseneazaUI(CapaUI ui) {
        float fade = tranzitie;
        titlu(ui, "MODURI", 0.20f, fade);
        deseneazaButoane(ui, fade);

        Setari s = app.setari;
        for (int m = 0; m < Setari.NR_MODURI; m++) {
            float y = btnY[m];
            String rec;
            if (m == Setari.MOD_SPRINT) {
                int ms = s.recordSprintMs();
                rec = ms > 0 ? Joc.formatTimp(ms / 1000f) : "-";
            } else {
                rec = s.record(m) + "";
            }
            float yInfo = y + BTN_INALT / 2f + 0.016f;
            ui.textFin(Setari.DESCRIERI_MODURI[m][0],
                    0.5f - BTN_LAT / 2f + 0.02f, yInfo, 0.013f, argb(fade * 0.7f, GRI));
            ui.textDreapta("REC " + rec, 0.5f + BTN_LAT / 2f - 0.02f, yInfo, 0.013f,
                    argb(fade * 0.85f, CULORI[m]));
        }
    }

    @Override
    protected void laButon(int i) {
        if (i == 4) { app.schimbaEcran(app.ecranMeniu); return; }
        app.ecranJoc.porneste(i);
        app.schimbaEcran(app.ecranJoc);
    }

    @Override
    public boolean inapoi() { app.schimbaEcran(app.ecranMeniu); return true; }
}
