package cl.flconsulting.ucopt;

//****************************************************************************
//Network Security Optimization Sub-Problem
//Bender Cuts Methodology
//Initially Created: December 2005 - Frank Leanez
//****************************************************************************

import java.util.ArrayList;

public class UCBenderCut {
	
	//Auxiliary attributtes;
	short period;							//Auxiliary for time
	short numunits;							//Number of Generation Units
	
	//Benders Cut Atributtes:
	int numcuts=0;							//Benders cut counter
	ArrayList<Float>  simplex;			//Simplex Multipliers Vector
	ArrayList<Float>  objvalue;			//Objective Function Values
	ArrayList<Integer>   timeperiod;		//Time period when cut is active
	ArrayList<Integer> firstmult;		//First simplex multiplier index in simplex vector
	ArrayList<Integer> lastmult;			//Last simplex multiplier index in simplex vector
	ArrayList<Float> pit;				//Primal values (from master problem solution);
	
	//First Contructor (UC independant):
	public UCBenderCut(){
		numcuts=0;
                initCuts();
	}
	//Second Contructor (UC dependant):
	public UCBenderCut(short numgen){
		numcuts=0;
		numunits=numgen;
                initCuts();
	}
	//Third Contructor (UC dependant): -NO USAR-
//	public UCBenderCut(short numgen, short period, float simplexmult[], float objval){
//		numcuts=1;
//		numunits=numgen;
//		simplex.add(simplexmult);		//Unsafe
//		objvalue.addElement(objval); 			//Unsafe
//		timeperiod.addElement(period); 			//Unsafe
//	}
        
        private void initCuts(){
            simplex = new ArrayList<Float>();			//Simplex Multipliers Vector
            objvalue = new ArrayList<Float>();			//Objective Function Values
            timeperiod = new ArrayList<Integer>();		//Time period when cut is active
            firstmult = new ArrayList<Integer>();		//First simplex multiplier index in simplex vector
            lastmult = new ArrayList<Integer>();			//Last simplex multiplier index in simplex vector
            pit = new ArrayList<Float>();				//Primal values (from master problem solution);
        }
        
	public int getnumcuts(){
		return numcuts;
	}
	public int gettimeperiod(int cutnumber){
		//return Integer.valueOf(timeperiod.elementAt(cutnumber).toString()).intValue(); 	//PROVISIONAL
            return timeperiod.get(cutnumber);
	}
	public int getfirstmult(int cutnumber){
		//return Integer.valueOf(firstmult.elementAt(cutnumber).toString()).intValue(); 	//PROVISIONAL
            return firstmult.get(cutnumber);
        }
	public int getlastmult(int cutnumber){
		//return Integer.valueOf(lastmult.elementAt(cutnumber).toString()).intValue(); 	//PROVISIONAL
            return lastmult.get(cutnumber);
        }
	public float getobjvalue(int cutnumber){
		//return Float.valueOf(objvalue.elementAt(cutnumber).toString()).floatValue(); 	//PROVISIONAL
            return objvalue.get(cutnumber);
	}
	
	public float[] getobjarray(int tt){
		int cutfound=0;
		for (int kk=0;kk<numcuts;kk++){
			if (gettimeperiod(kk)==tt){
				cutfound++;
			}
		}
		float[] objarray = new float[cutfound];
		cutfound=0;
		for (int kk=0;kk<numcuts;kk++){
			if (gettimeperiod(kk)==tt){
				objarray[cutfound]=objvalue.get(kk);
				cutfound++;
			}
		}
		return objarray;
	}
	
	public float[] getsimplexarray(int cutnumber){
		int ini=getfirstmult(cutnumber);
		int last=getlastmult(cutnumber);
		float[] simplexarray = new float[last-ini+1];
		for (int kk=ini;kk<=last;kk++){
                    simplexarray[kk-ini]=simplex.get(kk);
		}
		return simplexarray;
	}
	public float[] getpitarray(int cutnumber){
		int ini=getfirstmult(cutnumber);
		int last=getlastmult(cutnumber);
		float[] pitarray = new float[last-ini+1];
		for (int kk=ini;kk<=last;kk++){
                    pitarray[kk-ini]=pit.get(kk);
		}
		return pitarray;
	}
	public float[] getsimplexmult(int ii,int tt){
		int cutfound=0;
		for (int kk=0;kk<numcuts;kk++){
			if (gettimeperiod(kk)==tt){
				cutfound++;
			}
		}
		float[] simplexarray = new float[cutfound];
		cutfound=0;
		for (int kk=0;kk<numcuts;kk++){
			if (gettimeperiod(kk)==tt){
				int first=getfirstmult(kk);
				int last =getlastmult(kk);
				simplexarray[cutfound]=simplex.get(first+ii);
				cutfound++;
			}
		}
		return simplexarray;
	}
	public int[] getcutid(int tt){
		int cutfound=0;
		for (int kk=0;kk<numcuts;kk++){
			if (gettimeperiod(kk)==tt){
				cutfound++;
			}
		}
		int[] idarray = new int[cutfound];
		cutfound=0;
		for (int kk=0;kk<numcuts;kk++){
			if (gettimeperiod(kk)==tt){
				idarray[cutfound]=kk;
				cutfound++;
			}
		}
		return idarray;
	}
	public float selectsimplexmult(int ii,int tt){
		float simplexfound=0f;
		for (int kk=0;kk<numcuts;kk++){
			if (gettimeperiod(kk)==tt){
				int first=getfirstmult(kk);
				simplexfound=simplex.get(first+ii);
			}
		}
		return simplexfound;
	}
	public void addnewcut(int period, float[] simplexmult, float objval, double[] ppri){
		int ii;
		int numsimplexmult=simplexmult.length;
		firstmult.get(simplex.size());	//Unsafe
		for (ii=0;ii<numsimplexmult;ii++){
			simplex.add(simplexmult[ii]);//Unsafe
			pit.add((float)ppri[ii]);			//Unsafe
		}
		lastmult.add(simplex.size()-1);	//Unsafe
		objvalue.add(objval); 			//Unsafe
		timeperiod.add(period); 			//Unsafe
		numcuts++;
	}
//Fourth Constructor (UC independant)
	public UCBenderCut(float[] obj, float[][] Amatrix, float[] UBvector, float[] 
		               LBvector, float[] RHSvector, String glpkdir, 
		               String glpkpifname, String glpkdosbat, String glpkpofname){
		
		/*
		//GLPK CALL:: FILE OPTION:
		int ii=0;
		FileOutputStream os;
		BufferedOutputStream bos;
		FileInputStream is1;
		BufferedReader is;
		runExternalFile rr = new runExternalFile();  //(INDEPENDANT)
		
		//1--Linear Optimization File Creation:
		try{
			os  = new FileOutputStream(glpkdir+"\\bin\\"+glpkpifname);
			bos = new BufferedOutputStream(os,32768);
			PrintStream myPrintStream = new PrintStream(bos, false);
			
			//1.1--Objective function Name:
			myPrintStream.println("NAME NETOPT");
			
			//2--Row Definition:
			myPrintStream.println("ROWS");
			myPrintStream.println(" N OBJ");
			for (ii=0; ii<numunits; ii++){
				myPrintStream.println(" E PNEW"+ii);
			}
			
			//3--Columns: Objective and Constraint Matrix "A":
			myPrintStream.println("COLUMNS");
			
			for (kk=0; kk<numvar; kk++){
				
				//3.1--Print Objective Function:
				if (kk>numvar-2*numbran){
				myPrintStream.println(" X"+kk+" OBJ "+1);
				}else{
				myPrintStream.println(" X"+kk+" OBJ "+0);
				}
				
				//3.2--Print Constraints:
				for (ii=0; ii<numconstr; ii++){
					if (ii<numunits){
						myPrintStream.println(" X"+kk+" PNEW"+ii+" "+Amatrix[ii][kk]);
					}else if(ii>=numunits && ii<numunits+numbus){
						myPrintStream.println(" X"+kk+" DCPF"+(ii-numunits)+" "+Amatrix[ii][kk]);
					}else if(ii>=numunits+numbus && ii<numunits+numbus+numbran){
						myPrintStream.println(" X"+kk+" DCFLOW"+(ii-numunits-numbus)+" "+Amatrix[ii][kk]);
					}else if(ii>=numunits+numbus+numbran && ii<numunits+numbus+2*numbran){
						myPrintStream.println(" X"+kk+" FMAX"+(ii-numunits-numbus-numbran)+" "+Amatrix[ii][kk]);
					}else if(ii>=numunits+numbus+2*numbran && ii<numvar){
						myPrintStream.println(" X"+kk+" FMIN"+(ii-numvar+numbran)+" "+Amatrix[ii][kk]);
					}
				}
				
				//3.3--Print Slack Voltage Angle Constraint:
				if (kk==numunits){
					myPrintStream.println(" X"+kk+" TSLACK "+1);
				}else{
					myPrintStream.println(" X"+kk+" TSLACK "+0);
				}
			
			}
			
			
			//4--Print RHS Vector:
			myPrintStream.println("RHS");
			for (ii=0;ii<numconstr;ii++){
				if (ii<numunits){
					myPrintStream.println(" B PNEW"+ii+" "+RHSvector[ii]);
				}else if(ii>=numunits && ii<numunits+numbus){
					myPrintStream.println(" B DCPF"+(ii-numunits)+" "+RHSvector[ii]);
				}else if(ii>=numunits+numbus && ii<numunits+numbus+numbran){
					myPrintStream.println(" B DCFLOW"+(ii-numunits-numbus)+" "+RHSvector[ii]);
				}else if(ii>=numunits+numbus+numbran && ii<numunits+numbus+2*numbran){
					myPrintStream.println(" B FMAX"+(ii-numunits-numbus-numbran)+" "+RHSvector[ii]);
				}else if(ii>=numunits+numbus+2*numbran && ii<numvar){
					myPrintStream.println(" B FMIN"+(ii-numvar+numbran)+" "+RHSvector[ii]);
				}
			}
			myPrintStream.println(" B TSLACK 0");
			
			
			//5--Print RANGE Vector: (it is included to create always feaseble results)
			myPrintStream.println("RANGES");
			myPrintStream.println(" RR DCPF0 "+2*tol_b);
			
			
			//6--Print BOUNDS Vector:
			myPrintStream.println("BOUNDS");
			for (ii=0;ii<numvar;ii++){
				myPrintStream.println(" LO LIM X"+ii+" "+LBvector[ii]);
				myPrintStream.println(" UP LIM X"+ii+" "+UBvector[ii]);
			}
			
			
			//7--Enddata:
			myPrintStream.println("ENDATA");
			myPrintStream.close();
			
		} catch(Exception e) {
			String message = e.toString();
			System.out.println("File error: "+message);
		}
		
		//6.3--Call GLPK executable:
	    try {
	    	//--Actual GLPK call-- (windows os and unix)
	    	//Dorun(glpkdosbat,glpkdosbat);  //(DEEPEDIT)
	    	rr.exec(glpkdosbat);			 //(INDEPENDANT)
	    	
	    } catch(Exception e) {
			String message = e.toString();
			System.out.println("Error Calling GLPK: "+message);
	    }
	    
		//6.4--Reading Results from File:
	    try {
			is1 = new FileInputStream(glpkdir+"\\bin\\"+glpkpofname);
			is = new BufferedReader(new InputStreamReader(is1));
			String ln;
			cont=0;
			int contunits=0;
			boolean feasprimal=true;
			while((ln = is.readLine()) != null) {
			    if (cont==4){
			    	StringTokenizer reslin = new StringTokenizer(ln);
			    	String resstatus1 = reslin.nextToken();
			    	String resstatus2 = reslin.nextToken();
			    	System.out.println("Primal Solution Status: "+resstatus2);
			    	if (resstatus2.equals("UNDEFINED")){
			    		feasprimal=false;
			    		break;
			    	}else if (resstatus2.equals("OPTIMAL")){
			    		feasprimal=true;
			    	}
			    }
			    if (feasprimal){
			    if(cont==5){
			    	StringTokenizer reslin = new StringTokenizer(ln);
			    	String res1;
			    	res1 = reslin.nextToken();
			    	res1 = reslin.nextToken();
			    	res1 = reslin.nextToken();
			    	res1 = reslin.nextToken();
			    	objfunction=Float.valueOf(res1).floatValue();
			    }
			    if (cont>5){
				    if (objfunction>0){
				    	cutneed=true;
					    if (cont>9 && cont<=9+numunits){
					    	StringTokenizer reslin = new StringTokenizer(ln);
					    	String res1;
					    	for (ii=0;ii<6;ii++){res1 = reslin.nextToken();}
					    	res1 = reslin.nextToken();
					    	if (res1.equals("<")){
					    		dualval[contunits]=0;
					    	}else{
					    		dualval[contunits]=Float.valueOf(res1).floatValue();;
					    	}
					    	contunits++;
					    }
				    }else{
				    	break;
				    }
			    }
			    }
				cont++;
			}
			is1.close();
			is.close();
			is =null;
	    } catch(Exception  e) {
			String message = e.toString();
			System.out.println("File error: "+message);
	    }		
		*/

		
		
	}

}