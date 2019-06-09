package cl.flconsulting.ucopt;

//*********************
//* Data Base Manager 
//* Initially Created: 17/8/2005 - Frank Leanez
//*********************

import java.sql.*;
import java.io.*;



public class DataUnitUC{
	
	
	//Unit Data Atributtes readed from DB:
	short readunitid[];
	short readunitcentralid[];
	String readname[];
	String readconfig[];
	float readsn[];
	float readpmax[];
	float readpmin[];
	float readauxserv[];
	float readalpha[];
	float readbeta[];
	float readgamma[];
	float readfuelprice[];
	short readtminup[];
	short readtmindown[];
	short readtstartcold[];
	short readtstartwarm[];
	short readtstarthot[];
	int readtrunini[];
	float readcstcold[];
	float readcstwarm[];
	float readcsthot[];
	float readgradup[];
	float readgraddown[];
	short readbusbarto[];  //Overrided by DeepEdit conectivity
	String readctrla[];
	boolean readinservice[];
	short heatdataini[];
	short heatdataend[];
	short readblock[];
	float readheatmax[];
	float readheatmin[];
	float readheatrate[];
	
	
	//General Unit Data Atributtes:
	short numreadunit=0;  //Maximun rows to read from DB: 32768
	short numreadheat=0;
	Connection mylink;	  //Connection object with opened DB
	
//------------------------------------------------------------------------------
//CONSTRUCTORS FOR "DataUnitUC"
//------------------------------------------------------------------------------

	//--Constr 1-- Creates link with specified DB:
	public DataUnitUC(Connection link){
		mylink=link;
	}
	
	//--Constr 2-- Empty arg contructor:
	public DataUnitUC(){
	}
	
	
//------------------------------------------------------------------------------
//METHODS FOR READING DATA FROM TEXT FILES:
//------------------------------------------------------------------------------
	
	//--Method f1-- Read Data from Text File (.cvs files are only allowed by now)
	public void loaddatafromfile(String unitfileroot){
		
			
		//f1.1--Open text file:
		try{
			File inputFile = new File(unitfileroot+"\\unitdata.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			int cont=0;
			
		//f.1.2--Read lines:
			while ((line=in.readLine()) != null){
				String opoint[]=line.split(",");
				
				readunitid[cont] = Short.valueOf(opoint[1]).shortValue();
				readunitcentralid[cont] = Short.valueOf(opoint[2]).shortValue();
				readname[cont] = opoint[3];
				readconfig[cont] = opoint[4];
				readsn[cont] = Float.valueOf(opoint[5]).floatValue();
				readpmax[cont] = Float.valueOf(opoint[6]).floatValue();
				readpmin[cont] = Float.valueOf(opoint[7]).floatValue();
				readauxserv[cont] = Float.valueOf(opoint[8]).floatValue();
				readalpha[cont] = Float.valueOf(opoint[9]).floatValue();
				readbeta[cont] = Float.valueOf(opoint[10]).floatValue();
				readgamma[cont] = Float.valueOf(opoint[11]).floatValue();
				readfuelprice[cont] = Float.valueOf(opoint[12]).floatValue();
				readtminup[cont] = Short.valueOf(opoint[13]).shortValue();
				readtmindown[cont] = Short.valueOf(opoint[14]).shortValue();
				readtstartcold[cont] = Short.valueOf(opoint[15]).shortValue();
				readtstarthot[cont] = Short.valueOf(opoint[16]).shortValue();
				readtrunini[cont] = Integer.valueOf(opoint[17]).intValue();
				readcstcold[cont] = Float.valueOf(opoint[18]).floatValue();
				readcstwarm[cont] = Float.valueOf(opoint[19]).floatValue();
				readcsthot[cont] = Float.valueOf(opoint[20]).floatValue();
				readgradup[cont] = Float.valueOf(opoint[21]).floatValue();;
				readgraddown[cont] = Float.valueOf(opoint[22]).floatValue();
				readbusbarto[cont] = Short.valueOf(opoint[23]).shortValue();
				readctrla[cont] = opoint[24];
				//Read integer inservice and transform into a boolean:
				int readinservtemp=Integer.valueOf(opoint[25]).intValue();
				if (readinservtemp==1){
					readinservice[cont] = true;
				}else{
					readinservice[cont] = false;
				}
				cont++;
			}
			
		}catch(Exception ex){
			System.out.println("Unit Data File Reading Exception: "+ex);
		}
		
	}
	
	//--Method f2-- Determination of Total Units in Text File (.cvs files are only allowed by now)
	public short datalines(String unitfileroot){
		int cont=0;
		try{
			File inputFile = new File(unitfileroot+"\\unitdata.csv");
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line;
			while ((line=in.readLine()) != null){
				cont++;
			}
		}catch(Exception ex){
			System.out.println("Exception: "+ex);
		}
		numreadunit=(short)cont;
		return numreadunit;
	}
	
	
	

//------------------------------------------------------------------------------
//METHODS FOR READING DATABASES:
//------------------------------------------------------------------------------

	//--Method d1-- Determination of Total Units in DB:
	public short numtotalunit(){
		
		int totalid=0;
		String consult1="Select unitid from unitdata";
		try {
			Statement com1 = mylink.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet res1 = com1.executeQuery(consult1);
			res1.last();
			totalid=res1.getRow();
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
        numreadunit=(short)totalid;
        return numreadunit;
	
	}
	
	
	//--Method d2-- Determination of Total Heat Rate Data dimension:
	public short numtotalheat(){
		
		int totalid=0;
		String consult1="Select unitid from unitheatrate";
		try {
			Statement com1 = mylink.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet res1 = com1.executeQuery(consult1);
			res1.last();
			totalid=res1.getRow();
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
        //numreadunit=(short)totalid;
  		numreadheat=(short)totalid;

        return numreadheat;
	
	}
	
	
	//--Method d3-- Read ID from BD:(deprecated)
	public int readidunit(int context) {
		
		String consult1="SELECT unitid FROM unitdata";
		int idactual=0;
		try {
			Statement com1 = mylink.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet res1 = com1.executeQuery(consult1);
			res1.absolute(context+1);
			idactual=res1.getInt("unitid");
			System.out.println ("ID actual: "+ idactual);
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
        return idactual;
        
	}
	
	
	//--Method d4-- Objects Initialization:
	public void inidataunit(){
		
		readunitid = new short[numreadunit];
		readunitcentralid = new short[numreadunit];
		readname = new String[numreadunit];
		readconfig = new String[numreadunit];
		readsn = new float[numreadunit];
		readpmax = new float[numreadunit];
		readpmin = new float[numreadunit];
		readauxserv = new float[numreadunit];
		readalpha = new float[numreadunit];
		readbeta = new float[numreadunit];
		readgamma = new float[numreadunit];
		readfuelprice= new float[numreadunit];
		readtminup = new short[numreadunit];
		readtmindown = new short[numreadunit];
		readtstartcold = new short[numreadunit];
		readtstartwarm = new short[numreadunit];
		readtstarthot = new short[numreadunit];
		readtrunini = new int[numreadunit];
		readcstcold = new float[numreadunit];
		readcstwarm = new float[numreadunit];
		readcsthot = new float[numreadunit];
		readgradup = new float[numreadunit];
		readgraddown = new float[numreadunit];
		readbusbarto = new short[numreadunit];
		readctrla = new String[numreadunit];
		readinservice = new boolean[numreadunit];
		
		//Heat related arrays:
		heatdataini = new short[numreadunit];
		heatdataend = new short[numreadunit];
		readblock=new short[numreadheat];
		readheatmax=new float[numreadheat];
		readheatmin=new float[numreadheat];
		readheatrate=new float[numreadheat];
		
	}

	
	//--Method d5- Filling Class Atributtes reading from DB:
	public void completeselect(){
		
		short cont=0;
		String consult1="Select * from unitdata";
		try {
			Statement com1 = mylink.createStatement();
			ResultSet res1 = com1.executeQuery(consult1);
			while (res1.next()) {
				readunitid[cont] = res1.getShort("unitid");
				readunitcentralid[cont] = res1.getShort("unitcentralid");
				readname[cont] = res1.getString("name");
				readconfig[cont] = res1.getString("config");
				readsn[cont] = res1.getFloat("sn");
				readpmax[cont] = res1.getFloat("pmax");
				readpmin[cont] = res1.getFloat("pmin");
				readauxserv[cont] = res1.getFloat("auxserv");
				readalpha[cont] = res1.getFloat("alpha");
				readbeta[cont] = res1.getFloat("beta");
				readgamma[cont] = res1.getFloat("gamma");
				readfuelprice[cont] = res1.getFloat("fuelprice");
				readtminup[cont] = res1.getShort("tminup");
				readtmindown[cont] = res1.getShort("tmindown");
				readtstartcold[cont] = res1.getShort("tstartcold");
				readtstarthot[cont] = res1.getShort("tstarthot");
				readtrunini[cont] = res1.getInt("trunini");
				readcstcold[cont] = res1.getFloat("cstcold");
				readcstwarm[cont] = res1.getFloat("cstwarm");
				readcsthot[cont] = res1.getFloat("csthot");
				readgradup[cont] = res1.getFloat("gradup");
				readgraddown[cont] = res1.getFloat("graddown");
				readbusbarto[cont] = res1.getShort("busbarto");
				readctrla[cont] = res1.getString("ctrla");
				//Read integer inservice and transform into a boolean:
				byte readinservtemp=res1.getByte("inservice");
				if (readinservtemp==1){
					readinservice[cont] = true;
				}else{
					readinservice[cont] = false;
				}
				//System.out.println ("beta["+cont+"]="+readbeta[cont]);
				cont++;
			}
			
			cont=0;
			for (int ii=0; ii<numreadunit; ii++){
				heatdataini[ii]=cont;
				if (readinservice[ii]){
					
					String consult2="SELECT * FROM unitheatrate WHERE unitid="+readunitid[ii];
					Statement com2 = mylink.createStatement();
					ResultSet res2 = com1.executeQuery(consult2);
					
					while (res2.next()) {
						readblock[cont]=res2.getShort("block");
						readheatmax[cont]=res2.getFloat("outpmax");
						readheatmin[cont]=res2.getFloat("outpmin");
						readheatrate[cont]=res2.getFloat("heatrate");
						cont++;
					}
					
				}
				heatdataend[ii]=cont;
			}
        } catch(SQLException ex) {
        	System.err.println("SQLException: " + ex.getMessage());
        }
		
	}
	
	//--Method d6-- Heat Retriving Funtions:
	public float getblockpmax(int ii, int blockid){
		float blockmax=0;
		for (int kk=heatdataini[ii];kk<heatdataend[ii];kk++){
			if (readblock[kk]==blockid){
				blockmax=readheatmax[kk];
				break;
			}
		}
		return blockmax;
	}
	public float getblockpmin(int ii, int blockid){
		float blockmin=0;
		for (int kk=heatdataini[ii];kk<heatdataend[ii];kk++){
			if (readblock[kk]==blockid){
				blockmin=readheatmin[kk];
				break;
			}
		}
		return blockmin;
	}
	public float getblockheat(int ii, int blockid){
		float blockheat=0;
		for (int kk=heatdataini[ii];kk<heatdataend[ii];kk++){
			if (readblock[kk]==blockid){
				blockheat=readheatrate[kk];
				break;
			}
		}
		return blockheat;
	}
	public int getnumblocks(int ii){
		return heatdataend[ii]-heatdataini[ii];
	}
	
	
}//end of Class