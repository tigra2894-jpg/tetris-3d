package com.marius.tetris3d;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Cub {

    private static final String COD_VARFURI =
            "uniform mat4 uMVP;\n" +
            "uniform mat4 uModel;\n" +
            "attribute vec4 aPos;\n" +
            "attribute vec3 aNormal;\n" +
            "attribute vec2 aUV;\n" +
            "varying vec3 vNormal;\n" +
            "varying vec3 vLume;\n" +
            "varying vec2 vUV;\n" +
            "void main() {\n" +
            "  vNormal = normalize((uModel * vec4(aNormal, 0.0)).xyz);\n" +
            "  vLume = (uModel * aPos).xyz;\n" +
            "  vUV = aUV;\n" +
            "  gl_Position = uMVP * aPos;\n" +
            "}\n";

    private static final String COD_PIXELI =
            "precision mediump float;\n" +
            "uniform vec3 uLumina;\n" +
            "uniform vec3 uCamera;\n" +
            "uniform vec4 uCuloare;\n" +
            "varying vec3 vNormal;\n" +
            "varying vec3 vLume;\n" +
            "varying vec2 vUV;\n" +
            "void main() {\n" +
            "  vec2 d = abs(vUV - 0.5) * 2.0;\n" +
            "  float margine = max(d.x, d.y);\n" +
            "  float tesitura = 1.0 - smoothstep(0.78, 1.0, margine);\n" +
            "  vec3 N = normalize(vNormal);\n" +
            "  vec3 L = normalize(uLumina - vLume);\n" +
            "  vec3 V = normalize(uCamera - vLume);\n" +
            "  vec3 H = normalize(L + V);\n" +
            "  float difuz = max(dot(N, L), 0.0);\n" +
            "  float spec = pow(max(dot(N, H), 0.0), 54.0);\n" +
            "  float fresnel = pow(1.0 - max(dot(N, V), 0.0), 3.0);\n" +
            "  vec3 baza = uCuloare.rgb;\n" +
            "  vec3 culoare = baza * 0.30;\n" +
            "  culoare += baza * difuz * 0.90;\n" +
            "  culoare += vec3(1.0, 0.98, 0.95) * spec * 0.75;\n" +
            "  culoare += baza * fresnel * 0.55;\n" +
            "  culoare *= (0.70 + 0.30 * tesitura);\n" +
            "  culoare += baza * (1.0 - tesitura) * 0.60;\n" +
            "  gl_FragColor = vec4(culoare, uCuloare.a);\n" +
            "}\n";

    private static final float S = 0.46f;

    private static final float[] VARFURI = {
            -S,-S, S,  0,0,1,  0,0,   S,-S, S,  0,0,1,  1,0,
             S, S, S,  0,0,1,  1,1,  -S, S, S,  0,0,1,  0,1,
             S,-S,-S,  0,0,-1, 0,0,  -S,-S,-S,  0,0,-1, 1,0,
            -S, S,-S,  0,0,-1, 1,1,   S, S,-S,  0,0,-1, 0,1,
             S,-S, S,  1,0,0,  0,0,   S,-S,-S,  1,0,0,  1,0,
             S, S,-S,  1,0,0,  1,1,   S, S, S,  1,0,0,  0,1,
            -S,-S,-S, -1,0,0,  0,0,  -S,-S, S, -1,0,0,  1,0,
            -S, S, S, -1,0,0,  1,1,  -S, S,-S, -1,0,0,  0,1,
            -S, S, S,  0,1,0,  0,0,   S, S, S,  0,1,0,  1,0,
             S, S,-S,  0,1,0,  1,1,  -S, S,-S,  0,1,0,  0,1,
            -S,-S,-S,  0,-1,0, 0,0,   S,-S,-S,  0,-1,0, 1,0,
             S,-S, S,  0,-1,0, 1,1,  -S,-S, S,  0,-1,0, 0,1
    };

    private static final short[] INDICI = {
            0,1,2,  0,2,3,      4,5,6,  4,6,7,
            8,9,10, 8,10,11,    12,13,14, 12,14,15,
            16,17,18, 16,18,19, 20,21,22, 20,22,23
    };

    private final FloatBuffer bufVarfuri;
    private final ShortBuffer bufIndici;

    private final int program;
    private final int locPos, locNormal, locUV;
    private final int locMVP, locModel, locLumina, locCamera, locCuloare;

    private final float[] lumina = {10f, 16f, 14f};
    private static final float[] CAMERA = {0f, 1.5f, 26f};

    public Cub() {
        ByteBuffer bv = ByteBuffer.allocateDirect(VARFURI.length * 4);
        bv.order(ByteOrder.nativeOrder());
        bufVarfuri = bv.asFloatBuffer();
        bufVarfuri.put(VARFURI).position(0);

        ByteBuffer bi = ByteBuffer.allocateDirect(INDICI.length * 2);
        bi.order(ByteOrder.nativeOrder());
        bufIndici = bi.asShortBuffer();
        bufIndici.put(INDICI).position(0);

        int vs = compileaza(GLES20.GL_VERTEX_SHADER, COD_VARFURI);
        int fs = compileaza(GLES20.GL_FRAGMENT_SHADER, COD_PIXELI);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vs);
        GLES20.glAttachShader(program, fs);
        GLES20.glLinkProgram(program);

        locPos     = GLES20.glGetAttribLocation(program, "aPos");
        locNormal  = GLES20.glGetAttribLocation(program, "aNormal");
        locUV      = GLES20.glGetAttribLocation(program, "aUV");
        locMVP     = GLES20.glGetUniformLocation(program, "uMVP");
        locModel   = GLES20.glGetUniformLocation(program, "uModel");
        locLumina  = GLES20.glGetUniformLocation(program, "uLumina");
        locCamera  = GLES20.glGetUniformLocation(program, "uCamera");
        locCuloare = GLES20.glGetUniformLocation(program, "uCuloare");
    }

    private int compileaza(int tip, String cod) {
        int s = GLES20.glCreateShader(tip);
        GLES20.glShaderSource(s, cod);
        GLES20.glCompileShader(s);
        return s;
    }

    public void seteazaLumina(float x, float y, float z) {
        lumina[0] = x; lumina[1] = y; lumina[2] = z;
    }

    public void deseneaza(float[] mvp, float[] model,
                          float r, float g, float b, float alfa) {

        GLES20.glUseProgram(program);

        int pas = 8 * 4;

        bufVarfuri.position(0);
        GLES20.glEnableVertexAttribArray(locPos);
        GLES20.glVertexAttribPointer(locPos, 3, GLES20.GL_FLOAT, false, pas, bufVarfuri);

        bufVarfuri.position(3);
        GLES20.glEnableVertexAttribArray(locNormal);
        GLES20.glVertexAttribPointer(locNormal, 3, GLES20.GL_FLOAT, false, pas, bufVarfuri);

        bufVarfuri.position(6);
        GLES20.glEnableVertexAttribArray(locUV);
        GLES20.glVertexAttribPointer(locUV, 2, GLES20.GL_FLOAT, false, pas, bufVarfuri);

        GLES20.glUniformMatrix4fv(locMVP, 1, false, mvp, 0);
        GLES20.glUniformMatrix4fv(locModel, 1, false, model, 0);
        GLES20.glUniform3f(locLumina, lumina[0], lumina[1], lumina[2]);
        GLES20.glUniform3f(locCamera, CAMERA[0], CAMERA[1], CAMERA[2]);
        GLES20.glUniform4f(locCuloare, r, g, b, alfa);

        bufIndici.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDICI.length,
                GLES20.GL_UNSIGNED_SHORT, bufIndici);

        GLES20.glDisableVertexAttribArray(locPos);
        GLES20.glDisableVertexAttribArray(locNormal);
        GLES20.glDisableVertexAttribArray(locUV);
    }
}
