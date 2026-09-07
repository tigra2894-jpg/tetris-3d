package com.marius.tetris3d;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {

    private final Aplicatie app;
    private Cub cub;
    private long timpAnterior;

    public GameRenderer(Aplicatie app) {
        this.app = app;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        float[] fundal = app.setari.culoareFundal();
        GLES20.glClearColor(fundal[0], fundal[1], fundal[2], 1.0f);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        cub = new Cub();
        app.porneste(cub);
        timpAnterior = System.currentTimeMillis();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int latime, int inaltime) {
        GLES20.glViewport(0, 0, latime, inaltime);
        app.latimePx = latime;
        app.inaltimePx = inaltime;

        if (app.desenator != null) {
            app.desenator.raport = (float) latime / inaltime;
        }
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        if (!app.gata()) return;

        long acum = System.currentTimeMillis();
        float dt = (acum - timpAnterior) / 1000f;
        timpAnterior = acum;
        if (dt > 0.1f) dt = 0.1f;

        app.actualizeaza(dt);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        app.deseneaza();
    }
}
