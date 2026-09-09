package com.marius.tetris3d;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;

public class Aplicatie {

    public final Context context;
    public final Setari setari;
    public final Sunet sunet;
    public final Stele stele;

    public Desenator desenator;
    public CapaUI ui;

    private Ecran ecranCurent;
    private final ArrayList<Ecran> istoric = new ArrayList<>();

    public EcranMeniu       ecranMeniu;
    public EcranModuri      ecranModuri;
    public EcranSetari      ecranSetari;
    public EcranStatistici  ecranStatistici;
    public EcranJoc         ecranJoc;

    public float latimePx = 1f;
    public float inaltimePx = 1f;

    private float tranzitie = 0f;
    private Ecran ecranUrmator = null;

    public Aplicatie(Context context) {
        this.context = context;
        this.setari = new Setari(context);
        this.sunet = new Sunet();
        this.sunet.setPornit(setari.sunetPornit());
        this.stele = new Stele();
        this.ui = new CapaUI();
    }

    public void porneste(Cub cub) {
        desenator = new Desenator(cub);
        ui.pregatesteGL();

        ecranMeniu      = new EcranMeniu(this);
        ecranModuri     = new EcranModuri(this);
        ecranSetari     = new EcranSetari(this);
        ecranStatistici = new EcranStatistici(this);
        ecranJoc        = new EcranJoc(this);

        ecranCurent = ecranMeniu;
        ecranCurent.laIntrare();
    }

    public boolean gata() {
        return desenator != null && ecranCurent != null;
    }

    public void mergiLa(Ecran nou) {
        if (nou == null || nou == ecranCurent) return;
        istoric.add(ecranCurent);
        schimba(nou);
    }

    public void mergiLaRadacina(Ecran nou) {
        istoric.clear();
        schimba(nou);
    }

    public boolean inapoi() {
        if (ecranCurent != null && ecranCurent.inapoi()) return true;

        if (!istoric.isEmpty()) {
            Ecran precedent = istoric.remove(istoric.size() - 1);
            schimba(precedent);
            return true;
        }
        return false;
    }

    private void schimba(Ecran nou) {
        if (ecranCurent != null) ecranCurent.laIesire();
        ecranUrmator = nou;
        tranzitie = 1f;
    }

    public Ecran ecranCurent() {
        return ecranCurent;
    }

    public void meniu() {
        mergiLaRadacina(ecranMeniu);
    }

    /** porneste un joc nou; liber=true activeaza schimbarea piesei la apasare */
    public void jocNou(int mod, boolean liber) {
        setari.setUltimulMod(mod);
        ecranJoc.pregateste(mod, liber);
        mergiLa(ecranJoc);
    }

    public void iesire() {
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    public void actualizeaza(float dt) {
        if (!gata()) return;

        stele.actualizeaza(dt);

        if (tranzitie > 0f) {
            tranzitie -= dt * 4.5f;
            if (tranzitie <= 0.5f && ecranUrmator != null) {
                ecranCurent = ecranUrmator;
                ecranUrmator = null;
                ecranCurent.laIntrare();
            }
            if (tranzitie < 0f) tranzitie = 0f;
        }

        ecranCurent.actualizeaza(dt);
    }

    public void deseneaza() {
        if (!gata()) return;
        ui.inceputCadru();
        ecranCurent.deseneaza(desenator);
        ui.deseneazaPeEcran();
    }

    public float intunecare() {
        float t = tranzitie;
        if (t <= 0f) return 0f;
        return 1f - Math.abs(t - 0.5f) * 2f;
    }

    public boolean atingere(float xPx, float yPx) {
        if (!gata() || tranzitie > 0f) return false;
        return ecranCurent.atingere(xPx / latimePx, yPx / inaltimePx);
    }

    public boolean tragere(float xPx, float yPx, float dxPx, float dyPx) {
        if (!gata() || tranzitie > 0f) return false;
        return ecranCurent.tragere(xPx / latimePx, yPx / inaltimePx,
                                   dxPx / latimePx, dyPx / inaltimePx);
    }

    public boolean ridicare(float xPx, float yPx) {
        if (!gata() || tranzitie > 0f) return false;
        return ecranCurent.ridicare(xPx / latimePx, yPx / inaltimePx);
    }

    public void laPauza() {
        if (ecranJoc != null) ecranJoc.laPauzaAplicatie();
    }
}
