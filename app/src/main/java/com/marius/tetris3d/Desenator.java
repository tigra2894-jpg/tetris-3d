package com.marius.tetris3d;

import android.opengl.Matrix;

/**
 * Tot ce se deseneaza in joc trece pe aici.
 * Ecranele nu ating OpenGL direct: cer un cub, un text, un numar.
 */
public class Desenator {

    private final Cub cub;

    private final float[] proiectie = new float[16];
    private final float[] camera    = new float[16];
    private final float[] vizProj   = new float[16];
    private final float[] model     = new float[16];
    private final float[] mvp       = new float[16];

    // raportul ecranului, pentru asezarea corecta a textului
    public float raport = 0.5f;

    public Desenator(Cub cub) {
        this.cub = cub;
        Matrix.setIdentityM(vizProj, 0);
    }

    // ---------- camera ----------

    public void seteazaProiectie(float unghi, float raportEcran,
                                 float aproape, float departe) {
        raport = raportEcran;
        Matrix.perspectiveM(proiectie, 0, unghi, raportEcran, aproape, departe);
    }

    public void seteazaCamera(float ochiX, float ochiY, float ochiZ,
                              float tintaX, float tintaY, float tintaZ) {
        Matrix.setLookAtM(camera, 0,
                ochiX, ochiY, ochiZ,
                tintaX, tintaY, tintaZ,
                0f, 1f, 0f);
        Matrix.multiplyMM(vizProj, 0, proiectie, 0, camera, 0);
    }

    public void seteazaLumina(float x, float y, float z) {
        cub.seteazaLumina(x, y, z);
    }

    // ---------- desenare de baza ----------

    /** un cub simplu */
    public void cub(float x, float y, float z,
                    float r, float g, float b,
                    float alfa, float scara) {
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, x, y, z);
        Matrix.scaleM(model, 0, scara, scara, scara);
        Matrix.multiplyMM(mvp, 0, vizProj, 0, model, 0);
        cub.deseneaza(mvp, model, r, g, b, alfa);
    }

    /** cub cu scara diferita pe fiecare axa */
    public void cubIntins(float x, float y, float z,
                          float sx, float sy, float sz,
                          float r, float g, float b, float alfa) {
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, x, y, z);
        Matrix.scaleM(model, 0, sx, sy, sz);
        Matrix.multiplyMM(mvp, 0, vizProj, 0, model, 0);
        cub.deseneaza(mvp, model, r, g, b, alfa);
    }

    /** cub rotit in jurul unei axe */
    public void cubRotit(float x, float y, float z,
                         float unghi, float axaX, float axaY, float axaZ,
                         float r, float g, float b,
                         float alfa, float scara) {
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, x, y, z);
        Matrix.rotateM(model, 0, unghi, axaX, axaY, axaZ);
        Matrix.scaleM(model, 0, scara, scara, scara);
        Matrix.multiplyMM(mvp, 0, vizProj, 0, model, 0);
        cub.deseneaza(mvp, model, r, g, b, alfa);
    }

    // ---------- text ----------

    /** latimea totala a unui text, in unitati de lume */
    public float latimeText(String s, float scara) {
        if (s == null || s.isEmpty()) return 0f;
        return s.length() * (Cifre.latime() + 1) * scara - scara;
    }

    /** scrie un text pornind din coltul stanga-sus dat */
    public void text(String s, float x, float y, float scara,
                     float r, float g, float b) {
        text(s, x, y, 0f, scara, r, g, b, 1f);
    }

    public void text(String s, float x, float y, float z, float scara,
                     float r, float g, float b, float alfa) {
        if (s == null) return;
        float pas = (Cifre.latime() + 1) * scara;

        for (int k = 0; k < s.length(); k++) {
            char c = s.charAt(k);
            if (c == ' ') continue;

            int[][] m = Cifre.model(c);
            for (int rr = 0; rr < Cifre.inaltime(); rr++) {
                for (int cc = 0; cc < Cifre.latime(); cc++) {
                    if (m[rr][cc] == 1) {
                        cub(x + k * pas + cc * scara,
                            y - rr * scara,
                            z, r, g, b, alfa, scara);
                    }
                }
            }
        }
    }

    /** scrie un text centrat pe orizontala fata de x */
    public void textCentrat(String s, float xCentru, float y, float scara,
                            float r, float g, float b) {
        textCentrat(s, xCentru, y, 0f, scara, r, g, b, 1f);
    }

    public void textCentrat(String s, float xCentru, float y, float z, float scara,
                            float r, float g, float b, float alfa) {
        float lat = latimeText(s, scara);
        text(s, xCentru - lat / 2f, y, z, scara, r, g, b, alfa);
    }

    /** un numar, centrat */
    public void numarCentrat(int valoare, float xCentru, float y, float scara,
                             float r, float g, float b) {
        textCentrat(String.valueOf(valoare), xCentru, y, scara, r, g, b);
    }

    /** un numar, pornind din stanga */
    public void numar(int valoare, float x, float y, float scara,
                      float r, float g, float b) {
        text(String.valueOf(valoare), x, y, scara, r, g, b);
    }

    // ---------- forme ajutatoare ----------

    /** o linie orizontala din cuburi mici */
    public void linieOrizontala(float xStanga, float xDreapta, float y, float z,
                                float grosime,
                                float r, float g, float b, float alfa) {
        float pas = grosime * 1.6f;
        for (float x = xStanga; x <= xDreapta; x += pas) {
            cub(x, y, z, r, g, b, alfa, grosime);
        }
    }

    /** un chenar dreptunghiular din cuburi mici */
    public void chenar(float xCentru, float yCentru,
                       float latime, float inaltime, float z,
                       float grosime,
                       float r, float g, float b, float alfa) {
        float st = xCentru - latime / 2f;
        float dr = xCentru + latime / 2f;
        float jos = yCentru - inaltime / 2f;
        float sus = yCentru + inaltime / 2f;

        linieOrizontala(st, dr, sus, z, grosime, r, g, b, alfa);
        linieOrizontala(st, dr, jos, z, grosime, r, g, b, alfa);

        float pas = grosime * 1.6f;
        for (float y = jos; y <= sus; y += pas) {
            cub(st, y, z, r, g, b, alfa, grosime);
            cub(dr, y, z, r, g, b, alfa, grosime);
        }
    }
        }
