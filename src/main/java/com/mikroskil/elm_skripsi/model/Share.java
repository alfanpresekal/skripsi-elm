/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.model;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 *
 * @author Andy Wijaya
 */
public class Share {
    public static int NORM=0,REAL=1;
    private ArrayList<Record> records = new ArrayList<Record>();
    private ArrayList<Record> norm_records = new ArrayList<Record>();
    private String kodeSaham,namaSaham;
    private int jumlahData;
    private Date startDate,endDate;
    private double highestHigh,lowestHigh,highestOpen,lowestOpen,highestClose,lowestClose,highestLow,lowestLow,lowestEMA5,lowestEMA10,lowestEMA20,highestEMA5,highestEMA10,highestEMA20;

   
    public Share(String kodeSaham, String namaSaham) {
        this.kodeSaham = kodeSaham;
        this.namaSaham = namaSaham;
    }
    
    public Share(){}
    
    /** This function is to save to CVS File **/
    private void saveRecords(String fileName, ArrayList<Record> records){
        try{
            String[] header = {
                "Date","Open","High","Low","Close","EMA5","EMA10","EMA20"
            };
            
            File directory = new File("./csv");
            if(!directory.exists())
                directory.mkdir();
            
            File csvFile = new File("./csv/"+fileName+".csv");
            if(csvFile.exists())csvFile.delete();
            CSVWriter writer = null;
            writer = new CSVWriter(new FileWriter(csvFile));
            writer.writeNext(header);
            for(Record r: records)
                writer.writeNext(r.toArray());
            writer.close();
        }
        catch(IOException e){
            return;
        }
    }
    
    public void saveRecords(int mode){
        if(mode == NORM)
            saveRecords(getKodeSaham()+"-norm", norm_records);        
        else
            saveRecords(getKodeSaham(), records);
    }
    
    public void readRecords(int mode){
        if(mode == NORM)
            readRecords(getKodeSaham()+"-norm",norm_records);
        else
            readRecords(getKodeSaham(),records);
    }
    
    /** This function is to fill records with something in file **/
    private void readRecords(String fileName,ArrayList<Record> storeTo){
        File csvFile = new File("./csv/"+fileName+".csv");
        CSVReader reader = null;
        try {
            reader = new CSVReader(new FileReader(csvFile));
        } catch (FileNotFoundException ex) {
        }
        List<String[]> temp;
        if(csvFile.exists()){
            try {
                temp = reader.readAll();
                
            } catch (IOException ex) {
                return;
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
            
            for(String[] list : temp){
                try {
                    Record r = new Record(formatter.parse(list[0]),Double.parseDouble(list[1]),Double.parseDouble(list[2]), Double.parseDouble(list[3]),Double.parseDouble(list[4]),Double.parseDouble(list[5]),Double.parseDouble(list[6]),Double.parseDouble(list[7]));
                    storeTo.add(r);
                } catch (ParseException ex) {
                    
                }
            }
        }
    }
    
    public void deleteRecords(){
        File real = new File("./csv/"+getKodeSaham()+".csv");
        if(real.exists())real.delete();
        File norm = new File("./csv/"+getKodeSaham()+"-norm.csv");
        if(norm.exists())norm.delete();
    }
    
    public void setHighestLowest(){
        Collections.sort(getRecords(),new Comparator<Record>(){

            public int compare(Record o1, Record o2) {
                return Double.compare(o1.getOpen(),o2.getOpen());
            }
            
        });
        setLowestOpen(getRecords().get(0).getOpen());
        setHighestOpen(getRecords().get(getRecords().size()-1).getOpen());
        
        Collections.sort(getRecords(),new Comparator<Record>(){

            public int compare(Record o1, Record o2) {
                return Double.compare(o1.getHigh(),o2.getHigh());
            }
            
        });
        setLowestHigh(getRecords().get(0).getHigh());
        setHighestHigh(getRecords().get(getRecords().size()-1).getHigh());
        
        Collections.sort(getRecords(),new Comparator<Record>(){

            public int compare(Record o1, Record o2) {
                return Double.compare(o1.getLow(),o2.getLow());
            }
            
        });
        setLowestLow(getRecords().get(0).getLow());
        setHighestLow(getRecords().get(getRecords().size()-1).getLow());
        
        Collections.sort(getRecords(),new Comparator<Record>(){

            public int compare(Record o1, Record o2) {
                return Double.compare(o1.getClose(),o2.getClose());
            }
            
        });
        setLowestClose(getRecords().get(0).getClose());
        setHighestClose(getRecords().get(getRecords().size()-1).getClose());
        
        Collections.sort(getRecords(),new Comparator<Record>(){

            public int compare(Record o1, Record o2) {
                return Double.compare(o1.getEma5(),o2.getEma5());
            }
            
        });
        setLowestEMA5(getRecords().get(0).getEma5());
        setHighestEMA5(getRecords().get(getRecords().size()-1).getClose());
        
        Collections.sort(getRecords(),new Comparator<Record>(){

            public int compare(Record o1, Record o2) {
                return Double.compare(o1.getEma10(),o2.getEma10());
            }
            
        });
        setLowestEMA10(getRecords().get(0).getEma10());
        setHighestEMA10(getRecords().get(getRecords().size()-1).getEma10());
        
        Collections.sort(getRecords(),new Comparator<Record>(){

            public int compare(Record o1, Record o2) {
                return Double.compare(o1.getEma20(),o2.getEma20());
            }
            
        });
        setLowestEMA20(getRecords().get(0).getEma20());
        setHighestEMA20(getRecords().get(getRecords().size()-1).getEma20());
        
        Collections.sort(getRecords(),new Comparator<Record>(){

            public int compare(Record o1, Record o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
            
        });
        setStartDate(getRecords().get(0).getDate());
        setEndDate(getRecords().get(getRecords().size()-1).getDate());
    }
    
    public void setNormalizedData(){
        norm_records.clear();
        setHighestLowest();
        double openNorm = 0, highNorm = 0, lowNorm = 0, closeNorm =0, ema5Norm = 0, ema10Norm = 0, ema20Norm = 0;
        
        for(Record r : getRecords()){
            openNorm = (r.getOpen()-lowestOpen)/(highestOpen-lowestOpen);
            highNorm = (r.getHigh()-lowestHigh)/(highestHigh-lowestHigh);
            lowNorm  = (r.getLow()-lowestLow)/(highestLow-lowestLow);
            closeNorm = (r.getClose()-lowestClose)/(highestClose-lowestClose);
            ema5Norm = (r.getEma5()-lowestEMA5)/(highestEMA5-lowestEMA5);
            ema10Norm = (r.getEma10()-lowestEMA10)/(highestEMA10-lowestEMA10);
            ema20Norm = (r.getEma20()-lowestEMA20)/(highestEMA20-lowestEMA20);
            
            norm_records.add(new Record(r.getDate(), openNorm, highNorm, lowNorm, closeNorm, ema5Norm, ema10Norm, ema20Norm));
        }
    }
    
    public void countEMA5(){
        double movingAverage=0;
        for(int i=0;i<getRecords().size();i++){
            if(i<4){
                movingAverage+=getRecords().get(i).getClose();
                getRecords().get(i).setEma5(0);
                if(i==3)
                    movingAverage/=4.0;
            }
            else{
                double CONST = (2.0/(1+5));
                movingAverage = (CONST*(getRecords().get(i).getClose()-movingAverage))+movingAverage;
                getRecords().get(i).setEma5(movingAverage);
            }
        }
    }
    
    public void countEMA10(){
        double movingAverage=0;
        for(int i=0;i<getRecords().size();i++){
            if(i<9){
                movingAverage+=getRecords().get(i).getClose();
                getRecords().get(i).setEma10(0);
                if(i==8)
                    movingAverage/=9;
            }
            else{
                double CONST = (2.0/(1+10));
                movingAverage = (CONST*(getRecords().get(i).getClose()-movingAverage))+movingAverage;
                getRecords().get(i).setEma10(movingAverage);
            }
        }
    }
    
    public void countEMA20(){
        double movingAverage = 0;
        for(int i=0;i<getRecords().size();i++){
            if(i<19){
                movingAverage+=getRecords().get(i).getClose();
                getRecords().get(i).setEma20(0);
                if(i==18)
                    movingAverage/=19;
            }
            else{
                double CONST = (2.0/(1+20));
                movingAverage = (CONST*(getRecords().get(i).getClose()-movingAverage))+movingAverage;
                getRecords().get(i).setEma20(movingAverage);
            }
        }
    }
    
    public void countEMAN(int n){
        double movingAverage = 0;
        for(int i=0;i<getRecords().size();i++){
            if(i<n-1){
                movingAverage+=getRecords().get(i).getClose();
                getRecords().get(i).setEma20(0);
                if(i==n-1)
                    movingAverage/=19;
            }
            else{
                double CONST = (2.0/(1+n));
                movingAverage = (CONST*(getRecords().get(i).getClose()-movingAverage))+movingAverage;
                getRecords().get(i).setEma20(movingAverage);
            }
        }
    }
    
    public int combineRecord(ArrayList<Record> newRecords){
        int index;
        index=getRecords().size();
        getRecords().addAll(newRecords);        
        return index;
    }
    
    
    
    public String getKodeSaham() {
        return kodeSaham;
    }

    public void setKodeSaham(String kodeSaham) {
        this.kodeSaham = kodeSaham;
    }

    public String getNamaSaham() {
        return namaSaham;
    }

    public void setNamaSaham(String namaSaham) {
        this.namaSaham = namaSaham;
    }

    public ArrayList<Record> getRecords() {
        return records;
    }

    public void setRecords(ArrayList<Record> records) {
        this.records = records;
    }
    
    public int getJumlahData() {
        return jumlahData;
    }

    public void setJumlahData(int jumlahData) {
        this.jumlahData = jumlahData;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getHighestHigh() {
        return highestHigh;
    }

    public void setHighestHigh(double highestHigh) {
        this.highestHigh = highestHigh;
    }

    public double getLowestHigh() {
        return lowestHigh;
    }

    public void setLowestHigh(double lowestHigh) {
        this.lowestHigh = lowestHigh;
    }

    public double getHighestOpen() {
        return highestOpen;
    }

    public void setHighestOpen(double highestOpen) {
        this.highestOpen = highestOpen;
    }

    public double getLowestOpen() {
        return lowestOpen;
    }

    public void setLowestOpen(double lowestOpen) {
        this.lowestOpen = lowestOpen;
    }

    public double getHighestClose() {
        return highestClose;
    }

    public void setHighestClose(double highestClose) {
        this.highestClose = highestClose;
    }

    public double getLowestClose() {
        return lowestClose;
    }

    public void setLowestClose(double lowestClose) {
        this.lowestClose = lowestClose;
    }

    public double getHighestLow() {
        return highestLow;
    }

    public void setHighestLow(double highestLow) {
        this.highestLow = highestLow;
    }

    public double getLowestLow() {
        return lowestLow;
    }

    public void setLowestLow(double lowestLow) {
        this.lowestLow = lowestLow;
    }

    public double getLowestEMA5() {
        return lowestEMA5;
    }

    public void setLowestEMA5(double lowestEMA5) {
        this.lowestEMA5 = lowestEMA5;
    }

    public double getLowestEMA10() {
        return lowestEMA10;
    }

    public void setLowestEMA10(double lowestEMA10) {
        this.lowestEMA10 = lowestEMA10;
    }

    public double getLowestEMA20() {
        return lowestEMA20;
    }

    public void setLowestEMA20(double lowestEMA20) {
        this.lowestEMA20 = lowestEMA20;
    }

    public double getHighestEMA5() {
        return highestEMA5;
    }

    public void setHighestEMA5(double highestEMA5) {
        this.highestEMA5 = highestEMA5;
    }

    public double getHighestEMA10() {
        return highestEMA10;
    }

    public void setHighestEMA10(double highestEMA10) {
        this.highestEMA10 = highestEMA10;
    }

    public double getHighestEMA20() {
        return highestEMA20;
    }

    public void setHighestEMA20(double higgestEMA20) {
        this.highestEMA20 = higgestEMA20;
    }

    public ArrayList<Record> getNormRecords() {
        return norm_records;
    }
    
    public void setNormRecords(ArrayList<Record> records){
        this.norm_records = records;
    }
}
