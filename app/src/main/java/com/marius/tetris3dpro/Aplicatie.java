package com.marius.tetris3dpro;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

/** starea globala si navigarea intre ecrane; ruleaza pe firul GL */
public class Aplicatie {

    public final Setari setari;
    public final Sunet sunet;
    public final Vibratii vibratii;
    public final Fundal fundal = new Fundal();
    public final Particule particule = new Particule();
    public final Joc joc = new Joc();

    public Randare randare;
    public CapaUI ui;
    public Imagine imagine;
    public Scantei scantei;
    public Scantei stele;

    // imagini optionale din assets/texturi/ (null daca lipsesc)
    public final Bitmap imgBloc, imgFundalJoc, imgFundalMeniu, imgPanou, imgRama, imgPodea, imgParticula, imgStea, imgLogo;
    // texturile GL corespunzatoare (0 daca lipsesc); se recreeaza odata cu contextul GL
    public int texFundalJoc = 0, texFundalMeniu = 0, texPanou = 0, texRama = 0, texParticula = 0, texStea = 0;

    public float latime = 1f, inaltime = 1f, raport = 0.5f;
    public float timp = 0f;
    public int fps = 0;
    private int cadre = 0;
    private float acumFps = 0f;

    private Ecran ecranCurent;
    private Ecran ecranUrmator;
    private float fadeIesire = -1f;

    public EcranMeniu ecranMeniu;
    public EcranModuri ecranModuri;
    public EcranSetari ecranSetari;
    public EcranStatistici ecranStatistici;
    public EcranJoc ecranJoc;

    private final Activity activitate;
    private boolean pornita = false;

    public Aplicatie(Activity act) {
        activitate = act;
        setari = new Setari(act);
        sunet = new Sunet();
        vibratii = new Vibratii(act);
        imgBloc   = Texturi.citeste(act, "bloc");
        imgFundalJoc   = Texturi.citeste(act, "fundal_joc");
        imgFundalMeniu = Texturi.citeste(act, "fundal_meniu");
        imgPanou  = Texturi.citeste(act, "panou");
        imgRama   = Texturi.citeste(act, "rama");
        imgPodea  = Texturi.citeste(act, "podea");
        imgParticula = Texturi.citeste(act, "particula");
        imgStea      = Texturi.citeste(act, "stea");
        imgLogo   = Texturi.citeste(act, "logo");
        sunet.setPornit(setari.sunetPornit());
        vibratii.setPornit(setari.vibratiePornita());
    }

    public Context context() { return activitate; }

    /** apelat pe firul GL cand contextul e gata (si la fiecare recreare) */
    public void pornesteGL() {
        randare = new Randare();
        ui = new CapaUI();
        ui.pregatesteGL();
        imagine = new Imagine();
        scantei = new Scantei();
        stele = new Scantei();
        randare.seteazaTexturaBloc(Texturi.incarca(imgBloc));
        texFundalJoc   = Texturi.incarca(imgFundalJoc);
        texFundalMeniu = Texturi.incarca(imgFundalMeniu);
        texPanou  = Texturi.incarca(imgPanou);
        texRama   = Texturi.incarca(imgRama, true);
        texParticula = Texturi.incarca(imgParticula);
        texStea      = Texturi.incarca(imgStea);
        fundal.lotStele = stele;
        fundal.texStea  = texStea;
        fundal.imagine  = imagine;
        fundal.texPodea = Texturi.incarca(imgPodea, true);
        if (!pornita) {
            pornita = true;
            ecranMeniu = new EcranMeniu(this);
            ecranModuri = new EcranModuri(this);
            ecranSetari = new EcranSetari(this);
            ecranStatistici = new EcranStatistici(this);
            ecranJoc = new EcranJoc(this);
            ecranCurent = ecranMeniu;
            ecranCurent.laIntrare();
        }
    }

    public void redimensioneaza(int l, int i) {
        latime = l; inaltime = i;
        raport = (float) l / i;
        randare.seteazaProiectie(46f, raport, 1f, 120f);
        ui.redimensioneaza(l, i);
    }

    public void schimbaEcran(Ecran e) {
        if (e == ecranCurent) return;
        ecranUrmator = e;
        fadeIesire = 0f;
    }

    public Ecran ecranCurent() { return ecranCurent; }

    public void actualizeaza(float dt) {
        timp += dt;
        cadre++;
        acumFps += dt;
        if (acumFps >= 0.5f) { fps = Math.round(cadre / acumFps); cadre = 0; acumFps = 0f; }

        fundal.actualizeaza(dt);
        particule.actualizeaza(dt);

        if (fadeIesire >= 0f) {
            fadeIesire += dt * 5f;
            if (fadeIesire >= 1f) {
                ecranCurent.laIesire();
                ecranCurent = ecranUrmator;
                ecranUrmator = null;
                fadeIesire = -1f;
                ecranCurent.laIntrare();
            }
        }
        ecranCurent.actualizeaza(dt);
    }

    public void deseneaza() {
        randare.inceputCadru();
        // meniurile folosesc fundalul de joc cat timp nu exista fundal_meniu
        boolean inJoc = ecranCurent == ecranJoc || imgFundalMeniu == null;
        Bitmap imgFundal = inJoc ? imgFundalJoc : imgFundalMeniu;
        int texFundal = inJoc ? texFundalJoc : texFundalMeniu;
        if (texFundal != 0) {
            imagine.fundal(texFundal, imgFundal.getWidth(), imgFundal.getHeight(), raport, 0.8f);
        }
        ecranCurent.deseneaza3D(randare);
        randare.goleste();
        stele.goleste(texStea, randare.vizProj());
        scantei.goleste(texParticula, randare.vizProj());

        ui.inceputCadru();
        ecranCurent.deseneazaUI(ui);
        if (fadeIesire >= 0f) {
            ui.panou(0f, 0f, 1f, 1f, Ecran.argb(Math.min(1f, fadeIesire), 0x000000), 0f);
        }
        ui.deseneazaPeEcran();
    }

    public void apasare(int p, float x, float y)  { if (fadeIesire < 0f) ecranCurent.apasare(p, x, y); }
    public void miscare(int p, float x, float y)  { if (fadeIesire < 0f) ecranCurent.miscare(p, x, y); }
    public void ridicare(int p, float x, float y) { ecranCurent.ridicare(p, x, y); }

    public boolean inapoi() {
        return ecranCurent.inapoi();
    }

    public void laPauza() {
        if (ecranCurent != null) ecranCurent.laPauzaAplicatie();
    }

    public void iesi() {
        activitate.runOnUiThread(activitate::finish);
    }

    public float[][] culoriPiese() { return setari.culoriPiese(); }
}
