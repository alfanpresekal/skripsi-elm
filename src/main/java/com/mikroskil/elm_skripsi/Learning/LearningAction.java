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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Scanner;
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
    ConfusionMatrix confMatrix;
    
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
        choosen = CollectionUtils.find(shareContainer.getShare(), new Predicate<Share>(){
           public boolean evaluate(Share t){
               return t.getKodeSaham().equalsIgnoreCase(kodeSaham);
                       
           } 
        });
        choosen.readRecords(Share.REAL);
        choosen.readRecords(Share.NORM);
        choosen.setHighestLowest();
        setTrainSetBeginMonth();
    }
    
    public void setTrainSetBeginMonth(){
        getOwner().getCmbBulanTrainAwal().setEnabled(true);
        getOwner().fillBulanAwalTrain(new MultiComboBoxModel(new ArrayList<String>(Arrays.asList(months))));
//        if(year.equalsIgnoreCase("-Pilih-")){
//            return;
//        }
//        trainSetEndYear = Integer.parseInt(year);
//        getOwner().getCmbBulanTrainAwal().setEnabled(true);
//        getOwner().fillBulanAwalTrain(new MultiComboBoxModel(new ArrayList<String>(Arrays.asList(months))));
    }
    
    public void setTrainSetBeginYear(String month){
        if(month.equalsIgnoreCase("-pilih-")){
            return;
        }
        getOwner().getCmbTahunTrainAwal().setEnabled(true);
        trainSetStartMonth = getIntegerBulan(month);
        
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
    
    public void setTrainSetEndMonth(String year){
        if(year.equalsIgnoreCase("-pilih-")){
            return;
        }
        
        trainSetStartYear = Integer.parseInt(year);
        
        getOwner().getCmbBulanTrainAkhir().setEnabled(true);
        getOwner().fillBulanAkhirTrain(new MultiComboBoxModel(new ArrayList<String>(Arrays.asList(months))));
    }
    
    public void setTrainSetEndYear(String month){
        if(month.equalsIgnoreCase("-Pilih-")){
            return;
        }
        
        getOwner().getCmbTahunTrainAkhir().setEnabled(true);
        trainSetEndMonth = getIntegerBulan(month);
        
        Calendar temp = Calendar.getInstance();
        int startYear = trainSetStartYear;
        temp.setTime(choosen.getEndDate());
        int endYear = temp.get(Calendar.YEAR);
        
        ArrayList<String> years = new ArrayList<String>();
        for(int i=startYear; i<endYear;i++)
            years.add(String.format("%d",i));
        
        years.add(0,"-Pilih-");
        getOwner().fillTahunAkhirTrain(new MultiComboBoxModel(years));
    }
   
    public void setEndYear(String year){
        if(year.equalsIgnoreCase("-pilih-")){
            return;
        }
        trainSetEndYear = Integer.parseInt(year);
    }
    
    public int getIntegerBulan(String month){
        for(int i=0;i<months.length;i++){
            if(months[i].equalsIgnoreCase(month))
                return i;
        }
        return 0;
    }
    
    public void setTrainingTime(float time){
        ((LearningPage)getOwner()).setLabelWaktu(getTimeRender(time));
    }
    
    public String getTimeRender(float time){
        int index=0;
        if(time>=60){
            while(true){
                float temp = time/60;
                if(temp>=1){
                    time = temp;
                    index++;
                }
                else
                    break;
            }
        }
        switch(index){
            case 0 :
                return String.format("%.2f "+((time==1)?"Second":"Seconds"),time);
            case 1 :
                return String.format("%.2f "+((time==1)?"Minute":"Minutes"),time);
            case 2 :
                return String.format("%.2f "+((time==1)?"Hour":"Hours"),time);
            default :
                return "Invalid";
        }
    }
    
    public void doLearning(){
        File seedFile = new File("./config/seed.txt");
        File seedList = new File("./config/seedList.txt");
        fetchData();
        if(seedFile.exists() && seedList.exists()){
            try{
                Scanner scanner = new Scanner(seedFile);
                int seed = 1;
                while(scanner.hasNext()){
                    seed = scanner.nextInt();
                }
                confMatrix = new ConfusionMatrix();
                myLearningMachine = new LearningMachine(0,seed,"sig");
                learning();
                setTrainingTime(myLearningMachine.getTrainingTime());
                LearningMachineContainer container = LearningMachineContainer.getInstance();
                LearningDataResult result = CollectionUtils.find(container.getLearningDataResult(),
                    new Predicate<LearningDataResult>(){

                        public boolean evaluate(LearningDataResult t) {
                            return t.getKodeSaham().equalsIgnoreCase(choosen.getKodeSaham());
                        }

                    }
                );
                if(result != null){
                    container.getLearningDataResult().remove(result);
                }
                container.getLearningDataResult().add(new LearningDataResult(choosen.getKodeSaham(), myLearningMachine,trainSetEndYear));
                JOptionPane.showMessageDialog(this.owner,"Learning berhasil dilakukan");
            }
            catch(FileNotFoundException e){
                
            }
        }
        else{
            for(int i=0;i<1000;i++){
                confMatrix = new ConfusionMatrix();
                myLearningMachine = new LearningMachine(0, i, "sig");
                learning();
                getOuput();
                LearningResults.add(new LearningResult(i,confMatrix.getAccuracy()));
            }
            LearningResult max;
            max = Collections.max(LearningResults,new Comparator<LearningResult>(){
                public int compare(LearningResult obj1, LearningResult obj2) {
                    return Double.compare(obj1.getAccuracy(), obj2.getAccuracy());
                }
            });
            
            LearningMachineContainer container = LearningMachineContainer.getInstance();
            LearningDataResult result = CollectionUtils.find(container.getLearningDataResult(),
                new Predicate<LearningDataResult>(){

                    public boolean evaluate(LearningDataResult t) {
                        return t.getKodeSaham().equalsIgnoreCase(choosen.getKodeSaham());
                    }

                }
            );
            if(result != null){
                container.getLearningDataResult().remove(result);
            }
            container.getLearningDataResult().add(new LearningDataResult(choosen.getKodeSaham(), myLearningMachine,trainSetEndYear));
            JOptionPane.showMessageDialog(this.owner,"Learning berhasil dilakukan");
            
            FileWriter fw;
            try{
                seedFile.createNewFile();
                fw = new FileWriter(seedFile);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(String.format("%d", max.getSeed()));
                bw.close();
            }
            catch(IOException e){

            }

            try{
                seedList.createNewFile();
                fw = new FileWriter(seedList);
                BufferedWriter bw = new BufferedWriter(fw);
                for(LearningResult temp : LearningResults){
                    bw.write(temp.getSeed()+","+temp.getAccuracy());
                    bw.newLine();
                }
                bw.close();
            }
            catch(IOException e){

            }
        }
    }
    
    public void fetchData(){
        ArrayList<Record> temp;
        temp = new ArrayList<Record>(CollectionUtils.select(choosen.getRecords(),new Predicate<Record>(){

            public boolean evaluate(Record t) {
                Calendar temp = Calendar.getInstance();
                temp.setTime(t.getDate());
                
                Calendar startDate = Calendar.getInstance();
                startDate.set(trainSetStartYear, trainSetStartMonth-1, 0);
                
                Calendar endDate = Calendar.getInstance();
                endDate.set(trainSetEndYear, trainSetEndMonth-1,31);
                
                return (temp.compareTo(startDate) >= 0 && temp.compareTo(endDate) <= 0);
            }       
        }));
        train_set.setRecords(temp);
        temp = new ArrayList<Record>(CollectionUtils.select(choosen.getNormRecords(),new Predicate<Record>(){

            public boolean evaluate(Record t) {
                Calendar temp = Calendar.getInstance();
                temp.setTime(t.getDate());
                
                Calendar startDate = Calendar.getInstance();
                startDate.set(trainSetStartYear, trainSetStartMonth-1, 0);
                
                Calendar endDate = Calendar.getInstance();
                endDate.set(trainSetEndYear, trainSetEndMonth-1,31);
                
                return (temp.compareTo(startDate) >= 0 && temp.compareTo(endDate) <= 0);            }       
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
