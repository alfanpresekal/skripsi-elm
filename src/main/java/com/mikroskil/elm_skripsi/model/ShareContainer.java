/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mikroskil.elm_skripsi.model;

import com.mikroskil.elm_skripsi.importWizard.FileSelectionAction;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Andy Wijaya
 */
public class ShareContainer {
    public static int APPEND = 0,NEW = 1;
    
    private ArrayList<Share> share = new ArrayList<Share>();

    public ShareContainer() {
    }

    /** This function use to Save into XML File
     * @param mode **/
    public void saveShares(int mode){
        File directory = new File("./config");
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        
        try {
            documentBuilder = documentFactory.newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            return;
        }
        
        Document document = null;
        Element rootElement = null;
        File xmlFile = new File("./config/data.xml");
        
        if(!directory.exists())
            directory.mkdir();
        
        if(mode == APPEND){
            if(xmlFile.exists()){
                try {
                    document = documentBuilder.parse(xmlFile);
                    rootElement = (Element)document.getElementsByTagName("Sahams").item(0);
                } catch (SAXException ex) {
                    Logger.getLogger(ShareContainer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ShareContainer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            else{
                saveShares(NEW);
                return;
            }
        }
        else{
            if(xmlFile.exists())
                xmlFile.delete();
            document = documentBuilder.newDocument();
            rootElement = document.createElement("Sahams");
            document.appendChild(rootElement);
        }
        
        Element saham = null, kodeSaham = null, namaSaham = null;
        
        for(Share s : share){
            saham = document.createElement("Saham");
            rootElement.appendChild(saham);
            kodeSaham = document.createElement("Kode");
            kodeSaham.appendChild(document.createTextNode(s.getKodeSaham()));
            saham.appendChild(kodeSaham);
            namaSaham = document.createElement(("Nama"));
            namaSaham.appendChild(document.createTextNode(s.getNamaSaham()));
            saham.appendChild(namaSaham);
        }
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;

        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException ex) {
            return;
        }

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(xmlFile);

        try {
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            Logger.getLogger(FileSelectionAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /** This function use to get Data from the XML File **/
    public void readShares(){
        File xmlFile = new File("./config/data.xml");
        if(xmlFile.exists()){
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = null;
            try {
                documentBuilder = documentFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(ShareContainer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Document document = null;
            try {
                document = documentBuilder.parse(xmlFile);
            } catch (SAXException ex) {
                Logger.getLogger(ShareContainer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ShareContainer.class.getName()).log(Level.SEVERE, null, ex);
            }
            NodeList list = document.getElementsByTagName("Saham");
            for(int i = 0 ;i<list.getLength();i++){
                Node node = list.item(i);
                Share s = new Share();
                NodeList children = node.getChildNodes();
                s.setKodeSaham(children.item(0).getTextContent());
                s.setNamaSaham(children.item(1).getTextContent());
                share.add(s);
            }
        }
    }
   
    /** This function use to delete certain data from xml File
     * @param kodeSaham **/
    public void deleteShare(String kodeSaham){
        File xmlFile = new File("./config/data.xml");
        if(xmlFile.exists()){
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;

            try {
                docBuilder = docFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(FileSelectionAction.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            Element rootElement = null;
            Document doc = null;
            
            try {
                doc = docBuilder.parse(xmlFile);
                rootElement = (Element) doc.getElementsByTagName("Sahams").item(0);
            } catch (SAXException ex) {
                Logger.getLogger(FileSelectionAction.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FileSelectionAction.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try{
                NodeList nodeList = doc.getElementsByTagName("Kode");
                Node removeNode = null;
                for(int i=0;i<nodeList.getLength();i++){
                    if(nodeList.item(i).getTextContent().equalsIgnoreCase(kodeSaham)){
                        removeNode = nodeList.item(i);
                        break;
                    }
                }
                Node parent = removeNode.getParentNode();
                rootElement.removeChild(parent);
                
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(xmlFile);
		transformer.transform(source, result);
            }
            catch(NullPointerException e){
                
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(FileSelectionAction.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(FileSelectionAction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public ArrayList<Share> getShare() {
        return share;
    }

    public void setShare(ArrayList<Share> share) {
        this.share = share;
    }
    
}
