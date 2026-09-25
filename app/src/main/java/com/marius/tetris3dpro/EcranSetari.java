package com.marius.tetris3dpro;

public class EcranSetari extends EcranMeniuBaza {

    private static final int S_SUNET = 0, S_VIBRATIE = 1, S_BUTOANE = 2, S_FANTOMA = 3,
            S_NIVEL = 4, S_TEMA = 5, S_INAPOI = 6;

    public EcranSetari(Aplicatie app) {
        super(app,
              new String[]{"", "", "", "", "", "", "INAPOI"},
              new int[]{CIAN, CIAN, CIAN, CIAN, GALBEN, ROZ, GRI},
              0.33f, 0.088f);
        actualizeazaTexte();
    }

    private void actualizeazaTexte() {
        Setari s = app.setari;
        btnText[S_SUNET]    = "SUNET: " + (s.sunetPornit() ? "PORNIT" : "OPRIT");
        btnText[S_VIBRATIE] = "VIBRATIE: " + (s.vibratiePornita() ? "PORNIT" : "OPRIT");
        btnText[S_BUTOANE]  = "BUTOANE: " + (s.butoanePornite() ? "AFISATE" : "ASCUNSE");
        btnText[S_FANTOMA]  = "FANTOMA: " + (s.fantomaPornita() ? "PORNIT" : "OPRIT");
        btnText[S_NIVEL]    = "NIVEL START: " + s.nivelStart();
        btnText[S_TEMA]     = "TEMA: " + Setari.NUME_TEME[s.tema()];
    }

    @Override
    public void laIntrare() { super.laIntrare(); actualizeazaTexte(); }

    @Override
    public void deseneazaUI(CapaUI ui) {
        float fade = tranzitie;
        titlu(ui, "SETARI", 0.18f, fade);
        deseneazaButoane(ui, fade);
        ui.textFin("NIVEL: STANGA -  DREAPTA +", 0.5f, btnY[S_NIVEL] + BTN_INALT / 2f + 0.016f, 0.013f, argb(fade * 0.6f, GRI));

        float[][] cul = Setari.culoriTema(app.setari.tema());
        float y = btnY[S_TEMA] + BTN_INALT / 2f + 0.014f;
        for (int i = 0; i < cul.length; i++) {
            ui.cerc(0.5f - 0.09f + i * 0.03f, y, 0.006f, culoare(cul[i], fade));
        }
        ui.textFin("GESTURI: TRAGE = MUTA, TAP = ROTESTE, AZVARLE SUS/JOS = TRANTESTE, 2 DEGETE = HOLD",
                0.5f, 0.955f, 0.012f, argb(fade * 0.6f, GRI));
    }

    @Override
    public void apasare(int p, float x, float y) {
        super.apasare(p, x, y);
    }

    @Override
    protected void laButon(int i) {
        Setari s = app.setari;
        switch (i) {
            case S_SUNET:    s.setSunet(!s.sunetPornit()); app.sunet.setPornit(s.sunetPornit()); break;
            case S_VIBRATIE: s.setVibratie(!s.vibratiePornita()); app.vibratii.setPornit(s.vibratiePornita()); break;
            case S_BUTOANE:  s.setButoane(!s.butoanePornite()); break;
            case S_FANTOMA:  s.setFantoma(!s.fantomaPornita()); break;
            case S_NIVEL:    break;   // tratat in ridicare cu pozitia x
            case S_TEMA:     s.setTema((s.tema() + 1) % Setari.NR_TEME); break;
            case S_INAPOI:   app.schimbaEcran(app.ecranMeniu); return;
        }
        actualizeazaTexte();
    }

    @Override
    public void ridicare(int p, float x, float y) {
        int i = butonLa(x, y);
        if (i == S_NIVEL && i == apasatIdx) {
            Setari s = app.setari;
            int n = s.nivelStart() + (x < 0.5f ? -1 : 1);
            if (n < 1) n = 15;
            if (n > 15) n = 1;
            s.setNivelStart(n);
            app.sunet.reda(Sunet.MENIU);
            actualizeazaTexte();
            apasatIdx = -1;
            return;
        }
        super.ridicare(p, x, y);
    }

    @Override
    public boolean inapoi() { app.schimbaEcran(app.ecranMeniu); return true; }
}
