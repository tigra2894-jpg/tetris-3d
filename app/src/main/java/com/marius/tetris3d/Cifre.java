package com.marius.tetris3d;

public class Cifre {

    // grila 3 latime x 5 inaltime, randul 0 este sus
    private static final int[][][] DIGITE = {
        {{1,1,1},{1,0,1},{1,0,1},{1,0,1},{1,1,1}}, // 0
        {{0,1,0},{1,1,0},{0,1,0},{0,1,0},{1,1,1}}, // 1
        {{1,1,1},{0,0,1},{1,1,1},{1,0,0},{1,1,1}}, // 2
        {{1,1,1},{0,0,1},{1,1,1},{0,0,1},{1,1,1}}, // 3
        {{1,0,1},{1,0,1},{1,1,1},{0,0,1},{0,0,1}}, // 4
        {{1,1,1},{1,0,0},{1,1,1},{0,0,1},{1,1,1}}, // 5
        {{1,1,1},{1,0,0},{1,1,1},{1,0,1},{1,1,1}}, // 6
        {{1,1,1},{0,0,1},{0,1,0},{0,1,0},{0,1,0}}, // 7
        {{1,1,1},{1,0,1},{1,1,1},{1,0,1},{1,1,1}}, // 8
        {{1,1,1},{1,0,1},{1,1,1},{0,0,1},{1,1,1}}  // 9
    };

    private static final int[][] A = {{1,1,1},{1,0,1},{1,1,1},{1,0,1},{1,0,1}};
    private static final int[][] C = {{1,1,1},{1,0,0},{1,0,0},{1,0,0},{1,1,1}};
    private static final int[][] D = {{1,1,0},{1,0,1},{1,0,1},{1,0,1},{1,1,0}};
    private static final int[][] E = {{1,1,1},{1,0,0},{1,1,0},{1,0,0},{1,1,1}};
    private static final int[][] I = {{1,1,1},{0,1,0},{0,1,0},{0,1,0},{1,1,1}};
    private static final int[][] L = {{1,0,0},{1,0,0},{1,0,0},{1,0,0},{1,1,1}};
    private static final int[][] N = {{1,0,1},{1,1,1},{1,1,1},{1,0,1},{1,0,1}};
    private static final int[][] O = {{1,1,1},{1,0,1},{1,0,1},{1,0,1},{1,1,1}};
    private static final int[][] P = {{1,1,1},{1,0,1},{1,1,1},{1,0,0},{1,0,0}};
    private static final int[][] R = {{1,1,1},{1,0,1},{1,1,0},{1,0,1},{1,0,1}};
    private static final int[][] S = {{1,1,1},{1,0,0},{1,1,1},{0,0,1},{1,1,1}};
    private static final int[][] T = {{1,1,1},{0,1,0},{0,1,0},{0,1,0},{0,1,0}};
    private static final int[][] U = {{1,0,1},{1,0,1},{1,0,1},{1,0,1},{1,1,1}};
    private static final int[][] V = {{1,0,1},{1,0,1},{1,0,1},{1,0,1},{0,1,0}};
    private static final int[][] Z = {{1,1,1},{0,0,1},{0,1,0},{1,0,0},{1,1,1}};
    private static final int[][] GOL = {{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}};

    public static int[][] model(char c) {
        if (c >= '0' && c <= '9') return DIGITE[c - '0'];
        switch (Character.toUpperCase(c)) {
            case 'A': return A;
            case 'C': return C;
            case 'D': return D;
            case 'E': return E;
            case 'I': return I;
            case 'L': return L;
            case 'N': return N;
            case 'O': return O;
            case 'P': return P;
            case 'R': return R;
            case 'S': return S;
            case 'T': return T;
            case 'U': return U;
            case 'V': return V;
            case 'Z': return Z;
            default:  return GOL;
        }
    }

    public static int latime()  { return 3; }
    public static int inaltime(){ return 5; }
}
