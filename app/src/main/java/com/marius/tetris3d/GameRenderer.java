package com.marius.tetris3d;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {

    public Joc joc;
    private Cub cub;
    private Particule particule;
    private Stele stele;

    private final float[] proiectie = new float[16];
    private final float[] camera = new float[16];
    private final float[] vizProiectie = new float[16];
    private final float[] model = new float[16];
    private final float[] mvp = new float[16];

    private long timpAnterior;
    private float rotatieFundal = 0f;
    private float puls = 0f;
    private float leganare = 0f;
    private float timpTotal = 0f;

    private static final float[][] CULORI = {
            {0.15f, 0.85f, 0.95f},
            {0.95f, 0.85f, 0.15f},
            {0.70f, 0.25f, 0.90f},
            {0.20f, 0.85f, 0.35f},
            {0.95f, 0.20f, 0.25f},
            {0.20f, 0.35f, 0.95f},
            {0.98f, 0.55f, 0.10f}
    };

    private float offX, offY;

    // zonele apasabile, in coordonate de ecran (0..1)
    public float latEcran = 1f, inaltEcran = 1f;

    public GameRenderer(Context context) {
        joc = new Joc();
        particule = new Particule();
        stele = new Stele();
        joc.particule = particule;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.03f, 0.04f, 0.09f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        cub = new Cub();
        timpAnterior = System.currentTimeMillis();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int latime, int inaltime) {
        GLES20.glViewport(0, 0, latime, inaltime);
        latEcran = latime;
        inaltEcran = inaltime;

        float raport = (float) latime / inaltime;
        Matrix.perspectiveM(proiectie, 0, 46f, raport, 1f, 90f);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        long acum = System.currentTimeMillis();
        float dt = (acum - timpAnterior) / 1000f;
        timpAnterior = acum;
        if (dt > 0.1f) dt = 0.1f;

        joc.actualizeaza(dt);
        particule.actualizeaza(dt);
        stele.actualizeaza(dt);

        rotatieFundal += dt * 6f;
        puls += dt * 2.4f;
        leganare += dt * 0.28f;
        timpTotal += dt;

        // cutremur pe camera
        float scut = joc.cutremurGlobal;
        float zgX = (float) Math.sin(timpTotal * 47f) * scut * 0.42f;
        float zgY = (float) Math.cos(timpTotal * 39f) * scut * 0.32f;

        float camX = 3.4f + (float) Math.sin(leganare) * 1.3f + zgX;
        float camY = -2.6f + (float) Math.cos(leganare * 0.7f) * 0.8f + zgY;

        Matrix.setLookAtM(camera, 0,
                camX, camY, 30f,
                0f, 0.5f, 0f,
                0f, 1f, 0f);
        Matrix.multiplyMM(vizProiectie, 0, proiectie, 0, camera, 0);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float lx = (float) Math.sin(Math.toRadians(rotatieFundal)) * 16f;
        float lz = (float) Math.cos(Math.toRadians(rotatieFundal)) * 16f + 12f;
        cub.seteazaLumina(lx, 18f, lz);

        offX = -Joc.COLOANE / 2f + 0.5f;
        offY = -Joc.RANDURI / 2f + 1.0f;

        stele.deseneaza(cub, vizProiectie, model, mvp);

        deseneazaStalpi();
        deseneazaPodea();
        deseneazaBlocuri();

        if (!joc.terminat) deseneazaPiesa();

        particule.deseneaza(cub, vizProiectie, model, mvp, offX, offY);

        deseneazaUrmatoarea();
        deseneazaInterfata();
        deseneazaButonPauza();

        if (joc.pauza)   ecranPauza();
        if (joc.terminat) ecranFinal();
    }

    private void deseneazaStalpi() {
        for (int r = 0; r < Joc.RANDURI; r++) {
            deseneaza(offX - 0.85f, offY + r, -1.2f, 0.10f, 0.12f, 0.20f, 0.55f, 0.5f);
            deseneaza(offX + Joc.COLOANE - 0.15f, offY + r, -1.2f, 0.10f, 0.12f, 0.20f, 0.55f, 0.5f);
        }
    }

    // podeaua se incinge sub coloanele unde va cadea piesa
    private void deseneazaPodea() {
        float ap = joc.terminat ? 0f : joc.apropiere();

        for (int c = 0; c < Joc.COLOANE; c++) {
            boolean tinta = !joc.terminat && joc.coloanaTinta(c);

            float r = 0.20f, g = 0.24f, b = 0.40f, a = 0.85f;

            if (tinta) {
                float caldura = ap * ap;
                r = 0.20f + caldura * 0.80f;
                g = 0.24f + caldura * 0.28f;
                b = 0.40f - caldura * 0.34f;
                a = 0.85f + caldura * 0.15f;
            }
            deseneaza(offX + c, offY - 1f, -0.4f, r, g, b, a, 0.92f);
        }
    }

    // blocurile asezate; cele de sub piesa tremura si se lumineaza
    private void deseneazaBlocuri() {
        float ap = joc.terminat ? 0f : joc.apropiere();

        for (int r = 0; r < Joc.RANDURI; r++) {
            for (int c = 0; c < Joc.COLOANE; c++) {
                int val = joc.tabla[r][c];
                if (val == 0) continue;

                float[] cul = CULORI[val - 1];

                float trem = joc.tremurRand[r] + joc.cutremurGlobal * 0.6f;
                boolean simte = !joc.terminat && joc.coloanaTinta(c) && r <= joc.pozitieFantoma() + 1;
                if (simte) trem += ap * 0.55f;

                float dx = (float) Math.sin(timpTotal * 41f + r * 2.1f + c) * trem * 0.10f;
                float dy = (float) Math.cos(timpTotal * 53f + c * 1.7f) * trem * 0.07f;

                float lum = 1f + (simte ? ap * 0.45f : 0f) + trem * 0.20f;
                float rr = Math.min(1f, cul[0] * lum);
                float gg = Math.min(1f, cul[1] * lum);
                float bb = Math.min(1f, cul[2] * lum);

                deseneaza(offX + c + dx, offY + r + dy, 0f, rr, gg, bb, 1f, 1f);
            }
        }
    }

    private void deseneazaPiesa() {
        int[][] forma = joc.formaCurenta();
        float[] culP = CULORI[joc.tipCurent];
        for (int i = 0; i < 4; i++) {
            float cx = offX + joc.pieseX + forma[i][0];
            float cy = offY + joc.pieseYVizual() + forma[i][1];
            deseneaza(cx, cy, 0f, culP[0], culP[1], culP[2], 1f, 1f);
        }
    }

    private void deseneazaUrmatoarea() {
        int[][] forma = Joc.formaPiesei(joc.tipUrmator, 0);
        float[] cul = CULORI[joc.tipUrmator];
        float bazaX = offX + Joc.COLOANE / 2f - 1.1f;
        float bazaY = offY - 3.2f;
        float scara = 0.40f;

        for (int i = 0; i < 4; i++) {
            float cx = bazaX + forma[i][0] * scara;
            float cy = bazaY + forma[i][1] * scara;
            deseneaza(cx, cy, -0.3f, cul[0], cul[1], cul[2], 0.95f, scara);
        }
    }

    private void deseneazaInterfata() {
        float sus = offY + Joc.RANDURI + 2.6f;
        float scMic = 0.17f;
        float scMare = 0.26f;

        // stanga: SCOR + RECORD
        text("SCOR", offX - 0.8f, sus, scMic, 0.60f, 0.66f, 0.85f);
        deseneazaNumar(joc.scor, offX - 0.8f, sus - 1.15f, scMare, 1.0f, 0.92f, 0.50f);

        text("RECORD", offX - 0.8f, sus - 2.9f, scMic, 0.60f, 0.66f, 0.85f);
        deseneazaNumar(joc.record, offX - 0.8f, sus - 3.9f, scMic, 0.85f, 0.75f, 0.95f);

        // dreapta: NIVEL + LINII
        float dr = offX + Joc.COLOANE - 2.6f;
        text("NIVEL", dr, sus, scMic, 0.60f, 0.66f, 0.85f);
        deseneazaNumar(joc.nivel, dr, sus - 1.15f, scMare, 0.45f, 0.95f, 1.0f);

        text("LINII", dr, sus - 2.9f, scMic, 0.60f, 0.66f, 0.85f);
        deseneazaNumar(joc.linii, dr, sus - 3.9f, scMic, 0.70f, 0.80f, 1.0f);
    }

    // butonul de pauza: doua bare, sus in mijloc
    private void deseneazaButonPauza() {
        if (joc.terminat) return;
        float x = 0f;
        float y = offY + Joc.RANDURI + 1.2f;
        float s = 0.24f;

        for (int i = 0; i < 3; i++) {
            deseneaza(x - 0.3f, y - i * s, 0f, 0.75f, 0.82f, 1.0f, 0.85f, s);
            deseneaza(x + 0.3f, y - i * s, 0f, 0.75f, 0.82f, 1.0f, 0.85f, s);
        }
    }

    private void ecranPauza() {
        float s = 0.34f;
        text("PAUZA", -2.4f, 2.0f, s, 1.0f, 0.95f, 0.60f);
        text("ATINGE", -2.6f, -0.6f, 0.20f, 0.65f, 0.72f, 0.90f);
    }

    private void ecranFinal() {
        text("FINAL", -2.4f, 5.0f, 0.34f, 1.0f, 0.35f, 0.35f);

        text("SCOR", -1.6f, 2.4f, 0.20f, 0.65f, 0.72f, 0.90f);
        deseneazaNumar(joc.scor, -1.8f, 1.2f, 0.30f, 1.0f, 0.92f, 0.50f);

        text("RECORD", -2.2f, -0.8f, 0.18f, 0.65f, 0.72f, 0.90f);
        deseneazaNumar(joc.record, -1.8f, -1.8f, 0.24f, 0.85f, 0.75f, 0.95f);

        // butoane
        text("DIN NOU", -2.9f, -4.2f, 0.24f, 0.40f, 1.0f, 0.55f);
        text("IESIRE",  -2.4f, -7.0f, 0.24f, 1.0f, 0.45f, 0.45f);
    }

    private void text(String s, float x, float y, float scara,
                      float r, float g, float b) {
        float latC = (Cifre.latime() + 1) * scara;
        for (int k = 0; k < s.length(); k++) {
            int[][] m = Cifre.model(s.charAt(k));
            for (int rr = 0; rr < Cifre.inaltime(); rr++) {
                for (int cc = 0; cc < Cifre.latime(); cc++) {
                    if (m[rr][cc] == 1) {
                        deseneaza(x + k * latC + cc * scara,
                                  y - rr * scara, 0f, r, g, b, 1f, scara);
                    }
                }
            }
        }
    }

    private void deseneazaNumar(int valoare, float x, float y, float scara,
                                float r, float g, float b) {
        text(String.valueOf(valoare), x, y, scara, r, g, b);
    }

    private void deseneaza(float x, float y, float z,
                           float r, float g, float b,
                           float alfa, float scara) {
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, x, y, z);
        Matrix.scaleM(model, 0, scara, scara, scara);
        Matrix.multiplyMM(mvp, 0, vizProiectie, 0, model, 0);
        cub.deseneaza(mvp, model, r, g, b, alfa);
    }
}
