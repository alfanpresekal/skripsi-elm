/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.Learning;

import com.mikroskil.elm_skripsi.Library.LearningMachine;
import com.mikroskil.elm_skripsi.model.LearningDataResult;
import com.mikroskil.elm_skripsi.model.Record;
import com.mikroskil.elm_skripsi.model.Result;
import com.mikroskil.elm_skripsi.model.Share;
import com.mikroskil.elm_skripsi.model.ShareContainer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JOptionPane;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;

/**
 *
 * @author Andy Wijaya
 */
public class PredictAction {
    
    ArrayList<Result> showResult = new ArrayList<Result>();
    ArrayList<Result> realResult = new ArrayList<Result>();
    private ShareContainer shareContainer = new ShareContainer();
    private Share choosen,test_set = new Share("test_set",""),train_set = new Share("train_set","");
    double[] results,results_denorm;
    private LearningMachine myLearningMachine;
    private Predict owner;
    
    public PredictAction(Predict owner){
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
    
    public void setChoosen(final String kode){
        if(kode.equalsIgnoreCase("-pilih-"))return;
        choosen = CollectionUtils.find(shareContainer.getShare(), new Predicate<Share>(){
           public boolean evaluate(Share t){
               return t.getKodeSaham().equalsIgnoreCase(kode);
                       
           } 
        });
        choosen.readRecords(Share.REAL);
        Record temp = choosen.getRecords().get(choosen.getRecords().size()-1);
        Record newOne;
        try {
            newOne = new Record(temp.toArray());
            choosen.getRecords().add(newOne);
            choosen.countEMA5();
            choosen.countEMA20();
            choosen.countEMA10();
            choosen.setHighestLowest();
            choosen.setNormalizedData();
            ArrayList<Record> temp_train;
            ArrayList<Record> temp_test;
            temp_train = new ArrayList<Record>(choosen.getNormRecords().subList(0,choosen.getNormRecords().size()-1));
            temp_test = new ArrayList<Record>(choosen.getNormRecords().subList(choosen.getNormRecords().size()-1,choosen.getNormRecords().size()));
            train_set.setNormRecords(temp_train);
            test_set.setNormRecords(temp_test);
            myLearningMachine = new LearningMachine(0,0, "sig");
            myLearningMachine.train(train_set.getNormRecords());
            doPredict();
        } catch (Exception ex) {
            Logger.getLogger(PredictAction.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    
    public void doPredict(){
        fetchTestSet();
        getOutput();
    }
    
    public void getOutput(){
        results = myLearningMachine.testOut(test_set.getNormRecords());
        deNormalizeData();
        fetchResult();
        getOwner().fillLabelResult(showResult.get(showResult.size()-1).getResult());
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
    }
    
    public void fetchTestSet(){
        ArrayList<Record> temp;
        temp = new ArrayList<Record>(
                choosen.getRecords().subList(
                choosen.getRecords().size()-7, choosen.getRecords().size()-1
                )
        );
        
        Record tempRe = temp.get(temp.size()-1);
        temp.add(new Record(new Date(),tempRe.getOpen(),tempRe.getHigh(),tempRe.getLow(), tempRe.getClose(),tempRe.getEma5(), tempRe.getEma10(), tempRe.getEma20()));
        test_set.setRecords(temp);
        
        
        temp = new ArrayList<Record>(choosen.getNormRecords().subList(choosen.getNormRecords().size()-7, choosen.getNormRecords().size()-1));
        
        tempRe = temp.get(temp.size()-1);
        temp.add(new Record(new Date(),tempRe.getOpen(),tempRe.getHigh(),tempRe.getLow(), tempRe.getClose(),tempRe.getEma5(), tempRe.getEma10(), tempRe.getEma20()));
        test_set.setNormRecords(temp);
    }
    
    public void fillKodeSaham(){
        getOwner().fillCmbKodeSaham(new KodeSahamModel());
    }

    public Predict getOwner() {
        return owner;
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
}
