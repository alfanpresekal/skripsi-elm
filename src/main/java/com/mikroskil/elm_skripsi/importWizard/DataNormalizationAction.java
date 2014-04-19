/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.importWizard;

import com.mikroskil.elm_skripsi.model.Record;
import com.mikroskil.elm_skripsi.model.Share;
import com.mikroskil.elm_skripsi.model.ShareContainer;
import com.mikroskil.elm_skripsi.wizard.PageAction;
import com.mikroskil.elm_skripsi.wizard.WizardPage;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Andy Wijaya
 */
public class DataNormalizationAction extends PageAction{
    
    private ShareContainer shareContainer = new ShareContainer();
    private Share share;

    public DataNormalizationAction(WizardPage owner) {
        super(owner);
    }
    
    @Override
    public boolean next() {
        saveData();
        saveXmlFile();
        return true;
    }
    
    private void saveData(){
        share.saveRecords(Share.REAL);
        share.saveRecords(Share.NORM);
    }
    
    private void saveXmlFile(){
        shareContainer.saveShares(ShareContainer.APPEND);    
    }
    @Override
    public boolean cancel() {
        return true;
    }

    @Override
    public boolean initialize() {
        try {
            share = (Share)getOwner().getWizardAction().getExtra("Share");
            shareContainer.getShare().add(share);
            share.setNormalizedData();
            fillTable();
            
            ((DataNormalizationPage)getOwner()).fillInformation(share.getKodeSaham(),share.getJumlahData());
            
            return true;
        } catch (Exception ex) {
            Logger.getLogger(DataNormalizationAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private void fillTable(){
        ((DataNormalizationPage)getOwner()).fillTable(new NormalRecordModel());
    }
    
    private class NormalRecordModel extends AbstractTableModel{
        
        private final int DATE_INDEX=0,OPEN_INDEX=1,HIGH_INDEX=2,LOW_INDEX=3,CLOSE_INDEX=4,EMA5_INDEX=5,EMA10_INDEX=6,EMA20_INDEX=7;
        private final String[] columnNames = new String[]{
            "Date","Open","High","Low","Close","EMA5","EMA10","EMA20"
        };
        
        public int getRowCount() {
            return share.getNormRecords().size();
        }
        
        @Override
        public String getColumnName(int column){
            return columnNames[column];
        }
        
        public int getColumnCount() {
            return 8;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Record r = share.getNormRecords().get(rowIndex);
            switch(columnIndex){
                case DATE_INDEX:
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    return formatter.format(r.getDate());
                case OPEN_INDEX:
                    return String.format("%.6f",r.getOpen());
                case HIGH_INDEX:
                    return String.format("%.6f",r.getHigh());
                case LOW_INDEX:
                    return String.format("%.6f",r.getLow());
                case CLOSE_INDEX:
                    return String.format("%.6f",r.getClose());
                case EMA5_INDEX:
                    return String.format("%.6f",r.getEma5());
                case EMA10_INDEX:
                    return String.format("%.6f",r.getEma10());
                case EMA20_INDEX:
                    return String.format("%.6f",r.getEma20());
                default:
                    return "Invalid index";
            }
        }
        
    }
}
