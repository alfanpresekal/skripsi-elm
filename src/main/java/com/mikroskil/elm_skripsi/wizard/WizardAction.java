/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.wizard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import javax.swing.JFrame;

/**
 *
 * @author Andy Wijaya
 */
public class WizardAction {
    private JFrame parent;
    private DialogContainer dialogContainer;
    private int currentState, stateAmount;
    private ArrayList<WizardPage> wizardPages;
    private HashMap<String, Object> EXTRAS = new HashMap<String, Object>();
    private Stack runPageStack = new Stack();
    
    public WizardAction(JFrame parent){
        setParent(parent);
        setWizardPages(new ArrayList<WizardPage>());
        setDialogContainer(new DialogContainer(getParent(),true,this));
        setCurrentState(0);
        setStateAmount(0);
    }

    public WizardAction(JFrame parent, ArrayList<WizardPage> wizardPages){
        setParent(parent);
        setWizardPages(wizardPages);
        setDialogContainer(new DialogContainer(getParent(), true, this));
        setCurrentState(0);
        setStateAmount(getWizardPages().size());
    }
    
    /** Method of Class **/
    public void initComponents(){
        WizardPage current = getCurrentPage();
        
        if(isCurrentLast())
            dialogContainer.setAsLastPage();
        else if(isCurrentFirst())
            dialogContainer.setAsFirstPage();
        else 
            dialogContainer.setAsNormalPage();
        dialogContainer.setTitle(current.getTitle());
        dialogContainer.setContent(current);
        setErroMessage("");
        if(!dialogContainer.isShowing())
            dialogContainer.show();
    }
    
    public void show(){
        if(getCurrentPage().initialize()){
            initComponents();
        }
    }
    
    public void next(){
        if(getCurrentPage().next()){
            runPageStack.push(getCurrentPage());
            switchNext();
            if(getCurrentPage().initialize())
                initComponents();
        }
    }
    
    public void back(){
        if(getCurrentPage().cancel()){
            Object pop = runPageStack.pop();
            switchBack();
            if(getCurrentPage().initialize())
                initComponents();
        }
    }
    
    public void finish(){
        if(getCurrentPage().next())
            dialogContainer.dispose();
    }
    
    public void cancel(){
        while(!runPageStack.empty()){
            ((WizardPage)runPageStack.pop()).cancel();
        }
        dialogContainer.dispose();
    }
    
    public void add(WizardPage wizardPage){
        getWizardPages().add(wizardPage);
        setStateAmount(getStateAmount()+1);
    }
    
    public WizardPage getCurrentPage(){
        return getWizardPages().get(getCurrentState());
    }
    
    public boolean isCurrentFirst(){
        if(getCurrentState()==0)
            return true;
        return false;
    }
    
    public boolean isCurrentLast(){
        if(getCurrentState()==getWizardPages().size()-1)
            return true;
        return false;
    }
    
    public void switchNext(){
        setCurrentState(getCurrentState()+1);
    }
    
    public void switchBack(){
        setCurrentState(getCurrentState()-1);
    }
    
    /** Setter and Getter **/
    public JFrame getParent() {
        return parent;
    }

    public void setParent(JFrame parent) {
        this.parent = parent;
    }

    public DialogContainer getDialogContainer() {
        return dialogContainer;
    }

    public void setDialogContainer(DialogContainer dialogContainer) {
        this.dialogContainer = dialogContainer;
    }

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
    }

    public int getStateAmount() {
        return stateAmount;
    }

    public void setStateAmount(int stateAmount) {
        this.stateAmount = stateAmount;
    }

    public ArrayList<WizardPage> getWizardPages() {
        return wizardPages;
    }

    public void setWizardPages(ArrayList<WizardPage> wizardPages) {
        this.wizardPages = wizardPages;
    }

    void setErroMessage(String errorMessage) {
        dialogContainer.showErrorMessage(errorMessage);
    }

    public void setExtra(String key, Object value){
        if(EXTRAS.containsKey(key))
            EXTRAS.remove(key);
        EXTRAS.put(key, value);
    }
    
    public Object getExtra(String key) throws Exception{
        if(EXTRAS.containsKey(key))
            return EXTRAS.get(key);
        throw new Exception("Key not found");
    }
}
