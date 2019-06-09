package cl.flconsulting.ucopt;

//*********************
//* Data Base Manager 
//* Initially Created: 17/8/2005 - Frank Leanez
//*********************

import java.sql.*;
import java.io.*;


public class DataLoadUC{
	
	
	//General Load Atributtes readed from DB:
	short readloadid[];
	String readname[];
	short readbusbarto[];
	float readpini[];
	boolean readinservice[];
	String readctrla[];
	
	//Specific Demand Curv Atributtes readed from DB:
	short readtime[];
	float readpdem[][];
	
	//Other load Data Atributtes:
	short numperiod;		//Rows permited to read (by user)
	int numreadperiod=0;    //Maximun rows to read from datacurv table in DB: 2^32
	short numreadload=0;  	//Maximun rows to read from dataload table in DB: 32768
	Connection mylink;	   	//Connection object with opened DB
	
	
//------------------------------------------------------------------------------
//CONSTRUCTORS FOR "DataLoadUC"
//------------------------------------------------------------------------------

	//--Constr 1-- Creates link with specified DB:
	public DataLoadUC (Connection link) {
		mylink=link;
	}
	
	
	//--Constr 2-- Empty arg contructor:
	public DataLoadUC(){
	}
	
	
	
//------------------------------------------------------------------------------
//METHODS FOR READING DATA FROM TEXT FILES:
//------------------------------------------------------------------------------
	
	//--Method f1-- Read Data from Text File (.cvs files are only allowed by now)
	public void loaddatafromfile(String loadfileroot){
		
			
		//f1.1--Open text file:
		try{
			File inputFile = new File(loadfileroot+"\\loaddata.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			int cont=0;
			
		//f.1.2--Read lines:
			while ((line=in.readLine()) != null){
				String opoint[]=line.split(",");
				
				
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
			}
			
			for (int ii=0; ii<numreadload; ii++){
				for (int tt=0; tt<numperiod; tt++){
					readpdem[ii][tt]=0;
				}
				if (readinservice[ii]){
					cont=0;
					
					File curveFile = new File(loadfileroot+"\\loadcurv.csv");
					BufferedReader intemp = new BufferedReader(new FileReader(curveFile));
					String lineintemp;
					while ((lineintemp=intemp.readLine()) != null){
						String opoint[]=lineintemp.split(",");
						
						short loadtempid=Short.valueOf(opoint[1]).shortValue();
						if (loadtempid==readloadid[ii]){
							short periodtemp=Short.valueOf(opoint[2]).shortValue();
							periodtemp-=1;
							if (periodtemp<numperiod){
								readpdem[ii][periodtemp]+=Float.valueOf(opoint[3]).floatValue();
							}
						}
					}
					
				}
			}
			
		}catch(Exception ex){
			System.out.println("Load Reading Exception: "+ex);
		}
		
	}
	
	//--Method f2-- Determination of Demands in Text File (.cvs files are only allowed by now)
	public short loaddatalines(String loadfileroot){
		int cont=0;
		try{
			File inputFile = new File(loadfileroot+"\\loaddata.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line=in.readLine()) != null){
				cont++;
			}
		}catch(Exception ex){
			System.out.println("Exception: "+ex);
		}
		numreadload=(short)cont;
		return numreadload;
		
	}
	
	//--Method f3-- Determination of Load Curve points in Text File (.cvs files are only allowed by now)
	public int curvedatalines(String curvefileroot){
		int cont=0;
		try{
			File inputFile = new File(curvefileroot+"\\loadcurv.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line=in.readLine()) != null){
				cont++;
			}
		}catch(Exception ex){
			System.out.println("Exception: "+ex);
		}
		numreadperiod=cont;
		return numreadperiod;
		
	}
	
	
//------------------------------------------------------------------------------
//METHODS FOR "DataLoadUC"
//------------------------------------------------------------------------------

	//--Method 1-- Determination of Total Loads (from DB):
	public short numtotalload(){
		
		int totalid=0;
		String consult1="SELECT loadid FROM loaddata";
		try {
			Statement com1 = mylink.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet res1 = com1.executeQuery(consult1);
			res1.last();
			totalid=res1.getRow();
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
        numreadload=(short)totalid;
        return numreadload;
	
	}
	
	
	//--Method 2-- Determination of Evaluation period (from DB):
	public int sizedemand(){
		
		int totalid=0;
		String consult1="SELECT loadid FROM loadcurv";
		try {
			Statement com1 = mylink.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet res1 = com1.executeQuery(consult1);
			res1.last();
			totalid=res1.getRow();
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
        numreadperiod=(short)totalid;
        return numreadperiod;
	
	}
	
	
	//--Method 3-- Objects Initialization:
	public void inidataload(short numperiodtemp){
		
		readloadid = new short[numreadload];
		readname = new String[numreadload];
		readbusbarto = new short[numreadload];
		readpini = new float[numreadload];
		readctrla = new String[numreadload];
		readinservice = new boolean[numreadload];
		readpdem = new float[numreadload][numperiodtemp];
		numperiod=numperiodtemp;
	
	}
	
	
	//--Method 4-- Filling Class Atributtes reading from DB:
	public void completeselect(){
		
		short cont=0;
		String consult1="Select * from loaddata";
		try {
			Statement com1 = mylink.createStatement();
			ResultSet res1 = com1.executeQuery(consult1);
			while (res1.next()) {
				readloadid[cont] = res1.getShort("loadid");
				readname[cont] = res1.getString("name");
				readbusbarto[cont] = res1.getShort("busbarto");
				readpini[cont] = res1.getFloat("pini");
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
			
			for (int ii=0; ii<numreadload; ii++){
				for (int tt=0; tt<numperiod; tt++){
					readpdem[ii][tt]=0;
				}
				if (readinservice[ii]){
					cont=0;
					String consult2="SELECT * FROM loadcurv WHERE loadid="+readloadid[ii];
					Statement com2 = mylink.createStatement();
					ResultSet res2 = com1.executeQuery(consult2);
					while (res2.next()) {
						short periodtemp=res2.getShort("period");
						periodtemp-=1;
						if (periodtemp<numperiod){
							readpdem[ii][periodtemp]+=res2.getFloat("pdem");
						}
					}
				}
			}
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
		
	}
	
	
	//--Method 5-- Get Total Active Power Demand in selected hour:
	public float gethourpdem(int selectedhour){
		float hourpdem=0f;
		for (int ii=0; ii<numreadload; ii++){
			hourpdem+=readpdem[ii][selectedhour];
		}
		return hourpdem;
	}
	
	
}//end of Class