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
    
    private DenseMatrix inputWeight;
    private DenseMatrix biasOfHiddenNeurons;
    private double accuracy;

    public LearningResult(DenseMatrix inputWeight, DenseMatrix biasOfHiddenNeurons,double accuracy) {
        this.inputWeight = inputWeight;
        this.biasOfHiddenNeurons = biasOfHiddenNeurons;
        this.accuracy = accuracy;
    }
    
    public DenseMatrix getInputWeight() {
        return inputWeight;
    }

    public void setInputWeight(DenseMatrix inputWeight) {
        this.inputWeight = inputWeight;
    }

    public DenseMatrix getBiasOfHiddenNeurons() {
        return biasOfHiddenNeurons;
    }

    public void setBiasOfHiddenNeurons(DenseMatrix biasOfHiddenNeurons) {
        this.biasOfHiddenNeurons = biasOfHiddenNeurons;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }
    
    
    
}
