/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Andy Wijaya
 */
public final class Record {
    private Date date;
    private double open,high,low,close,ema5,ema10,ema20;

    public Record() {
    }
    
    /**
     *
     * @param date
     * @param open
     * @param high
     * @param low
     * @param close
     * @throws java.lang.Exception
     */
    public Record(String date, String open, String high, String low, String close) throws Exception {
        setDate(date);
        setOpen(open);
        setHigh(high);
        setLow(low);
        setClose(close);
    }
    
    public Record(String[] records) throws Exception{
        if(records.length==8){
            setDate(records[0]);
            setOpen(records[1]);
            setHigh(records[2]);
            setLow(records[3]);
            setClose(records[4]);
            setEma5(records[5]);
            setEma10(records[6]);
            setEma20(records[7]);
        }
    }
    
    public Record(Date date, double open, double high, double low, double close, double ema5, double ema10, double ema20) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.ema5 = ema5;
        this.ema10 = ema10;
        this.ema20 = ema20;
    }
    
    
    
    public Date getDate() {
        return date;
    }

    public void setDate(String date) throws Exception {
        try{
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            this.date = formatter.parse(date);
        }
        catch(ParseException e){
            throw new Exception("Error in converting Date.");
        }
    }

    public double getOpen() {
        return open;
    }
    
    
    public void setOpen(String open) throws Exception {
        try{
            this.open = Double.parseDouble(open);
        }
        catch(NumberFormatException e){
            throw new Exception("Error in converting Open.");
        }
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(String high) throws Exception {
        try{
            this.high = Double.parseDouble(high);
        }
        catch(NumberFormatException e){
            throw new Exception("Error in converting High.");
        }
    }

    public double getLow() {
        return low;
    }

    public void setLow(String low) throws Exception {
        try{
            this.low = Double.parseDouble(low);
        }
        catch(NumberFormatException e){
            throw new Exception("Error in converting Low.");
        }
    }

    public double getClose() {
        return close;
    }

    public void setClose(String close) throws Exception {
        try{
            this.close = Double.parseDouble(close);
        }
        catch(NumberFormatException e){
            throw new Exception("Error in converting Close.");
        }
    }

    public double getEma5() {
        return ema5;
    }

    public void setEma5(double ema5) {
        this.ema5 = ema5;
    }
    
    public void setEma5(String ema5) throws Exception{
        try{
            this.ema5 = Double.parseDouble(ema5);
        }
        catch(NumberFormatException e){
            throw new Exception("Error in converting Close.");
        }
    }

    public double getEma10() {
        return ema10;
    }

    public void setEma10(double ema10) {
        this.ema10 = ema10;
    }
    
    public void setEma10(String ema10) throws Exception{
        try{
            this.ema10 = Double.parseDouble(ema10);
        }
        catch(NumberFormatException e){
            throw new Exception("Error in converting Close.");
        }
    }

    public double getEma20() {
        return ema20;
    }

    public void setEma20(double ema20) {
        this.ema20 = ema20;
    }
    
    public void setEma20(String ema20) throws Exception{
        try{
            this.ema20 = Double.parseDouble(ema20);
        }
        catch(NumberFormatException e){
            throw new Exception("Error in converting Close.");
        }
    }
    
    public String[] toArray(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        return new String[]{
            formatter.format(getDate()),
            String.format("%.6f",getOpen()),
            String.format("%.6f",getHigh()),
            String.format("%.6f",getLow()),
            String.format("%.6f",getClose()),
            String.format("%.6f",getEma5()),
            String.format("%.6f",getEma10()),
            String.format("%.6f",getEma20())
        };
    }
    
    public String[] toArrayWithoutDate(){
        return new String[]{
            String.format("%.6f",getOpen()),
            String.format("%.6f",getHigh()),
            String.format("%.6f",getLow()),
            String.format("%.6f",getClose()),
            String.format("%.6f",getEma5()),
            String.format("%.6f",getEma10()),
            String.format("%.6f",getEma20())
        };
    }
}
