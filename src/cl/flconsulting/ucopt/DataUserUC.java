package cl.flconsulting.ucopt;

//*********************
//* Data Base Manager 
//* Initially Created: 17/8/2005 - Frank Leanez
//*********************

import java.sql.*;
import java.io.*;

public class DataUserUC{
	
	
	//User Availability Data Atributtes readed from DB:
	short readuseroutid[];
	short readunitid[];
	short readfromout[];
	short readtoout[];
	char readtype[];
	float readpmax[];
	float readpmin[];
	float readbeta[];
	boolean readinservice[];
	boolean readmustrun[];
	
	//General Unit Data Atributtes:
	short numreaduserout=0;  //Maximun rows to read from DB: 32768
	Connection mylink;	   //Connection object with opened DB
	
//------------------------------------------------------------------------------
//CONSTRUCTORS FOR "DataUserUC"
//------------------------------------------------------------------------------

	//--Constr 1-- Creates link with specified DB:
	public DataUserUC(Connection link) {
		mylink=link;
	}
	
	//--Constr 2-- Empty arg contructor:
	public DataUserUC(){
	}
	
	
//------------------------------------------------------------------------------
//METHODS FOR READING DATA FROM TEXT FILES:
//------------------------------------------------------------------------------
	
	//--Method f1-- Read Data from Text File (.cvs files are only allowed by now)
	public void loaddatafromfile(String userfileroot){
		
			
		//f1.1--Open text file:
		try{
			File inputFile = new File(userfileroot+"\\userdata.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			int cont=0;
			
		//f.1.2--Read lines:
			while ((line=in.readLine()) != null){
				String opoint[]=line.split(",");
				
				if (opoint.length!=10){
					break;
				}
				
				readuseroutid[cont] = Short.valueOf(opoint[1]).shortValue();
				readunitid[cont] = Short.valueOf(opoint[2]).shortValue();
				readfromout[cont] = Short.valueOf(opoint[3]).shortValue();
				readtoout[cont] = Short.valueOf(opoint[4]).shortValue();
				readfromout[cont] -= 1;
				readtoout[cont] -= 1;
				String readtypetemp = opoint[5];
				readtype[cont] = readtypetemp.charAt(0);
				readpmax[cont] = Float.valueOf(opoint[6]).floatValue();
				readpmin[cont] = Float.valueOf(opoint[7]).floatValue();
				readbeta[cont] = Float.valueOf(opoint[8]).floatValue();
				//Read integer inservice and transform into a boolean:
				byte readinservtemp=Byte.valueOf(opoint[9]).byteValue();
				if (readinservtemp==1){
					readinservice[cont] = true;
				}else{
					readinservice[cont] = false;
				}
				byte readmustruntemp=Byte.valueOf(opoint[10]).byteValue();
				if (readmustruntemp==1){
					readmustrun[cont] = true;
				}else{
					readmustrun[cont] = false;
				}
				cont++;
			}
			
		}catch(Exception ex){
			System.out.println("User Outage File Reading Exception: "+ex);
		}
		
	}
	
	//--Method f2-- Determination of Total Units in Text File (.cvs files are only allowed by now)
	public short datalines(String userfileroot){
		int cont=0;
		try{
			File inputFile = new File(userfileroot+"\\userdata.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line=in.readLine()) != null){
				cont++;
			}
		}catch(Exception ex){
			System.out.println("Exception: "+ex);
		}
		numreaduserout=(short)cont;
		return numreaduserout;
	}
	
	
	

//------------------------------------------------------------------------------
//METHODS FOR "DataUserUC"
//------------------------------------------------------------------------------

	//--Method 1-- Determination of Units Availabilty introduced externally by user (from DB):
	public short numtotaluser(){
		
		int totalid=0;
		String consult1="SELECT useroutid FROM userdata";
		try {
			Statement com1 = mylink.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet res1 = com1.executeQuery(consult1);
			res1.last();
			totalid=res1.getRow();
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
        numreaduserout=(short)totalid;
        return numreaduserout;
	
	}
	
	
	//--Method 2-- Objects Initialization:
	public void inidatauser(){
		
		readuseroutid = new short[numreaduserout];
		readunitid = new short[numreaduserout];
		readfromout = new short[numreaduserout];
		readtoout = new short[numreaduserout];
		readtype = new char[numreaduserout];
		readpmax = new float[numreaduserout];
		readpmin = new float[numreaduserout];
		readbeta = new float[numreaduserout];
		readinservice = new boolean[numreaduserout];
		readmustrun = new boolean[numreaduserout];
		
	}

	
	//--Method 4-- Filling Class Atributtes reading from DB:
	public void completeselect(){
		
		short cont=0;
		String consult1="Select * from userdata";
		try {
			Statement com1 = mylink.createStatement();
			ResultSet res1 = com1.executeQuery(consult1);
			while (res1.next()) {
				readuseroutid[cont] = res1.getShort("useroutid");
				readunitid[cont] = res1.getShort("unitid");
				readfromout[cont] = res1.getShort("fromout");
				readtoout[cont] = res1.getShort("toout");
				readfromout[cont] -= 1;
				readtoout[cont] -= 1;
				String readtypetemp = res1.getObject("type").toString();
				readtype[cont] = readtypetemp.charAt(0);
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
				byte readmustruntemp=res1.getByte("mustrun");
				if (readmustruntemp==1){
					readmustrun[cont] = true;
				}else{
					readmustrun[cont] = false;
				}
				cont++;
			}
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
		
	}
	
	
	
}//end of Class