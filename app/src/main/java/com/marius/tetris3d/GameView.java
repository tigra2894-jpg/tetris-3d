package com.marius.tetris3d;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class GameView extends GLSurfaceView {

    private final GameRenderer renderer;
    private final Aplicatie app;

    private float ultimX, ultimY;

    public GameView(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        app = new Aplicatie(context);
        renderer = new GameRenderer(app);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public void laPauzaAplicatie() {
        app.laPauza();
    }

    public boolean inapoi() {
        return app.inapoi();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ultimX = x;
                ultimY = y;
                queueEvent(() -> app.atingere(x, y));
                return true;

            case MotionEvent.ACTION_MOVE: {
                final float dx = x - ultimX;
                final float dy = y - ultimY;
                queueEvent(() -> app.tragere(x, y, dx, dy));
                ultimX = x;
                ultimY = y;
                return true;
            }

            case MotionEvent.ACTION_UP:
                queueEvent(() -> app.ridicare(x, y));
                return true;
        }
        return super.onTouchEvent(e);
    }
}
