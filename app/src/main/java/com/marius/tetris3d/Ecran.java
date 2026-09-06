package com.marius.tetris3d;

/**
 * Clasa de baza pentru orice ecran al aplicatiei:
 * meniu, moduri, setari, statistici, joc, pauza, final.
 *
 * Fiecare ecran stie trei lucruri:
 *   - sa se actualizeze in timp (animatii)
 *   - sa se deseneze
 *   - sa raspunda la atingere
 */
public abstract class Ecran {

    protected Aplicatie app;

    // cat timp e deschis ecranul, pentru animatii de intrare
    protected float timp = 0f;

    public Ecran(Aplicatie app) {
        this.app = app;
    }

    /** se cheama cand ecranul devine cel activ */
    public void laIntrare() {
        timp = 0f;
    }

    /** se cheama cand se paraseste ecranul */
    public void laIesire() { }

    /** avansul in timp; dt e in secunde */
    public void actualizeaza(float dt) {
        timp += dt;
    }

    /** desenarea ecranului */
    public abstract void deseneaza(Desenator d);

    /**
     * atingere pe ecran.
     * x si y sunt in coordonate normalizate 0..1
     * (0,0 = coltul din stanga sus, 1,1 = dreapta jos)
     * intoarce true daca atingerea a fost folosita
     */
    public boolean atingere(float x, float y) {
        return false;
    }

    /** degetul se misca pe ecran */
    public boolean tragere(float x, float y, float dx, float dy) {
        return false;
    }

    /** degetul s-a ridicat */
    public boolean ridicare(float x, float y) {
        return false;
    }

    /** butonul fizic de inapoi al telefonului */
    public boolean inapoi() {
        return false;
    }

    // ---------- ajutor pentru zone apasabile ----------

    /**
     * verifica daca punctul (x,y) e intr-un dreptunghi.
     * toate valorile sunt 0..1
     */
    protected boolean inZona(float x, float y,
                             float stanga, float sus,
                             float dreapta, float jos) {
        return x >= stanga && x <= dreapta && y >= sus && y <= jos;
    }

    /**
     * zona unui rand de meniu: pe toata latimea,
     * centrata pe inaltimea data, cu o grosime data
     */
    protected boolean inRand(float y, float centru, float grosime) {
        return y >= centru - grosime / 2f && y <= centru + grosime / 2f;
    }

    // ---------- animatie de intrare ----------

    /** de la 0 la 1 in prima jumatate de secunda */
    protected float aparitie() {
        float p = timp / 0.45f;
        if (p > 1f) p = 1f;
        // pornire lenta, oprire lina
        return p * p * (3f - 2f * p);
    }

    /** pulsatie lenta, intre 0 si 1 */
    protected float puls(float viteza) {
        return 0.5f + 0.5f * (float) Math.sin(timp * viteza);
    }
}
