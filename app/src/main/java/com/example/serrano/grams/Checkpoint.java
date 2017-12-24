package com.example.serrano.grams;

/**
 * Created by Paolo on 3/4/2017.
 */

public class Checkpoint {

    private String chkName;
    private int chkX, chkY;
    private int tapX, tapY;
    private int screenWidth, screenHeight;

    public Checkpoint(String chkName, int chkX, int chkY){
    this.chkName = chkName;
        this.chkX = chkX;
        this.chkY = chkY;
    }

    public String getChkName() {
        return chkName;
    }

    public void setChkName(String chkName) {
        this.chkName = chkName;
    }

    public int getChkX() {
        return chkX;
    }

    public void setChkX(int chkX) {
        this.chkX = chkX;
    }

    public int getChkY() {
        return chkY;
    }

    public void setChkY(int chkY) {
        this.chkY = chkY;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public void setTapX(int chkX) {
        this.tapX = chkX;
    }

    public void setTapY(int chkY) {
        this.tapY = chkY;
    }

    public int getTapX() {
        return this.tapX;
    }

    public int getTapY() {
        return this.tapY;
    }
}
