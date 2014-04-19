/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Andy Wijaya
 */
public class Result {
    private Date date;
    private int result;
    private double real,predicted;
    public static final int NAIK = 1,TURUN = -1,TETAP =0;

    public Result(Date date, int result,double real,double predicted) {
        this.date = date;
        this.result = result;
        this.real = real;
        this.predicted = predicted;
    }

    public String getDate() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        return format.format(date);
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getResult() {
        switch(result)
        {
            case 1 : return "Naik";
            case -1 : return "Turun";
            case 0 : return "Tetap";
            default : return "Invalid";
        }
    }
    
    public int getRealResult(){
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
    
    public double getReal() {
        return real;
    }

    public void setReal(double real) {
        this.real = real;
    }

    public double getPredicted() {
        return predicted;
    }

    public void setPredicted(double predicted) {
        this.predicted = predicted;
    }
    
}
