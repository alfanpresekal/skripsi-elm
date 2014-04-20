/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.Learning;

import com.mikroskil.elm_skripsi.Library.LearningMachine;
import com.mikroskil.elm_skripsi.model.Record;
import com.mikroskil.elm_skripsi.model.Result;
import com.mikroskil.elm_skripsi.model.Share;
import com.mikroskil.elm_skripsi.model.ShareContainer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JDialog;
import javax.swing.table.AbstractTableModel;
import no.uib.cipr.matrix.NotConvergedException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import com.mikroskil.elm_skripsi.Library.ConfusionMatrix;

/**
 *
 * @author Andy Wijaya
 */
public class LearningAndTestingAction {
    LearningMachine myLearningMachine = new LearningMachine(0,20,"sig");
    ArrayList<Result> showResult;
    ArrayList<Result> realResult;
    double[] results,results_denorm;
    ShareContainer shareContainer = new ShareContainer();
    Share choosen,train_set = new Share("train_set",""),test_set = new Share("test_set","");
    JDialog owner;
    int trainSetStartYear,trainSetEndYear,testSetStartYear,testSetEndYear;
    int trainsetStartMonth,trainsetEndMonth,testSetStartMonth,testSetEndMonth;
    ConfusionMatrix confMatrix = new ConfusionMatrix();
    private final String[] months = {
        "-Pilih-","Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember"
    };
   
    public LearningAndTestingAction(JDialog owner){
        this.showResult = new ArrayList<Result>();
        this.realResult = new ArrayList<Result>();
        this.owner = owner;
    }
        
    public void initialize(){
        fetchKodeSahams();
    }
    
    public void fetchKodeSahams(){
        shareContainer.readShares();
        shareContainer.getShare().add(0,new Share("-Pilih-",""));
        fillKodeSaham();
    }
    
    public void fillKodeSaham(){
        ((LearningAndTesting)getOwner()).fillKodeSaham(new KodeSahamModel());
    }
    
    public void doLearningAndTesting(){
        fetchData();
        trainData();
        getOutput();
        ((LearningAndTesting)owner).setPresisiAndRecall(confMatrix.getPresisiNaik(), confMatrix.getPresisiTetap(), confMatrix.getPresisiTurun(), confMatrix.getRecallNaik(), confMatrix.getRecallTetap(), confMatrix.getRecallTurun());
        ((LearningAndTesting)owner).setAccuracy(confMatrix.getAccuracy());
    }
    
    public void trainData(){
        try {
            myLearningMachine.train(train_set.getNormRecords());
        } catch (NotConvergedException ex) {
            Logger.getLogger(LearningAndTestingAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void getOutput(){
        results = myLearningMachine.testOut(test_set.getNormRecords());
        deNormalizeData();
        fillTable();
    }
    
    public void deNormalizeData(){
        results_denorm = new double[results.length];
        choosen.setHighestLowest();
        double highestClose = choosen.getHighestClose(), lowestClose = choosen.getLowestClose();
        for(int i=0;i<results.length;i++){
            results_denorm[i] = 0.5*(results[i]+1)*(highestClose-lowestClose)+lowestClose;
        }
    }
    
    public void fillTable(){
        fetchResult();
        ((LearningAndTesting)owner).fillResultTable(new ResultTableModel());
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
    
    public void fetchData(){
        ArrayList<Record> temp;
        temp = new ArrayList<Record>(CollectionUtils.select(choosen.getRecords(),new Predicate<Record>(){

            public boolean evaluate(Record t) {
                Calendar temp = Calendar.getInstance();
                temp.setTime(t.getDate());
                if(temp.get(Calendar.YEAR)>=trainSetStartYear && temp.get(Calendar.YEAR)<=trainSetEndYear)
                    if(temp.get(Calendar.MONTH)>= trainsetStartMonth-1 && temp.get(Calendar.MONTH)<=trainsetEndMonth-1)
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
                    if(temp.get(Calendar.MONTH)>= trainsetStartMonth-1 && temp.get(Calendar.MONTH)<=trainsetEndMonth-1)
                        return true;
                return false;
            }       
        }));
        train_set.setNormRecords(temp);
        
        temp = new ArrayList<Record>(CollectionUtils.select(choosen.getRecords(),new Predicate<Record>(){

            public boolean evaluate(Record t) {
                Calendar temp = Calendar.getInstance();
                temp.setTime(t.getDate());
                if(temp.get(Calendar.YEAR)>=testSetStartYear && temp.get(Calendar.YEAR)<=testSetEndYear)
                    if(temp.get(Calendar.MONTH)>= testSetStartMonth-1 && temp.get(Calendar.MONTH)<=testSetEndMonth-1)
                        return true;
                return false;
            }       
        }));
        test_set.setRecords(temp);
        temp = new ArrayList<Record>(CollectionUtils.select(choosen.getNormRecords(),new Predicate<Record>(){

            public boolean evaluate(Record t) {
                Calendar temp = Calendar.getInstance();
                temp.setTime(t.getDate());
                if(temp.get(Calendar.YEAR)>=testSetStartYear && temp.get(Calendar.YEAR)<=testSetEndYear)
                    if(temp.get(Calendar.MONTH)>= testSetStartMonth-1 && temp.get(Calendar.MONTH)<=testSetEndMonth-1)
                        return true;
                return false;
            }       
        }));
        test_set.setNormRecords(temp);
    }
    
    public JDialog getOwner() {
        return owner;
    }

    public void setOwner(JDialog owner) {
        this.owner = owner;
    }

    public void setChoosen(final String kodeSaham) {
        if(kodeSaham.equalsIgnoreCase("-pilih-")){
            ((LearningAndTesting)getOwner()).getCmbTahunTrainAwal().setEnabled(false);
            ((LearningAndTesting)getOwner()).getCmbTahunTrainAkhir().setEnabled(false);
            ((LearningAndTesting)getOwner()).getCmbTahunTestAwal().setEnabled(false);
            ((LearningAndTesting)getOwner()).getCmbTahunTestAkhir().setEnabled(false);
            return;
        }
        ((LearningAndTesting)getOwner()).getCmbTahunTrainAwal().setEnabled(true);
        choosen = CollectionUtils.find(shareContainer.getShare(),new Predicate<Share>(){

            public boolean evaluate(Share t) {
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
        ((LearningAndTesting)getOwner()).fillTahunAwalTrain(new MultiComboBoxModel(years));
    }

    public void setEndYearTrain(String year) {
        if(year.equalsIgnoreCase("-pilih-")){
            ((LearningAndTesting)getOwner()).getCmbTahunTrainAkhir().setEnabled(false);
            ((LearningAndTesting)getOwner()).getCmbTahunTestAwal().setEnabled(false);
            ((LearningAndTesting)getOwner()).getCmbTahunTestAkhir().setEnabled(false);
            return;
        }
        ((LearningAndTesting)getOwner()).getCmbTahunTrainAkhir().setEnabled(true);
        trainSetStartYear = Integer.parseInt(year);
        
        Calendar temp = Calendar.getInstance();
        int startYear = Integer.parseInt(year);
        temp.setTime(choosen.getEndDate());
        int endYear = temp.get(Calendar.YEAR);
        
        ArrayList<String> years = new ArrayList<String>();
        for(int i=startYear;i<endYear;i++)
            years.add(String.format("%d",i));
        years.add(0,"-Pilih-");
        ((LearningAndTesting)getOwner()).fillTahunAkhirTrain(new MultiComboBoxModel(years));
    }
    
    public void setStartYearTest(String year){
        if(year.equalsIgnoreCase("-pilih-")){
            ((LearningAndTesting)getOwner()).getCmbTahunTestAwal().setEnabled(false);
            ((LearningAndTesting)getOwner()).getCmbTahunTestAkhir().setEnabled(false);
            return;
        }
        ((LearningAndTesting)getOwner()).getCmbTahunTestAwal().setEnabled(true);
        trainSetEndYear = Integer.parseInt(year);
        
        Calendar temp = Calendar.getInstance();
        int startYear = Integer.parseInt(year)+1;
        temp.setTime(choosen.getEndDate());
        int endYear = temp.get(Calendar.YEAR);
        
        ArrayList<String> years = new ArrayList<String>();
        for(int i=startYear;i<=endYear;i++)
            years.add(String.format("%d",i));
        years.add(0,"-Pilih-");
        ((LearningAndTesting)getOwner()).fillTahunAwalTest(new MultiComboBoxModel(years));
    }
    
    public void setEndYearTest(String year){
        if(year.equalsIgnoreCase("-pilih-")){
            ((LearningAndTesting)getOwner()).getCmbTahunTestAkhir().setEnabled(false);
            return;
        }
        ((LearningAndTesting)getOwner()).getCmbTahunTestAkhir().setEnabled(true);
        testSetStartYear = Integer.parseInt(year);
        
        Calendar temp = Calendar.getInstance();
        int startYear = Integer.parseInt(year);
        temp.setTime(choosen.getEndDate());
        int endYear = temp.get(Calendar.YEAR);
        
        ArrayList<String> years = new ArrayList<String>();
        for(int i=startYear;i<=endYear;i++)
            years.add(String.format("%d",i));
        years.add(0,"-Pilih-");
        ((LearningAndTesting)getOwner()).fillTahunAkhirTest(new MultiComboBoxModel(years));
    }
    
    public void setEndYear(String year){
        if(year.equalsIgnoreCase("-pilih-"))
            return;
        testSetEndYear = Integer.parseInt(year);
        ((LearningAndTesting)getOwner()).getCmbBulanTrainAwal().setEnabled(true);
        
        ((LearningAndTesting)getOwner()).fillBulanAwalTrain(new MultiComboBoxModel(new ArrayList<String>(Arrays.asList(months))));
    }
    
    public void setEndMonthTrain(String month){
        if(month.equalsIgnoreCase("-pilih-"))
            return;
        
        int index = trainsetStartMonth = getIntegerBulan(month);
        ((LearningAndTesting)getOwner()).getCmbBulanTrainAkhir().setEnabled(true);
        ArrayList<String> temp = new ArrayList<String>();
        for(int i = index; i<months.length;i++){
            temp.add(months[i]);
        }
        temp.add(0,"-pilih-");
        ((LearningAndTesting)getOwner()).fillBulanAkhirTrain(new MultiComboBoxModel(temp));
    }
    
    public void setStartMonthTest(String month){
        if(month.equalsIgnoreCase("-pilih-"))
            return;
        
        trainsetEndMonth = getIntegerBulan(month);
        ((LearningAndTesting)getOwner()).getCmbBulanTestAwal().setEnabled(true);
        
        ((LearningAndTesting)getOwner()).fillBulanAwalTest(new MultiComboBoxModel(new ArrayList<String>(Arrays.asList(months))));
    }
    
    public void setEndMonthTest(String month){
        if(month.equalsIgnoreCase("-pilih-"))
            return;
        
        int index = testSetStartMonth = getIntegerBulan(month);
        ((LearningAndTesting)getOwner()).getCmbBulanTestAkhir().setEnabled(true);
        ArrayList<String> temp = new ArrayList<String>();
        for(int i = index; i<months.length;i++){
            temp.add(months[i]);
        }
        temp.add(0,"-pilih-");
        ((LearningAndTesting)getOwner()).fillBulanAkhirTest(new MultiComboBoxModel(temp));
    }
    
    public void setEndMonth(String month){
        if(month.equalsIgnoreCase("-pilih-"))
            return;
        
        testSetEndMonth = getIntegerBulan(month);
    }
    
    public int getIntegerBulan(String month){
        for(int i=0;i<months.length;i++){
            if(months[i].equalsIgnoreCase(month))
                return i;
        }
        return 0;
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
    private class ResultTableModel extends AbstractTableModel{

        public int getRowCount() {
            return showResult.size();
        }

        public int getColumnCount() {
            return 3;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Result temp = showResult.get(rowIndex);
            Result real = realResult.get(rowIndex);
            switch(columnIndex)
            {
                case 0 : return temp.getDate();
                case 1 : return temp.getResult();
                case 2 : return real.getResult();
                default : return "invalid index";
            }
        }
        
    }
}
