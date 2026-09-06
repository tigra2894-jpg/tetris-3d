package com.marius.tetris3d;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class GameView extends GLSurfaceView {

    private final GameRenderer renderer;

    private float startX, startY;
    private long startTime;
    private boolean swipeFacut;

    private static final float PRAG = 60f;

    public GameView(Context context) {
        super(context);

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

        switch (e.getAction()) {

            case MotionEvent.ACTION_DOWN:
                startX = x;
                startY = y;
                startTime = System.currentTimeMillis();
                swipeFacut = false;
                return true;

            case MotionEvent.ACTION_MOVE: {
                float dx = x - startX;
                float dy = y - startY;

                if (Math.abs(dx) > PRAG && Math.abs(dx) > Math.abs(dy)) {
                    if (dx > 0) renderer.joc.muta(1);
                    else renderer.joc.muta(-1);
                    startX = x;
                    startY = y;
                    swipeFacut = true;
                }
                else if (dy > PRAG && Math.abs(dy) > Math.abs(dx)) {
                    renderer.joc.coboaraRapid();
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
                    renderer.joc.roteste();
                }
                else if ((startY - y) > PRAG * 2) {
                    renderer.joc.trantesteJos();
                }
                return true;
            }
        }
        return super.onTouchEvent(e);
    }
}
