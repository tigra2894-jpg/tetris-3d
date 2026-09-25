package com.marius.tetris3dpro;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class GameView extends GLSurfaceView {

    private final Aplicatie app;
    private final GameRenderer renderer;

    public GameView(Activity act) {
        super(act);
        app = new Aplicatie(act);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setPreserveEGLContextOnPause(true);
        renderer = new GameRenderer(app);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    public Aplicatie aplicatie() { return app; }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int actiune = ev.getActionMasked();
        int idx = ev.getActionIndex();
        float w = Math.max(1f, getWidth()), h = Math.max(1f, getHeight());

        switch (actiune) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int id = ev.getPointerId(idx);
                final float x = ev.getX(idx) / w, y = ev.getY(idx) / h;
                queueEvent(() -> app.apasare(id, x, y));
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                for (int i = 0; i < ev.getPointerCount(); i++) {
                    final int id = ev.getPointerId(i);
                    final float x = ev.getX(i) / w, y = ev.getY(i) / h;
                    queueEvent(() -> app.miscare(id, x, y));
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL: {
                final int id = ev.getPointerId(idx);
                final float x = ev.getX(idx) / w, y = ev.getY(idx) / h;
                queueEvent(() -> app.ridicare(id, x, y));
                break;
            }
        }
        return true;
    }

    @Override
    public void onPause() {
        queueEvent(app::laPauza);
        super.onPause();
    }
}
