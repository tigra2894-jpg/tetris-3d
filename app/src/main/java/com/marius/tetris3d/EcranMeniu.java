package com.marius.tetris3d;

public class EcranMeniu extends Ecran {

    private static final float Y_JOACA  = 0.46f;
    private static final float Y_MODURI = 0.58f;
    private static final float Y_SETARI = 0.68f;
    private static final float Y_STAT   = 0.78f;
    private static final float Y_IESIRE = 0.88f;
    private static final float GROSIME  = 0.08f;

    private int apasat = -1;
    private float stralucire = 0f;

    private static final int NR_PIESE = 5;
    private final float[] px = new float[NR_PIESE];
    private final float[] py = new float[NR_PIESE];
    private final float[] pz = new float[NR_PIESE];
    private final float[] rot = new float[NR_PIESE];
    private final float[] vitRot = new float[NR_PIESE];
    private final float[] vitY = new float[NR_PIESE];
    private final int[] tip = new int[NR_PIESE];

    public EcranMeniu(Aplicatie app) {
        super(app);

        java.util.Random rnd = new java.util.Random(11);
        for (int i = 0; i < NR_PIESE; i++) {
            px[i] = (rnd.nextFloat() - 0.5f) * 14f;
            py[i] = (rnd.nextFloat() - 0.5f) * 26f;
            pz[i] = -10f - rnd.nextFloat() * 12f;
            rot[i] = rnd.nextFloat() * 360f;
            vitRot[i] = 8f + rnd.nextFloat() * 16f;
            vitY[i] = 0.5f + rnd.nextFloat() * 1.1f;
            tip[i] = rnd.nextInt(7);
        }
    }

    @Override
    public void laIntrare() {
        super.laIntrare();
        apasat = -1;
        stralucire = 0f;
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);

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

        float leg = timp * 0.22f;
        d.seteazaCamera(
                (float) Math.sin(leg) * 1.0f,
                (float) Math.cos(leg * 0.8f) * 0.6f,
                26f,
                0f, 0f, 0f);

        d.seteazaLumina(
                (float) Math.sin(timp * 0.5f) * 16f,
                14f,
                (float) Math.cos(timp * 0.5f) * 16f + 12f);

        app.stele.deseneaza2(d);
        deseneazaPieseFundal(d);

        float apTitlu = intrare(0f, 0.5f);
        float pulsTitlu = 0.85f + 0.15f * puls(1.6f);
        float yTitlu = ecranLaLume(0.14f);

        d.textPotrivit("TETRIS", 0f, yTitlu, 0.40f,
                1.0f * pulsTitlu, 0.85f * pulsTitlu, 0.30f, apTitlu, 0.80f);
        d.textPotrivit("3D", 0f, yTitlu - 2.6f, 0.40f,
                0.35f, 0.90f * pulsTitlu, 1.0f * pulsTitlu, apTitlu, 0.45f);

        optiune(d, "JOACA",      0, Y_JOACA,  0.30f, 0.40f, 1.00f, 0.55f);
        optiune(d, "MODURI",     1, Y_MODURI, 0.24f, 0.55f, 0.80f, 1.00f);
        optiune(d, "SETARI",     2, Y_SETARI, 0.24f, 0.85f, 0.75f, 1.00f);
        optiune(d, "STATISTICI", 3, Y_STAT,   0.20f, 0.70f, 0.75f, 0.90f);
        optiune(d, "IESIRE",     4, Y_IESIRE, 0.20f, 1.00f, 0.45f, 0.45f);
    }

    private float intrare(float intarziere, float durata) {
        float t = (timp - intarziere) / durata;
        if (t < 0f) return 0f;
        if (t > 1f) return 1f;
        return t * t * (3f - 2f * t);
    }

    private void optiune(Desenator d, String s, int index, float yEcran,
                         float scaraMax, float r, float g, float b) {

        float ap = intrare(index * 0.08f, 0.4f);
        float scara = scaraMax * (0.7f + 0.3f * ap);

        float lum = 1f;
        if (apasat == index) lum = 1f + stralucire * 0.9f;

        d.textPotrivit(s, 0f, ecranLaLume(yEcran), scara,
                Math.min(1f, r * lum), Math.min(1f, g * lum), Math.min(1f, b * lum),
                ap, 0.86f);
    }

    private float ecranLaLume(float yEcran) {
        float h = 26f;
        return h / 2f - yEcran * h;
    }

    private void deseneazaPieseFundal(Desenator d) {
        float[][] culori = app.setari.culoriPiese();

        for (int i = 0; i < NR_PIESE; i++) {
            int[][] forma = Joc.formaPiesei(tip[i], 0);
            float[] cul = culori[tip[i]];

            for (int k = 0; k < 4; k++) {
                float cx = px[i] + (forma[k][0] - 1.5f) * 0.75f;
                float cy = py[i] + (forma[k][1] - 2.0f) * 0.75f;

                d.cubRotit(cx, cy, pz[i],
                        rot[i], 0.5f, 1f, 0.3f,
                        cul[0] * 0.55f, cul[1] * 0.55f, cul[2] * 0.55f,
                        0.30f, 0.65f);
            }
        }
    }

    @Override
    public boolean atingere(float x, float y) {

        if (inRand(y, Y_JOACA, GROSIME)) {
            apasa(0);
            app.jocNou(app.setari.ultimulMod());
            return true;
        }
        if (inRand(y, Y_MODURI, GROSIME)) {
            apasa(1);
            app.mergiLa(app.ecranModuri);
            return true;
        }
        if (inRand(y, Y_SETARI, GROSIME)) {
            apasa(2);
            app.mergiLa(app.ecranSetari);
            return true;
        }
        if (inRand(y, Y_STAT, GROSIME)) {
            apasa(3);
            app.mergiLa(app.ecranStatistici);
            return true;
        }
        if (inRand(y, Y_IESIRE, GROSIME)) {
            apasa(4);
            app.iesire();
            return true;
        }
        return false;
    }

    private void apasa(int index) {
        apasat = index;
        stralucire = 1f;
        app.sunet.rotire();
    }

    @Override
    public boolean inapoi() {
        app.iesire();
        return true;
    }
        }
