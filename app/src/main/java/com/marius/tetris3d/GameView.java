package com.marius.tetris3d;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class GameView extends GLSurfaceView {

    private final GameRenderer renderer;
    private final Context ctx;

    private float startX, startY;
    private long startTime;
    private boolean swipeFacut;

    private static final float PRAG = 55f;

    public GameView(Context context) {
        super(context);
        ctx = context;

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        renderer = new GameRenderer(context);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        float w = getWidth();
        float h = getHeight();

        Joc joc = renderer.joc;

        if (e.getAction() == MotionEvent.ACTION_DOWN) {

            // ecran de final: butoane
            if (joc.terminat) {
                if (y > h * 0.62f && y < h * 0.76f) {
                    joc.jocNou();
                } else if (y > h * 0.78f) {
                    if (ctx instanceof Activity) ((Activity) ctx).finish();
                }
                return true;
            }

            // in pauza: orice atingere reia jocul
            if (joc.pauza) {
                joc.pauza = false;
                return true;
            }

            // butonul de pauza: sus in mijloc
            if (y < h * 0.13f && x > w * 0.38f && x < w * 0.62f) {
                joc.pauza = true;
                return true;
            }

            startX = x;
            startY = y;
            startTime = System.currentTimeMillis();
            swipeFacut = false;
            return true;
        }

        if (joc.terminat || joc.pauza) return true;

        switch (e.getAction()) {

            case MotionEvent.ACTION_MOVE: {
                float dx = x - startX;
                float dy = y - startY;

                if (Math.abs(dx) > PRAG && Math.abs(dx) > Math.abs(dy)) {
                    joc.muta(dx > 0 ? 1 : -1);
                    startX = x;
                    startY = y;
                    swipeFacut = true;
                } else if (dy > PRAG && Math.abs(dy) > Math.abs(dx)) {
                    joc.coboaraRapid();
                    startY = y;
                    swipeFacut = true;
                }
                return true;
            }

            case MotionEvent.ACTION_UP: {
                long durata = System.currentTimeMillis() - startTime;
                float dx = Math.abs(x - startX);
                float dy = Math.abs(y - startY);

                if (!swipeFacut && durata < 250 && dx < PRAG && dy < PRAG) {
                    joc.roteste();
                } else if ((startY - y) > PRAG * 2) {
                    joc.trantesteJos();
                }
                return true;
            }
        }
        return super.onTouchEvent(e);
    }
}
