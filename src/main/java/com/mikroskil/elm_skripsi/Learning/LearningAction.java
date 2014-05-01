/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.Learning;

import com.mikroskil.elm_skripsi.Library.ConfusionMatrix;
import com.mikroskil.elm_skripsi.Library.LearningMachine;
import com.mikroskil.elm_skripsi.model.LearningDataResult;
import com.mikroskil.elm_skripsi.model.Record;
import com.mikroskil.elm_skripsi.model.Result;
import com.mikroskil.elm_skripsi.model.Share;
import com.mikroskil.elm_skripsi.model.ShareContainer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JOptionPane;
import no.uib.cipr.matrix.NotConvergedException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

/**
 *
 * @author Andy Wijaya
 */
public class LearningAction {
    
    LearningPage owner;
    Share choosen;
    ArrayList<Result> showResult = new ArrayList<Result>();
    ArrayList<Result> realResult = new ArrayList<Result>();
    double[] results,results_denorm;
    ShareContainer shareContainer = new ShareContainer();
    private final String[] months = {
        "-Pilih-","Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember"
    };
    ConfusionMatrix confMatrix = new ConfusionMatrix();
    
    int trainSetStartYear,trainSetEndYear,trainSetStartMonth,trainSetEndMonth;
    Share train_set = new Share("train_set",""),test_set = new Share("test_set","");
    LearningMachine myLearningMachine;
    
    ArrayList<LearningResult> LearningResults = new ArrayList<LearningResult>();
    
    public LearningAction(LearningPage owner){
        this.owner = owner;
    }
    
    public void initialize(){
        fetchKodeSaham();
    }
    
    public void fetchKodeSaham(){
        shareContainer.readShares();
        shareContainer.getShare().add(0,new Share("-Pilih-",""));
        fillKodeSaham();
    }
    
    public void fillKodeSaham(){
        getOwner().fillKodeSaham(new KodeSahamModel());
    }
    
    public LearningPage getOwner(){
        return owner;
    }
    
    public void setChoosen(final String kodeSaham){
        if(kodeSaham.equalsIgnoreCase("-Pilih-")){  
            return;
        }
        getOwner().getCmbTahunTrainAwal().setEnabled(true);
        choosen = CollectionUtils.find(shareContainer.getShare(), new Predicate<Share>(){
           public boolean evaluate(Share t){
               return t.getKodeSaham().equalsIgnoreCase(kodeSaham);
                       
           } 
        });
        choosen.readRecords(Share.REAL);
        choosen.readRecords(Share.NORM);
        choosen.setHighestLowest();
        setTrainSetBeginYear();
    }
    
    public void setTrainSetBeginYear(){
        Calendar temp = Calendar.getInstance();
        temp.setTime(choosen.getStartDate());
        int startYear = temp.get(Calendar.YEAR);
        temp.setTime(choosen.getEndDate());
        int endYear = temp.get(Calendar.YEAR);
        
        ArrayList<String> years = new ArrayList<String>();
        
        for(int i=startYear;i<endYear-1;i++)
            years.add(String.format("%d",i));
        years.add(0,"-Pilih-");
        getOwner().fillTahunAwalTrain(new MultiComboBoxModel(years));
    }
    
    public void setTrainSetEndYear(String year){
        if(year.equalsIgnoreCase("-Pilih-")){
            return;
        }
        getOwner().getCmbTahunTrainAkhir().setEnabled(true);
        trainSetStartYear = Integer.parseInt(year);
        
        Calendar temp = Calendar.getInstance();
        int startYear = Integer.parseInt(year);
        temp.setTime(choosen.getEndDate());
        int endYear = temp.get(Calendar.YEAR);
        
        ArrayList<String> years = new ArrayList<String>();
        for(int i=startYear; i<endYear;i++)
            years.add(String.format("%d",i));
        
        years.add(0,"-Pilih-");
        getOwner().fillTahunAkhirTrain(new MultiComboBoxModel(years));
    }
    
    public void setTrainSetBeginMonth(String year){
        if(year.equalsIgnoreCase("-Pilih-")){
            return;
        }
        trainSetEndYear = Integer.parseInt(year);
        getOwner().getCmbBulanTrainAwal().setEnabled(true);
        getOwner().fillBulanAwalTrain(new MultiComboBoxModel(new ArrayList<String>(Arrays.asList(months))));
    }
    
    public void setTrainSetEndMonth(String month){
        if(month.equalsIgnoreCase("-pilih-")){
            return;
        }
        
        int index = trainSetStartMonth = getIntegerBulan(month);
        getOwner().getCmbBulanTrainAkhir().setEnabled(true);
        ArrayList<String> temp = new ArrayList<String>();
        for(int i=index; i<months.length;i++){
            temp.add(months[i]);
        }
        temp.add(0,"-Pilih-");
        getOwner().fillBulanAkhirTrain(new MultiComboBoxModel(temp));
    }
   
    public void setEndMonth(String month){
        if(month.equalsIgnoreCase("-pilih-")){
            return;
        }
        int index = trainSetEndMonth = getIntegerBulan(month);
        
    }
    
    public int getIntegerBulan(String month){
        for(int i=0;i<months.length;i++){
            if(months[i].equalsIgnoreCase(month))
                return i;
        }
        return 0;
    }
    
    public void doLearning(){
        File seedInputWeight = new File("./config/inputweight.txt");
        File seedBias = new File("./config/bias.txt");
        fetchData();
        if(seedInputWeight.exists() && seedBias.exists()){
            myLearningMachine = new LearningMachine(0,false,"sig");
            learning();
            LearningMachineContainer temp = LearningMachineContainer.getInstance();
            LearningDataResult result = CollectionUtils.find(temp.getLearningDataResult(), new Predicate<LearningDataResult>(){

                public boolean evaluate(LearningDataResult t) {
                    return t.getKodeSaham().equalsIgnoreCase(choosen.getKodeSaham());
                }
                
            });
            if(result!=null)
                temp.getLearningDataResult().remove(result);
                
            temp.getLearningDataResult().add(new LearningDataResult(choosen.getKodeSaham(),myLearningMachine,trainSetEndYear));
            JOptionPane.showMessageDialog(this.owner,"Learning Berhasil dilakukan");
        }
        else{
            myLearningMachine = new LearningMachine(0,true,"sig");
            for(int i=0;i<1000;i++){
                learning();
                getOuput();
                LearningResults.add(new LearningResult(myLearningMachine.getFirstInputWeight(),myLearningMachine.getFirstBiasofHiddenNeurons(),confMatrix.getAccuracy()));     
            }
            LearningResult max;
            max = Collections.max(LearningResults,new Comparator<LearningResult>(){
                
                public int compare(LearningResult obj1, LearningResult obj2) {
                    return Double.compare(obj1.getAccuracy(), obj2.getAccuracy());
                }
                
            });
            
            String contentInputWeight = "";
            for(int i=0;i<max.getInputWeight().getData().length;i++){
                if(i==0){
                    contentInputWeight += String.format("%f",max.getInputWeight().getData()[i]);
                }
                else{
                    contentInputWeight += ","+String.format("%f",max.getInputWeight().getData()[i]);
                }
            }
            
            
            FileWriter fw;
            try {
                seedInputWeight.createNewFile();
                fw = new FileWriter(seedInputWeight);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(contentInputWeight);
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(LearningAction.class.getName()).log(Level.SEVERE, null, ex);
            }
      
            String contentBias = "";
            for(int i=0;i<max.getBiasOfHiddenNeurons().getData().length;i++){
                if(i==0){
                    contentBias += String.format("%f",max.getBiasOfHiddenNeurons().getData()[i]);
                }
                else{
                    contentBias += ","+String.format("%f",max.getBiasOfHiddenNeurons().getData()[i]);
                }
            }
            
            try {
                seedBias.createNewFile();
                fw = new FileWriter(seedBias);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(contentBias);
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(LearningAction.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            JOptionPane.showMessageDialog(this.owner,max.getAccuracy());
        }
    }
    
    public void fetchData(){
        ArrayList<Record> temp;
        temp = new ArrayList<Record>(CollectionUtils.select(choosen.getRecords(),new Predicate<Record>(){

            public boolean evaluate(Record t) {
                Calendar temp = Calendar.getInstance();
                temp.setTime(t.getDate());
                if(temp.get(Calendar.YEAR)>=trainSetStartYear && temp.get(Calendar.YEAR)<=trainSetEndYear)
                    if(temp.get(Calendar.MONTH)>= trainSetStartMonth-1 && temp.get(Calendar.MONTH)<=trainSetEndMonth-1)
                        return true;
                return false;
            }       
        }));
        train_set.setRecords(temp);
        temp = new ArrayList<Record>(CollectionUtils.select(choosen.getNormRecords(),new Predicate<Record>(){

            public boolean evaluate(Record t) {
                Calendar temp = Calendar.getInstance();
                temp.setTime(t.getDate());
                if(temp.get(Calendar.YEAR)>=trainSetStartYear && temp.get(Calendar.YEAR)<=trainSetEndYear)
                    if(temp.get(Calendar.MONTH)>= trainSetStartMonth-1 && temp.get(Calendar.MONTH)<=trainSetEndMonth-1)
                        return true;
                return false;
            }       
        }));
        train_set.setNormRecords(temp);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(choosen.getEndDate());
        final int lastYear = cal.get(Calendar.YEAR);        
        temp = new ArrayList<Record>(CollectionUtils.select(choosen.getRecords(),new Predicate<Record>(){

            public boolean evaluate(Record t) {
                Calendar temp = Calendar.getInstance();
                temp.setTime(t.getDate());
                if(temp.get(Calendar.YEAR)>=lastYear && temp.get(Calendar.YEAR)<=lastYear)
                    if(temp.get(Calendar.MONTH)>= 1-1 && temp.get(Calendar.MONTH)<=12-1)
                        return true;
                return false;
            }       
        }));
        test_set.setRecords(temp);
        temp = new ArrayList<Record>(CollectionUtils.select(choosen.getNormRecords(),new Predicate<Record>(){

            public boolean evaluate(Record t) {
                Calendar temp = Calendar.getInstance();
                temp.setTime(t.getDate());
                if(temp.get(Calendar.YEAR)>=lastYear && temp.get(Calendar.YEAR)<=lastYear)
                    if(temp.get(Calendar.MONTH)>= 1-1 && temp.get(Calendar.MONTH)<=12-1)
                        return true;
                return false;
            }       
        }));
        test_set.setNormRecords(temp);
    }
    
    public void learning(){
        try{
            myLearningMachine.train(train_set.getNormRecords());
        }
        catch (NotConvergedException ex) {
            
        }
    }
    
    public void getOuput(){
        results = myLearningMachine.testOut(test_set.getNormRecords());
        deNormalizeData();
        fetchResult();
    }
    
    public void deNormalizeData(){
        results_denorm = new double[results.length];
        choosen.setHighestLowest();
        double highestClose = choosen.getHighestClose(), lowestClose = choosen.getLowestClose();
        for(int i=0;i<results.length;i++){
            results_denorm[i] = 0.5*(results[i]+1)*(highestClose-lowestClose)+lowestClose;
        }
    }
    
    private void fetchResult(){
        showResult.clear();
        realResult.clear();
        double temp;
        Date first = test_set.getRecords().get(0).getDate();
        temp = train_set.getRecords().get(train_set.getRecords().size()-1).getClose();
        for(int i=0;i<results_denorm.length;i++){
            Result r;
            if(temp==Math.round(results_denorm[i]))
                r = new Result(first,Result.TETAP,test_set.getRecords().get(i).getClose(),results_denorm[i]);
            else if(temp>results_denorm[i])
                r = new Result(first,Result.TURUN,test_set.getRecords().get(i).getClose(),results_denorm[i]);
            else 
                r = new Result(first, Result.NAIK,test_set.getRecords().get(i).getClose(),results_denorm[i]);
            showResult.add(r);
            final Date firstTemp = first;
            temp=results_denorm[i];
            try{
                first = test_set.getRecords().get(i+1).getDate();
            }
            catch(IndexOutOfBoundsException e){break;}
        }
        temp = train_set.getRecords().get(train_set.getRecords().size()-1).getClose();
        for(Record r : test_set.getRecords()){
            Result t;
            if(temp==r.getClose())
                t = new Result(r.getDate(),Result.TETAP,r.getClose(),r.getClose());
            else if(temp>r.getClose())
                t = new Result(r.getDate(),Result.TURUN,r.getClose(),r.getClose());
            else
                t = new Result(r.getDate(),Result.NAIK,r.getClose(),r.getClose());
            temp=r.getClose();
            realResult.add(t);
        }
        confMatrix.count(realResult, showResult);
    }
    
    private class KodeSahamModel extends AbstractListModel implements ComboBoxModel{
        
        String selection = shareContainer.getShare().get(0).getKodeSaham();
        
        public void setSelectedItem(Object anItem) {
            selection = (String)anItem;
        }

        public Object getSelectedItem() {
            return selection;
        }

        public int getSize() {
            return shareContainer.getShare().size();
        }

        public Object getElementAt(int index) {
            return shareContainer.getShare().get(index).getKodeSaham();
        }
    }
     private class MultiComboBoxModel extends AbstractListModel implements ComboBoxModel{
        ArrayList<String> Data;
        String selection;
        
        public MultiComboBoxModel(ArrayList<String> data){
            this.Data = data;
            selection = data.get(0);
        }

        public int getSize() {
            return Data.size();
        }

        public Object getElementAt(int index) {
            return Data.get(index);
        }

        public void setSelectedItem(Object anItem) {
            selection = (String)anItem;
        }

        public Object getSelectedItem() {
            return selection;
        }
        
    }
}
