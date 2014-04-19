/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.importWizard;

import java.io.File;
import java.io.FileReader;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import au.com.bytecode.opencsv.CSVReader;
import java.io.FileNotFoundException;
import com.mikroskil.elm_skripsi.model.*;
import com.mikroskil.elm_skripsi.wizard.PageAction;
import com.mikroskil.elm_skripsi.wizard.WizardPage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;


/**
 *
 * @author Andy Wijaya
 */
public class FileSelectionAction extends PageAction {
    
    private final Share share = new Share();
    private final String[] columnNames = new String[]{
        "Date","Open","High","Low","Close"
    };
    private int DATE,OPEN,HIGH,LOW,CLOSE;
    
    public FileSelectionAction(WizardPage owner) {
        super(owner);
    }
    
    public void browseFile(){
        share.getRecords().clear();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if(f.isDirectory()){
                    return true;
                }
                
                String fileName = f.getName();
                int i = fileName.lastIndexOf(".");
                String ext="";
                if(i>=0)
                    ext = fileName.substring(i+1);
                return ext.equalsIgnoreCase("csv");
            }

            @Override
            public String getDescription() {
                return "cvs file [*.cvs]";
            }
        });
        int returnVal = fileChooser.showDialog(getOwner().getParent(),null);
        if(returnVal == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            ((FileSelectionPage)getOwner()).setFileText(fileChooser.getSelectedFile().getPath());
            FileReader fileReader = null;
            try {
                fileReader = new FileReader(file);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FileSelectionAction.class.getName()).log(Level.SEVERE, null, ex);
            }
            CSVReader reader = new CSVReader(fileReader);
            String[] nextLine;
            int line=0;
            try {
                while((nextLine = reader.readNext()) != null){
                    if(line ==0){
                        for(int i=0;i<5;i++){
                            if(nextLine[i].equalsIgnoreCase("date"))
                                DATE = i;
                            else if(nextLine[i].equalsIgnoreCase("open"))
                                OPEN = i;
                            else if(nextLine[i].equalsIgnoreCase("high"))
                                HIGH = i;
                            else if(nextLine[i].equalsIgnoreCase("low"))
                                LOW = i;
                            else if(nextLine[i].equalsIgnoreCase("close"))
                                CLOSE = i;
                        }
                    }
                    else{
                        try{
                            Record r = new Record();
                            r.setDate(nextLine[DATE]);
                            r.setOpen(nextLine[OPEN]);
                            r.setHigh(nextLine[HIGH]);
                            r.setLow(nextLine[LOW]);
                            r.setClose(nextLine[CLOSE]);
                                    
                            share.getRecords().add(r);
                            r=null;
                        }
                        catch(Exception e){
                            getOwner().setErrorMessage("line "+line+" : "+e.getMessage());
                            return;
                        }
                    }
                    line++;
                }
            } catch (IOException ex) {
                Logger.getLogger(FileSelectionAction.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            fillTable();
            /** Clean Memory from un-use variable **/
            reader = null;
            file = null;
            nextLine = null;
        }
        
        fileChooser = null;        
    }
    
    private void fillTable(){
        ((FileSelectionPage)getOwner()).setTableModel(new RecordTableModel());
    }

    @Override
    public boolean next() {
        if(isValid()){
            share.setKodeSaham(((FileSelectionPage)getOwner()).getKodeSaham());
            share.setNamaSaham(((FileSelectionPage)getOwner()).getNamaSaham());
            getOwner().getWizardAction().setExtra("Share", share);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean cancel() {
        return true;
    }
    
    @Override
    public boolean initialize() {
        return true;
    }
    
    public boolean isValid(){
        if(((FileSelectionPage)getOwner()).getKodeSaham().isEmpty()){
            getOwner().setErrorMessage("Kode Saham tidak boleh kosong");
            return false;
        }
        else if(((FileSelectionPage)getOwner()).getNamaSaham().isEmpty()){
            getOwner().setErrorMessage("Nama Saham tidak boleh kosong");
            return false;
        }
        else if (((FileSelectionPage)getOwner()).getFile().isEmpty()){
            getOwner().setErrorMessage("Pilih File CSV");
            return false;
        }
        
        ShareContainer temp = new ShareContainer();
        temp.readShares();
        if(temp.getShare().size()>0){
            if(CollectionUtils.find(temp.getShare(),new Predicate<Share>(){

                public boolean evaluate(Share t) {
                    return t.getKodeSaham().equalsIgnoreCase((String)((FileSelectionPage)getOwner()).getKodeSaham());
                }
                
            }) != null){
                getOwner().setErrorMessage("Kode Saham telah ada sebelumnya");
                return false;
            }
        }
        return true;
    }

    private class RecordTableModel extends AbstractTableModel{
        private final int DATE_INDEX = 0;
        private final int OPEN_INDEX = 1;
        private final int HIGH_INDEX = 2;
        private final int LOW_INDEX = 3;
        private final int CLOSE_INDEX = 4;
        private final ArrayList<Record> records = share.getRecords();
        
        public int getRowCount() {
            return records.size();
        }

        public int getColumnCount() {
            return 5;
        }
        
        public String getColumnName(int column){
            return columnNames[column];
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Record r = records.get(rowIndex);
            switch(columnIndex){
                case DATE_INDEX:
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    return formatter.format(r.getDate());
                case OPEN_INDEX:
                    return r.getOpen();
                case HIGH_INDEX:
                    return r.getHigh();
                case LOW_INDEX:
                    return r.getLow();
                case CLOSE_INDEX:
                    return r.getClose();
                default:
                    return "Invalid index";
            }
        }
        
    }
    
}
