package com.marius.tetris3dpro;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Deseneaza un dreptunghi cu o textura: imaginea de fundal pe tot ecranul
 * sau un panou asezat in scena 3D (peretele din spatele tablei).
 */
public class Imagine {

    private static final String VS =
        "uniform mat4 uMVP;\n" +
        "attribute vec3 aPos;\n" +
        "attribute vec2 aUV;\n" +
        "uniform vec2 uEstompare;\n" +
        "varying vec2 vUV;\n" +
        "varying float vVizibil;\n" +
        "void main() {\n" +
        "  vUV = aUV;\n" +
        "  vVizibil = clamp((aPos.z - uEstompare.x) / (uEstompare.y - uEstompare.x), 0.0, 1.0);\n" +
        "  gl_Position = uMVP * vec4(aPos, 1.0);\n" +
        "}\n";

    private static final String FS =
        "precision mediump float;\n" +
        "varying vec2 vUV;\n" +
        "varying float vVizibil;\n" +
        "uniform sampler2D uTex;\n" +
        "uniform vec4 uCuloare;\n" +
        "void main() {\n" +
        "  vec4 c = texture2D(uTex, vUV) * uCuloare;\n" +
        "  gl_FragColor = vec4(c.rgb, c.a * vVizibil);\n" +
        "}\n";

    private final int program;
    private final int locPos, locUV, locMVP, locTex, locCuloare, locEstompare;
    // intre z = estompareDeparte (invizibil) si z = estompareAproape (vizibil complet)
    private float estompareDeparte = -2000f, estompareAproape = -1999f;
    private final float[] varfuri = new float[20];   // 4 x (x y z u v)
    private final FloatBuffer buf;
    private final float[] identitate = new float[16];

    public Imagine() {
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, compileaza(GLES20.GL_VERTEX_SHADER, VS));
        GLES20.glAttachShader(program, compileaza(GLES20.GL_FRAGMENT_SHADER, FS));
        GLES20.glLinkProgram(program);
        locPos     = GLES20.glGetAttribLocation(program, "aPos");
        locUV      = GLES20.glGetAttribLocation(program, "aUV");
        locMVP     = GLES20.glGetUniformLocation(program, "uMVP");
        locTex     = GLES20.glGetUniformLocation(program, "uTex");
        locCuloare = GLES20.glGetUniformLocation(program, "uCuloare");
        locEstompare = GLES20.glGetUniformLocation(program, "uEstompare");

        ByteBuffer bb = ByteBuffer.allocateDirect(varfuri.length * 4);
        bb.order(ByteOrder.nativeOrder());
        buf = bb.asFloatBuffer();
        Matrix.setIdentityM(identitate, 0);
    }

    private static int compileaza(int tip, String cod) {
        int s = GLES20.glCreateShader(tip);
        GLES20.glShaderSource(s, cod);
        GLES20.glCompileShader(s);
        return s;
    }

    /**
     * Imagine pe tot ecranul, decupata ca sa-l acopere fara deformare ("cover").
     * luminozitate < 1 intuneca imaginea ca piesele sa iasa in evidenta.
     */
    public void fundal(int textura, int latImg, int inaltImg, float raportEcran, float luminozitate) {
        if (textura == 0 || latImg <= 0 || inaltImg <= 0) return;
        float raportImg = (float) latImg / inaltImg;
        float u0 = 0f, u1 = 1f, v0 = 0f, v1 = 1f;
        if (raportImg > raportEcran) {
            float f = raportEcran / raportImg;
            u0 = 0.5f - f / 2f; u1 = 0.5f + f / 2f;
        } else {
            float f = raportImg / raportEcran;
            v0 = 0.5f - f / 2f; v1 = 0.5f + f / 2f;
        }
        seteaza(-1f, -1f, 1f, 1f, 0f, u0, u1, v0, v1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        deseneaza(textura, identitate, luminozitate, luminozitate, luminozitate, 1f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    /** dreptunghi vertical in scena 3D, intre (x0,y0) si (x1,y1), la adancimea z */
    public void panou(int textura, float[] vizProj, float x0, float y0, float x1, float y1, float z,
                      float r, float g, float b, float alfa) {
        if (textura == 0) return;
        seteaza(x0, y0, x1, y1, z, 0f, 1f, 0f, 1f);
        deseneaza(textura, vizProj, r, g, b, alfa);
    }

    /**
     * bara texturata in scena 3D; textura (o fasie verticala) se repeta de "repetari" ori
     * pe lungimea barei. rotita = true pentru bare orizontale (fasia culcata pe o parte).
     */
    public void bara(int textura, float[] vizProj, float x0, float y0, float x1, float y1, float z,
                     float repetari, boolean rotita) {
        if (textura == 0) return;
        if (!rotita) {
            seteaza(x0, y0, x1, y1, z, 0f, 1f, 0f, repetari);
        } else {
            // u al texturii merge de jos in sus pe bara, v de la stanga la dreapta
            float[] d = varfuri;
            d[0]  = x0; d[1]  = y0; d[2]  = z; d[3]  = 1f; d[4]  = 0f;
            d[5]  = x1; d[6]  = y0; d[7]  = z; d[8]  = 1f; d[9]  = repetari;
            d[10] = x0; d[11] = y1; d[12] = z; d[13] = 0f; d[14] = 0f;
            d[15] = x1; d[16] = y1; d[17] = z; d[18] = 0f; d[19] = repetari;
            buf.position(0);
            buf.put(d).position(0);
        }
        deseneaza(textura, vizProj, 1f, 1f, 1f, 1f);
    }

    /**
     * podea orizontala la inaltimea y, intre x0..x1 si z0 (departe) .. z1 (aproape);
     * textura se repeta de repX x repZ ori si se estompeaza spre departe
     */
    public void podea(int textura, float[] vizProj, float x0, float x1, float z0, float z1, float y,
                      float repX, float repZ, float luminozitate, float alfa, float zVizibil) {
        if (textura == 0) return;
        float[] d = varfuri;
        d[0]  = x0; d[1]  = y; d[2]  = z1; d[3]  = 0f;   d[4]  = repZ;
        d[5]  = x1; d[6]  = y; d[7]  = z1; d[8]  = repX; d[9]  = repZ;
        d[10] = x0; d[11] = y; d[12] = z0; d[13] = 0f;   d[14] = 0f;
        d[15] = x1; d[16] = y; d[17] = z0; d[18] = repX; d[19] = 0f;
        buf.position(0);
        buf.put(d).position(0);
        estompareDeparte = z0; estompareAproape = zVizibil;
        deseneaza(textura, vizProj, luminozitate, luminozitate, luminozitate, alfa);
        estompareDeparte = -2000f; estompareAproape = -1999f;
    }

    /** colturile: stanga-jos, dreapta-jos, stanga-sus, dreapta-sus; v=0 este sus in imagine */
    private void seteaza(float x0, float y0, float x1, float y1, float z,
                         float u0, float u1, float v0, float v1) {
        float[] d = varfuri;
        d[0]  = x0; d[1]  = y0; d[2]  = z; d[3]  = u0; d[4]  = v1;
        d[5]  = x1; d[6]  = y0; d[7]  = z; d[8]  = u1; d[9]  = v1;
        d[10] = x0; d[11] = y1; d[12] = z; d[13] = u0; d[14] = v0;
        d[15] = x1; d[16] = y1; d[17] = z; d[18] = u1; d[19] = v0;
        buf.position(0);
        buf.put(d).position(0);
    }

    private void deseneaza(int textura, float[] mvp, float r, float g, float b, float a) {
        GLES20.glUseProgram(program);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glUniformMatrix4fv(locMVP, 1, false, mvp, 0);
        GLES20.glUniform4f(locCuloare, r, g, b, a);
        GLES20.glUniform2f(locEstompare, estompareDeparte, estompareAproape);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textura);
        GLES20.glUniform1i(locTex, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        buf.position(0);
        GLES20.glEnableVertexAttribArray(locPos);
        GLES20.glVertexAttribPointer(locPos, 3, GLES20.GL_FLOAT, false, 20, buf);
        buf.position(3);
        GLES20.glEnableVertexAttribArray(locUV);
        GLES20.glVertexAttribPointer(locUV, 2, GLES20.GL_FLOAT, false, 20, buf);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(locPos);
        GLES20.glDisableVertexAttribArray(locUV);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
    }
}
