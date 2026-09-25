package com.marius.tetris3dpro;

public class EcranStatistici extends EcranMeniuBaza {

    private float tineApasat = -1f;
    private boolean sters = false;
    private float mesajSters = 0f;

    public EcranStatistici(Aplicatie app) {
        super(app,
              new String[]{"INAPOI"},
              new int[]{GRI},
              0.90f, 0.1f);
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);
        if (tineApasat >= 0f) {
            tineApasat += dt;
            if (tineApasat > 1.6f && !sters) {
                app.setari.stergeStatistici();
                app.setari.stergeRecorduri();
                sters = true;
                mesajSters = 2f;
                app.vibratii.lung();
                app.sunet.reda(Sunet.TRANTIRE);
            }
        }
        if (mesajSters > 0f) mesajSters -= dt;
    }

    @Override
    public void deseneazaUI(CapaUI ui) {
        float fade = tranzitie;
        Setari s = app.setari;
        titlu(ui, "STATISTICI", 0.14f, fade);

        float y = 0.24f;
        ui.panou(0.10f, y - 0.03f, 0.80f, 0.57f, argb(fade * 0.55f, 0x0B1020), 0.02f);
        ui.chenar(0.10f, y - 0.03f, 0.80f, 0.57f, argb(fade * 0.5f, CIAN), 0.02f, 0.0015f);

        float pas = 0.046f;
        rand(ui, "JOCURI JUCATE", s.jocuriJucate() + "", y, fade, ALB); y += pas;
        rand(ui, "LINII TOTALE", s.liniiTotale() + "", y, fade, ALB); y += pas;
        rand(ui, "PIESE ASEZATE", s.pieseTotale() + "", y, fade, ALB); y += pas;
        rand(ui, "TIMP JUCAT", timp(s.timpTotalSecunde()), y, fade, ALB); y += pas;
        rand(ui, "NIVEL MAXIM", s.celMaiBunNivel() + "", y, fade, GALBEN); y += pas;
        rand(ui, "TETRISURI", s.tetrisuri() + "", y, fade, CIAN); y += pas;
        rand(ui, "T-SPIN-URI", s.tspinuri() + "", y, fade, ROZ); y += pas;
        rand(ui, "COMBO MAXIM", s.comboMaxim() + "", y, fade, ROZ); y += pas;
        rand(ui, "PERFECT CLEAR", s.curatariPerfecte() + "", y, fade, VERDE); y += pas + 0.012f;

        rand(ui, "RECORD CLASIC", s.record(Setari.MOD_CLASIC) + "", y, fade, GALBEN); y += pas;
        int ms = s.recordSprintMs();
        rand(ui, "RECORD SPRINT", ms > 0 ? Joc.formatTimp(ms / 1000f) : "-", y, fade, GALBEN); y += pas;
        rand(ui, "RECORD ULTRA", s.record(Setari.MOD_ULTRA) + "", y, fade, GALBEN);

        if (tineApasat >= 0f && !sters) {
            float p = Math.min(1f, tineApasat / 1.6f);
            ui.bara(0.2f, 0.815f, 0.6f, 0.012f, p, argb(fade * 0.4f, FUNDAL_BTN), argb(fade, ROSU));
            ui.textFin("TINE APASAT PENTRU STERGERE", 0.5f, 0.85f, 0.015f, argb(fade * 0.8f, ROSU));
        } else if (mesajSters > 0f) {
            ui.textFin("STATISTICI STERSE", 0.5f, 0.85f, 0.018f, argb(fade * Math.min(1f, mesajSters), ROSU));
        } else {
            ui.textFin("TINE APASAT PE TABEL 2 SECUNDE PENTRU STERGERE", 0.5f, 0.85f, 0.012f, argb(fade * 0.45f, GRI));
        }

        deseneazaButoane(ui, fade);
    }

    private static String timp(int sec) {
        int h = sec / 3600, m = (sec / 60) % 60, s = sec % 60;
        return h > 0 ? String.format("%dh %02dm", h, m) : String.format("%dm %02ds", m, s);
    }

    private void rand(CapaUI ui, String et, String val, float y, float a, int cul) {
        ui.text(et, 0.14f, y, 0.019f, argb(a * 0.75f, GRI));
        ui.textDreapta(val, 0.86f, y, 0.022f, argb(a, cul));
    }

    @Override
    public void apasare(int p, float x, float y) {
        super.apasare(p, x, y);
        if (y > 0.2f && y < 0.8f) { tineApasat = 0f; sters = false; }
    }

    @Override
    public void ridicare(int p, float x, float y) {
        tineApasat = -1f;
        super.ridicare(p, x, y);
    }

    @Override
    protected void laButon(int i) { app.schimbaEcran(app.ecranMeniu); }

    @Override
    public boolean inapoi() { app.schimbaEcran(app.ecranMeniu); return true; }
}
