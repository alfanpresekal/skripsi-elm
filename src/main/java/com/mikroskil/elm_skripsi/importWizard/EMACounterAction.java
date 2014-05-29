/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.importWizard;

import com.mikroskil.elm_skripsi.model.Record;
import com.mikroskil.elm_skripsi.model.Share;
import com.mikroskil.elm_skripsi.wizard.PageAction;
import com.mikroskil.elm_skripsi.wizard.WizardPage;
import java.text.SimpleDateFormat;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Andy Wijaya
 */
public class EMACounterAction extends PageAction {
    
    private Share share;
    
    public EMACounterAction(WizardPage owner) {
        super(owner);
    }

    @Override
    public boolean next() {
        getOwner().getWizardAction().setExtra("Share", share);
        return true;
    }

    @Override
    public boolean cancel() {
        return true;
    }

    @Override
    public boolean initialize() {
        share.countEMA5();
        
        share.countEMA10();
        
        share.countEMA20();
        
        share.setJumlahData(share.getRecords().size());
        share.setHighestLowest();
        ((EMACounterPage)getOwner()).setTableModel(new RecordTableModel());
        fillInformation();
        return true;
    }
    
    private void fillInformation(){
        ((EMACounterPage)getOwner()).fillInformation(share);
    }
    
    /** Getter and Setter
     * @return  **/
    public Share getShare() {
        return share;
    }

    public void setShare(Share share) {
        this.share = share;
    }
    
    private class RecordTableModel extends AbstractTableModel{
        private final int DATE_INDEX=0,OPEN_INDEX=1,HIGH_INDEX=2,LOW_INDEX=3,CLOSE_INDEX=4,EMA5_INDEX=5,EMA10_INDEX=6,EMA20_INDEX=7;
        private final String[] columnNames = new String[]{
            "Date","Open","High","Low","Close","EMA5","EMA10","EMA20"
        };

        public int getRowCount() {
            return share.getRecords().size();
        }

        public int getColumnCount() {
            return 8;
        }
        
        @Override
        public String getColumnName(int column){
            return columnNames[column];
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Record r = share.getRecords().get(rowIndex);
            switch(columnIndex){
                case DATE_INDEX:
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    return formatter.format(r.getDate());
                case OPEN_INDEX:
                    return String.format("%.2f",r.getOpen());
                case HIGH_INDEX:
                    return String.format("%.2f",r.getHigh());
                case LOW_INDEX:
                    return String.format("%.2f",r.getLow());
                case CLOSE_INDEX:
                    return String.format("%.2f",r.getClose());
                case EMA5_INDEX:
                    return String.format("%.2f",r.getEma5());
                case EMA10_INDEX:
                    return String.format("%.2f",r.getEma10());
                case EMA20_INDEX:
                    return String.format("%.2f",r.getEma20());
                default:
                    return "Invalid index";
            }
        }
        
    }
}
