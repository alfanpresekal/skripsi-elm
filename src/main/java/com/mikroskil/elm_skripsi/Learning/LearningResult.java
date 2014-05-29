/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.Learning;

import no.uib.cipr.matrix.DenseMatrix;

/**
 *
 * @author Andy Wijaya
 */
public class LearningResult {
    
    private int seed;
    private double accuracy;

    public LearningResult(int seed,double accuracy) {
        this.seed = seed;
        this.accuracy = accuracy;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }
   
    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }
    
    
    
}
