/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.wizard;

/**
 *
 * @author Andy Wijaya
 */
public abstract class PageAction {
    private WizardPage owner;
    
    public PageAction(WizardPage owner){
        setOwner(owner);
    }

    public WizardPage getOwner() {
        return owner;
    }

    public void setOwner(WizardPage owner) {
        this.owner = owner;
    }

    public abstract boolean next();
    public abstract boolean cancel();
    public abstract boolean initialize();
}
