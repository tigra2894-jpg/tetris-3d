package com.marius.tetris3d;

import android.graphics.Color;

public class EcranMeniu extends Ecran {

    private static final float Y_TITLU  = 0.17f;
    private static final float Y_SUBT   = 0.245f;

    private static final float Y_JOACA  = 0.50f;
    private static final float Y_MODURI = 0.60f;
    private static final float Y_SETARI = 0.69f;
    private static final float Y_STAT   = 0.78f;
    private static final float Y_IESIRE = 0.87f;
    private static final float Y_AUTOR  = 0.965f;
    private static final float GROSIME  = 0.055f;

    private int apasat = -1;
    private float stralucire = 0f;

    private static final int NR_PIESE = 6;
    private final float[] px = new float[NR_PIESE];
    private final float[] py = new float[NR_PIESE];
    private final float[] pz = new float[NR_PIESE];
    private final float[] rot = new float[NR_PIESE];
    private final float[] vitRot = new float[NR_PIESE];
    private final float[] vitY = new float[NR_PIESE];
    private final int[] tip = new int[NR_PIESE];

    private Fundal fundal;

    public EcranMeniu(Aplicatie app) {
        super(app);
        fundal = new Fundal();

        java.util.Random rnd = new java.util.Random(11);
        for (int i = 0; i < NR_PIESE; i++) {
            px[i] = (rnd.nextFloat() - 0.5f) * 16f;
            py[i] = (rnd.nextFloat() - 0.5f) * 26f;
            pz[i] = -8f - rnd.nextFloat() * 10f;
            rot[i] = rnd.nextFloat() * 360f;
            vitRot[i] = 10f + rnd.nextFloat() * 18f;
            vitY[i] = 0.4f + rnd.nextFloat() * 0.9f;
            tip[i] = rnd.nextInt(7);
        }
    }

    @Override
    public void laIntrare() {
        super.laIntrare();
        apasat = -1;
        stralucire = 0f;
        fundal.seteazaAccent(false);
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);
        fundal.actualizeaza(dt);

        for (int i = 0; i < NR_PIESE; i++) {
            rot[i] += dt * vitRot[i];
            py[i] += dt * vitY[i];
            if (py[i] > 16f) {
                py[i] = -16f;
                tip[i] = (tip[i] + 3) % 7;
            }
        }

        stralucire -= dt * 3.2f;
        if (stralucire < 0f) {
            stralucire = 0f;
            apasat = -1;
        }
    }

    @Override
    public void deseneaza(Desenator d) {
        d.seteazaProiectie(48f, d.raport, 1f, 90f);

        float leg = timp * 0.20f;
        d.seteazaCamera(
                (float) Math.sin(leg) * 0.9f,
                (float) Math.cos(leg * 0.8f) * 0.5f,
                26f,
                0f, 0f, 0f);

        d.seteazaLumina(
                (float) Math.sin(timp * 0.5f) * 10f,
                18f, 18f);

        fundal.deseneazaCeata(d);
        app.stele.deseneaza2(d);
        deseneazaPieseFundal(d);

        float apTitlu = intrare(0f, 0.5f);
        float pulsTitlu = 0.90f + 0.10f * puls(1.5f);

        app.ui.textCentrat("TETRIS", 0.5f, Y_TITLU, 0.110f,
                culoare((int) (255 * pulsTitlu), (int) (215 * pulsTitlu), 60, apTitlu));

        app.ui.textCentrat("3D", 0.5f, Y_SUBT, 0.072f,
                culoare(60, (int) (225 * pulsTitlu), 255, apTitlu));

        optiune("JOACA",      0, Y_JOACA,  0.056f, 110, 255, 150);
