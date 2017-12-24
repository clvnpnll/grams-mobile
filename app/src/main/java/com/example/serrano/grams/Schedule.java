package com.example.serrano.grams;

/**
 * Created by Paolo on 2/9/2017.
 */

public class Schedule {

    private String chkName, chkStart, chkEnd, chkStatus;

    public Schedule (String chkName, String chkStart, String chkEnd, String chkStatus){
        this.setChkName(chkName);
        this.setChkStart(chkStart);
        this.setChkEnd(chkEnd);
        this.setChkStatus(chkStatus);
    }

    public String getChkName() {
        return chkName;
    }

    public void setChkName(String chkName) {
        this.chkName = chkName;
    }

    public String getChkStart() {
        return chkStart;
    }

    public void setChkStart(String chkStart) {
        this.chkStart = chkStart;
    }

    public String getChkEnd() {
        return chkEnd;
    }

    public void setChkEnd(String chkEnd) {
        this.chkEnd = chkEnd;
    }

    public String getChkStatus() {
        return chkStatus;
    }

    public void setChkStatus(String chkStatus) {
        this.chkStatus = chkStatus;
    }
}
