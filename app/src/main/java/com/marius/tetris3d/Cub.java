package com.marius.tetris3d;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Cub cu muchii foarte rotunjite, contur luminos pe margini,
 * reflexii colorate din mediu si textura subtila pe suprafata.
 */
public class Cub {

    private static final String COD_VARFURI =
            "uniform mat4 uMVP;\n" +
            "uniform mat4 uModel;\n" +
            "attribute vec4 aPos;\n" +
            "attribute vec3 aNormal;\n" +
            "attribute vec2 aUV;\n" +
            "varying vec3 vNormal;\n" +
            "varying vec2 vUV;\n" +
            "varying vec3 vLume;\n" +
            "void main() {\n" +
            "  vNormal = normalize((uModel * vec4(aNormal, 0.0)).xyz);\n" +
            "  vLume = (uModel * aPos).xyz;\n" +
            "  vUV = aUV;\n" +
            "  gl_Position = uMVP * aPos;\n" +
            "}\n";

    private static final String COD_PIXELI =
            "precision mediump float;\n" +
            "uniform vec4 uCuloare;\n" +
            "varying vec3 vNormal;\n" +
            "varying vec2 vUV;\n" +
            "varying vec3 vLume;\n" +

            "const vec3 L  = normalize(vec3(0.35, 0.82, 0.45));\n" +
            "const vec3 V  = vec3(0.0, 0.0, 1.0);\n" +
            // lumini colorate din mediu, ca de la neoane
            "const vec3 NEON_A = normalize(vec3(-0.85, 0.15, 0.50));\n" +
            "const vec3 NEON_B = normalize(vec3( 0.80, -0.25, 0.55));\n" +
            "const vec3 CUL_A = vec3(1.00, 0.35, 0.85);\n" +   // roz
            "const vec3 CUL_B = vec3(0.25, 0.85, 1.00);\n" +   // cyan

            // zgomot simplu, pentru textura subtila
            "float zgomot(vec2 p) {\n" +
            "  return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);\n" +
            "}\n" +

            "void main() {\n" +
            "  vec2 d = abs(vUV - 0.5) * 2.0;\n" +
            // muchii mult mai rotunjite, ca niste pernute
            "  float colt = length(max(d - 0.52, 0.0)) / 0.48;\n" +
            "  if (colt > 1.0) discard;\n" +
            "  float rotunjire = 1.0 - smoothstep(0.86, 1.0, colt);\n" +

            "  vec3 N = normalize(vNormal);\n" +
            "  vec3 H = normalize(L + V);\n" +

            "  float dif  = max(dot(N, L), 0.0);\n" +
            "  float sus  = max(N.y, 0.0);\n" +
            "  float fata = max(N.z, 0.0);\n" +
            "  float lat  = abs(N.x);\n" +
            "  float jos  = max(-N.y, 0.0);\n" +

            "  vec3 baza = uCuloare.rgb;\n" +

            // luminozitate diferita pe fiecare fata, dar umbra ramane colorata
            "  float nivel = 0.34\n" +
            "              + sus  * 0.78\n" +
            "              + fata * 0.42\n" +
            "              + lat  * 0.16\n" +
            "              - jos  * 0.10\n" +
            "              + dif  * 0.22;\n" +
            "  vec3 culoare = baza * nivel;\n" +

            // reflexii colorate din mediu, ca de la neoanele din jur
            "  float refA = pow(max(dot(N, NEON_A), 0.0), 2.2);\n" +
            "  float refB = pow(max(dot(N, NEON_B), 0.0), 2.2);\n" +
            "  culoare += CUL_A * refA * 0.22;\n" +
            "  culoare += CUL_B * refB * 0.20;\n" +

            // pata moale de lumina pe fata de sus
            "  float pata = pow(max(dot(N, H), 0.0), 12.0);\n" +
            "  culoare += vec3(1.0, 0.98, 0.94) * pata * (0.18 + sus * 0.45);\n" +

            // sclipire mica si ascutita
            "  float sclip = pow(max(dot(N, H), 0.0), 70.0);\n" +
            "  culoare += vec3(1.0) * sclip * (0.20 + sus * 0.40);\n" +

            // conturul luminos de pe muchii, de culoarea blocului
            "  float contur = smoothstep(0.55, 0.98, colt);\n" +
            "  vec3 culContur = baza * 1.5 + vec3(0.28);\n" +
            "  culoare = mix(culoare, culContur, contur * 0.55);\n" +

            // textura subtila, zgarieturi fine
            "  float t = zgomot(floor(vUV * 42.0));\n" +
            "  culoare *= 0.965 + t * 0.070;\n" +

            "  gl_FragColor = vec4(culoare, uCuloare.a * rotunjire);\n" +
            "}\n";

    private static final float S = 0.47f;

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
    private final int locMVP, locModel, locCuloare;

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
        locCuloare = GLES20.glGetUniformLocation(program, "uCuloare");
    }

    private int compileaza(int tip, String cod) {
        int s = GLES20.glCreateShader(tip);
        GLES20.glShaderSource(s, cod);
        GLES20.glCompileShader(s);
        return s;
    }

    public void seteazaLumina(float x, float y, float z) {
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
        GLES20.glUniform4f(locCuloare, r, g, b, alfa);

        bufIndici.position(0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDICI.length,
                GLES20.GL_UNSIGNED_SHORT, bufIndici);

        GLES20.glDisableVertexAttribArray(locPos);
        GLES20.glDisableVertexAttribArray(locNormal);
        GLES20.glDisableVertexAttribArray(locUV);
    }
}
