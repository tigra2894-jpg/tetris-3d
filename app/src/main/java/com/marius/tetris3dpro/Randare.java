package com.marius.tetris3dpro;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Randare in loturi: toate cuburile unui cadru sunt scrise intr-un singur
 * buffer de varfuri (deja transformate pe CPU) si trimise la GPU cu
 * cateva apeluri glDrawElements, in loc de un apel per cub.
 *
 * Lotul opac se deseneaza primul, cu scriere in depth buffer;
 * lotul transparent se deseneaza dupa, fara scriere in depth buffer,
 * in ordinea in care a fost cerut.
 */
public class Randare {

    private static final String COD_VARFURI =
            "uniform mat4 uVP;\n" +
            "attribute vec3 aPos;\n" +
            "attribute vec3 aNormal;\n" +
            "attribute vec2 aUV;\n" +
            "attribute vec4 aCuloare;\n" +
            "varying vec3 vNormal;\n" +
            "varying vec2 vUV;\n" +
            "varying vec4 vCuloare;\n" +
            "void main() {\n" +
            "  vNormal = aNormal;\n" +
            "  vUV = aUV;\n" +
            "  vCuloare = aCuloare;\n" +
            "  gl_Position = uVP * vec4(aPos, 1.0);\n" +
            "}\n";

    private static final String COD_PIXELI =
            "precision mediump float;\n" +
            "varying vec3 vNormal;\n" +
            "varying vec2 vUV;\n" +
            "varying vec4 vCuloare;\n" +
            "const vec3 L  = normalize(vec3(0.35, 0.82, 0.45));\n" +
            "const vec3 V  = vec3(0.0, 0.0, 1.0);\n" +
            "const vec3 NEON_A = normalize(vec3(-0.85, 0.15, 0.50));\n" +
            "const vec3 NEON_B = normalize(vec3( 0.80, -0.25, 0.55));\n" +
            "const vec3 CUL_A = vec3(1.00, 0.35, 0.85);\n" +
            "const vec3 CUL_B = vec3(0.25, 0.85, 1.00);\n" +
            "float zgomot(vec2 p) {\n" +
            "  return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);\n" +
            "}\n" +
            "void main() {\n" +
            "  vec2 d = abs(vUV - 0.5) * 2.0;\n" +
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
            "  vec3 baza = vCuloare.rgb;\n" +
            "  float nivel = 0.34 + sus * 0.78 + fata * 0.42 + lat * 0.16 - jos * 0.10 + dif * 0.22;\n" +
            "  vec3 culoare = baza * nivel;\n" +
            "  float refA = pow(max(dot(N, NEON_A), 0.0), 2.2);\n" +
            "  float refB = pow(max(dot(N, NEON_B), 0.0), 2.2);\n" +
            "  culoare += CUL_A * refA * 0.22;\n" +
            "  culoare += CUL_B * refB * 0.20;\n" +
            "  float pata = pow(max(dot(N, H), 0.0), 12.0);\n" +
            "  culoare += vec3(1.0, 0.98, 0.94) * pata * (0.18 + sus * 0.45);\n" +
            "  float sclip = pow(max(dot(N, H), 0.0), 70.0);\n" +
            "  culoare += vec3(1.0) * sclip * (0.20 + sus * 0.40);\n" +
            "  float contur = smoothstep(0.55, 0.98, colt);\n" +
            "  vec3 culContur = baza * 1.5 + vec3(0.28);\n" +
            "  culoare = mix(culoare, culContur, contur * 0.55);\n" +
            "  float t = zgomot(floor(vUV * 42.0));\n" +
            "  culoare *= 0.965 + t * 0.070;\n" +
            "  gl_FragColor = vec4(culoare, vCuloare.a * rotunjire);\n" +
            "}\n";

    private static final float S = 0.47f;

    /** 24 varfuri: pozitie(3) normala(3) uv(2) */
    private static final float[] CUB = {
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

    private static final short[] INDICI_CUB = {
            0,1,2,  0,2,3,      4,5,6,  4,6,7,
            8,9,10, 8,10,11,    12,13,14, 12,14,15,
            16,17,18, 16,18,19, 20,21,22, 20,22,23
    };

    private static final int VARFURI_PER_CUB = 24;
    private static final int INDICI_PER_CUB = 36;
    /** pozitie(3) normala(3) uv(2) culoare(4) */
    private static final int FLOATURI_PER_VARF = 12;
    private static final int OCTETI_PER_VARF = FLOATURI_PER_VARF * 4;

    /** limita de indici pe 16 biti: 65536 / 24 = 2730 cuburi */
    private static final int MAX_CUBURI = 2700;

    private final float[] dateOpac;
    private final float[] dateTransp;
    private int nrOpac = 0;
    private int nrTransp = 0;

    private final FloatBuffer bufVarfuri;
    private final int vbo;
    private final int ibo;

    private final int program;
    private final int locPos, locNormal, locUV, locCuloare, locVP;

    private final float[] proiectie = new float[16];
    private final float[] camera    = new float[16];
    private final float[] vizProj   = new float[16];
    private final float[] rot       = new float[16];

    public float raport = 0.5f;
    private float fovY = 46f;
    private float camEyeZ = 26f;

    public int cuburiCadru = 0;
    public int apeluriCadru = 0;

    public Randare() {
        dateOpac   = new float[MAX_CUBURI * VARFURI_PER_CUB * FLOATURI_PER_VARF];
        dateTransp = new float[MAX_CUBURI * VARFURI_PER_CUB * FLOATURI_PER_VARF];

        ByteBuffer bv = ByteBuffer.allocateDirect(dateOpac.length * 4);
        bv.order(ByteOrder.nativeOrder());
        bufVarfuri = bv.asFloatBuffer();

        short[] indici = new short[MAX_CUBURI * INDICI_PER_CUB];
        for (int c = 0; c < MAX_CUBURI; c++) {
            int baza = c * VARFURI_PER_CUB;
            for (int i = 0; i < INDICI_PER_CUB; i++) {
                indici[c * INDICI_PER_CUB + i] = (short) (baza + INDICI_CUB[i]);
            }
        }
        ByteBuffer bi = ByteBuffer.allocateDirect(indici.length * 2);
        bi.order(ByteOrder.nativeOrder());
        ShortBuffer bufIndici = bi.asShortBuffer();
        bufIndici.put(indici).position(0);

        int[] buf = new int[2];
        GLES20.glGenBuffers(2, buf, 0);
        vbo = buf[0];
        ibo = buf[1];

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indici.length * 2,
                bufIndici, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, dateOpac.length * 4,
                null, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        int vs = compileaza(GLES20.GL_VERTEX_SHADER, COD_VARFURI);
        int fs = compileaza(GLES20.GL_FRAGMENT_SHADER, COD_PIXELI);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vs);
        GLES20.glAttachShader(program, fs);
        GLES20.glLinkProgram(program);

        locPos     = GLES20.glGetAttribLocation(program, "aPos");
        locNormal  = GLES20.glGetAttribLocation(program, "aNormal");
        locUV      = GLES20.glGetAttribLocation(program, "aUV");
        locCuloare = GLES20.glGetAttribLocation(program, "aCuloare");
        locVP      = GLES20.glGetUniformLocation(program, "uVP");

        Matrix.setIdentityM(vizProj, 0);
    }

    private int compileaza(int tip, String cod) {
        int s = GLES20.glCreateShader(tip);
        GLES20.glShaderSource(s, cod);
        GLES20.glCompileShader(s);
        return s;
    }

    // ---------- camera ----------

    public void seteazaProiectie(float unghi, float raportEcran, float aproape, float departe) {
        raport = raportEcran;
        fovY = unghi;
        Matrix.perspectiveM(proiectie, 0, unghi, raportEcran, aproape, departe);
    }

    public void seteazaCamera(float ochiX, float ochiY, float ochiZ,
                              float tintaX, float tintaY, float tintaZ) {
        camEyeZ = ochiZ;
        Matrix.setLookAtM(camera, 0, ochiX, ochiY, ochiZ, tintaX, tintaY, tintaZ, 0f, 1f, 0f);
        Matrix.multiplyMM(vizProj, 0, proiectie, 0, camera, 0);
    }

    public float inaltimeLaZ(float z) {
        float dist = camEyeZ - z;
        if (dist < 1f) dist = 1f;
        return 2f * dist * (float) Math.tan(Math.toRadians(fovY / 2f));
    }

    public float latimeLaZ(float z) {
        return inaltimeLaZ(z) * raport;
    }

    // ---------- adaugare cuburi ----------

    public void cub(float x, float y, float z,
                    float r, float g, float b, float alfa, float scara) {
        cubIntins(x, y, z, scara, scara, scara, r, g, b, alfa);
    }

    public void cubIntins(float x, float y, float z,
                          float sx, float sy, float sz,
                          float r, float g, float b, float alfa) {
        if (alfa <= 0.002f) return;
        boolean opac = alfa >= 0.995f;
        float[] date = opac ? dateOpac : dateTransp;
        int n = opac ? nrOpac : nrTransp;
        if (n >= MAX_CUBURI) { goleste(); n = 0; date = opac ? dateOpac : dateTransp; }

        // cuburile intinse (pereti, grila) primesc UV constant => fara colturi rotunjite
        boolean plat = sx != sy || sy != sz;
        int k = n * VARFURI_PER_CUB * FLOATURI_PER_VARF;
        for (int v = 0; v < VARFURI_PER_CUB; v++) {
            int s = v * 8;
            date[k++] = x + CUB[s] * sx;
            date[k++] = y + CUB[s + 1] * sy;
            date[k++] = z + CUB[s + 2] * sz;
            date[k++] = CUB[s + 3];
            date[k++] = CUB[s + 4];
            date[k++] = CUB[s + 5];
            date[k++] = plat ? 0.5f : CUB[s + 6];
            date[k++] = plat ? 0.5f : CUB[s + 7];
            date[k++] = r; date[k++] = g; date[k++] = b; date[k++] = alfa;
        }
        if (opac) nrOpac = n + 1; else nrTransp = n + 1;
    }

    public void cubRotit(float x, float y, float z,
                         float unghi, float axaX, float axaY, float axaZ,
                         float r, float g, float b, float alfa, float scara) {
        if (alfa <= 0.002f) return;
        boolean opac = alfa >= 0.995f;
        float[] date = opac ? dateOpac : dateTransp;
        int n = opac ? nrOpac : nrTransp;
        if (n >= MAX_CUBURI) { goleste(); n = 0; date = opac ? dateOpac : dateTransp; }

        Matrix.setRotateM(rot, 0, unghi, axaX, axaY, axaZ);
        float m0 = rot[0], m1 = rot[1], m2 = rot[2];
        float m4 = rot[4], m5 = rot[5], m6 = rot[6];
        float m8 = rot[8], m9 = rot[9], m10 = rot[10];

        int k = n * VARFURI_PER_CUB * FLOATURI_PER_VARF;
        for (int v = 0; v < VARFURI_PER_CUB; v++) {
            int s = v * 8;
            float px = CUB[s] * scara, py = CUB[s + 1] * scara, pz = CUB[s + 2] * scara;
            float nx = CUB[s + 3], ny = CUB[s + 4], nz = CUB[s + 5];

            date[k++] = x + m0 * px + m4 * py + m8 * pz;
            date[k++] = y + m1 * px + m5 * py + m9 * pz;
            date[k++] = z + m2 * px + m6 * py + m10 * pz;
            date[k++] = m0 * nx + m4 * ny + m8 * nz;
            date[k++] = m1 * nx + m5 * ny + m9 * nz;
            date[k++] = m2 * nx + m6 * ny + m10 * nz;
            date[k++] = CUB[s + 6];
            date[k++] = CUB[s + 7];
            date[k++] = r; date[k++] = g; date[k++] = b; date[k++] = alfa;
        }
        if (opac) nrOpac = n + 1; else nrTransp = n + 1;
    }

    // ---------- trimitere la GPU ----------

    public void inceputCadru() {
        nrOpac = 0;
        nrTransp = 0;
        cuburiCadru = 0;
        apeluriCadru = 0;
    }

    /** trimite tot ce s-a adunat; se cheama la sfarsitul cadrului */
    public void goleste() {
        if (nrOpac == 0 && nrTransp == 0) return;

        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(locVP, 1, false, vizProj, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ibo);

        GLES20.glEnableVertexAttribArray(locPos);
        GLES20.glEnableVertexAttribArray(locNormal);
        GLES20.glEnableVertexAttribArray(locUV);
        GLES20.glEnableVertexAttribArray(locCuloare);
        GLES20.glVertexAttribPointer(locPos,     3, GLES20.GL_FLOAT, false, OCTETI_PER_VARF, 0);
        GLES20.glVertexAttribPointer(locNormal,  3, GLES20.GL_FLOAT, false, OCTETI_PER_VARF, 12);
        GLES20.glVertexAttribPointer(locUV,      2, GLES20.GL_FLOAT, false, OCTETI_PER_VARF, 24);
        GLES20.glVertexAttribPointer(locCuloare, 4, GLES20.GL_FLOAT, false, OCTETI_PER_VARF, 32);

        if (nrOpac > 0) {
            GLES20.glDepthMask(true);
            trimite(dateOpac, nrOpac);
        }
        if (nrTransp > 0) {
            GLES20.glDepthMask(false);
            trimite(dateTransp, nrTransp);
            GLES20.glDepthMask(true);
        }

        GLES20.glDisableVertexAttribArray(locPos);
        GLES20.glDisableVertexAttribArray(locNormal);
        GLES20.glDisableVertexAttribArray(locUV);
        GLES20.glDisableVertexAttribArray(locCuloare);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        cuburiCadru += nrOpac + nrTransp;
        nrOpac = 0;
        nrTransp = 0;
    }

    private void trimite(float[] date, int nrCuburi) {
        int floaturi = nrCuburi * VARFURI_PER_CUB * FLOATURI_PER_VARF;
        bufVarfuri.position(0);
        bufVarfuri.put(date, 0, floaturi);
        bufVarfuri.position(0);
        // glBufferData (nu SubData) ca driverul sa aloce un buffer nou si sa nu astepte
        // dupa desenarea precedenta
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, floaturi * 4, bufVarfuri, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, nrCuburi * INDICI_PER_CUB,
                GLES20.GL_UNSIGNED_SHORT, 0);
        apeluriCadru++;
    }
}
