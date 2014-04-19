/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.delete;

import com.mikroskil.elm_skripsi.model.Share;
import com.mikroskil.elm_skripsi.model.ShareContainer;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JDialog;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
/**
 *
 * @author Andy Wijaya
 */
public class DeleteAction {
    
    private final ShareContainer shareContainer = new ShareContainer();
    private JDialog owner;
    private Share share;

    public DeleteAction(JDialog owner) {
        this.owner = owner;
    }
    
    public void initialize(){
        fetchKodeSaham();
    }
    
    public void fetchKodeSaham(){
        shareContainer.readShares();
        shareContainer.getShare().add(0,new Share("-Pilih-",""));
        fillComboBox();
    }

    public void next(){
        shareContainer.deleteShare(share.getKodeSaham());
        share.deleteRecords();
    }
    
    public void setChoosen(final String kodeSaham){
        share = CollectionUtils.find(shareContainer.getShare(), new Predicate<Share>(){

            public boolean evaluate(Share t) {
                return (t.getKodeSaham().equalsIgnoreCase(kodeSaham));
            }
            
        });
    }
    
    private void fillComboBox() {
        ((DeleteDialog)getOwner()).fillComboBox(new KodeComboModel());
    }

    public JDialog getOwner() {
        return owner;
    }

    public void setOwner(JDialog owner) {
        this.owner = owner;
    }
    
    
    private class KodeComboModel extends AbstractListModel implements ComboBoxModel{
        
        String selection = shareContainer.getShare().get(0).getKodeSaham();
        
        public int getSize() {
            return shareContainer.getShare().size();
        }
        
        @Override
        public Object getElementAt(int index) {
            return shareContainer.getShare().get(index).getKodeSaham();
        }
        
        public void setSelectedItem(Object anItem) {
            selection = (String)anItem;
        }

        public Object getSelectedItem() {
            return selection;
        }
    }
}
