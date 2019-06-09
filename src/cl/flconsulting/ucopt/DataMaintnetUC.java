package cl.flconsulting.ucopt;

//*********************
//* Data Base Manager 
//* Initially Created: 17/8/2005 - Frank Leanez
//*********************

import java.sql.*;
import java.io.*;


public class DataMaintnetUC{
	
	
	//Maintenance Data Atributtes readed from DB:
	short readnetid[];
	short readfrommaint[];
	short readtomaint[];
	//char readtype[];
	float readR[];
	float readX[];
	float readpmax[];
	boolean readinservice[];
	
	//General Unit Data Atributtes:
	short numreadmaint=0;  //Maximun rows to read from DB: 32768
	Connection mylink;	  //Connection object with opened DB
	
//------------------------------------------------------------------------------
//CONSTRUCTORS FOR "DataMaintUC"
//------------------------------------------------------------------------------

	//--Constr 1-- Creates link with specified DB:
	public DataMaintnetUC(Connection link) {
		mylink=link;
	}
	
	
	//--Constr 2-- Empty arg contructor:
	public DataMaintnetUC(){
	}
	
	
	
//------------------------------------------------------------------------------
//METHODS FOR READING DATA FROM TEXT FILES:
//------------------------------------------------------------------------------
	
	
	//--Method f1-- Read Data from Text File (.cvs files are only allowed by now)
	public void loaddatafromfile(String dirfileroot){
	
	
		//f1.1--Open text file:
		try{
			File inputFile = new File(dirfileroot+"\\maintenet.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			int cont=0;
			
		//f.1.2--Read lines:
			while ((line=in.readLine()) != null){
				String opoint[]=line.split(",");
				
				if (opoint.length!=7){
					break;
				}
				
				readnetid[cont] = Short.valueOf(opoint[1]).shortValue();
				readfrommaint[cont] = Short.valueOf(opoint[2]).shortValue();
				readtomaint[cont] = Short.valueOf(opoint[3]).shortValue();
				readfrommaint[cont] -= 1;
				readtomaint[cont] -= 1;
				//String readtypetemp = res1.getObject("type").toString();
				//readtype[cont] = readtypetemp.charAt(0);
				readR[cont] = Float.valueOf(opoint[4]).floatValue();
				readX[cont] = Float.valueOf(opoint[5]).floatValue();
				readpmax[cont] = Float.valueOf(opoint[6]).floatValue();
				//Read integer inservice and transform into a boolean:
				byte readinservtemp=Byte.valueOf(opoint[7]).byteValue();
				if (readinservtemp==1){
					readinservice[cont] = true;
				}else{
					readinservice[cont] = false;
				}
				cont++;
				
			}
			
		}catch(Exception ex){
			System.out.println("Network Maintenance File Reading Exception: "+ex);
		}
		
	}
	
	//--Method f2-- Determination of Demands in Text File (.cvs files are only allowed by now)
	public short datalines(String loadfileroot){
		int cont=0;
		try{
			File inputFile = new File(loadfileroot+"\\maintenet.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line=in.readLine()) != null){
				cont++;
			}
		}catch(Exception ex){
			System.out.println("Exception: "+ex);
		}
		numreadmaint=(short)cont;
		return numreadmaint;
	}
		

//------------------------------------------------------------------------------
//METHODS FOR "DataMaintnetUC"
//------------------------------------------------------------------------------

	//--Method 1-- Determination of Total Maintenance task in DB:
	public short numtotalmaintnet(){
		
		int totalid=0;
		String consult1="SELECT netid FROM maintenet";
		try {
			Statement com1 = mylink.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet res1 = com1.executeQuery(consult1);
			res1.last();
			totalid=res1.getRow();
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
        numreadmaint=(short)totalid;
        return numreadmaint;
	
	}
	
	
	//--Method 2-- Objects Initialization:
	public void inidatamaintnet(){
		
		//readmaintid = new short[numreadmaint];
		readnetid = new short[numreadmaint];
		readfrommaint = new short[numreadmaint];
		readtomaint = new short[numreadmaint];
		//readtype = new char[numreadmaint];
		readR = new float[numreadmaint];
		readX = new float[numreadmaint];
		readpmax = new float[numreadmaint];
		readinservice = new boolean[numreadmaint];
		
	}

	
	//--Method 4-- Filling Class Atributtes reading from DB:
	public void completeselect(){
		
		short cont=0;
		String consult1="Select * from maintenet";
		try {
			Statement com1 = mylink.createStatement();
			ResultSet res1 = com1.executeQuery(consult1);
			while (res1.next()) {
				readnetid[cont] = res1.getShort("netid");
				readfrommaint[cont] = res1.getShort("frommaint");
				readtomaint[cont] = res1.getShort("tomaint");
				readfrommaint[cont] -= 1;
				readtomaint[cont] -= 1;
				//String readtypetemp = res1.getObject("type").toString();
				//readtype[cont] = readtypetemp.charAt(0);
				readR[cont] = res1.getFloat("R");
				readX[cont] = res1.getFloat("X");
				readpmax[cont] = res1.getFloat("pmax");
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