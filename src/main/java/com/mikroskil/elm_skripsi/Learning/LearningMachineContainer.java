/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.Learning;

import com.mikroskil.elm_skripsi.model.LearningDataResult;
import java.util.ArrayList;


/**
 *
 * @author Andy Wijaya
 */
public class LearningMachineContainer {
    
    private static ArrayList<LearningDataResult> learningDataResult = new ArrayList<LearningDataResult>();
    private static LearningMachineContainer instance;
    
    public static LearningMachineContainer getInstance(){
        if(instance == null){
            instance = new LearningMachineContainer();
        }
        return instance;
    }    
    
    public ArrayList<LearningDataResult> getLearningDataResult(){
        return learningDataResult;
    }
}
