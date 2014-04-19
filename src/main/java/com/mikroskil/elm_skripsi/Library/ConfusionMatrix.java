/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.Library;

import com.mikroskil.elm_skripsi.model.Result;
import java.util.ArrayList;

/**
 *
 * @author Andy Wijaya
 */
public class ConfusionMatrix {
    private final int NAIK = 0,TETAP=1,TURUN=2,CTO=3;
    private int[][] confMatrix = new int[4][4];
    private double recallNaik,recallTetap,recallTurun,PresisiNaik,PresisiTetap,PresisiTurun,Accuracy;
    
    public ConfusionMatrix(){
        nullifyConfMatrix();
    }
    
    private void nullifyConfMatrix(){
        for (int[] confMatrix1 : confMatrix) {
            for (int j = 0; j < confMatrix1.length; j++) {
                confMatrix1[j] = 0;
            }
        }
    }
    
    public void count(ArrayList<Result>real,ArrayList<Result>pred){
        nullifyConfMatrix();
        for(int i=0;i<real.size();i++){
            Result r = real.get(i);
            Result p = pred.get(i);
            if(r.getResult().equals(p.getResult())){
                int x = getHooker(r.getRealResult());
                confMatrix[x][x]+=1;
            }
            else{
                int x = getHooker(p.getRealResult());
                int y = getHooker(r.getRealResult());
                confMatrix[x][y]+=1;
            }
        }
        countClassificationOverall();
        countTruthOverall();
        countAccuracy();
        countPrecision();
        countRecall();
    }
    
    private int getHooker(int result){
        switch(result){
            case Result.NAIK: return NAIK;
            case Result.TETAP : return TETAP;
            case Result.TURUN : return TURUN;
        }
        return -1;
    }
    
    private void countClassificationOverall(){
        for(int[]t : confMatrix){
            int co =0;
            for(int i=0;i<3;i++){
                co+=t[i];
            }
            t[CTO]=co;
        }
    }
    
    private void countTruthOverall(){
        for(int i=0;i<4;i++){
            int to =0;
            for(int j=0;j<3;j++){
                to+=confMatrix[j][i];
            }
            confMatrix[CTO][i] = to;
        }
    }
    
    private void countAccuracy(){
        int total = confMatrix[NAIK][NAIK]+confMatrix[TETAP][TETAP]+confMatrix[TURUN][TURUN];
        Accuracy = ((double)total/confMatrix[CTO][CTO])*100;               
    }
    
    private void countPrecision(){
        int devider;
        devider = (confMatrix[NAIK][CTO]==0)?1:confMatrix[NAIK][CTO];
        PresisiNaik = confMatrix[NAIK][NAIK]/devider;
        devider = (confMatrix[TETAP][CTO]==0)?1:confMatrix[TETAP][CTO];
        PresisiTetap = confMatrix[TETAP][TETAP]/devider;
        devider = (confMatrix[TURUN][CTO]==0)?1:confMatrix[TURUN][CTO];
        PresisiTurun = confMatrix[TURUN][TURUN]/confMatrix[TURUN][CTO];
    }
    
    private void countRecall(){
        int devider;
        devider = (confMatrix[NAIK][CTO]==0)?1:confMatrix[CTO][NAIK];
        recallNaik = confMatrix[NAIK][NAIK]/devider;
        devider = (confMatrix[TETAP][CTO]==0)?1:confMatrix[CTO][TETAP];
        recallTetap = confMatrix[TETAP][TETAP]/devider;
        devider = (confMatrix[TURUN][CTO]==0)?1:confMatrix[CTO][TURUN];
        recallTurun = confMatrix[TURUN][TURUN]/devider;
    }
}
