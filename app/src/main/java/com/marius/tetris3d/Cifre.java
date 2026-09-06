package com.marius.tetris3d;

public class Cifre {

    // fiecare cifra desenata pe o grila de 3 latime x 5 inaltime
    // 1 = cub plin, 0 = gol. Randul 0 este sus.
    private static final int[][][] MODELE = {
        // 0
        {{1,1,1},{1,0,1},{1,0,1},{1,0,1},{1,1,1}},
        // 1
        {{0,1,0},{1,1,0},{0,1,0},{0,1,0},{1,1,1}},
        // 2
        {{1,1,1},{0,0,1},{1,1,1},{1,0,0},{1,1,1}},
        // 3
        {{1,1,1},{0,0,1},{1,1,1},{0,0,1},{1,1,1}},
        // 4
        {{1,0,1},{1,0,1},{1,1,1},{0,0,1},{0,0,1}},
        // 5
        {{1,1,1},{1,0,0},{1,1,1},{0,0,1},{1,1,1}},
        // 6
        {{1,1,1},{1,0,0},{1,1,1},{1,0,1},{1,1,1}},
        // 7
        {{1,1,1},{0,0,1},{0,1,0},{0,1,0},{0,1,0}},
        // 8
        {{1,1,1},{1,0,1},{1,1,1},{1,0,1},{1,1,1}},
        // 9
        {{1,1,1},{1,0,1},{1,1,1},{0,0,1},{1,1,1}}
    };

    public static int[][] model(int cifra) {
        if (cifra < 0 || cifra > 9) return MODELE[0];
        return MODELE[cifra];
    }

    public static int latime()  { return 3; }
    public static int inaltime(){ return 5; }
}
