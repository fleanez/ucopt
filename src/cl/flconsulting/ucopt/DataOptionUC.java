package cl.flconsulting.ucopt;

//*********************
//* Data Base Manager 
//* Initially Created: 15/6/2008 - Frank Leanez
//*********************

import java.sql.*;
import java.io.*;


public class DataOptionUC{
		
	//General OPTIONS Atributtes readed from DB:
	String installdir;
	short numaxunits;
	short numperiods;
	short timediv;
	long itermax;
	long itermaxnet;
	long itertime;
	float tol_obj;
	float tol_dg;
	float tol_b;
	String priorsearch;
	String dbtype;
	String dbname;
	String dbloc;
	String dblog;
	String dbpass;
	String optimizer;
	String glpkdir;
	String glpklpmethod;
	String glpkpiftype;
	String glpkpifname;
	String glpkdosbat;
	String glpkpofname;
	String minosdir;
	String minoslineabat;
	String minosquadrabat;
	float subalfa;
	float subbeta;
	float c_coef;
	float epsaug;
	float tolset;
	int dispwidth;
	int dispheight;
	int dispsquare;
	boolean usefitto;
	boolean userepdetail;
	
	//Other load Data Atributtes:
	Connection mylink;	   	//Connection object with opened DB
	
	
//------------------------------------------------------------------------------
//CONSTRUCTORS FOR "DataLoadUC"
//------------------------------------------------------------------------------

	//--Constr 1-- Creates link with specified DB:
	public DataOptionUC (Connection link) {
		mylink=link;
	}
	
	
	//--Constr 2-- Empty arg contructor:
	public DataOptionUC(){
	}
	
	
	
//------------------------------------------------------------------------------
//METHODS FOR READING DATA FROM TEXT FILES:
//------------------------------------------------------------------------------
	
	//--Method f1--Set attributes:
	public void setoption(String optionname, Object newValue){
		
		String optionValue=(String)newValue;
		if (optionname.equals("installdir")){
			installdir=(String)newValue;
		}else if(optionname.equals("numaxunits")){
			numaxunits=Short.valueOf((String)newValue).shortValue();
		}
		
		
		
	}
	
	/*
	public void setoptionfloat(float newvalue){
		
		switch
		case newvalue:
		
	}

	
	public void setoptionstring(String newvalue){
		
		switch
		case newvalue:
		
	}
	
	*/
	
	
	
	//--Method f1-- Read Data from Text File (.cvs files are only allowed by now)
	public void loaddatafromfile(String loadfileroot){
		
			
		//f1.1--Open text file:
		try{
			File inputFile = new File(loadfileroot+"\\optiondata.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			int cont=0;
			
			//f.1.2--Read lines:
			while ((line=in.readLine()) != null){
				String opoint[]=line.split(",");
				
				/*Ejemplo de como leer datos
				readloadid[cont] = Short.valueOf(opoint[1]).shortValue();
				readname[cont] = opoint[2];
				readbusbarto[cont] = Short.valueOf(opoint[3]).shortValue();
				readpini[cont] = Float.valueOf(opoint[4]).floatValue();
				readctrla[cont] = opoint[6];
				//Read integer inservice and transform into a boolean:
				byte readinservtemp=Byte.valueOf(opoint[5]).byteValue();
				if (readinservtemp==1){
					readinservice[cont] = true;
				}else{
					readinservice[cont] = false;
				}
				cont++;
				*/
			}

			
		}catch(Exception e){
		}
	}

//------------------------------------------------------------------------------
//METHODS FOR "DataOptionUC"
//------------------------------------------------------------------------------
	
	
	//--Method 3-- Objects Initialization:
	public void inidataoption(){		
	}
	
	
	//--Method 4-- Filling Class Atributtes reading from DB:
	public void completeselect(){		
	}
	
	
	
}//end of Class