package com.marius.tetris3d;

import android.opengl.Matrix;

public class Desenator {

    private final Cub cub;

    private final float[] proiectie = new float[16];
    private final float[] camera    = new float[16];
    private final float[] vizProj   = new float[16];
    private final float[] model     = new float[16];
    private final float[] mvp       = new float[16];

    public float raport = 0.5f;
    private float fovY = 46f;
    private float camEyeZ = 26f;

    public Desenator(Cub cub) {
        this.cub = cub;
        Matrix.setIdentityM(vizProj, 0);
    }

    public void seteazaProiectie(float unghi, float raportEcran,
                                 float aproape, float departe) {
        raport = raportEcran;
        fovY = unghi;
        Matrix.perspectiveM(proiectie, 0, unghi, raportEcran, aproape, departe);
    }

    public void seteazaCamera(float ochiX, float ochiY, float ochiZ,
                              float tintaX, float tintaY, float tintaZ) {
        camEyeZ = ochiZ;
        Matrix.setLookAtM(camera, 0,
                ochiX, ochiY, ochiZ,
                tintaX, tintaY, tintaZ,
                0f, 1f, 0f);
        Matrix.multiplyMM(vizProj, 0, proiectie, 0, camera, 0);
    }

    public void seteazaLumina(float x, float y, float z) {
        cub.seteazaLumina(x, y, z);
    }

    public float inaltimeLaZ(float z) {
        float dist = camEyeZ - z;
        if (dist < 1f) dist = 1f;
        return 2f * dist * (float) Math.tan(Math.toRadians(fovY / 2f));
    }

    public float latimeLaZ(float z) {
        return inaltimeLaZ(z) * raport;
    }

    public void cub(float x, float y, float z,
                    float r, float g, float b,
                    float alfa, float scara) {
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, x, y, z);
        Matrix.scaleM(model, 0, scara, scara, scara);
        Matrix.multiplyMM(mvp, 0, vizProj, 0, model, 0);
        cub.deseneaza(mvp, model, r, g, b, alfa);
    }

    public void cubIntins(float x, float y, float z,
                          float sx, float sy, float sz,
                          float r, float g, float b, float alfa) {
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, x, y, z);
        Matrix.scaleM(model, 0, sx, sy, sz);
        Matrix.multiplyMM(mvp, 0, vizProj, 0, model, 0);
        cub.deseneaza(mvp, model, r, g, b, alfa);
    }

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

    public float latimeText(String s, float scara) {
        if (s == null || s.isEmpty()) return 0f;
        return s.length() * (Cifre.latime() + 1) * scara - scara;
    }

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

    public void textCentrat(String s, float xCentru, float y, float scara,
                            float r, float g, float b) {
        textCentrat(s, xCentru, y, 0f, scara, r, g, b, 1f);
    }

    public void textCentrat(String s, float xCentru, float y, float z, float scara,
                            float r, float g, float b, float alfa) {
        float lat = latimeText(s, scara);
        text(s, xCentru - lat / 2f, y, z, scara, r, g, b, alfa);
    }

    public void textPotrivit(String s, float xCentru, float y, float scaraMax,
                             float r, float g, float b, float alfa,
                             float procentDinEcran) {
        float latimeMax = latimeLaZ(0f) * procentDinEcran;
        float scara = scaraMax;
        float lat = latimeText(s, scara);
        if (lat > latimeMax && lat > 0f) {
            scara = scaraMax * (latimeMax / lat);
        }
        textCentrat(s, xCentru, y, 0f, scara, r, g, b, alfa);
    }

    public void numarCentrat(int valoare, float xCentru, float y, float scara,
                             float r, float g, float b) {
        textCentrat(String.valueOf(valoare), xCentru, y, scara, r, g, b);
    }

    public void numar(int valoare, float x, float y, float scara,
                      float r, float g, float b) {
        text(String.valueOf(valoare), x, y, scara, r, g, b);
    }

    public void linieOrizontala(float xStanga, float xDreapta, float y, float z,
                                float grosime,
                                float r, float g, float b, float alfa) {
        float pas = grosime * 1.6f;
        for (float x = xStanga; x <= xDreapta; x += pas) {
            cub(x, y, z, r, g, b, alfa, grosime);
        }
    }

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
