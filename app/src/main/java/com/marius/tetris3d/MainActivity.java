package com.marius.tetris3d;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends Activity {

    private GameView gameView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        prefs = getSharedPreferences("tetris3d", MODE_PRIVATE);

        gameView = new GameView(this);
        setContentView(gameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.onPause();
        salveazaRecord();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.onResume();
    }

    private void salveazaRecord() {
        // recordul se citeste din joc si se scrie in memoria telefonului
        try {
            java.lang.reflect.Field f = GameView.class.getDeclaredField("renderer");
            f.setAccessible(true);
            GameRenderer rend = (GameRenderer) f.get(gameView);
            if (rend != null && rend.joc != null) {
                int rec = Math.max(rend.joc.record, prefs.getInt("record", 0));
                prefs.edit().putInt("record", rec).apply();
            }
        } catch (Exception ignored) { }
    }
}
