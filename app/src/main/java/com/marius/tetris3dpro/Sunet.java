package com.marius.tetris3dpro;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

/**
 * Efecte sonore sintetizate o singura data la pornire si tinute in memorie
 * ca AudioTrack-uri statice; redarea nu mai creeaza fire sau buffere noi.
 */
public class Sunet {

    public static final int MUTARE = 0, ROTIRE = 1, ATERIZARE = 2, TRANTIRE = 3,
            LINIE = 4, TETRIS = 5, NIVEL = 6, FINAL = 7, HOLD = 8, MENIU = 9,
            TSPIN = 10, COMBO = 11, VICTORIE = 12, BLOCAT = 13;
    private static final int NR = 14;

    private static final int RATA = 22050;

    private final AudioTrack[] piste = new AudioTrack[NR];
    private boolean pornit = true;

    public Sunet() {
        piste[MUTARE]    = fa(ton(0.045f, 520f, 380f, 0.30f, 0));
        piste[ROTIRE]    = fa(ton(0.07f, 660f, 880f, 0.35f, 1));
        piste[ATERIZARE] = fa(ton(0.09f, 190f, 90f, 0.55f, 2));
        piste[TRANTIRE]  = fa(ton(0.13f, 260f, 60f, 0.75f, 2));
        piste[LINIE]     = fa(acord(0.28f, new float[]{523f, 659f, 784f}, 0.55f));
        piste[TETRIS]    = fa(arpegiu(0.55f, new float[]{523f, 659f, 784f, 1047f}, 0.65f));
        piste[NIVEL]     = fa(arpegiu(0.6f, new float[]{392f, 523f, 659f, 784f, 1047f}, 0.6f));
        piste[FINAL]     = fa(arpegiu(1.2f, new float[]{440f, 392f, 330f, 262f, 196f}, 0.6f));
        piste[HOLD]      = fa(ton(0.08f, 440f, 560f, 0.35f, 1));
        piste[MENIU]     = fa(ton(0.06f, 720f, 720f, 0.30f, 1));
        piste[TSPIN]     = fa(arpegiu(0.4f, new float[]{784f, 988f, 1175f}, 0.6f));
        piste[COMBO]     = fa(ton(0.12f, 880f, 1320f, 0.45f, 1));
        piste[VICTORIE]  = fa(arpegiu(1.4f, new float[]{523f, 659f, 784f, 1047f, 1319f, 1568f}, 0.65f));
        piste[BLOCAT]    = fa(ton(0.05f, 140f, 120f, 0.35f, 0));
    }

    public void setPornit(boolean v) { pornit = v; }

    public void reda(int id) {
        if (!pornit || id < 0 || id >= NR) return;
        AudioTrack t = piste[id];
        if (t == null) return;
        try {
            t.pause();
            t.flush();
            t.reloadStaticData();
            t.play();
        } catch (IllegalStateException ignored) { }
    }

    public void elibereaza() {
        for (int i = 0; i < NR; i++) {
            if (piste[i] != null) { piste[i].release(); piste[i] = null; }
        }
    }

    // ---------- sinteza ----------

    private AudioTrack fa(short[] date) {
        try {
            AudioTrack t;
            if (Build.VERSION.SDK_INT >= 23) {
                t = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(RATA)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build())
                        .setBufferSizeInBytes(date.length * 2)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build();
            } else {
                t = new AudioTrack(AudioManager.STREAM_MUSIC, RATA,
                        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                        date.length * 2, AudioTrack.MODE_STATIC);
            }
            t.write(date, 0, date.length);
            return t;
        } catch (Exception e) {
            return null;
        }
    }

    /** forma: 0 sinus, 1 triunghi, 2 zgomot + sinus */
    private static short[] ton(float durata, float f0, float f1, float vol, int forma) {
        int n = (int) (RATA * durata);
        short[] d = new short[n];
        double faza = 0;
        java.util.Random r = new java.util.Random(7);
        for (int i = 0; i < n; i++) {
            float t = (float) i / n;
            float f = f0 + (f1 - f0) * t;
            faza += 2 * Math.PI * f / RATA;
            double v;
            if (forma == 1) {
                v = 2.0 / Math.PI * Math.asin(Math.sin(faza));
            } else if (forma == 2) {
                v = Math.sin(faza) * 0.7 + (r.nextDouble() * 2 - 1) * 0.3 * (1 - t);
            } else {
                v = Math.sin(faza);
            }
            float env = anvelopa(t, 0.02f);
            d[i] = (short) (v * env * vol * 32767);
        }
        return d;
    }

    private static short[] acord(float durata, float[] frecv, float vol) {
        int n = (int) (RATA * durata);
        short[] d = new short[n];
        for (int i = 0; i < n; i++) {
            float t = (float) i / n;
            double v = 0;
            for (float f : frecv) v += Math.sin(2 * Math.PI * f * i / RATA);
            v /= frecv.length;
            d[i] = (short) (v * anvelopa(t, 0.03f) * vol * 32767);
        }
        return d;
    }

    private static short[] arpegiu(float durata, float[] note, float vol) {
        int n = (int) (RATA * durata);
        short[] d = new short[n];
        int perNota = n / note.length;
        for (int k = 0; k < note.length; k++) {
            for (int i = 0; i < perNota; i++) {
                int idx = k * perNota + i;
                if (idx >= n) break;
                float t = (float) i / perNota;
                double v = Math.sin(2 * Math.PI * note[k] * i / RATA) * 0.8
                         + Math.sin(2 * Math.PI * note[k] * 2 * i / RATA) * 0.2;
                d[idx] = (short) (v * anvelopa(t, 0.05f) * vol * 32767);
            }
        }
        return d;
    }

    private static float anvelopa(float t, float atac) {
        if (t < atac) return t / atac;
        float rest = (t - atac) / (1f - atac);
        return (float) Math.pow(1f - rest, 1.6);
    }
}
