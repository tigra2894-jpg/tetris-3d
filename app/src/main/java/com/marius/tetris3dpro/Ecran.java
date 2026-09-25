package com.marius.tetris3dpro;

public abstract class Ecran {

    protected final Aplicatie app;
    protected float tranzitie = 0f;   // 0..1, fade-in la intrare

    public Ecran(Aplicatie app) { this.app = app; }

    public void laIntrare() { tranzitie = 0f; }
    public void laIesire() { }
    public void laPauzaAplicatie() { }

    public void actualizeaza(float dt) {
        if (tranzitie < 1f) tranzitie = Math.min(1f, tranzitie + dt * 3.5f);
    }

    /** deseneaza scena 3D (cuburi) */
    public abstract void deseneaza3D(Randare r);

    /** deseneaza stratul 2D */
    public abstract void deseneazaUI(CapaUI ui);

    /** x, y in fractiuni de ecran 0..1; pointer = id-ul degetului */
    public void apasare(int pointer, float x, float y) { }
    public void miscare(int pointer, float x, float y) { }
    public void ridicare(int pointer, float x, float y) { }

    /** true daca a consumat evenimentul */
    public boolean inapoi() { return false; }

    protected static int argb(float alfa, int rgb) {
        int a = Math.max(0, Math.min(255, Math.round(alfa * 255f)));
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    protected static int culoare(float[] c, float alfa) {
        int r = Math.min(255, Math.round(c[0] * 255));
        int g = Math.min(255, Math.round(c[1] * 255));
        int b = Math.min(255, Math.round(c[2] * 255));
        return argb(alfa, (r << 16) | (g << 8) | b);
    }

    protected static float neted(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }
}
