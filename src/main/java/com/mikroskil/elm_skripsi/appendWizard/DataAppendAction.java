/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.appendWizard;

import au.com.bytecode.opencsv.CSVReader;
import com.mikroskil.elm_skripsi.importWizard.FileSelectionAction;
import com.mikroskil.elm_skripsi.model.Record;
import com.mikroskil.elm_skripsi.model.Share;
import com.mikroskil.elm_skripsi.model.ShareContainer;
import com.mikroskil.elm_skripsi.wizard.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
/**
 *
 * @author Andy Wijaya
 */
public class DataAppendAction extends PageAction{
    
    private ShareContainer shareContainer = new ShareContainer();
    private Share choosen;
    private final ArrayList<Record> records = new ArrayList<Record>();
    private int DATE,OPEN,HIGH,LOW,CLOSE;
    
    public DataAppendAction(WizardPage owner) {
        super(owner);
    }
    
    @Override
    public boolean next() {
        if(isValid()){
            getOwner().getWizardAction().setExtra("share",choosen);
            getOwner().getWizardAction().setExtra("record",records);
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
        fetchKodeSaham();
        return true;
    }
    
    public String browseFile(){
        records.clear();
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
        
        int returnVal = fileChooser.showDialog(getOwner().getParent(), null);
        if(returnVal == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
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
                                    
                            records.add(r);
                            r=null;
                        }
                        catch(Exception e){
                            getOwner().setErrorMessage("line "+line+" : "+e.getMessage());
                            return "";
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
            return fileChooser.getSelectedFile().getPath();
        }
        return "";
    }
    
    public void fetchKodeSaham(){
        shareContainer.readShares();
        shareContainer.getShare().add(0, new Share("-Pilih-",""));       
        fillComboBox();
    }
    
    private void fillTable() {
        ((DataAppendPage)getOwner()).fillTable(new RecordTableModel());
    }
    
    private void fillComboBox(){
        ((DataAppendPage)getOwner()).fillComboBox(new KodeComboModel());
    }

    private boolean isValid() {
        if(((DataAppendPage)getOwner()).getComboBoxSelected().equalsIgnoreCase("-Pilih-")){
            getOwner().setErrorMessage("Pilih salah satu kode saham");
            return false;
        }
        if(((DataAppendPage)getOwner()).getFile().isEmpty()){
            getOwner().setErrorMessage("Pilih File yang akan ditambahkan");
            return false;
        }
        return true;
    }
    
    public String getNamaSaham(String string) {
        for(Share s : shareContainer.getShare()){
            if(s.getKodeSaham().equalsIgnoreCase(string)){
                choosen = s;
                return s.getNamaSaham();
            }
        }
        return "";
    }
    
    private class RecordTableModel extends AbstractTableModel{
        private String[] columnNames = {
            "Date","Open","High","Low","Close"
        };
        private final int DATE = 0, OPEN = 1, HIGH = 2, LOW = 3, CLOSE = 4;
        
        public int getRowCount() {
            return records.size();
        }

        @Override
        public String getColumnName(int columnIndex){
            return columnNames[columnIndex];
        }
        
        public int getColumnCount() {
            return columnNames.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Record r = records.get(rowIndex);
            switch(columnIndex){
                case DATE :
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                    return formatter.format(r.getDate());
                case OPEN :
                    return r.getOpen();
                case HIGH :
                    return r.getHigh();
                case LOW :
                    return r.getLow();
                case CLOSE:
                    return r.getClose();
                default:
                    return "invalid index";
            }
        }
        
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
