package cl.flconsulting.ucopt;

//*********************
//* Data Base Manager 
//* Initially Created: 17/8/2005 - Frank Leanez
//*********************

import java.sql.*;
import java.io.*;


public class DataMaintUC{
	
	
	//Maintenance Data Atributtes readed from DB:
	short readmaintid[];
	short readunitid[];
	short readfrommaint[];
	short readtomaint[];
	char readtype[];
	float readpmax[];
	float readpmin[];
	float readbeta[];
	boolean readinservice[];
	
	//General Unit Data Atributtes:
	short numreadmaint=0;  //Maximun rows to read from DB: 32768
	Connection mylink;	  //Connection object with opened DB
	
//------------------------------------------------------------------------------
//CONSTRUCTORS FOR "DataMaintUC"
//------------------------------------------------------------------------------

	//--Constr 1-- Creates link with specified DB:
	public DataMaintUC(Connection link) {
		mylink=link;
	}
	
	
	//--Constr 2-- Empty arg contructor:
	public DataMaintUC(){
	}
	
	
	
//------------------------------------------------------------------------------
//METHODS FOR READING DATA FROM TEXT FILES:
//------------------------------------------------------------------------------
	
	//--Method f1-- Read Data from Text File (.cvs files are only allowed by now)
	public void loaddatafromfile(String dirfileroot){
		
		//f1.1--Open text file:
		try{
			File inputFile = new File(dirfileroot+"\\maintedata.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			int cont=0;
			
		//f.1.2--Read lines:
			while ((line=in.readLine()) != null){
				String opoint[]=line.split(",");
				
				if (opoint.length!=8){
					break;
				}
				
				readmaintid[cont] = Short.valueOf(opoint[1]).shortValue();
				readunitid[cont] = Short.valueOf(opoint[2]).shortValue();
				readfrommaint[cont] = Short.valueOf(opoint[3]).shortValue();
				readtomaint[cont] = Short.valueOf(opoint[4]).shortValue();
				readfrommaint[cont] -= 1;
				readtomaint[cont] -= 1;
				
				String readtypetemp = opoint[1];
				readtype[cont] = readtypetemp.charAt(0);
				
				readpmax[cont] = Float.valueOf(opoint[5]).floatValue();
				readpmin[cont] = Float.valueOf(opoint[6]).floatValue();
				readbeta[cont] = Float.valueOf(opoint[7]).floatValue();
				//Read integer inservice and transform into a boolean:
				byte readinservtemp=Byte.valueOf(opoint[8]).byteValue();
				if (readinservtemp==1){
					readinservice[cont] = true;
				}else{
					readinservice[cont] = false;
				}
				cont++;
			}
			
		}catch(Exception ex){
			System.out.println("Unit Maintenance File Reading Exception: "+ex);
		}
		
	}
	
	//--Method f2-- Determination of Total Units in Text File (.cvs files are only allowed by now)
	public short datalines(String dirfileroot){
		int cont=0;
		try{
			File inputFile = new File(dirfileroot+"\\maintedata.csv");
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
//METHODS FOR CONSULT DATABASE TABLE "DataMaintUC"
//------------------------------------------------------------------------------

	//--Method 1-- Determination of Total Maintenance task in DB:
	public short numtotalmaint(){
		
		int totalid=0;
		String consult1="SELECT maintid FROM maintedata";
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
	public void inidatamaint(){
		
		readmaintid = new short[numreadmaint];
		readunitid = new short[numreadmaint];
		readfrommaint = new short[numreadmaint];
		readtomaint = new short[numreadmaint];
		readtype = new char[numreadmaint];
		readpmax = new float[numreadmaint];
		readpmin = new float[numreadmaint];
		readbeta = new float[numreadmaint];
		readinservice = new boolean[numreadmaint];
		
	}

	
	//--Method 4-- Filling Class Atributtes reading from DB:
	public void completeselect(){
		
		short cont=0;
		String consult1="Select * from maintedata";
		try {
			Statement com1 = mylink.createStatement();
			ResultSet res1 = com1.executeQuery(consult1);
			while (res1.next()) {
				readmaintid[cont] = res1.getShort("maintid");
				readunitid[cont] = res1.getShort("unitid");
				readfrommaint[cont] = res1.getShort("frommaint");
				readtomaint[cont] = res1.getShort("tomaint");
				readfrommaint[cont] -= 1;
				readtomaint[cont] -= 1;
				String readtypetemp = res1.getObject("type").toString();
				readtype[cont] = readtypetemp.charAt(0);
				//readtype[cont] = "co�o";
				readpmax[cont] = res1.getFloat("pmax");
				readpmin[cont] = res1.getFloat("pmin");
				readbeta[cont] = res1.getFloat("beta");
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