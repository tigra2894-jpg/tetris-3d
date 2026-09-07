package com.marius.tetris3d;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Stratul de interfata: text normal, cu font real si umbre,
 * desenat pe o imagine si afisat peste scena 3D.
 *
 * Pozitiile sunt fractiuni de ecran: 0..1 pe orizontala si verticala,
 * la fel ca la atingere (0,0 = stanga sus, 1,1 = dreapta jos).
 */
public class CapaUI {

    private Bitmap bitmap;
    private Canvas canvas;
    private int latimePx = 2, inaltimePx = 2;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);

    private int textura = -1;
    private int program;
    private int locPos, locUV, locTextura;
    private FloatBuffer bufVarf;

    private static final String VS =
        "attribute vec2 aPos;\n" +
        "attribute vec2 aUV;\n" +
        "varying vec2 vUV;\n" +
        "void main() {\n" +
        "  vUV = aUV;\n" +
        "  gl_Position = vec4(aPos, 0.0, 1.0);\n" +
        "}\n";

    private static final String FS =
        "precision mediump float;\n" +
        "varying vec2 vUV;\n" +
        "uniform sampler2D uTex;\n" +
        "void main() {\n" +
        "  gl_FragColor = texture2D(uTex, vUV);\n" +
        "}\n";

    public void pregatesteGL() {
        int vs = compileaza(GLES20.GL_VERTEX_SHADER, VS);
        int fs = compileaza(GLES20.GL_FRAGMENT_SHADER, FS);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vs);
        GLES20.glAttachShader(program, fs);
        GLES20.glLinkProgram(program);

        locPos     = GLES20.glGetAttribLocation(program, "aPos");
        locUV      = GLES20.glGetAttribLocation(program, "aUV");
        locTextura = GLES20.glGetUniformLocation(program, "uTex");

        float[] date = {
            -1f,  1f,   0f, 0f,
            -1f, -1f,   0f, 1f,
             1f,  1f,   1f, 0f,
             1f, -1f,   1f, 1f
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(date.length * 4);
        bb.order(ByteOrder.nativeOrder());
        bufVarf = bb.asFloatBuffer();
        bufVarf.put(date).position(0);

        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        textura = tex[0];

        redimensioneaza(2, 2);
    }

    /** se cheama la fiecare schimbare de marime a ecranului */
    public void redimensioneaza(int latimeRealaPx, int inaltimeRealaPx) {
        // rezolutie mai mica pentru viteza; textul tot arata clar
        float scara = Math.min(1f, 760f / Math.max(1, latimeRealaPx));
        latimePx = Math.max(2, Math.round(latimeRealaPx * scara));
        inaltimePx = Math.max(2, Math.round(inaltimeRealaPx * scara));

        bitmap = Bitmap.createBitmap(latimePx, inaltimePx, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    /** goleste stratul; se cheama o data pe cadru, inainte de desenare */
    public void inceputCadru() {
        if (bitmap != null) bitmap.eraseColor(Color.TRANSPARENT);
    }

    /** trimite stratul pe ecran; se cheama dupa ce s-a desenat scena 3D */
    public void deseneazaPeEcran() {
        if (canvas == null) return;

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textura);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        GLES20.glUseProgram(program);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        bufVarf.position(0);
        GLES20.glEnableVertexAttribArray(locPos);
        GLES20.glVertexAttribPointer(locPos, 2, GLES20.GL_FLOAT, false, 16, bufVarf);

        bufVarf.position(2);
        GLES20.glEnableVertexAttribArray(locUV);
        GLES20.glVertexAttribPointer(locUV, 2, GLES20.GL_FLOAT, false, 16, bufVarf);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textura);
        GLES20.glUniform1i(locTextura, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(locPos);
        GLES20.glDisableVertexAttribArray(locUV);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    private int compileaza(int tip, String cod) {
        int s = GLES20.glCreateShader(tip);
        GLES20.glShaderSource(s, cod);
        GLES20.glCompileShader(s);
        return s;
    }

    // ---------- desenare text ----------

    public void text(String s, float xFrac, float yFrac, float marime, int culoare) {
        scrie(s, xFrac, yFrac, marime, culoare, Paint.Align.LEFT);
    }

    public void textCentrat(String s, float xFrac, float yFrac, float marime, int culoare) {
        scrie(s, xFrac, yFrac, marime, culoare, Paint.Align.CENTER);
    }

    public void textDreapta(String s, float xFrac, float yFrac, float marime, int culoare) {
        scrie(s, xFrac, yFrac, marime, culoare, Paint.Align.RIGHT);
    }

    private void scrie(String s, float xFrac, float yFrac, float marime,
                       int culoare, Paint.Align aliniere) {
        if (canvas == null || s == null || s.isEmpty()) return;

        float px = xFrac * latimePx;
        float py = yFrac * inaltimePx;
        float marimePx = marime * inaltimePx;

        paint.setTypeface(font);
        paint.setTextSize(marimePx);
        paint.setTextAlign(aliniere);

        // umbra, usor deplasata
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAlpha(150);
        canvas.drawText(s, px + marimePx * 0.05f, py + marimePx * 0.09f, paint);

        // contur negru, ca textul sa iasa in evidenta pe orice fundal
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(marimePx * 0.11f);
        paint.setColor(Color.BLACK);
        paint.setAlpha(210);
        canvas.drawText(s, px, py, paint);

        // textul propriu-zis
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(culoare);
        paint.setAlpha(255);
        canvas.drawText(s, px, py, paint);
    }

    /** latimea unui text, ca fractiune din latimea ecranului */
    public float latimeTextFrac(String s, float marime) {
        if (canvas == null || s == null) return 0f;
        paint.setTypeface(font);
        paint.setTextSize(marime * inaltimePx);
        return paint.measureText(s) / latimePx;
    }

    // ---------- panouri si bare, pentru fundal sub text ----------

    public void panou(float xFrac, float yFrac, float latFrac, float inaltFrac,
                      int culoare, float razaColtFrac) {
        if (canvas == null) return;
        float st = xFrac * latimePx;
        float su = yFrac * inaltimePx;
        float dr = st + latFrac * latimePx;
        float jo = su + inaltFrac * inaltimePx;
        float raza = razaColtFrac * inaltimePx;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(culoare);
        canvas.drawRoundRect(st, su, dr, jo, raza, raza, paint);
    }

    public void bara(float xFrac, float yFrac, float latFrac, float inaltFrac,
                     float umplere, int culoareFundal, int culoareUmplere) {
        panou(xFrac, yFrac, latFrac, inaltFrac, culoareFundal, inaltFrac * 0.5f);
        if (umplere > 0.01f) {
            panou(xFrac, yFrac, latFrac * Math.min(1f, umplere), inaltFrac,
                    culoareUmplere, inaltFrac * 0.5f);
        }
    }
}
