package com.marius.tetris3d;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class Sunet {

    private static final int RATA = 22050;

    // sunetele sunt generate o data si tinute in memorie
    private final short[] sRotire;
    private final short[] sMutare;
    private final short[] sAterizare;
    private final short[] sTrantire;
    private final short[] sLinie;
    private final short[] sTetris;
    private final short[] sNivel;
    private final short[] sFinal;

    private boolean pornit = true;

    public Sunet() {
        sRotire    = clic(900f, 0.05f, 0.35f);
        sMutare    = clic(420f, 0.04f, 0.22f);
        sAterizare = bubuit(110f, 0.16f, 0.55f);
        sTrantire  = bubuit(70f, 0.30f, 0.85f);
        sLinie     = explozie(0.35f, 0.70f);
        sTetris    = explozieMare(0.60f);
        sNivel     = acordUrcator();
        sFinal     = tonCazator();
    }

    public void setPornit(boolean p) { pornit = p; }
    public boolean pornit() { return pornit; }

    // ---------- redare ----------
    private void reda(final short[] date, final float volum) {
        if (!pornit || date == null) return;

        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    AudioTrack at = new AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            RATA,
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            date.length * 2,
                            AudioTrack.MODE_STATIC);

                    at.write(date, 0, date.length);
                    at.setVolume(volum);
                    at.play();

                    Thread.sleep((long) (date.length * 1000L / RATA) + 60);
                    at.stop();
                    at.release();
                } catch (Exception ignored) { }
            }
        }).start();
    }

    public void rotire()    { reda(sRotire, 0.5f); }
    public void mutare()    { reda(sMutare, 0.35f); }
    public void aterizare() { reda(sAterizare, 0.7f); }
    public void trantire()  { reda(sTrantire, 0.9f); }
    public void linie()     { reda(sLinie, 0.8f); }
    public void tetris()    { reda(sTetris, 1.0f); }
    public void nivel()     { reda(sNivel, 0.75f); }
    public void final_()    { reda(sFinal, 0.8f); }

    // ---------- generatoare ----------

    // clic scurt: unda dreptunghiulara care se stinge rapid
    private short[] clic(float frecventa, float durata, float amp) {
        int n = (int) (RATA * durata);
        short[] d = new short[n];
        for (int i = 0; i < n; i++) {
            float t = (float) i / RATA;
            float stingere = (float) Math.exp(-t * 38f);
            float v = (float) Math.sin(2 * Math.PI * frecventa * t);
            v += 0.4f * (float) Math.sin(2 * Math.PI * frecventa * 2.02f * t);
            d[i] = (short) (v * stingere * amp * 32000);
        }
        return d;
    }

    // bubuit: ton jos care coboara si mai mult
    private short[] bubuit(float frecventa, float durata, float amp) {
        int n = (int) (RATA * durata);
        short[] d = new short[n];
        float faza = 0f;
        for (int i = 0; i < n; i++) {
            float t = (float) i / RATA;
            float p = t / durata;
            float f = frecventa * (1f - p * 0.45f);
            faza += 2 * Math.PI * f / RATA;

            float stingere = (float) Math.exp(-t * 9f);
            float v = (float) Math.sin(faza);
            v += 0.30f * (float) (Math.random() - 0.5) * (1f - p);
            d[i] = (short) (v * stingere * amp * 32000);
        }
        return d;
    }

    // explozie: zgomot filtrat + suierat
    private short[] explozie(float durata, float amp) {
        int n = (int) (RATA * durata);
        short[] d = new short[n];
        float ultim = 0f;
        for (int i = 0; i < n; i++) {
            float t = (float) i / RATA;
            float p = t / durata;

            float zgomot = (float) (Math.random() - 0.5) * 2f;
            ultim = ultim * 0.72f + zgomot * 0.28f;

            float suier = (float) Math.sin(2 * Math.PI * (1400f - p * 1100f) * t) * 0.35f;
            float stingere = (float) Math.exp(-t * 8f);

            d[i] = (short) ((ultim + suier) * stingere * amp * 30000);
        }
        return d;
    }

    // explozie mare pentru 4 linii: acord plus zgomot
    private short[] explozieMare(float durata) {
        int n = (int) (RATA * durata);
        short[] d = new short[n];
        float ultim = 0f;
        float[] note = {261.6f, 329.6f, 392.0f, 523.3f};

        for (int i = 0; i < n; i++) {
            float t = (float) i / RATA;
            float p = t / durata;

            float zgomot = (float) (Math.random() - 0.5) * 2f;
            ultim = ultim * 0.80f + zgomot * 0.20f;

            float acord = 0f;
            for (float f : note) {
                acord += (float) Math.sin(2 * Math.PI * f * t);
            }
            acord /= note.length;

            float stingere = (float) Math.exp(-t * 4.5f);
            float v = acord * 0.65f + ultim * 0.45f * (1f - p);

            d[i] = (short) (v * stingere * 30000);
        }
        return d;
    }

    // trei note urcatoare la nivel nou
    private short[] acordUrcator() {
        float[] note = {392.0f, 523.3f, 659.3f};
        float durNota = 0.11f;
        int nNota = (int) (RATA * durNota);
        short[] d = new short[nNota * note.length];

        int k = 0;
        for (float f : note) {
            for (int i = 0; i < nNota; i++) {
                float t = (float) i / RATA;
                float stingere = (float) Math.exp(-t * 12f);
                float v = (float) Math.sin(2 * Math.PI * f * t);
                v += 0.30f * (float) Math.sin(2 * Math.PI * f * 2f * t);
                d[k++] = (short) (v * stingere * 0.6f * 32000);
            }
        }
        return d;
    }

    // ton care coboara si se stinge, la final de joc
    private short[] tonCazator() {
        float durata = 1.1f;
        int n = (int) (RATA * durata);
        short[] d = new short[n];
        float faza = 0f;

        for (int i = 0; i < n; i++) {
            float t = (float) i / RATA;
            float p = t / durata;
            float f = 440f * (1f - p * 0.72f);
            faza += 2 * Math.PI * f / RATA;

            float stingere = (float) Math.exp(-t * 2.2f);
            float v = (float) Math.sin(faza);
            v += 0.25f * (float) Math.sin(faza * 0.5f);

            d[i] = (short) (v * stingere * 0.7f * 32000);
        }
        return d;
    }
                                              }
