package cl.flconsulting.ucopt;

//*********************
//* Data Base Manager 
//* Initially Created: 29/01/2006 - Frank Leanez
//*********************

import java.sql.*;
import java.io.*;


public class DataSystemUC{
	
	
	//User Availability Data Atributtes readed from DB:
	//short readid[];
	//short readperiod[];
	float readspin[];
	float readprim[];
	float readstop[];
	float readpmax[];
	
	//General Unit Data Atributtes:
	short numreadsystem=0;  //Maximun rows to read from DB: 32768
	short numperiod=0;		//Rows permited to read (by user)
	Connection mylink;	    //Connection object with opened DB
	
//------------------------------------------------------------------------------
//CONSTRUCTORS FOR "DataUserUC"
//------------------------------------------------------------------------------

	//--Constr 1-- Creates link with specified DB:
	public DataSystemUC(Connection link) {
		mylink=link;
	}
	
	//--Constr 2-- Empty arg contructor:
	public DataSystemUC(){
	}
	
	
//------------------------------------------------------------------------------
//METHODS FOR READING DATA FROM TEXT FILES:
//------------------------------------------------------------------------------
	
	//--Method f1-- Read Data from Text File (.cvs files are only allowed by now)
	public void loaddatafromfile(String systemfileroot){
		
			
		//f1.1--Open text file:
		try{
			File inputFile = new File(systemfileroot+"\\systemdata.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			int cont=0;
			
		//f.1.2--Read lines:
			while ((line=in.readLine()) != null){
				String opoint[]=line.split(",");
				
				short periodtemp = Short.valueOf(opoint[1]).shortValue();
				if ((periodtemp-1)<numperiod){ //Data out of evaluation-period will be ignored
					readspin[periodtemp-1] = Float.valueOf(opoint[2]).floatValue();
					readprim[periodtemp-1] = Float.valueOf(opoint[3]).floatValue();
					readstop[periodtemp-1] = Float.valueOf(opoint[4]).floatValue();
					readpmax[periodtemp-1] = Float.valueOf(opoint[5]).floatValue();
				}
				cont++;
			}
			
		}catch(Exception ex){
			System.out.println("System Parameter Reading Exception: "+ex);
		}
		
	}
	
	//--Method f2-- Determination of Total Units in Text File (.cvs files are only allowed by now)
	public short datalines(String systemfileroot){
		int cont=0;
		try{
			File inputFile = new File(systemfileroot+"\\systemdata.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line=in.readLine()) != null){
				cont++;
			}
		}catch(Exception ex){
			System.out.println("Exception: "+ex);
		}
		numreadsystem=(short)cont;
		return numreadsystem;
	}
	
	
//------------------------------------------------------------------------------
//METHODS FOR "DataUserUC"
//------------------------------------------------------------------------------

	//--Method 1-- Unnecessary:
	public short numtotalsystem(){
		
		int totalid=0;
		String consult1="SELECT period FROM systemdata";
		try {
			Statement com1 = mylink.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet res1 = com1.executeQuery(consult1);
			res1.last();
			totalid=res1.getRow();
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
        numreadsystem=(short)totalid;
        return numreadsystem;
	
	}
	
	
	//--Method 2-- Objects Initialization:
	public void inidatasystem(short numperiodtemp){
		
		readspin= new float[numperiodtemp];
		readprim= new float[numperiodtemp];
		readstop= new float[numperiodtemp];
		readpmax= new float[numperiodtemp];
		numperiod=numperiodtemp;
		for (int tt=0;tt<numperiod;tt++){
			readspin[tt]= 0;
			readprim[tt]= 0;
			readstop[tt]= 0;
			readpmax[tt]= Float.MAX_VALUE;
		}
	
	}
	
	
	//--Method 3-- Filling Class Atributtes reading from DB:
	public void completeselect(){
		
		short cont=0;
		String consult1="Select * from systemdata";
		try {
			Statement com1 = mylink.createStatement();
			ResultSet res1 = com1.executeQuery(consult1);
			while (res1.next()) {
				short periodtemp = res1.getShort("period");
				if ((periodtemp-1)<numperiod){ //Data out of evaluation-period will be ignored
					readspin[periodtemp-1] = res1.getFloat("spinreserve");
					readprim[periodtemp-1] = res1.getFloat("primreserve");
					readstop[periodtemp-1] = res1.getFloat("stopreserve");
					readpmax[periodtemp-1] = res1.getFloat("pmax");
				}
			}
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
		
	}
	
	
	
}//end of Class