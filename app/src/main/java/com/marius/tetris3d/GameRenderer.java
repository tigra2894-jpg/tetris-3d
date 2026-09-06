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

        // camera: coborata si dusa lateral, se leagana foarte incet
        float camX = 3.4f + (float) Math.sin(leganare) * 1.3f;
        float camY = -2.6f + (float) Math.cos(leganare * 0.7f) * 0.8f;
        float camZ = 30f;

        Matrix.setLookAtM(camera, 0,
                camX, camY, camZ,
                0f, 0.5f, 0f,
                0f, 1f, 0f);
        Matrix.multiplyMM(vizProiectie, 0, proiectie, 0, camera, 0);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float lx = (float) Math.sin(Math.toRadians(rotatieFundal)) * 16f;
        float lz = (float) Math.cos(Math.toRadians(rotatieFundal)) * 16f + 12f;
        cub.seteazaLumina(lx, 18f, lz);

        float latTabla = Joc.COLOANE;
        float inaltTabla = Joc.RANDURI;
        offX = -latTabla / 2f + 0.5f;
        offY = -inaltTabla / 2f + 1.0f;

        stele.deseneaza(cub, vizProiectie, model, mvp);

        deseneazaChenar(latTabla);
        deseneazaBlocuri();
        deseneazaFantoma();
        deseneazaPiesa();

        particule.deseneaza(cub, vizProiectie, model, mvp, offX, offY);

        deseneazaInterfata();
    }

    private void deseneazaChenar(float latTabla) {
        // stalpi laterali subtiri, impinsi in spate
        for (int r = 0; r < Joc.RANDURI; r++) {
            deseneaza(offX - 0.85f, offY + r, -1.2f, 0.10f, 0.12f, 0.20f, 0.60f, 0.55f);
            deseneaza(offX + latTabla - 0.15f, offY + r, -1.2f, 0.10f, 0.12f, 0.20f, 0.60f, 0.55f);
        }
        // podeaua, mai vizibila
        for (int c = 0; c < Joc.COLOANE; c++) {
            deseneaza(offX + c, offY - 1f, -0.4f, 0.20f, 0.24f, 0.40f, 0.85f, 0.92f);
        }
    }

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

    private void deseneazaFantoma() {
        int[][] forma = joc.formaCurenta();
        int yFantoma = joc.pozitieFantoma();
        float[] cul = CULORI[joc.tipCurent];
        float pulsatie = 0.20f + 0.14f * (float) Math.abs(Math.sin(puls));
        for (int i = 0; i < 4; i++) {
            float cx = offX + joc.pieseX + forma[i][0];
            float cy = offY + yFantoma + forma[i][1];
            deseneaza(cx, cy, 0f, cul[0], cul[1], cul[2], pulsatie, 0.90f);
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

    private void deseneazaInterfata() {
        float sus = offY + Joc.RANDURI + 1.2f;
        float sc = 0.26f;

        deseneazaNumar(joc.scor, offX - 0.5f, sus, sc, 1.0f, 0.92f, 0.50f);
        deseneazaNumar(joc.nivel, offX + Joc.COLOANE - 1.6f, sus, sc, 0.45f, 0.95f, 1.0f);
        deseneazaNumar(joc.linii, offX + Joc.COLOANE - 1.6f, sus - 1.9f, sc, 0.70f, 0.80f, 1.0f);

        deseneazaUrmatoarea();
    }

    private void deseneazaUrmatoarea() {
        int[][] forma = Joc.formaPiesei(joc.tipUrmator, 0);
        float[] cul = CULORI[joc.tipUrmator];
        float bazaX = offX + Joc.COLOANE / 2f - 1.2f;
        float bazaY = offY - 3.8f;
        float scara = 0.42f;

        for (int i = 0; i < 4; i++) {
            float cx = bazaX + forma[i][0] * scara;
            float cy = bazaY + forma[i][1] * scara;
            deseneaza(cx, cy, 0f, cul[0], cul[1], cul[2], 1f, scara);
        }
    }

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
