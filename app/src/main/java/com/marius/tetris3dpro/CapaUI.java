package com.marius.tetris3dpro;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Stratul 2D de interfata, desenat cu Canvas pe o imagine si afisat
 * ca textura peste scena 3D.
 *
 * Optimizari fata de versiunea veche:
 *   - textura se aloca o singura data, apoi se actualizeaza cu texSubImage2D
 *   - comenzile de desenare sunt inregistrate intr-o semnatura; daca un cadru
 *     este identic cu cel precedent, imaginea nu se mai redeseneaza si nu se
 *     mai trimite la GPU
 *   - rezolutie limitata la 720px latime
 */
public class CapaUI {

    private Bitmap bitmap;
    private Canvas canvas;
    private int latimePx = 2, inaltimePx = 2;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Typeface fontGros = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
    private final Typeface fontNormal = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
    private final RectF rect = new RectF();

    /**
     * rama butoanelor (assets/texturi/buton.png): alb pe transparent, colorata la desenare.
     * Capetele (cu detaliile din colturi) nu se intind, doar mijlocul se lungeste.
     */
    public Bitmap ramaButon;
    private static final float RAMA_CAP = 0.15f;       // latimea unui capat, din latimea imaginii
    private static final float RAMA_MARGINE_X = 0.0155f; // de la marginea imaginii la linia neon
    private static final float RAMA_MARGINE_Y = 0.072f;
    /**
     * chenarul ferestrelor de dialog (assets/texturi/panou_dialog.png), alb pe transparent.
     * Colturile si banda din mijlocul laturii de sus raman fixe, laturile se intind.
     * Valorile sunt fractiuni din imagine, masurate pe imaginea primita.
     */
    public Bitmap ramaDialog;
    private static final float DLG_ST = 34f / 864f, DLG_DR = 34f / 864f;   // pana la linia neon
    private static final float DLG_SUS = 54f / 884f, DLG_JOS = 34f / 884f;
    private static final float DLG_COLT_X = 190f / 864f, DLG_COLT_Y = 190f / 884f;
    private static final float DLG_BANDA_ST = 350f / 864f, DLG_BANDA_DR = 513f / 864f;
    private final RectF tinta = new RectF();

    /** cutia pentru HOLD / NEXT (assets/texturi/cutie_piesa.png), alb pe transparent */
    public Bitmap cutiePiesa;
    private static final float CUTIE_MARGINE = 45f / 774f;   // de la marginea imaginii la linia neon

    private final Paint paintRama = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private final Rect sursa = new Rect();

    private int textura = -1;
    private int program;
    private int locPos, locUV, locTextura;
    private FloatBuffer bufVarf;
    private boolean texturaAlocata = false;

    private final StringBuilder semnatura = new StringBuilder(2048);
    private String semnaturaAnterioara = "";
    private boolean murdar = true;

    public int cadreSarite = 0;

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
        texturaAlocata = false;

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textura);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        if (bitmap == null) redimensioneaza(2, 2);
    }

    public void redimensioneaza(int latimeRealaPx, int inaltimeRealaPx) {
        float scara = Math.min(1f, 720f / Math.max(1, latimeRealaPx));
        int l = Math.max(2, Math.round(latimeRealaPx * scara));
        int i = Math.max(2, Math.round(inaltimeRealaPx * scara));
        if (bitmap != null && l == latimePx && i == inaltimePx) return;

        latimePx = l;
        inaltimePx = i;
        bitmap = Bitmap.createBitmap(latimePx, inaltimePx, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        texturaAlocata = false;
        semnaturaAnterioara = "";
        murdar = true;
    }

    public float raport() {
        return (float) latimePx / inaltimePx;
    }

    /** incepe inregistrarea comenzilor pentru un cadru nou */
    public void inceputCadru() {
        semnatura.setLength(0);
        // comenzile se golesc si cand cadrul precedent a fost sarit (identic),
        // altfel se aduna de la un cadru la altul si se redau suprapuse
        nrComenzi = 0;
    }

    /** deseneaza (daca s-a schimbat ceva) si afiseaza stratul peste scena */
    public void deseneazaPeEcran() {
        if (canvas == null || textura < 0) return;

        String s = semnatura.toString();
        boolean schimbat = murdar || !s.equals(semnaturaAnterioara);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textura);

        if (schimbat) {
            bitmap.eraseColor(Color.TRANSPARENT);
            redaComenzi();
            semnaturaAnterioara = s;
            murdar = false;

            if (!texturaAlocata) {
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                texturaAlocata = true;
            } else {
                GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
            }
        } else {
            cadreSarite++;
        }

        GLES20.glUseProgram(program);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        bufVarf.position(0);
        GLES20.glEnableVertexAttribArray(locPos);
        GLES20.glVertexAttribPointer(locPos, 2, GLES20.GL_FLOAT, false, 16, bufVarf);
        bufVarf.position(2);
        GLES20.glEnableVertexAttribArray(locUV);
        GLES20.glVertexAttribPointer(locUV, 2, GLES20.GL_FLOAT, false, 16, bufVarf);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(locTextura, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(locPos);
        GLES20.glDisableVertexAttribArray(locUV);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    private int compileaza(int tip, String cod) {
        int s = GLES20.glCreateShader(tip);
        GLES20.glShaderSource(s, cod);
        GLES20.glCompileShader(s);
        return s;
    }

    // ---------- lista de comenzi ----------

    private static final int C_TEXT = 1;
    private static final int C_PANOU = 2;
    private static final int C_CHENAR = 3;
    private static final int C_BARA = 4;
    private static final int C_CERC = 5;
    private static final int C_IMAGINE = 6;
    private static final int C_RAMA = 7;
    private static final int C_DIALOG = 8;
    private static final int C_CUTIE = 9;

    private static final int MAX_COMENZI = 256;
    private final int[] cTip = new int[MAX_COMENZI];
    private final String[] cText = new String[MAX_COMENZI];
    private final float[] cA = new float[MAX_COMENZI];
    private final float[] cB = new float[MAX_COMENZI];
    private final float[] cC = new float[MAX_COMENZI];
    private final float[] cD = new float[MAX_COMENZI];
    private final float[] cE = new float[MAX_COMENZI];
    private final int[] cCul = new int[MAX_COMENZI];
    private final int[] cCul2 = new int[MAX_COMENZI];
    private final int[] cAlin = new int[MAX_COMENZI];
    private final Bitmap[] cImg = new Bitmap[MAX_COMENZI];
    private int nrComenzi = 0;

    private void adauga(int tip, String text, float a, float b, float c, float d, float e,
                        int cul, int cul2, int alin) {
        if (nrComenzi >= MAX_COMENZI) return;
        int i = nrComenzi++;
        cTip[i] = tip; cText[i] = text;
        cA[i] = a; cB[i] = b; cC[i] = c; cD[i] = d; cE[i] = e;
        cCul[i] = cul; cCul2[i] = cul2; cAlin[i] = alin;
        cImg[i] = null;

        semnatura.append(tip).append('|');
        if (text != null) semnatura.append(text);
        semnatura.append('|').append((int) (a * 2000)).append(',').append((int) (b * 2000))
                 .append(',').append((int) (c * 2000)).append(',').append((int) (d * 2000))
                 .append(',').append((int) (e * 2000)).append(',').append(cul).append(',')
                 .append(cul2).append(',').append(alin).append(';');
    }

    private void redaComenzi() {
        for (int i = 0; i < nrComenzi; i++) {
            switch (cTip[i]) {
                case C_TEXT:   redaText(i); break;
                case C_PANOU:  redaPanou(i); break;
                case C_CHENAR: redaChenar(i); break;
                case C_BARA:   redaBara(i); break;
                case C_CERC:   redaCerc(i); break;
                case C_IMAGINE: redaImagine(i); break;
                case C_RAMA:   redaRama(i); break;
                case C_DIALOG: redaDialog(i); break;
                case C_CUTIE:  redaCutie(i); break;
            }
        }
        nrComenzi = 0;
    }

    // ---------- API public ----------

    public void text(String s, float xFrac, float yFrac, float marime, int culoare) {
        if (s == null || s.isEmpty()) return;
        adauga(C_TEXT, s, xFrac, yFrac, marime, 0f, 0f, culoare, 0, 0);
    }

    public void textCentrat(String s, float xFrac, float yFrac, float marime, int culoare) {
        if (s == null || s.isEmpty()) return;
        adauga(C_TEXT, s, xFrac, yFrac, marime, 0f, 0f, culoare, 0, 1);
    }

    public void textDreapta(String s, float xFrac, float yFrac, float marime, int culoare) {
        if (s == null || s.isEmpty()) return;
        adauga(C_TEXT, s, xFrac, yFrac, marime, 0f, 0f, culoare, 0, 2);
    }

    /** text subtire, fara contur, pentru descrieri */
    public void textFin(String s, float xFrac, float yFrac, float marime, int culoare) {
        if (s == null || s.isEmpty()) return;
        adauga(C_TEXT, s, xFrac, yFrac, marime, 1f, 0f, culoare, 0, 1);
    }

    public void panou(float xFrac, float yFrac, float latFrac, float inaltFrac,
                      int culoare, float razaColtFrac) {
        adauga(C_PANOU, null, xFrac, yFrac, latFrac, inaltFrac, razaColtFrac, culoare, 0, 0);
    }

    public void chenar(float xFrac, float yFrac, float latFrac, float inaltFrac,
                       int culoare, float razaColtFrac, float grosimeFrac) {
        adauga(C_CHENAR, null, xFrac, yFrac, latFrac, inaltFrac, razaColtFrac, culoare,
                Float.floatToIntBits(grosimeFrac), 0);
    }

    public void bara(float xFrac, float yFrac, float latFrac, float inaltFrac,
                     float umplere, int culoareFundal, int culoareUmplere) {
        adauga(C_BARA, null, xFrac, yFrac, latFrac, inaltFrac, umplere, culoareFundal, culoareUmplere, 0);
    }

    public void cerc(float xFrac, float yFrac, float razaFrac, int culoare) {
        adauga(C_CERC, null, xFrac, yFrac, razaFrac, 0f, 0f, culoare, 0, 0);
    }

    /** buton rotunjit cu text centrat */
    public void buton(String s, float xCentru, float yCentru, float lat, float inalt,
                      int culoareFundal, int culoareContur, int culoareText, float marimeText) {
        if (ramaButon != null) {
            panou(xCentru - lat / 2f, yCentru - inalt / 2f, lat, inalt, culoareFundal, inalt * 0.2f);
            adauga(C_RAMA, null, xCentru - lat / 2f, yCentru - inalt / 2f, lat, inalt, 0f, culoareContur, 0, 0);
        } else {
            panou(xCentru - lat / 2f, yCentru - inalt / 2f, lat, inalt, culoareFundal, inalt * 0.35f);
            chenar(xCentru - lat / 2f, yCentru - inalt / 2f, lat, inalt, culoareContur, inalt * 0.35f, 0.0025f);
        }
        textCentrat(s, xCentru, yCentru + marimeText * 0.35f, marimeText, culoareText);
    }

    /**
     * imagine (de ex. logo) centrata in (xCentru, yCentru), cat mai mare
     * fara sa depaseasca latMax x inaltMax (fractiuni de ecran), cu proportiile pastrate
     */
    public void imagine(Bitmap b, float xCentru, float yCentru, float latMax, float inaltMax, float alfa) {
        if (b == null || alfa <= 0.002f || nrComenzi >= MAX_COMENZI) return;
        adauga(C_IMAGINE, null, xCentru, yCentru, latMax, inaltMax, alfa, System.identityHashCode(b), 0, 0);
        cImg[nrComenzi - 1] = b;
    }

    /** fereastra de dialog: fundal inchis + chenar (imaginea panou_dialog sau un chenar simplu) */
    public void fereastra(float xFrac, float yFrac, float latFrac, float inaltFrac,
                          int culoareFundal, int culoareContur) {
        if (ramaDialog != null) {
            panou(xFrac, yFrac, latFrac, inaltFrac, culoareFundal, 0.012f);
            adauga(C_DIALOG, null, xFrac, yFrac, latFrac, inaltFrac, 0f, culoareContur, 0, 0);
        } else {
            panou(xFrac, yFrac, latFrac, inaltFrac, culoareFundal, 0.02f);
            chenar(xFrac, yFrac, latFrac, inaltFrac, culoareContur, 0.02f, 0.0018f);
        }
    }

    /** cutie patrata centrata in (xFrac, yFrac); latura liniei neon = laturaFrac din latimea ecranului */
    public void cutie(float xFrac, float yFrac, float laturaFrac, int culoare) {
        if (cutiePiesa == null || Color.alpha(culoare) == 0) return;
        adauga(C_CUTIE, null, xFrac, yFrac, laturaFrac, 0f, 0f, culoare, 0, 0);
    }

    public boolean areCutie() { return cutiePiesa != null; }

    public float latimeTextFrac(String s, float marime) {
        if (canvas == null || s == null) return 0f;
        paint.setTypeface(fontGros);
        paint.setTextSize(marime * inaltimePx);
        return paint.measureText(s) / latimePx;
    }

    // ---------- redare efectiva ----------

    private void redaText(int i) {
        String s = cText[i];
        float px = cA[i] * latimePx;
        float py = cB[i] * inaltimePx;
        float marimePx = cC[i] * inaltimePx;
        boolean fin = cD[i] > 0.5f;
        int culoare = cCul[i];
        int alfa = Color.alpha(culoare);
        if (alfa == 0) return;

        Paint.Align al = cAlin[i] == 1 ? Paint.Align.CENTER
                       : cAlin[i] == 2 ? Paint.Align.RIGHT : Paint.Align.LEFT;

        paint.setTypeface(fin ? fontNormal : fontGros);
        paint.setTextSize(marimePx);
        paint.setTextAlign(al);

        if (!fin) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            paint.setAlpha(alfa * 150 / 255);
            canvas.drawText(s, px + marimePx * 0.05f, py + marimePx * 0.09f, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(marimePx * 0.11f);
            paint.setColor(Color.BLACK);
            paint.setAlpha(alfa * 210 / 255);
            canvas.drawText(s, px, py, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(culoare);
        canvas.drawText(s, px, py, paint);
    }

    private void redaPanou(int i) {
        float st = cA[i] * latimePx;
        float su = cB[i] * inaltimePx;
        float dr = st + cC[i] * latimePx;
        float jo = su + cD[i] * inaltimePx;
        float raza = cE[i] * inaltimePx;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(cCul[i]);
        rect.set(st, su, dr, jo);
        canvas.drawRoundRect(rect, raza, raza, paint);
    }

    private void redaChenar(int i) {
        float st = cA[i] * latimePx;
        float su = cB[i] * inaltimePx;
        float dr = st + cC[i] * latimePx;
        float jo = su + cD[i] * inaltimePx;
        float raza = cE[i] * inaltimePx;
        float gros = Float.intBitsToFloat(cCul2[i]) * inaltimePx;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1f, gros));
        paint.setColor(cCul[i]);
        rect.set(st, su, dr, jo);
        canvas.drawRoundRect(rect, raza, raza, paint);
    }

    private void redaBara(int i) {
        float st = cA[i] * latimePx;
        float su = cB[i] * inaltimePx;
        float lat = cC[i] * latimePx;
        float ina = cD[i] * inaltimePx;
        float raza = ina * 0.5f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(cCul[i]);
        rect.set(st, su, st + lat, su + ina);
        canvas.drawRoundRect(rect, raza, raza, paint);
        float u = Math.max(0f, Math.min(1f, cE[i]));
        if (u > 0.01f) {
            paint.setColor(cCul2[i]);
            rect.set(st, su, st + lat * u, su + ina);
            canvas.drawRoundRect(rect, raza, raza, paint);
        }
    }

    private void redaImagine(int i) {
        Bitmap b = cImg[i];
        if (b == null) return;
        float latMax = cC[i] * latimePx, inaltMax = cD[i] * inaltimePx;
        float scara = Math.min(latMax / b.getWidth(), inaltMax / b.getHeight());
        float l = b.getWidth() * scara, h = b.getHeight() * scara;
        float cx = cA[i] * latimePx, cy = cB[i] * inaltimePx;
        rect.set(cx - l / 2f, cy - h / 2f, cx + l / 2f, cy + h / 2f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setAlpha(Math.max(0, Math.min(255, Math.round(cE[i] * 255f))));
        paint.setFilterBitmap(true);
        canvas.drawBitmap(b, null, rect, paint);
        paint.setAlpha(255);
    }

    private void redaRama(int i) {
        Bitmap b = ramaButon;
        if (b == null) return;
        int cul = cCul[i];
        paintRama.setColorFilter(new LightingColorFilter(cul & 0xFFFFFF, 0));
        paintRama.setAlpha(Color.alpha(cul));

        float st = cA[i] * latimePx, su = cB[i] * inaltimePx;
        float lat = cC[i] * latimePx, inalt = cD[i] * inaltimePx;
        int bw = b.getWidth(), bh = b.getHeight();
        // scara verticala: linia neon cade exact pe marginea butonului
        float s = inalt / (bh * (1f - 2f * RAMA_MARGINE_Y));
        float mx = bw * RAMA_MARGINE_X * s, my = bh * RAMA_MARGINE_Y * s;
        float x0 = st - mx, x1 = st + lat + mx, y0 = su - my, y1 = su + inalt + my;
        int capSrc = Math.round(bw * RAMA_CAP);
        float cap = Math.min(capSrc * s, (x1 - x0) / 2f);

        sursa.set(0, 0, capSrc, bh);
        rect.set(x0, y0, x0 + cap, y1);
        canvas.drawBitmap(b, sursa, rect, paintRama);
        sursa.set(capSrc, 0, bw - capSrc, bh);
        rect.set(x0 + cap, y0, x1 - cap, y1);
        canvas.drawBitmap(b, sursa, rect, paintRama);
        sursa.set(bw - capSrc, 0, bw, bh);
        rect.set(x1 - cap, y0, x1, y1);
        canvas.drawBitmap(b, sursa, rect, paintRama);
    }

    private void redaDialog(int i) {
        Bitmap b = ramaDialog;
        if (b == null) return;
        int cul = cCul[i];
        paintRama.setColorFilter(new LightingColorFilter(cul & 0xFFFFFF, 0));
        paintRama.setAlpha(Color.alpha(cul));

        float st = cA[i] * latimePx, su = cB[i] * inaltimePx;
        float lat = cC[i] * latimePx, inalt = cD[i] * inaltimePx;
        int bw = b.getWidth(), bh = b.getHeight();
        // scara: colturile ocupa cam 17% din latura mai mica a ferestrei
        float s = 0.6f * Math.min(lat, inalt) / (bw * (1f - DLG_ST - DLG_DR));
        float x0 = st - bw * DLG_ST * s, x1 = st + lat + bw * DLG_DR * s;
        float y0 = su - bh * DLG_SUS * s, y1 = su + inalt + bh * DLG_JOS * s;

        int cx = Math.round(bw * DLG_COLT_X), cy = Math.round(bh * DLG_COLT_Y);
        float dcx = cx * s, dcy = cy * s;
        int bSt = Math.round(bw * DLG_BANDA_ST), bDr = Math.round(bw * DLG_BANDA_DR);
        float mijloc = (x0 + x1) / 2f, dBanda = (bDr - bSt) * s;

        // randul de sus: colt, latura, banda (fixa, centrata), latura, colt
        bucata(b, 0, 0, cx, cy,             x0, y0, x0 + dcx, y0 + dcy);
        bucata(b, cx, 0, bSt, cy,           x0 + dcx, y0, mijloc - dBanda / 2f, y0 + dcy);
        bucata(b, bSt, 0, bDr, cy,          mijloc - dBanda / 2f, y0, mijloc + dBanda / 2f, y0 + dcy);
        bucata(b, bDr, 0, bw - cx, cy,      mijloc + dBanda / 2f, y0, x1 - dcx, y0 + dcy);
        bucata(b, bw - cx, 0, bw, cy,       x1 - dcx, y0, x1, y0 + dcy);
        // laturile stanga si dreapta
        bucata(b, 0, cy, cx, bh - cy,       x0, y0 + dcy, x0 + dcx, y1 - dcy);
        bucata(b, bw - cx, cy, bw, bh - cy, x1 - dcx, y0 + dcy, x1, y1 - dcy);
        // randul de jos
        bucata(b, 0, bh - cy, cx, bh,       x0, y1 - dcy, x0 + dcx, y1);
        bucata(b, cx, bh - cy, bw - cx, bh, x0 + dcx, y1 - dcy, x1 - dcx, y1);
        bucata(b, bw - cx, bh - cy, bw, bh, x1 - dcx, y1 - dcy, x1, y1);
    }

    private void redaCutie(int i) {
        Bitmap b = cutiePiesa;
        if (b == null) return;
        int cul = cCul[i];
        paintRama.setColorFilter(new LightingColorFilter(cul & 0xFFFFFF, 0));
        paintRama.setAlpha(Color.alpha(cul));
        float latura = cC[i] * latimePx / (1f - 2f * CUTIE_MARGINE);
        float cx = cA[i] * latimePx, cy = cB[i] * inaltimePx;
        tinta.set(cx - latura / 2f, cy - latura / 2f, cx + latura / 2f, cy + latura / 2f);
        canvas.drawBitmap(b, null, tinta, paintRama);
    }

    private void bucata(Bitmap b, int sx0, int sy0, int sx1, int sy1,
                        float dx0, float dy0, float dx1, float dy1) {
        if (dx1 - dx0 < 0.5f || dy1 - dy0 < 0.5f) return;
        sursa.set(sx0, sy0, sx1, sy1);
        tinta.set(dx0, dy0, dx1, dy1);
        canvas.drawBitmap(b, sursa, tinta, paintRama);
    }

    private void redaCerc(int i) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(cCul[i]);
        canvas.drawCircle(cA[i] * latimePx, cB[i] * inaltimePx, cC[i] * inaltimePx, paint);
    }
}
