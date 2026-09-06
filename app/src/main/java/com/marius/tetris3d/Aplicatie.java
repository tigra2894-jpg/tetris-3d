package com.marius.tetris3d;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;

/**
 * Creierul aplicatiei.
 * Tine ecranul curent, comuta intre ecrane, pastreaza
 * lucrurile comune: setari, sunet, stele, desenator.
 */
public class Aplicatie {

    public final Context context;
    public final Setari setari;
    public final Sunet sunet;
    public final Stele stele;

    public Desenator desenator;

    // ecranul activ si istoricul, pentru butonul de inapoi
    private Ecran ecranCurent;
    private final ArrayList<Ecran> istoric = new ArrayList<>();

    // ecranele, create o singura data
    public EcranMeniu       ecranMeniu;
    public EcranModuri      ecranModuri;
    public EcranSetari      ecranSetari;
    public EcranStatistici  ecranStatistici;
    public EcranJoc         ecranJoc;

    // marimea ecranului in pixeli
    public float latimePx = 1f;
    public float inaltimePx = 1f;

    // tranzitie intre ecrane
    private float tranzitie = 0f;
    private Ecran ecranUrmator = null;

    public Aplicatie(Context context) {
        this.context = context;
        this.setari = new Setari(context);
        this.sunet = new Sunet();
        this.sunet.setPornit(setari.sunetPornit());
        this.stele = new Stele();
    }

    /** se cheama o data, cand suprafata grafica e gata */
    public void porneste(Cub cub) {
        desenator = new Desenator(cub);

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

    // ---------- comutarea intre ecrane ----------

    /** merge la un ecran nou si tine minte de unde a venit */
    public void mergiLa(Ecran nou) {
        if (nou == null || nou == ecranCurent) return;
        istoric.add(ecranCurent);
        schimba(nou);
    }

    /** merge la un ecran fara sa retina drumul (ex: meniul principal) */
    public void mergiLaRadacina(Ecran nou) {
        istoric.clear();
        schimba(nou);
    }

    /** se intoarce la ecranul anterior; true daca a reusit */
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

    // ---------- scurtaturi folosite de ecrane ----------

    public void meniu() {
        mergiLaRadacina(ecranMeniu);
    }

    public void jocNou(int mod) {
        setari.setUltimulMod(mod);
        ecranJoc.pregateste(mod);
        mergiLa(ecranJoc);
    }

    public void iesire() {
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    // ---------- bucla ----------

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
        ecranCurent.deseneaza(desenator);
    }

    /** cat de intunecat e ecranul in timpul tranzitiei: 0 deloc, 1 negru */
    public float intunecare() {
        // curba: negru la mijlocul tranzitiei
        float t = tranzitie;
        if (t <= 0f) return 0f;
        return 1f - Math.abs(t - 0.5f) * 2f;
    }

    // ---------- atingeri ----------

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

    // ---------- pauza aplicatiei ----------

    public void laPauza() {
        if (ecranJoc != null) ecranJoc.laPauzaAplicatie();
    }
}
