package com.marius.tetris3dpro;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Sprite-uri luminoase (scantei, stele) desenate intr-un singur lot.
 * Fiecare sprite este un patrat in planul XY (camera priveste aproape
 * de-a lungul lui -Z), cu textura pe fundal negru adunata peste scena
 * (blending aditiv): negrul nu schimba nimic, lumina se aduna.
 */
public class Scantei {

    private static final String VS =
        "uniform mat4 uVP;\n" +
        "attribute vec3 aPos;\n" +
        "attribute vec2 aUV;\n" +
        "attribute vec4 aCuloare;\n" +
        "varying vec2 vUV;\n" +
        "varying vec4 vCuloare;\n" +
        "void main() {\n" +
        "  vUV = aUV;\n" +
        "  vCuloare = aCuloare;\n" +
        "  gl_Position = uVP * vec4(aPos, 1.0);\n" +
        "}\n";

    private static final String FS =
        "precision mediump float;\n" +
        "varying vec2 vUV;\n" +
        "varying vec4 vCuloare;\n" +
        "uniform sampler2D uTex;\n" +
        "void main() {\n" +
        "  float l = texture2D(uTex, vUV).r;\n" +
        // miezul tinde spre alb, marginile pastreaza culoarea piesei
        "  vec3 c = mix(vCuloare.rgb, vec3(1.0), l * l * 0.6) * l;\n" +
        "  gl_FragColor = vec4(c * vCuloare.a, 1.0);\n" +
        "}\n";

    private static final int MAX = 1024;
    private static final int FLOATURI_PER_VARF = 9;   // pozitie(3) uv(2) culoare(4)

    private final float[] date = new float[MAX * 4 * FLOATURI_PER_VARF];
    private final FloatBuffer bufVarfuri;
    private final ShortBuffer bufIndici;
    private int nr = 0;

    private final int program;
    private final int locPos, locUV, locCuloare, locVP, locTex;

    public Scantei() {
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, compileaza(GLES20.GL_VERTEX_SHADER, VS));
        GLES20.glAttachShader(program, compileaza(GLES20.GL_FRAGMENT_SHADER, FS));
        GLES20.glLinkProgram(program);
        locPos     = GLES20.glGetAttribLocation(program, "aPos");
        locUV      = GLES20.glGetAttribLocation(program, "aUV");
        locCuloare = GLES20.glGetAttribLocation(program, "aCuloare");
        locVP      = GLES20.glGetUniformLocation(program, "uVP");
        locTex     = GLES20.glGetUniformLocation(program, "uTex");

        ByteBuffer bv = ByteBuffer.allocateDirect(date.length * 4);
        bv.order(ByteOrder.nativeOrder());
        bufVarfuri = bv.asFloatBuffer();

        short[] ind = new short[MAX * 6];
        for (int i = 0; i < MAX; i++) {
            short b = (short) (i * 4);
            ind[i * 6]     = b;
            ind[i * 6 + 1] = (short) (b + 1);
            ind[i * 6 + 2] = (short) (b + 2);
            ind[i * 6 + 3] = (short) (b + 2);
            ind[i * 6 + 4] = (short) (b + 1);
            ind[i * 6 + 5] = (short) (b + 3);
        }
        ByteBuffer bi = ByteBuffer.allocateDirect(ind.length * 2);
        bi.order(ByteOrder.nativeOrder());
        bufIndici = bi.asShortBuffer();
        bufIndici.put(ind).position(0);
    }

    private static int compileaza(int tip, String cod) {
        int s = GLES20.glCreateShader(tip);
        GLES20.glShaderSource(s, cod);
        GLES20.glCompileShader(s);
        return s;
    }

    /** adauga un sprite centrat in (x,y,z), cu latura "marime", rotit cu "unghi" grade in planul ecranului */
    public void adauga(float x, float y, float z, float marime, float unghi,
                       float r, float g, float b, float intensitate) {
        if (nr >= MAX || intensitate <= 0.002f) return;
        double a = Math.toRadians(unghi);
        float h = marime / 2f;
        float cx = (float) Math.cos(a) * h, sx = (float) Math.sin(a) * h;
        // colturile (-1,-1) (1,-1) (-1,1) (1,1) rotite
        int k = nr * 4 * FLOATURI_PER_VARF;
        k = varf(k, x - cx + sx, y - sx - cx, z, 0f, 1f, r, g, b, intensitate);
        k = varf(k, x + cx + sx, y + sx - cx, z, 1f, 1f, r, g, b, intensitate);
        k = varf(k, x - cx - sx, y - sx + cx, z, 0f, 0f, r, g, b, intensitate);
        varf(k, x + cx - sx, y + sx + cx, z, 1f, 0f, r, g, b, intensitate);
        nr++;
    }

    private int varf(int k, float x, float y, float z, float u, float v,
                     float r, float g, float b, float a) {
        date[k++] = x; date[k++] = y; date[k++] = z;
        date[k++] = u; date[k++] = v;
        date[k++] = r; date[k++] = g; date[k++] = b; date[k++] = a;
        return k;
    }

    /** deseneaza tot ce s-a adunat; se cheama dupa lotul de cuburi, ca adancimea sa fie completa */
    public void goleste(int textura, float[] vizProj) {
        if (nr == 0) return;
        if (textura == 0) { nr = 0; return; }

        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(locVP, 1, false, vizProj, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textura);
        GLES20.glUniform1i(locTex, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        bufVarfuri.position(0);
        bufVarfuri.put(date, 0, nr * 4 * FLOATURI_PER_VARF);

        int pas = FLOATURI_PER_VARF * 4;
        bufVarfuri.position(0);
        GLES20.glEnableVertexAttribArray(locPos);
        GLES20.glVertexAttribPointer(locPos, 3, GLES20.GL_FLOAT, false, pas, bufVarfuri);
        bufVarfuri.position(3);
        GLES20.glEnableVertexAttribArray(locUV);
        GLES20.glVertexAttribPointer(locUV, 2, GLES20.GL_FLOAT, false, pas, bufVarfuri);
        bufVarfuri.position(5);
        GLES20.glEnableVertexAttribArray(locCuloare);
        GLES20.glVertexAttribPointer(locCuloare, 4, GLES20.GL_FLOAT, false, pas, bufVarfuri);

        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDepthMask(false);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        bufIndici.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, nr * 6, GLES20.GL_UNSIGNED_SHORT, bufIndici);

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glDepthMask(true);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glDisableVertexAttribArray(locPos);
        GLES20.glDisableVertexAttribArray(locUV);
        GLES20.glDisableVertexAttribArray(locCuloare);
        nr = 0;
    }
}
