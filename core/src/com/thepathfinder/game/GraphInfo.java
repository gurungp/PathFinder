package com.thepathfinder.game;


import com.badlogic.gdx.graphics.Color;

/**
 * Created by gurungp on 21/11/2016.
 */


class GraphInfo implements Comparable<GraphInfo>{

    float X;
    float Y;
    boolean isBlock = false;
    boolean opened = false;
    boolean closed = false;
    int parentNode = 0;
    int number=0;
    int G=0;
    int H=0;
    int F=0;
    Color c;


    public GraphInfo(float x, float y,int num) {
        X = x;
        Y = y;
        number = num;
        c = Color.WHITE;
    }


    public float getX() {
        return X;
    }

    public float getY() {
        return Y;
    }

    public boolean isBlock() {
        return isBlock;
    }

    public void setY(float y) {
        Y = y;
    }

    public void setX(float x) {
        X = x;
    }

    public int getParentNode() {
        return parentNode;
    }

    public void setParentNode(int p){
        this.parentNode=p;
    }

    public boolean isOpened() {
        return opened;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void setBlock(boolean block) {
        isBlock = block;
    }

    public void colorSet(Color color){
        this.c = color;
    }

    @Override
    public int compareTo(GraphInfo graphInfo) {
        return this.F - graphInfo.F;
    }
}
