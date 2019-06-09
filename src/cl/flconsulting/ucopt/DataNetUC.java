package cl.flconsulting.ucopt;

//*********************
//* Data Base Manager 
//* Initially Created: 20/11/2005 - Frank Leanez
//*********************

import java.sql.*;
import java.io.*;


public class DataNetUC{
	
	
	//Network Components Data Atributtes readed from DB:
	short readnetid[];
	String readname[];
	float readr[];
	float readx[];
	float readsn[];
	float readpmax[];
	short readbusbarfrom[];//Overriden by DeepEdit conectivity
	short readbusbarto[];  //Overriden by DeepEdit conectivity
	String readctrla[];
	boolean readinservice[];
	
	//General Network Components Data Atributtes:
	short numreadnet=0;   //Maximun rows to read from DB: 32768
	Connection mylink;	  //Connection object with opened DB
	
//------------------------------------------------------------------------------
//CONSTRUCTORS FOR "DataNetUC"
//------------------------------------------------------------------------------

	//--Constr 1-- Creates link with specified DB:
	public DataNetUC(Connection link) {
		mylink=link;
	}
	
	//--Constr 2-- Empty arg contructor:
	public DataNetUC(){
	}
	
//------------------------------------------------------------------------------
//METHODS FOR READING DATA FROM TEXT FILES:
//------------------------------------------------------------------------------
	
	//--Method f1-- Read Data from Text File (.cvs files are only allowed by now)
	public void loaddatafromfile(String networkfileroot){
		
			
		//f1.1--Open text file:
		try{
			File inputFile = new File(networkfileroot+"\\networkdata.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			int cont=0;
			
		//f.1.2--Read lines:
			while ((line=in.readLine()) != null){
				String opoint[]=line.split(",");
				
				readnetid[cont] = Short.valueOf(opoint[1]).shortValue();
				readname[cont] = opoint[2];
				readr[cont] = Float.valueOf(opoint[3]).floatValue();
				readx[cont] = Float.valueOf(opoint[4]).floatValue();
				readsn[cont] = Float.valueOf(opoint[5]).floatValue();
				readpmax[cont] = Float.valueOf(opoint[6]).floatValue();
				readbusbarfrom[cont] = Short.valueOf(opoint[7]).shortValue();
				readbusbarto[cont] = Short.valueOf(opoint[8]).shortValue();
				readctrla[cont] = opoint[10];
				//Read integer inservice and transform into a boolean:
				byte readinservtemp=Byte.valueOf(opoint[9]).byteValue();
				if (readinservtemp==1){
					readinservice[cont] = true;
				}else{
					readinservice[cont] = false;
				}
				cont++;
				
			}
			
		}catch(Exception ex){
			System.out.println("Network File Reading Exception: "+ex);
		}
		
	}
	
	//--Method f2-- Determination of Total Units in Text File (.cvs files are only allowed by now)
	public short datalines(String networkfileroot){
		int cont=0;
		try{
			File inputFile = new File(networkfileroot+"\\networkdata.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line=in.readLine()) != null){
				cont++;
			}
		}catch(Exception ex){
			System.out.println("Exception: "+ex);
		}
		numreadnet=(short)cont;
		return numreadnet;
	}
	
	
	

//------------------------------------------------------------------------------
//METHODS FOR "DataNetUC"
//------------------------------------------------------------------------------

	//--Method 1-- Determination of Total Network Components in DB:
	public short numtotalnet(){
		
		int totalid=0;
		String consult1="Select netid from networkdata";
		try {
			Statement com1 = mylink.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet res1 = com1.executeQuery(consult1);
			res1.last();
			totalid=res1.getRow();
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
        numreadnet=(short)totalid;
        return numreadnet;
	
	}
	
	
	//--Method 2-- Objects Initialization:
	public void inidatanet(){
		
		readnetid = new short[numreadnet];
		readname = new String[numreadnet];
		readr = new float[numreadnet];
		readx = new float[numreadnet];
		readsn = new float[numreadnet];
		readpmax = new float[numreadnet];
		readbusbarfrom = new short[numreadnet];
		readbusbarto = new short[numreadnet];
		readctrla = new String[numreadnet];
		readinservice = new boolean[numreadnet];
		
	}
	
	
	//--Method 3-- Filling Class Atributtes reading from DB:
	public void completeselect(){
		
		short cont=0;
		String consult1="Select * from networkdata";
		try {
			Statement com1 = mylink.createStatement();
			ResultSet res1 = com1.executeQuery(consult1);
			while (res1.next()) {
				readnetid[cont] = res1.getShort("netid");
				readname[cont] = res1.getString("name");
				readr[cont] = res1.getFloat("R");
				readx[cont] = res1.getFloat("X");
				readsn[cont] = res1.getFloat("sn");
				readpmax[cont] = res1.getFloat("pmax");
				readbusbarfrom[cont] = res1.getShort("busbarfrom");
				readbusbarto[cont] = res1.getShort("busbarto");
				readctrla[cont] = res1.getString("ctrla");
				//Read integer inservice and transform into a boolean:
				byte readinservtemp=res1.getByte("inservice");
				if (readinservtemp==1){
					readinservice[cont] = true;
				}else{
					readinservice[cont] = false;
				}
				cont++;
			}
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
		
	}
	
	
	
}//end of Class