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

    private final float[] proiectie = new float[16];
    private final float[] camera = new float[16];
    private final float[] vizProiectie = new float[16];
    private final float[] model = new float[16];
    private final float[] mvp = new float[16];

    private long timpAnterior;
    private float rotatieFundal = 0f;
    private float puls = 0f;

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

    public GameRenderer(Context context) {
        joc = new Joc();
        particule = new Particule();
        joc.particule = particule;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.04f, 0.05f, 0.10f, 1.0f);
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

        float raport = (float) latime / inaltime;
        Matrix.perspectiveM(proiectie, 0, 52f, raport, 1f, 80f);

        Matrix.setLookAtM(camera, 0,
                0f, 2.0f, 30f,
                0f, 0f, 0f,
                0f, 1f, 0f);

        Matrix.multiplyMM(vizProiectie, 0, proiectie, 0, camera, 0);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        long acum = System.currentTimeMillis();
        float dt = (acum - timpAnterior) / 1000f;
        timpAnterior = acum;
        if (dt > 0.1f) dt = 0.1f;

        joc.actualizeaza(dt);
        particule.actualizeaza(dt);
        rotatieFundal += dt * 6f;
        puls += dt * 2.4f;

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float lx = (float) Math.sin(Math.toRadians(rotatieFundal)) * 14f;
        float lz = (float) Math.cos(Math.toRadians(rotatieFundal)) * 14f + 10f;
        cub.seteazaLumina(lx, 16f, lz);

        float latTabla = Joc.COLOANE;
        float inaltTabla = Joc.RANDURI;
        offX = -latTabla / 2f + 0.5f;
        offY = -inaltTabla / 2f + 1.0f;

        deseneazaChenar(latTabla);
        deseneazaBlocuri();
        deseneazaFantoma();
        deseneazaPiesa();

        particule.deseneaza(cub, vizProiectie, model, mvp, offX, offY);

        deseneazaInterfata();
    }

    // ---------- chenarul tablei ----------
    private void deseneazaChenar(float latTabla) {
        for (int r = 0; r < Joc.RANDURI; r++) {
            deseneaza(offX - 1f, offY + r, -0.5f, 0.16f, 0.18f, 0.28f, 0.55f, 1f);
            deseneaza(offX + latTabla, offY + r, -0.5f, 0.16f, 0.18f, 0.28f, 0.55f, 1f);
        }
        for (int c = -1; c <= Joc.COLOANE; c++) {
            deseneaza(offX + c, offY - 1f, -0.5f, 0.22f, 0.24f, 0.36f, 0.75f, 1f);
        }
    }

    // ---------- blocurile asezate ----------
    private void deseneazaBlocuri() {
        for (int r = 0; r < Joc.RANDURI; r++) {
            for (int c = 0; c < Joc.COLOANE; c++) {
                int val = joc.tabla[r][c];
                if (val > 0) {
                    float[] cul = CULORI[val - 1];
                    deseneaza(offX + c, offY + r, 0f, cul[0], cul[1], cul[2], 1f, 1f);
                }
            }
        }
    }

    // ---------- umbra piesei ----------
    private void deseneazaFantoma() {
        int[][] forma = joc.formaCurenta();
        int yFantoma = joc.pozitieFantoma();
        float pulsatie = 0.16f + 0.10f * (float) Math.abs(Math.sin(puls));
        for (int i = 0; i < 4; i++) {
            float cx = offX + joc.pieseX + forma[i][0];
            float cy = offY + yFantoma + forma[i][1];
            deseneaza(cx, cy, 0f, 0.55f, 0.60f, 0.75f, pulsatie, 0.94f);
        }
    }

    // ---------- piesa care cade ----------
    private void deseneazaPiesa() {
        int[][] forma = joc.formaCurenta();
        float[] culP = CULORI[joc.tipCurent];
        for (int i = 0; i < 4; i++) {
            float cx = offX + joc.pieseX + forma[i][0];
            float cy = offY + joc.pieseYVizual() + forma[i][1];
            deseneaza(cx, cy, 0f, culP[0], culP[1], culP[2], 1f, 1f);
        }
    }

    // ---------- interfata: scor, nivel, linii, piesa urmatoare ----------
    private void deseneazaInterfata() {
        float sus = offY + Joc.RANDURI + 1.5f;
        float scaraCifre = 0.30f;

        // scor, sus in stanga
        deseneazaNumar(joc.scor, offX - 0.5f, sus, scaraCifre,
                1.0f, 0.95f, 0.55f);

        // nivel, sus in dreapta
        deseneazaNumar(joc.nivel, offX + Joc.COLOANE - 2.0f, sus, scaraCifre,
                0.45f, 0.95f, 1.0f);

        // linii, sub nivel
        deseneazaNumar(joc.linii, offX + Joc.COLOANE - 2.0f, sus - 2.2f, scaraCifre,
                0.70f, 0.80f, 1.0f);

        // piesa urmatoare, jos sub tabla
        deseneazaUrmatoarea();
    }

    private void deseneazaUrmatoarea() {
        int[][] forma = Joc.formaPiesei(joc.tipUrmator, 0);
        float[] cul = CULORI[joc.tipUrmator];
        float bazaX = offX + Joc.COLOANE / 2f - 1.5f;
        float bazaY = offY - 4.2f;
        float scara = 0.45f;

        for (int i = 0; i < 4; i++) {
            float cx = bazaX + forma[i][0] * scara;
            float cy = bazaY + forma[i][1] * scara;
            deseneaza(cx, cy, 0f, cul[0], cul[1], cul[2], 1f, scara);
        }
    }

    // deseneaza un numar din cuburi mici
    private void deseneazaNumar(int valoare, float x, float y, float scara,
                                float r, float g, float b) {
        String text = String.valueOf(valoare);
        float latCifra = (Cifre.latime() + 1) * scara;

        for (int k = 0; k < text.length(); k++) {
            int cifra = text.charAt(k) - '0';
            int[][] m = Cifre.model(cifra);

            for (int rr = 0; rr < Cifre.inaltime(); rr++) {
                for (int cc = 0; cc < Cifre.latime(); cc++) {
                    if (m[rr][cc] == 1) {
                        float px = x + k * latCifra + cc * scara;
                        float py = y - rr * scara;
                        deseneaza(px, py, 0f, r, g, b, 1f, scara);
                    }
                }
            }
        }
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
