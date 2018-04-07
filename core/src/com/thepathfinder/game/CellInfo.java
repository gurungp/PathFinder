package com.thepathfinder.game;

/**
 * Created by gurungp on 25/11/2016.
 */

public class CellInfo {

    int number;
    int i;
    int j;
    int F;

    public CellInfo(int iValue, int jValue, int num, int f) {
        i = iValue;
        j = jValue;
        number = num;
        F = f;
    }

    public int getNumber() {
        return number;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

}
