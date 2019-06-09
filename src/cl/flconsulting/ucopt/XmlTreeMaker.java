package cl.flconsulting.ucopt;

//****************************************************************************
//Unit-Commitment XML-Option-File Reader
//Initially Created: 29/10/2005 - Frank Leanez
//****************************************************************************

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;


public class XmlTreeMaker extends DefaultHandler{
	
	private int fields=0;
	int maxdata=500;
	
	int actualelement=-1;
	int contelements=-1;
	private int contdisplayable=0;

	int control[] = new int [maxdata];
	TreeXml treedata[] = new TreeXml[maxdata];
	
	public XmlTreeMaker(String Xmlfile){
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse( new File(Xmlfile), this);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	public String returnchardata(String datalook){
		short kk=0;
		boolean datafound=false;
		String retdata="";
		while (kk<contelements+1 && !datafound){
			if (treedata[kk].getelementname().equals(datalook)){
				retdata=treedata[kk].getchardata();
				datafound=true;
			}
			kk++;
		}
		return retdata;
	}
	
	public int numelements(){
		return contelements;
	}
	
	public int numchardata(){
		return fields;
	}

	
// SAX DocumentHandler methods:
	public void startDocument() throws SAXException{
		//System.out.println("<?xml version='1.0' encoding='UTF-8'?>");
    }
    
	public void startElement(String namespaceURI,String lName,String qName,Attributes attrs)
    throws SAXException {
    	actualelement++;
        contelements++;
        treedata[contelements]= new TreeXml();
        if (contelements==0){
        	control[0]=0;
        	treedata[contelements].setparentindex(-1);
        }else{
        	control[actualelement]=contelements;
        	treedata[contelements].setparentindex(control[actualelement-1]);
        }
    	//Read Element Name:
        String eName = lName;
        if ("".equals(eName)){
        	eName = qName;
        }
        treedata[contelements].setelementname(eName);
        treedata[contelements].setownindex(contelements);
        //Read Attributes:
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name 
                if ("".equals(aName)) aName = attrs.getQName(i);
                //Add Atributtes Reaction:
                if (aName.equals("displayname")){
                	treedata[contelements].setdisplayname(attrs.getValue(i));
                	contdisplayable++;
                }
                if (aName.equals("type")){
                	treedata[contelements].settype(attrs.getValue(i));                	
                }
            }
        }
	}
	
	public void endElement(String uri,String localName,String qName)
	throws SAXException	{
    	actualelement--;
	}
	
	public void characters(char buf[], int offset, int len) throws SAXException{
		String s = new String(buf, offset, len);
		if(!s.trim().equals("")){
			treedata[contelements].setchardata(s);
			fields++;
		}
	}
	
	public int numdisplayelements(){
		return contdisplayable;
	}
	
}


class TreeXml{
	
	private String elementname="";
	private String chardata="";
	private String displayname="";
	private String type="";
	private int ownindex=0;
	private int parentindex=0;
	
	public TreeXml(){}
	
	public TreeXml(String e, String c, String d, String t, int own, int p){
		elementname=e;
		chardata=c;
		displayname=d;
		type=t;
		ownindex=own;
		parentindex=p;
	}
	
	public void setelementname(String e){
		elementname=e;
	}
	public void setchardata(String c){
		chardata=c;
	}
	public void setdisplayname(String d){
		displayname=d;
	}
	public void settype(String t){
		type=t;
	}
	public void setownindex(int own){
		ownindex=own;
	}
	public void setparentindex(int p){
		parentindex=p;
	}
	
	public String getelementname(){
		return elementname;
	}
	public String getchardata(){
		return chardata;
	}
	public String getdisplayname(){
		return displayname;
	}
	public String gettype(){
		return type;
	}
	public int getownindex(){
		return ownindex;
	}
	public int getparentindex(){
		return parentindex;
	}
	public boolean isleaf(){
		if (chardata.equals("")){
			return false;
		}else{
			return true;
		}
	}
	public boolean isdisplayable(){
		if (displayname.equals("")){
			return false;
		}else{
			return true;
		}
	}
	
	public String toString(){
		return displayname;
	}
}