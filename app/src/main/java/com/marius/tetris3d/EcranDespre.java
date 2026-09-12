package com.marius.tetris3d;

import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;

/**
 * Ecranul DESPRE: un mesaj scurt si un link de sustinere.
 */
public class EcranDespre extends Ecran {

    private static final float Y_TITLU  = 0.16f;

    private static final float Y_R1     = 0.36f;
    private static final float Y_R2     = 0.42f;

    private static final float Y_CAFEA  = 0.58f;
    private static final float Y_LINK   = 0.635f;

    private static final float Y_INAPOI = 0.90f;

    private static final String LINK = "https://revolut.me/tibyy94";

    private float stralucireLink = 0f;

    private Fundal fundal;

    private static final int NR_PIESE = 4;
    private final float[] px = new float[NR_PIESE];
    private final float[] py = new float[NR_PIESE];
    private final float[] pz = new float[NR_PIESE];
    private final float[] rot = new float[NR_PIESE];
    private final float[] vitRot = new float[NR_PIESE];
    private final float[] vitY = new float[NR_PIESE];
    private final int[] tip = new int[NR_PIESE];

    public EcranDespre(Aplicatie app) {
        super(app);
        fundal = new Fundal();

        java.util.Random rnd = new java.util.Random(77);
        for (int i = 0; i < NR_PIESE; i++) {
            px[i] = (rnd.nextFloat() - 0.5f) * 15f;
            py[i] = (rnd.nextFloat() - 0.5f) * 24f;
            pz[i] = -9f - rnd.nextFloat() * 9f;
            rot[i] = rnd.nextFloat() * 360f;
            vitRot[i] = 9f + rnd.nextFloat() * 15f;
            vitY[i] = 0.35f + rnd.nextFloat() * 0.7f;
            tip[i] = rnd.nextInt(7);
        }
    }

    @Override
    public void laIntrare() {
        super.laIntrare();
        stralucireLink = 0f;
        fundal.seteazaAccent(false);
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);
        fundal.actualizeaza(dt);

        for (int i = 0; i < NR_PIESE; i++) {
            rot[i] += dt * vitRot[i];
            py[i] += dt * vitY[i];
            if (py[i] > 15f) {
                py[i] = -15f;
                tip[i] = (tip[i] + 2) % 7;
            }
        }

        stralucireLink -= dt * 2.6f;
        if (stralucireLink < 0f) stralucireLink = 0f;
    }

    @Override
    public void deseneaza(Desenator d) {
        d.seteazaProiectie(48f, d.raport, 1f, 90f);

        float leg = timp * 0.18f;
        d.seteazaCamera(
                (float) Math.sin(leg) * 0.7f,
                (float) Math.cos(leg * 0.8f) * 0.4f,
                26f, 0f, 0f, 0f);

        d.seteazaLumina(0f, 18f, 18f);

        fundal.deseneazaCeata(d);
        app.stele.deseneaza2(d);
        pieseFundal(d);

        float ap = intrare(0f, 0.5f);

        app.ui.textCentrat("DESPRE", 0.5f, Y_TITLU, 0.070f,
                culoare(220, 230, 255, ap));

        float ap1 = intrare(0.10f, 0.45f);
        app.ui.textCentrat("UN JOC FACUT CU DRAG,", 0.5f, Y_R1, 0.030f,
                culoare(200, 210, 235, ap1));

        float ap2 = intrare(0.20f, 0.45f);
        app.ui.textCentrat("DE TIBERIU.", 0.5f, Y_R2, 0.034f,
                culoare(255, 220, 130, ap2));

        float ap3 = intrare(0.40f, 0.5f);
        float p = 0.85f + 0.15f * puls(1.4f);

        app.ui.textCentrat("O CAFEA", 0.5f, Y_CAFEA, 0.024f,
                culoare(150, 160, 190, ap3));

        float lumL = 1f + stralucireLink * 0.7f;
        app.ui.textCentrat("REVOLUT.ME/TIBYY94", 0.5f, Y_LINK, 0.028f,
                culoare((int) Math.min(255, 130 * lumL * p),
                        (int) Math.min(255, 220 * lumL * p),
                        (int) Math.min(255, 255 * lumL), ap3));

        app.ui.textCentrat("INAPOI", 0.5f, Y_INAPOI, 0.030f,
                Color.rgb(150, 160, 190));
    }

    private float intrare(float intarziere, float durata) {
        float t = (timp - intarziere) / durata;
        if (t < 0f) return 0f;
        if (t > 1f) return 1f;
        return t * t * (3f - 2f * t);
    }

    private int culoare(int r, int g, int b, float alfa) {
        int a = (int) (Math.max(0f, Math.min(1f, alfa)) * 255);
        return Color.argb(a, r, g, b);
    }

    private void pieseFundal(Desenator d) {
        float[][] culori = app.setari.culoriPiese();

        for (int i = 0; i < NR_PIESE; i++) {
            int[][] forma = Joc.formaPiesei(tip[i], 0);
            float[] cul = culori[tip[i]];

            for (int k = 0; k < 4; k++) {
                float cx = px[i] + (forma[k][0] - 1.5f) * 0.78f;
                float cy = py[i] + (forma[k][1] - 2.0f) * 0.78f;

                d.cubRotit(cx, cy, pz[i],
                        rot[i], 0.5f, 1f, 0.3f,
                        cul[0] * 0.70f, cul[1] * 0.70f, cul[2] * 0.70f,
                        0.28f, 0.66f);
            }
        }
    }

    @Override
    public boolean atingere(float x, float y) {

        if (inRand(y, Y_LINK, 0.05f)) {
            stralucireLink = 1f;
            app.sunet.rotire();
            deschideLink();
            return true;
        }

        if (inRand(y, Y_INAPOI, 0.05f)) {
            app.sunet.mutare();
            app.inapoi();
            return true;
        }
        return false;
    }

    private void deschideLink() {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(LINK));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.context.startActivity(i);
        } catch (Exception ignored) { }
    }

    @Override
    public boolean inapoi() {
        return false;
    }
}
