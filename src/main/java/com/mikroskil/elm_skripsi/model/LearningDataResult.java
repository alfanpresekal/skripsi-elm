/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.model;

import com.mikroskil.elm_skripsi.Library.LearningMachine;

/**
 *
 * @author Andy Wijaya
 */
public class LearningDataResult {
    
    private String kodeSaham;
    private LearningMachine learningMachine;
    private int endYear;

    public LearningDataResult(String kodeSaham, LearningMachine learningMachine,int endYear) {
        this.kodeSaham = kodeSaham;
        this.learningMachine = learningMachine;
        this.endYear = endYear;
    }

    public String getKodeSaham() {
        return kodeSaham;
    }

    public void setKodeSaham(String kodeSaham) {
        this.kodeSaham = kodeSaham;
    }

    public LearningMachine getLearningMachine() {
        return learningMachine;
    }

    public void setLearningMachine(LearningMachine learningMachine) {
        this.learningMachine = learningMachine;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }
    
    
}
