package com.marius.tetris3dpro;

public class EcranMeniu extends EcranMeniuBaza {

    public EcranMeniu(Aplicatie app) {
        super(app,
              new String[]{"JOACA", "MODURI", "SETARI", "STATISTICI", "IESIRE"},
              new int[]{CIAN, GALBEN, VERDE, ROZ, ROSU},
              0.50f, 0.098f);
    }

    @Override
    protected void deseneazaExtra3D(Randare r) {
        // logo 3D: "T" mare din cuburi, rotindu-se usor
        float ang = (float) Math.sin(app.timp * 0.8f) * 18f;
        float[][] cul = app.culoriPiese();
        int[][] forma = {{-1, 1}, {0, 1}, {1, 1}, {0, 0}, {0, -1}};
        for (int i = 0; i < forma.length; i++) {
            float[] c = cul[(i + 2) % 7];
            double a = Math.toRadians(ang);
            float x = forma[i][0] * 1.15f, y = 12.2f + forma[i][1] * 1.15f;
            float rx = (float) (x * Math.cos(a));
            float rz = (float) (x * Math.sin(a));
            r.cubRotit(rx, y, rz, ang, 0f, 1f, 0f, c[0], c[1], c[2], 1f, 1.05f);
        }
    }

    @Override
    public void deseneazaUI(CapaUI ui) {
        float fade = tranzitie;
        if (app.imgLogo != null) {
            ui.imagine(app.imgLogo, 0.5f, 0.325f, 0.86f, 0.17f, fade);
        } else {
            ui.textCentrat("TETRIS", 0.5f, 0.31f, 0.095f, argb(fade, ALB));
            ui.textCentrat("3D PRO", 0.5f, 0.375f, 0.052f, argb(fade, CIAN));
        }

        int mod = app.setari.ultimulMod();
        ui.textFin("ULTIMUL MOD: " + Setari.NUME_MODURI[mod], 0.5f, 0.435f, 0.018f, argb(fade * 0.75f, GRI));

        deseneazaButoane(ui, fade);

        int rec = app.setari.record(Setari.MOD_CLASIC);
        ui.textFin("RECORD CLASIC  " + rec, 0.5f, 0.955f, 0.018f, argb(fade * 0.7f, GALBEN));
        ui.textFin("v2.0", 0.95f, 0.98f, 0.014f, argb(fade * 0.5f, GRI));
    }

    @Override
    protected void laButon(int i) {
        switch (i) {
            case 0: app.ecranJoc.porneste(app.setari.ultimulMod()); app.schimbaEcran(app.ecranJoc); break;
            case 1: app.schimbaEcran(app.ecranModuri); break;
            case 2: app.schimbaEcran(app.ecranSetari); break;
            case 3: app.schimbaEcran(app.ecranStatistici); break;
            case 4: app.iesi(); break;
        }
    }
}
