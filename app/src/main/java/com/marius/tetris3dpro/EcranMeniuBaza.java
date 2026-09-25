package com.marius.tetris3dpro;

import java.util.Random;

/** fundal 3D comun pentru meniuri: piese plutitoare + stele, si o lista de butoane */
public abstract class EcranMeniuBaza extends Ecran {

    protected static final int ALB = 0xFFFFFF, GRI = 0xB9C2D6, CIAN = 0x4DE1FF, GALBEN = 0xFFD84D,
            ROZ = 0xFF5FA8, VERDE = 0x7CFF6B, FUNDAL_BTN = 0x2A3350, ROSU = 0xFF5F5F;

    private static final int NR_PLUTITOARE = 9;
    private final float[] px = new float[NR_PLUTITOARE], py = new float[NR_PLUTITOARE], pz = new float[NR_PLUTITOARE];
    private final float[] pv = new float[NR_PLUTITOARE], pr = new float[NR_PLUTITOARE], prv = new float[NR_PLUTITOARE];
    private final int[] pt = new int[NR_PLUTITOARE];
    private final Random rnd = new Random(3);

    protected static final float BTN_LAT = 0.64f, BTN_INALT = 0.078f;
    protected final float[] btnY;
    protected final String[] btnText;
    protected final int[] btnCul;
    protected final float[] btnApasat;
    protected int apasatIdx = -1;

    public EcranMeniuBaza(Aplicatie app, String[] texte, int[] culori, float yPrim, float pas) {
        super(app);
        btnText = texte; btnCul = culori;
        btnY = new float[texte.length];
        btnApasat = new float[texte.length];
        for (int i = 0; i < texte.length; i++) btnY[i] = yPrim + i * pas;
        for (int i = 0; i < NR_PLUTITOARE; i++) reseteaza(i, true);
    }

    private void reseteaza(int i, boolean oriunde) {
        px[i] = (rnd.nextFloat() - 0.5f) * 22f;
        py[i] = oriunde ? (rnd.nextFloat() - 0.5f) * 30f : -18f;
        pz[i] = -6f - rnd.nextFloat() * 14f;
        pv[i] = 0.8f + rnd.nextFloat() * 1.6f;
        pr[i] = rnd.nextFloat() * 360f;
        prv[i] = (rnd.nextFloat() - 0.5f) * 60f;
        pt[i] = rnd.nextInt(Joc.NR_PIESE);
    }

    @Override
    public void actualizeaza(float dt) {
        super.actualizeaza(dt);
        for (int i = 0; i < NR_PLUTITOARE; i++) {
            py[i] += pv[i] * dt;
            pr[i] += prv[i] * dt;
            if (py[i] > 18f) reseteaza(i, false);
        }
        for (int i = 0; i < btnApasat.length; i++)
            if (i != apasatIdx) btnApasat[i] = Math.max(0f, btnApasat[i] - dt * 5f);
    }

    @Override
    public void deseneaza3D(Randare r) {
        r.seteazaCamera((float) Math.sin(app.timp * 0.3f) * 0.8f, 0f, 30f, 0f, 0f, 0f);
        float[] fundal = app.setari.culoareFundal();
        float[] accent = {0.35f + fundal[0] * 3f, 0.55f + fundal[1] * 3f, 0.9f};
        app.fundal.deseneazaStele(r, accent);
        app.fundal.deseneazaPodea(r, -13f, accent);

        float[][] cul = app.culoriPiese();
        for (int i = 0; i < NR_PLUTITOARE; i++) {
            float[] c = cul[pt[i]];
            int[][] f = Joc.FORME[pt[i]][0];
            float ad = 1f - (-pz[i] - 6f) / 14f;
            float alfa = 0.35f + 0.45f * ad;
            for (int k = 0; k < 4; k++) {
                float lx = f[k][0] - 0.5f, ly = f[k][1] - 0.5f;
                double a = Math.toRadians(pr[i]);
                float rx = (float) (lx * Math.cos(a) - ly * Math.sin(a));
                float ry = (float) (lx * Math.sin(a) + ly * Math.cos(a));
                r.cubRotit(px[i] + rx, py[i] + ry, pz[i], pr[i] * 0.7f, 0.3f, 1f, 0.2f,
                        c[0], c[1], c[2], alfa, 0.95f);
            }
        }
        deseneazaExtra3D(r);
    }

    protected void deseneazaExtra3D(Randare r) { }

    protected void deseneazaButoane(CapaUI ui, float fade) {
        for (int i = 0; i < btnText.length; i++) {
            float ap = btnApasat[i];
            float lat = BTN_LAT + ap * 0.03f;
            ui.buton(btnText[i], 0.5f, btnY[i], lat, BTN_INALT,
                    argb(fade * (0.72f + ap * 0.25f), FUNDAL_BTN),
                    argb(fade * (0.6f + ap * 0.4f), btnCul[i]),
                    argb(fade, ALB), 0.030f);
        }
    }

    protected int butonLa(float x, float y) {
        if (Math.abs(x - 0.5f) > BTN_LAT / 2f + 0.03f) return -1;
        for (int i = 0; i < btnY.length; i++)
            if (Math.abs(y - btnY[i]) <= BTN_INALT / 2f + 0.008f) return i;
        return -1;
    }

    @Override
    public void apasare(int p, float x, float y) {
        int i = butonLa(x, y);
        apasatIdx = i;
        if (i >= 0) btnApasat[i] = 1f;
    }

    @Override
    public void ridicare(int p, float x, float y) {
        int i = butonLa(x, y);
        if (i >= 0 && i == apasatIdx) {
            app.sunet.reda(Sunet.MENIU);
            app.vibratii.scurt();
            laButon(i);
        }
        apasatIdx = -1;
    }

    protected abstract void laButon(int idx);

    protected void titlu(CapaUI ui, String s, float y, float fade) {
        ui.textCentrat(s, 0.5f, y, 0.058f, argb(fade, ALB));
        ui.bara(0.3f, y + 0.022f, 0.4f, 0.005f, 1f, 0, argb(fade * 0.8f, CIAN));
    }
}
