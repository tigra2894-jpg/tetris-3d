# Tetris 3D Pro

Joc Tetris 3D pentru Android (OpenGL ES 2.0), versiunea 2.0 rescrisa pentru performanta si gameplay modern.

## Descarcare APK

- **Release** (dupa merge in `main`): tab-ul *Releases* -> `Tetris3DPro.apk`
- **Orice build** (inclusiv PR-uri): tab-ul *Actions* -> rulare -> artefactul `Tetris3DPro-apk`

APK-ul este semnat cu cheia de debug; la instalare activeaza "Surse necunoscute".

## Noutati fata de v1

- Randare batch: toate cuburile intr-un frame se deseneaza in 1-2 apeluri GL (fata de un apel per cub).
- UI pe textura actualizata doar cand se schimba ceva.
- Gameplay modern: 7-bag, hold, coada de 5 piese, rotiri SRS cu kick-uri, rotire 180, lock delay,
  T-spin, combo, back-to-back, perfect clear.
- Moduri: Clasic, Sprint (40 linii), Ultra (2 minute), Liber.
- Controale: butoane pe ecran + gesturi (swipe, tap rotire, flick jos = hard drop, flick sus = hold).
- Vibratii, sunete pre-generate, particule, 5 teme de culori, statistici si recorduri.

## Build local

```bash
./gradlew assembleDebug
```

Necesita JDK 17 si Android SDK 34 (`local.properties` cu `sdk.dir`).
