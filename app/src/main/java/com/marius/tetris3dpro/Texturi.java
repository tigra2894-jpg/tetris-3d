package com.marius.tetris3dpro;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Incarcarea imaginilor din assets/texturi/.
 * Fiecare textura este optionala: daca fisierul lipseste, jocul foloseste
 * aspectul procedural (fara imagini).
 */
public final class Texturi {

    public static final String DOSAR = "texturi/";
    private static final String[] EXTENSII = {".png", ".jpg", ".webp"};
    private static final int MAX_LATURA = 2048;

    private Texturi() { }

    /** citeste assets/texturi/<nume>.(png|jpg|webp); null daca nu exista */
    public static Bitmap citeste(Context ctx, String nume) {
        for (String ext : EXTENSII) {
            String cale = DOSAR + nume + ext;
            try {
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                try (InputStream in = ctx.getAssets().open(cale)) {
                    BitmapFactory.decodeStream(in, null, o);
                }
                int pas = 1;
                while (Math.max(o.outWidth, o.outHeight) / pas > MAX_LATURA) pas *= 2;

                BitmapFactory.Options d = new BitmapFactory.Options();
                d.inSampleSize = pas;
                d.inPreferredConfig = Bitmap.Config.ARGB_8888;
                d.inScaled = false;
                try (InputStream in = ctx.getAssets().open(cale)) {
                    Bitmap b = BitmapFactory.decodeStream(in, null, d);
                    if (b != null) return b;
                }
            } catch (IOException ignored) {
                // fisierul cu extensia asta nu exista, incercam urmatoarea
            }
        }
        return null;
    }

    /**
     * Trimite imaginea la GPU. Texturile cu laturi putere a lui 2 primesc
     * mipmap-uri si se pot repeta; celelalte (NPOT) nu pot, in GLES 2.0.
     * Returneaza 0 daca bitmap-ul e null.
     */
    public static int incarca(Bitmap b) {
        return incarca(b, false);
    }

    /** repeta = true: textura se poate repeta (GL_REPEAT), doar daca laturile sunt puteri ale lui 2 */
    public static int incarca(Bitmap b, boolean repeta) {
        if (b == null) return 0;
        int[] id = new int[1];
        GLES20.glGenTextures(1, id, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id[0]);

        boolean pot = putere2(b.getWidth()) && putere2(b.getHeight());
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                pot ? GLES20.GL_LINEAR_MIPMAP_LINEAR : GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        int impachetare = repeta && pot ? GLES20.GL_REPEAT : GLES20.GL_CLAMP_TO_EDGE;
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, impachetare);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, impachetare);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, b, 0);
        if (pot) GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return id[0];
    }

    private static boolean putere2(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
}
