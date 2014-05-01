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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

/**
 *
 * @author Andy Wijaya
 */
public class TestingAction {
    
    ArrayList<Result> showResult = new ArrayList<Result>();
    ArrayList<Result> realResult = new ArrayList<Result>();
    private ShareContainer shareContainer = new ShareContainer();
    private TestingPage owner;
    private Share choosen,test_set = new Share("test_set","");
    double[] results,results_denorm;
    private int testSetBeginYear, testSetEndYear, testSetBeginMonth, testSetEndMonth;
    private final String[] months = {
        "-Pilih-","Januari","Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","November","Desember"
    };
    private ConfusionMatrix confMatrix = new ConfusionMatrix();
    
    private LearningMachine myLearningMachine;
    
    public TestingAction(TestingPage owner){
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
        getOwner().fillCmbKodeSaham(new KodeSahamModel());
    }

    public TestingPage getOwner() {
        return owner;
    }
    
    public void setChoosen(final String kode){
        if(kode.equalsIgnoreCase("-pilih-")){
            return;
        }
        LearningMachineContainer temp = LearningMachineContainer.getInstance(); 
        LearningDataResult isExists = CollectionUtils.find(
                temp.getLearningDataResult(), 
                new Predicate<LearningDataResult>(){
                        public boolean evaluate(LearningDataResult t){
                            return t.getKodeSaham().equalsIgnoreCase(kode);
                        }
                }
        );
        if(isExists!=null){
            myLearningMachine = isExists.getLearningMachine();
            choosen = CollectionUtils.find(shareContainer.getShare(), new Predicate<Share>(){
               public boolean evaluate(Share t){
                   return t.getKodeSaham().equalsIgnoreCase(kode);

               } 
            });
            choosen.readRecords(Share.REAL);
            choosen.readRecords(Share.NORM);
            choosen.setHighestLowest();
            setTestSetBeginYear();
        }
        else{
            JOptionPane.showMessageDialog(this.owner,"Kode Saham Belum di learning");
        }
    }
    
    private void setTestSetBeginYear(){
        LearningMachineContainer temp = LearningMachineContainer.getInstance();
        LearningDataResult isExists = CollectionUtils.find(
                temp.getLearningDataResult(), 
                new Predicate<LearningDataResult>(){
                        public boolean evaluate(LearningDataResult t){
                            return t.getKodeSaham().equalsIgnoreCase(choosen.getKodeSaham());
                        }
                }
        );
        getOwner().getCmbTahunTestAwal().setEnabled(true);
        int startYear = isExists.getEndYear();
        
        Calendar tempCal = Calendar.getInstance();
        tempCal.setTime(choosen.getEndDate());
        
        int endYear = tempCal.get(Calendar.YEAR);
        
        ArrayList<String> years = new ArrayList<String>();
        
        for(int i=startYear;i<=endYear;i++)
            years.add(String.format("%d",i));
        years.add(0,"-Pilih-");
        getOwner().fillCmbTahunTestAwal(new MultiComboBoxModel(years));
    }
        
    public void setEndYearTest(String year){
        if(year.equalsIgnoreCase("-pilih-")){
            return;
        }
        getOwner().getCmbTahunTestAkhir().setEnabled(true);
        testSetBeginYear = Integer.parseInt(year);
        
        Calendar temp = Calendar.getInstance();
        int startYear = Integer.parseInt(year);
        temp.setTime(choosen.getEndDate());
        int endYear = temp.get(Calendar.YEAR);
        
        ArrayList<String> years = new ArrayList<String>();
        for(int i=startYear; i<=endYear;i++)
            years.add(String.format("%d",i));
        
        years.add(0,"-Pilih-");
        getOwner().fillCmbTahunTestAkhir(new MultiComboBoxModel(years));
        
    }
    
    public void setTestSetBeginMonth(String year){
        if(year.equalsIgnoreCase("-pilih-")){
            return;
        }
        testSetEndYear = Integer.parseInt(year);
        getOwner().getCmbBulanTestAwal().setEnabled(true);
        getOwner().fillCmbBulanTestAwal(new MultiComboBoxModel(new ArrayList<String>(Arrays.asList(months))));
    }
    
    public void setTestSetEndMonth(String month){
        if(month.equalsIgnoreCase("-pilih-")){
            return;
        }
        
        int index = testSetBeginMonth = getIntegerBulan(month);
        
        getOwner().getCmbBulanTestAkhir().setEnabled(true);
        ArrayList<String> temp = new ArrayList<String>();
        for(int i=index; i<months.length;i++){
            temp.add(months[i]);
        }
        temp.add(0,"-Pilih-");
        getOwner().fillCmbBulanTestAkhir(new MultiComboBoxModel(temp));
    }
    
    public void setEndMonth(String month){
        if(month.equalsIgnoreCase("-pilih-")){
            return;
        }
        int index = testSetEndMonth = getIntegerBulan(month);
        
    }
    
    public int getIntegerBulan(String month){
        for(int i=0;i<months.length;i++){
            if(months[i].equalsIgnoreCase(month))
                return i;
        }
        return 0;
    }
    
    public void doTesting(){
        fetchData();
        getOutput();
    }
    
    public void fetchData(){
        ArrayList<Record> temp;
        temp = new ArrayList<Record>(CollectionUtils.select(choosen.getRecords(),new Predicate<Record>(){

           public boolean evaluate(Record t) {
                Calendar temp = Calendar.getInstance();
                temp.setTime(t.getDate());
                if(temp.get(Calendar.YEAR)>=testSetBeginYear && temp.get(Calendar.YEAR)<=testSetEndYear)
                    if(temp.get(Calendar.MONTH)>= testSetBeginMonth-1 && temp.get(Calendar.MONTH)<=testSetEndMonth-1)
                        return true;
                return false;
            }          
        }));
        test_set.setRecords(temp);
        temp = new ArrayList<Record>(CollectionUtils.select(choosen.getNormRecords(),new Predicate<Record>(){

            public boolean evaluate(Record t) {
                Calendar temp = Calendar.getInstance();
                temp.setTime(t.getDate());
                if(temp.get(Calendar.YEAR)>=testSetBeginYear && temp.get(Calendar.YEAR)<=testSetEndYear)
                    if(temp.get(Calendar.MONTH)>= testSetBeginMonth-1 && temp.get(Calendar.MONTH)<=testSetEndMonth-1)
                        return true;
                return false;
            }       
        }));
        test_set.setNormRecords(temp);
    }
    
    public void getOutput(){
        results = myLearningMachine.testOut(test_set.getNormRecords());
        deNormalizeData();
        fillTable();
        getOwner().setPresisiAndRecall(confMatrix.getPresisiNaik(), confMatrix.getPresisiTetap(), confMatrix.getPresisiTurun(), confMatrix.getRecallNaik(), confMatrix.getRecallTetap(), confMatrix.getRecallTurun());
        getOwner().setAccuracy(confMatrix.getAccuracy());
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
        getOwner().fillResultTable(new ResultTableModel());
    }
    
    private void fetchResult(){
        showResult.clear();
        realResult.clear();
        double temp;
        Date first = test_set.getRecords().get(0).getDate();
        temp = test_set.getRecords().get(0).getClose();
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
        temp = test_set.getRecords().get(0).getClose();
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
    private class ResultTableModel extends AbstractTableModel{
        private String[] fields = {"Tanggal","Hasil Predisi","Kenyataan"};
        public int getRowCount() {
            return showResult.size();
        }

        public int getColumnCount() {
            return 3;
        }
        
        public String getColumnName(int index){
            return this.fields[index];
                    
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
