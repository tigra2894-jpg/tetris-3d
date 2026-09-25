package com.marius.tetris3dpro;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GameRenderer implements GLSurfaceView.Renderer {

    private final Aplicatie app;
    private long ultimulTimp = 0L;

    public GameRenderer(Aplicatie app) { this.app = app; }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        app.pornesteGL();
        ultimulTimp = 0L;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        GLES20.glViewport(0, 0, w, h);
        app.redimensioneaza(w, h);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        long acum = System.nanoTime();
        float dt = ultimulTimp == 0L ? 1f / 60f : (acum - ultimulTimp) / 1_000_000_000f;
        ultimulTimp = acum;
        if (dt > 0.05f) dt = 0.05f;
        if (dt < 0.001f) dt = 0.001f;

        float[] f = app.setari.culoareFundal();
        GLES20.glClearColor(f[0], f[1], f[2], 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        app.actualizeaza(dt);
        app.deseneaza();
    }
}
