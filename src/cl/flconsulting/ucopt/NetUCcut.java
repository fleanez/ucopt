package cl.flconsulting.ucopt;

//****************************************************************************
//Network Security Optimization Sub-Problem
//Bender Cuts Methodology
//Initially Created: November 2005 - Frank Leanez
//****************************************************************************
import java.io.*;
import java.util.StringTokenizer;

//public class networkcut extends AnalysisTool{		//(DEEPEDIT)
public class NetUCcut {								//(INDEPENDANT)

    //Data Atributtes:
    short numbran;
    short numunits;
    short numbus;
    short numbusloads;
    short numloads;
    short numbusgen;
    short numperiods;
    float sref = 100;  								//(INDEPENDANT)
    float tol_b = 0.01f;							//(INDEPENDANT)
    int busid[];
    int branchid[];
    int genid[];
    int busgenid[];
    int busloadid[];
    float pload[][];
    //float netbusp[];
    //short pact[];
    //byte ubin[];
    int busfrom[];
    int busto[];
    float BranchX[];
    float Bmattemp[][];
    float Flowmax[];
    float dualval[];
    UCBenderCut bendercut;	//Bender Cut Object
    DataMaintnetUC netmaint;  //Programmed maintenance vector

    //Internal Optimization Matrices:
    int numvar;			//Total Subproblem Variables
    int numconstr;		//Total Subproblem Constraints
    float Amatrix[][];
    float RHSvector[];

//------------------------------------------------------------------------------
//First Class Constructor:
//------------------------------------------------------------------------------
    //public networkcut( short numperiods, DataNetUC mydbnet, DataUnitUC mydbunit, DataLoadUC mydbload, Schematic sch){}//(DEEPEDIT)
    public NetUCcut(short numunitstemp, short numtt, DataNetUC mydbnet, DataUnitUC mydbunit,
            DataLoadUC mydbload, DataMaintnetUC mynetmaint) {	//(INDEPENDANT)

        //1--Create Network Arrays from DB:
        numbran = mydbnet.numtotalnet();
        numunits = numunitstemp;
        numperiods = numtt;
        int contbus = 0;
        int BranchI[] = new int[numbran];
        int BranchJ[] = new int[numbran];
        busfrom = new int[numbran];
        busto = new int[numbran];
        int busidtemp[] = new int[2 * numbran];  //Temp bus id register (2*numbran is its maximum)
        BranchX = new float[numbran];
        Flowmax = new float[numbran];
        for (int ii = 0; ii < numbran; ii++) {
            BranchX[ii] = mydbnet.readx[ii];
            BranchI[ii] = mydbnet.readbusbarfrom[ii];
            BranchJ[ii] = mydbnet.readbusbarto[ii];
            Flowmax[ii] = mydbnet.readpmax[ii];
            System.out.println("Flowmax[" + ii + "]=" + Flowmax[ii]);
        }

        //2--Asign (also new numbering) Busbar vector:
        for (int ii = 0; ii < numbran; ii++) {
            if (ii == 0) {
                busidtemp[0] = BranchI[ii];
                busidtemp[1] = BranchJ[ii];
                contbus = 2;
            } else {
                boolean foundI = false, foundJ = false;;
                for (int kk = 0; kk < contbus; kk++) {
                    if (BranchI[ii] == busidtemp[kk]) {
                        foundI = true;
                    }
                    if (BranchJ[ii] == busidtemp[kk]) {
                        foundJ = true;
                    }
                }
                if (!foundI) {
                    busidtemp[contbus] = BranchI[ii];
                    contbus++;
                }
                if (!foundJ) {
                    busidtemp[contbus] = BranchJ[ii];
                    contbus++;
                }
            }
        }

        //3--Re-Asign new buses id:
        numbus = (short) contbus;
        busid = new int[numbus];
        for (int ii = 0; ii < numbus; ii++) {
            busid[ii] = busidtemp[ii];
            System.out.println("Busid[" + ii + "]=" + busid[ii]);
        }
        for (int ii = 0; ii < numbran; ii++) {
            busfrom[ii] = getnewbus(BranchI[ii]);
            busto[ii] = getnewbus(BranchJ[ii]);
        }

        //4--Create Suceptance Array (B Matrix):
        createB();

        //5--Register special conditions and maintenance:
        netmaint = mynetmaint;

        //6--Generator busbar location vector:
        busgenid = new int[numunits];
        for (int ii = 0; ii < numunits; ii++) {
            busgenid[ii] = getnewbus(mydbunit.readbusbarto[ii]);
        }

        //7--Load busbar location vector:
        numloads = mydbload.numtotalload();
        busloadid = new int[numloads];
        //netbusp=new float[numbus];
        for (int ii = 0; ii < numloads; ii++) {
            busloadid[ii] = getnewbus(mydbload.readbusbarto[ii]);
        }

        //8--Read load curv:
        pload = new float[numloads][numperiods];
        for (int tt = 0; tt < numperiods; tt++) {
            for (int ii = 0; ii < numloads; ii++) {
                pload[ii][tt] = mydbload.readpdem[ii][tt];
                System.out.println("pload[" + ii + "][" + tt + "]=" + pload[ii][tt]);
            }
        }

        //9--Create Initial A Constraint Matrix:
        createA();

        //10--Initialize Benders Cut SubClass:
        bendercut = new UCBenderCut(numunits);

    }

//------------------------------------------------------------------------------
//Main Methods:
//------------------------------------------------------------------------------
    //Calcule cuts:
    public short calculatecuts(String LPmethod, byte ubin[][], double pact[][],
            String glpkdir, String glpkpifname, String glpkdosbat,
            String glpkpofname, String optimizer, short typen1,
            float tolbtemp, String minoslineabat) {

        float tol_b = tolbtemp;
        boolean cutneed = false;						//True when violations are detected;
        int ii, kk, tt, cont, ss = 0;						//Counters
        short contcut = 0;							//Benders Cut Counter
        short subprob;								//Number of Subproblems (including n-1)
        int branchout;								//Out of service Line Id
        double objcoef[] = new double[numvar];		//Objective function coef
        double RHS[] = new double[numconstr];			//Right hand side vector
        //byte constrtype[]=new byte[numconstr];	//Constraint type: 0=equality, 1=morethan
        double LBvector[] = new double[numvar];		//Lower bound vector
        double UBvector[] = new double[numvar];		//Upper bound vector
        //double Acoef[];							//Non-Cero Amatrix Elements Vector
        //int Arow[];								//Non-Cero Amatrix Row Index Vector
        //int Acol[];								//Non-Cero Amatrix Column Index Vector
        dualval = new float[numunits];				//Dual Simplex Variables for P duplication
        float objfunction = 0;						//Objective Function Value

        FileOutputStream os;
        BufferedOutputStream bos;
        FileInputStream is1;
        BufferedReader is;
        runExternalFile rr = new runExternalFile();  //(INDEPENDANT)

        for (tt = 0; tt < numperiods; tt++) {

            //==================================
            //TRANSMISION SUBPROBLEM FORMULATION:
            //==================================
            //1- Create objective function vector
            //2- Include changes in A & B for maintenance
            //3- Determine 
            //4- Calculate RHS vector
            //5- Undo changes to A & B for maintenance
            //6- 
            //1--Create/update Bounds
            for (kk = 0; kk < numvar; kk++) {
                if (kk >= numvar - 2 * numbran) {
                    LBvector[kk] = 0;
                } else {
                    LBvector[kk] = -100000f;
                }
                UBvector[kk] = 100000f;
            }

            //2--Determine number of subproblems:
            if (typen1 == 0) {
                subprob = 1;
            } else if (typen1 == 1) {
                subprob = 2;
            } else if (typen1 == 2) {
                subprob = 1;
            } else if (typen1 == 3) {
                subprob = numbran;
                subprob += 1;
            } else {
                subprob = 1;
            }

            for (ss = 0; ss < subprob; ss++) {
                cutneed = false;

                //2--Determine Line out of service: (branchout=-1 is the steady state case)
                if (typen1 == 3) {
                    branchout = ss - 1;
                } else {
                    branchout = (-1);
                }

                //3--RHS Vector Creation/Modification:
                createRHS(tt, ubin, pact, branchout);

                //4--Modify Bmatrix & Amatrix:
                if (branchout >= 0) {
                    extractlinefromB(branchout);
                    refreshDCLFAmatrix();
                    ignoreflowAmatrix(branchout);
                }

                //10-----GLPK OPTIMIZATION CALL (FILE OPTION)-----
                if (optimizer.equals("glpk")) {

                    //10.1--Linear Optimization File Creation:
                    try {
                        os = new FileOutputStream(glpkdir + "\\bin\\" + glpkpifname);
                        bos = new BufferedOutputStream(os, 32768);
                        PrintStream myPrintStream = new PrintStream(bos, false);

                        //1--Objective function Name:
                        myPrintStream.println("NAME NETOPT");

                        //2--Row Definition:
                        myPrintStream.println("ROWS");
                        myPrintStream.println(" N OBJ");

                        for (ii = 0; ii < numunits; ii++) {
                            myPrintStream.println(" E PNEW" + ii);
                        }
                        for (ii = 0; ii < numbus; ii++) {
                            if (ii == 0) {
                                myPrintStream.println(" E DCPF" + ii);
                            } else {
                                myPrintStream.println(" E DCPF" + ii);
                            }
                        }
                        for (ii = 0; ii < numbran; ii++) {
                            if (branchout == ii) {				//PROVICIONAL!!!
                                myPrintStream.println(" L DCFLOW" + ii);
                            } else {
                                myPrintStream.println(" E DCFLOW" + ii);
                            }
                        }
                        for (ii = 0; ii < numbran; ii++) {
                            myPrintStream.println(" L FMAX" + ii);
                        }
                        for (ii = 0; ii < numbran; ii++) {
                            myPrintStream.println(" L FMIN" + ii);
                        }
                        myPrintStream.println(" E TSLACK");

                        //3--Columns: Objective and Constraint Matrix "A":
                        myPrintStream.println("COLUMNS");

                        for (kk = 0; kk < numvar; kk++) {

                            //3.1--Print Objective Function:
                            if (kk >= numvar - 2 * numbran) {
                                myPrintStream.println(" X" + kk + " OBJ " + 1);
                            } else {
                                myPrintStream.println(" X" + kk + " OBJ " + 0);
                            }

                            //3.2--Print Constraints:
                            for (ii = 0; ii < numconstr; ii++) {
                                if (ii < numunits) {
                                    myPrintStream.println(" X" + kk + " PNEW" + ii + " " + Amatrix[ii][kk]);
                                } else if (ii >= numunits && ii < numunits + numbus) {
                                    myPrintStream.println(" X" + kk + " DCPF" + (ii - numunits) + " " + Amatrix[ii][kk]);
                                } else if (ii >= numunits + numbus && ii < numunits + numbus + numbran) {
                                    myPrintStream.println(" X" + kk + " DCFLOW" + (ii - numunits - numbus) + " " + Amatrix[ii][kk]);
                                } else if (ii >= numunits + numbus + numbran && ii < numunits + numbus + 2 * numbran) {
                                    myPrintStream.println(" X" + kk + " FMAX" + (ii - numunits - numbus - numbran) + " " + Amatrix[ii][kk]);
                                } else if (ii >= numunits + numbus + 2 * numbran && ii < numvar) {
                                    myPrintStream.println(" X" + kk + " FMIN" + (ii - numvar + numbran) + " " + Amatrix[ii][kk]);
                                }
                            }

                            //3.3--Print Slack Voltage Angle Constraint:
                            if (kk == numunits) {
                                myPrintStream.println(" X" + kk + " TSLACK " + 1);
                            } else {
                                myPrintStream.println(" X" + kk + " TSLACK " + 0);
                            }

                        }

                        //4--Print RHS Vector:
                        myPrintStream.println("RHS");
                        for (ii = 0; ii < numconstr; ii++) {
                            if (ii < numunits) {
                                myPrintStream.println(" B PNEW" + ii + " " + RHSvector[ii]);
                            } else if (ii >= numunits && ii < numunits + numbus) {
                                myPrintStream.println(" B DCPF" + (ii - numunits) + " " + RHSvector[ii]);
                            } else if (ii >= numunits + numbus && ii < numunits + numbus + numbran) {
                                myPrintStream.println(" B DCFLOW" + (ii - numunits - numbus) + " " + RHSvector[ii]);
                            } else if (ii >= numunits + numbus + numbran && ii < numunits + numbus + 2 * numbran) {
                                myPrintStream.println(" B FMAX" + (ii - numunits - numbus - numbran) + " " + RHSvector[ii]);
                            } else if (ii >= numunits + numbus + 2 * numbran && ii < numvar) {
                                myPrintStream.println(" B FMIN" + (ii - numvar + numbran) + " " + RHSvector[ii]);
                            }
                        }
                        myPrintStream.println(" B TSLACK 0");

                        //5--Print RANGE Vector: (it is included to create always feasible results)
                        myPrintStream.println("RANGES");
                        myPrintStream.println(" RR DCPF0 " + 2 * tol_b);

                        //6--Print BOUNDS Vector:
                        myPrintStream.println("BOUNDS");
                        for (ii = 0; ii < numvar; ii++) {
                            myPrintStream.println(" LO LIM X" + ii + " " + LBvector[ii]);
                            myPrintStream.println(" UP LIM X" + ii + " " + UBvector[ii]);
                        }

                        //7--Enddata:
                        myPrintStream.println("ENDATA");
                        myPrintStream.close();

                    } catch (Exception e) {
                        String message = e.toString();
                        System.out.println("File error: " + message);
                    }

                    //10.2--Call GLPK executable:
                    try {
                        //--Actual GLPK call-- (windows os and unix)
                        //Dorun(glpkdosbat,glpkdosbat);  //(DEEPEDIT)
//                        rr.exec(glpkdosbat);			 //(INDEPENDANT)
                        runExternalFile.doRun("cmd /C " + File.separator + glpkdosbat, glpkdosbat, false);
                    } catch (Exception e) {
                        String message = e.toString();
                        System.out.println("Error Calling GLPK: " + message);
                    }

                    //10.3--Reading Results from File:
                    try {
                        is1 = new FileInputStream(glpkdir + "\\bin\\" + glpkpofname);
                        is = new BufferedReader(new InputStreamReader(is1));
                        String ln;
                        cont = 0;
                        int contunits = 0;
                        boolean feasprimal = true;
                        while ((ln = is.readLine()) != null) {
                            if (cont == 4) {
                                StringTokenizer reslin = new StringTokenizer(ln);
                                String resstatus1 = reslin.nextToken();
                                String resstatus2 = reslin.nextToken();
                                System.out.println("Primal Solution Status: " + resstatus2);
                                if (resstatus2.equals("UNDEFINED")) {
                                    feasprimal = false;
                                    break;
                                } else if (resstatus2.equals("OPTIMAL")) {
                                    feasprimal = true;
                                }
                            }
                            if (feasprimal) {
                                if (cont == 5) {
                                    StringTokenizer reslin = new StringTokenizer(ln);
                                    String res1;
                                    res1 = reslin.nextToken();
                                    res1 = reslin.nextToken();
                                    res1 = reslin.nextToken();
                                    res1 = reslin.nextToken();
                                    objfunction = Float.valueOf(res1).floatValue() * sref;
                                }
                                if (cont > 5) {
                                    StringTokenizer reslin = new StringTokenizer(ln);
                                    String res1;
                                    if (objfunction > tol_b) {
                                        cutneed = true;
                                        if (cont > 9 && cont <= 9 + numunits) {
                                            for (ii = 0; ii < 6; ii++) {
                                                res1 = reslin.nextToken();
                                            }
                                            res1 = reslin.nextToken();
                                            if (res1.equals("<")) {
                                                dualval[contunits] = 0;
                                            } else {
                                                dualval[contunits] = Float.valueOf(res1).floatValue();
                                            }
                                            contunits++;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                            cont++;
                        }
                        is1.close();
                        is.close();
                        is = null;
                    } catch (Exception e) {
                        String message = e.toString();
                        System.out.println("File error: " + message);
                    }

                    //11----MINOS OPTIMIZATION SOFTWARE------
                } else if (optimizer.equals("minos")) {

                    //int numres=1; //Provicional!!!
                    try {
                        os = new FileOutputStream("./economic.dat");
                        bos = new BufferedOutputStream(os, 32768);
                        PrintStream myPrintStream = new PrintStream(bos, false);

                        //11.1--Linear Optimization File Creation:
                        //Num of Uncertanties:
                        myPrintStream.println(numvar);
                        myPrintStream.println(" ");

                        //Num of Constraints:
                        myPrintStream.println(numconstr + 1);
                        myPrintStream.println(" ");

                        //Linear Objective Function Coeficients:
                        for (kk = 0; kk < numvar; kk++) {
                            if (kk >= numvar - 2 * numbran) {
                                myPrintStream.println("1");
                            } else {
                                myPrintStream.println("0");
                            }
                        }
                        myPrintStream.println(" ");

                        //"Right-Hand" constraint vector:
                        for (kk = 0; kk < numconstr; kk++) {
                            if (kk == 0) {
                                myPrintStream.println(RHSvector[kk]);//+tol_b);
                            } else {
                                myPrintStream.println(RHSvector[kk]);
                            }
                        }
                        myPrintStream.println(RHSvector[0] + tol_b); 	//Range for feasebility
                        myPrintStream.println(" ");

                        //Constraint Matrix "A":
                        for (kk = 0; kk < numconstr + 1; kk++) {
                            if (kk < numconstr) {
                                for (ii = 0; ii < numvar; ii++) {
                                    myPrintStream.println(Amatrix[kk][ii]);
                                }
                            } else {										//Range for feasebility
                                for (ii = 0; ii < numvar; ii++) {
                                    if (ii == 0) {
                                        myPrintStream.println("1.0");
                                    } else {
                                        myPrintStream.println("0.0");
                                    }
                                }
                            }
                            myPrintStream.println(" ");
                        }
                        myPrintStream.println(" ");

                        /*Constraints type definition:
				*Equality		=1
				*Greater than	=2
				*Lower than		=3
				
				* Asignacion de Tipo de Restriccion (Segun el DeepEdit)
				*   vec_clave[i]=1:   (=)
				*   vec_clave[i]=2:   (<=)
				*   vec_clave[i]=3:   (>=)
                         */
                        for (kk = 0; kk < numconstr + 1; kk++) {
                            if (kk < numbus + numunits + numbran && kk != 0) {
                                if ((kk - numbus + numunits) == branchout) {
                                    myPrintStream.println(2);
                                } else {
                                    myPrintStream.println(1);
                                }
                            } else if (kk == (numconstr - 1)) {
                                myPrintStream.println(1);
                                //Establish Range:
                            } else if (kk == 0) {
                                myPrintStream.println(3);
                            } else if (kk == numconstr) {
                                myPrintStream.println(2);
                            } else {
                                myPrintStream.println(2);
                            }
                        }
                        myPrintStream.println(" ");

                        //Lower Bounds:
                        for (kk = 0; kk < numvar; kk++) {
                            myPrintStream.println(LBvector[kk]);
                        }
                        myPrintStream.println(" ");

                        //Upper Bounds:
                        for (kk = 0; kk < numvar; kk++) {
                            myPrintStream.println(UBvector[kk]);
                        }
                        myPrintStream.println(" ");

                        //Optimization flag (min, max):
                        myPrintStream.println("min");
                        myPrintStream.println("NET");
                        myPrintStream.close();

                        //12.2--Hessian File Creation: (DEPRECATED)
                        /*
				os  = new FileOutputStream("./matw.dat");
				bos = new BufferedOutputStream(os,32768);
				myPrintStream = new PrintStream(bos, false);
				
				//Num of Uncertanties:
				myPrintStream.println(numvar);
				myPrintStream.println(" ");
				//Hessian Matrix (null for LP)
				for (ii=0; ii<numvar; ii++){
					for (kk=0; kk<numvar; kk++){
						myPrintStream.println(0.0);
					}
					myPrintStream.println(" ");
				}
				myPrintStream.close();
				//myPrintStream.println (" ");
                         */
                    } catch (Exception e) {
                        System.out.println("File error");
                    }

                    //12.3--Call Minos Executable:
                    System.out.println("Call Minos");
                    try {
                        //Dorun(minoslineabat,minoslineabat); 	//(DEEPEDIT)
//                        rr.exec(minoslineabat);			 		//(INDEPENDANT)
                        runExternalFile.doRun("cmd /C " + File.separator + minoslineabat, "cuadra > sys.log", false);
                    } catch (Exception e) {
                        String message = e.toString();
                    }

                    //12.4--Reading Results from File:
                    try {
                        kk = 0;
                        is1 = new FileInputStream("./soluci.dat");
                        is = new BufferedReader(new InputStreamReader(is1));
                        String ln;
                        int contdual = 0;
                        while ((ln = is.readLine()) != null) {
                            if (kk != 0) {
                                if (objfunction > tol_b) {
                                    cutneed = true;
                                    if (kk > numvar && kk <= numvar + numunits) {
                                        dualval[contdual] = Float.valueOf(ln).floatValue();
                                        contdual++;
                                    }

                                }
                            } else {
                                objfunction = Float.valueOf(ln).floatValue() * sref;
                            }
                            kk++;
                        }
                        is.close();
                        is = null;

                    } catch (Exception e) {
                        String message = e.toString();
                        System.out.println("Error: " + message);
                    }
                }

                /*13----GLPK OPTIMIZATION CALL (DIRECT CALLABLE)-----(DEPRECATED FOR A WHILE)
				
				//1--Buid objective function coeficients:
				for (kk=0;kk<numvar;kk++){
					if (kk<numunits+numbus+numbran){
						objcoef[kk]=0;
					}else{
						objcoef[kk]=1;
					}
				}
				2--Buid contraint A matrix (Takes only non-cero coef from Amatrix) PROVICIONAL!!
				for (ii=0;ii<numconstr;ii++){
					for (kk=0;kk<numvar;kk++){
						if (Amatrix[ii][kk]!=0){
							cont++;
						}
					}
				}
				int numAnotnull=cont;
				cont=0;
				Acoef=new double[numAnotnull];
				Arow=new int[numAnotnull];
				Acol=new int[numAnotnull];
				for (ii=0;ii<numconstr;ii++){
					for (kk=0;kk<numvar;kk++){
						if (Amatrix[ii][kk]!=0){
						Acoef[cont]=(double)Amatrix[ii][kk];
						Arow[cont]=ii;
						Acol[cont]=kk;
						cont++;
						}
					}
				}
				//3--Build RHS vector:
				createRHS(tt,ubin,pact);
				for (ii=0;ii<numconstr;ii++){
					RHS[ii]=(double)RHSvector[ii];
					System.out.println ("RHS: "+RHS[ii]);
				}
				
				//4--Build Lower/Upper Bound vectors:
				for (kk=0;kk<numvar;kk++){
					if (kk>=numvar-2*numbran){
						LBvector[kk]=0;
					}else{
						LBvector[kk]=-100000f;
					}
					UBvector[kk]=100000f;
				}
				
				//GLPK CALL (DEPRECARTED FOR A WHILE):
				5--Constraint type definition:
				for (ii=0;ii<numvar;ii++){
					if (ii<numunits+numbus+numbran){
						constrtype[ii]=0;
					}else{
						constrtype[ii]=1;
					}
				}
				//6--Call GLPK from runGLPKsol class native interface:
				try{
					runGLPKsol runglpk = new runGLPKsol();
					runglpk.exec(glpkdir,LPmethod,objcoef,Arow,Acol,Acoef,RHS,constrtype,LBvector,UBvector);
				}catch(Exception e){
					String message = e.toString();
					System.out.println("Error Calling GLPK: "+message);
				}
				//7--Retrieve Optimization Results:
                 */
                //13--Include recently computed Bender CUT in respective class:
                if (cutneed) {
                    double[] pprovi = new double[numunits];
                    for (ii = 0; ii < numunits; ii++) {
                        pprovi[ii] = pact[ii][tt];
                    }
                    bendercut.addnewcut(tt, dualval, objfunction, pprovi);
                    contcut++;
                } else {
                    //contcut=0;
                }

                //14--Undo Modifications to Bmatrix & Amatrix:
                if (branchout >= 0) {
                    includelineinB(branchout);
                    refreshDCLFAmatrix();
                    includeflowAmatrix(branchout);
                }

            }//end of ss (for each subprob)
        }//end of tt (for each time)

        return contcut;

    }

//Return all existing Bender CUTs - Including de last added
    public UCBenderCut returnlastcut() {
        return bendercut;
    }

//Return internal class busbar numeration:
    public short getnewbus(int branchnode) {
        short cont = 0;
        while (cont < numbus) {
            if (busid[cont] == branchnode) {
                break;
            } else {
                cont++;
            }
        }
        return cont;
    }

//Susceptance "B" Matrix Methods:	
    public void createB() {
        Bmattemp = new float[numbus][numbus];
        for (int ii = 0; ii < numbran; ii++) {
            Bmattemp[busfrom[ii]][busto[ii]] += (-1 / BranchX[ii]);
            Bmattemp[busto[ii]][busfrom[ii]] += (-1 / BranchX[ii]);
            Bmattemp[busfrom[ii]][busfrom[ii]] += (1 / BranchX[ii]);
            Bmattemp[busto[ii]][busto[ii]] += (1 / BranchX[ii]);
        }
    }

    public void extractlinefromB(int ii) {
        Bmattemp[busfrom[ii]][busto[ii]] += (1 / BranchX[ii]) + tol_b;
        Bmattemp[busto[ii]][busfrom[ii]] += (1 / BranchX[ii]) + tol_b;
        Bmattemp[busfrom[ii]][busfrom[ii]] += (-1 / BranchX[ii]) + tol_b;
        Bmattemp[busto[ii]][busto[ii]] += (-1 / BranchX[ii]) + tol_b;
    }

    public void includelineinB(int ii) {
        Bmattemp[busfrom[ii]][busto[ii]] += (-1 / BranchX[ii]) - tol_b;
        Bmattemp[busto[ii]][busfrom[ii]] += (-1 / BranchX[ii]) - tol_b;
        Bmattemp[busfrom[ii]][busfrom[ii]] += (1 / BranchX[ii]) - tol_b;
        Bmattemp[busto[ii]][busto[ii]] += (1 / BranchX[ii]) - tol_b;
    }

//Optimization Constraint A Matrix Computation:
    public void createA() {
        int ii, kk, rowA, colA1, colA2;  				//Auxiliary variables and counters
        numvar = numunits + numbus + numbran * 3;			//Number of uncertanties
        numconstr = numvar + 1;      					//Number of constraint Plus 1 for slack angle = 0
        Amatrix = new float[numconstr][numvar];		//A cosntraint Matrix definition
        long countcero = 0;							//Ceros in Amatrix counter 

        //CONSTRAINT 1: Active power duplication variables
        for (ii = 0; ii < numunits; ii++) {
            //1.1--Identity:
            for (kk = 0; kk < numunits; kk++) {
                if (kk == ii) {
                    Amatrix[ii][kk] = 1;
                } else {
                    Amatrix[ii][kk] = 0;
                    countcero++;
                }
            }
            //1.2--Fill Ceros:
            for (kk = numunits; kk < numvar; kk++) {
                Amatrix[ii][kk] = 0;
                countcero++;
            }
        }

        //CONSTRAINT 2: DC Power Flow Equations
        for (ii = numunits; ii < numunits + numbus; ii++) {
            //2.1--Busbar where generators are located:
            for (kk = 0; kk < numunits; kk++) {
                Amatrix[ii][kk] = 0;
                if (busgenid[kk] == (ii - numunits)) {
                    Amatrix[ii][kk] = 1;
                }
            }
            //2.2--B Matrix Inclusion:
            for (kk = numunits; kk < numunits + numbus; kk++) {
                Amatrix[ii][kk] = (-1) * Bmattemp[ii - numunits][kk - numunits];
            }
            //2.3--Fill Ceros:
            for (kk = numunits + numbus; kk < numvar; kk++) {
                Amatrix[ii][kk] = 0;
                countcero++;
            }
        }

        //CONSTRAINT 3: DC Flow Line Limits
        for (ii = 0; ii < numbran; ii++) {
            rowA = numunits + numbus + ii;
            //3.1--Fill Ceros (first because it could be quite difficult):
            for (kk = 0; kk < numvar; kk++) {
                Amatrix[rowA][kk] = 0;
                countcero++;
            }
            //3.2--Voltage Angle Differences:
            colA1 = busfrom[ii] + numunits;
            colA2 = busto[ii] + numunits;
            Amatrix[rowA][colA1] = 1;
            Amatrix[rowA][colA2] = -1;
            //3.3--Serie Reactances:
            Amatrix[rowA][rowA] = (-1) * BranchX[ii];
        }

        //CONSTRAINT 4: Slack Positive Variables included in DC Flow Limits:
        for (ii = 0; ii < numbran; ii++) {
            //4.1--Identities:
            rowA = numunits + numbus + numbran + ii;
            for (kk = 0; kk < numbran; kk++) {
                colA1 = numunits + numbus + kk;
                colA2 = colA1 + numbran;
                if (kk == ii) {
                    Amatrix[rowA][colA1] = 1;
                    Amatrix[rowA][colA2] = -1;
                } else {
                    Amatrix[rowA][colA1] = 0;
                    Amatrix[rowA][colA2] = 0;
                    countcero += 2;
                }
            }
            //4.2--Fill Ceros:
            for (kk = 0; kk < numunits + numbus; kk++) {
                Amatrix[rowA][kk] = 0;
                countcero++;
            }
            for (kk = numvar - numbran; kk < numvar; kk++) {
                Amatrix[rowA][kk] = 0;
                countcero++;
            }
        }

        //CONSTRAINT 5: Slack Negative Variables included in DC Flow Limits:
        for (ii = 0; ii < numbran; ii++) {
            //5.1--Identities:
            rowA = numvar - numbran + ii;
            for (kk = 0; kk < numbran; kk++) {
                colA1 = numunits + numbus + kk;
                colA2 = colA1 + 2 * numbran;
                if (kk == ii) {
                    Amatrix[rowA][colA1] = -1;
                    Amatrix[rowA][colA2] = -1;
                } else {
                    Amatrix[rowA][colA1] = 0;
                    Amatrix[rowA][colA2] = 0;
                    countcero += 2;
                }
            }
            //5.2--Fill Ceros:
            for (kk = 0; kk < numunits + numbus; kk++) {
                Amatrix[rowA][kk] = 0;
                countcero++;
            }
            for (kk = numvar - 2 * numbran; kk < numvar - numbran; kk++) {
                Amatrix[rowA][kk] = 0;
                countcero++;
            }
        }

        //CONSTRAINT 6: Slack Voltage angle equal to cero (arbitrary to busbar 0)
        for (kk = 0; kk < numvar; kk++) {
            if (kk == numunits) {
                Amatrix[numvar][kk] = 1;
            } else {
                Amatrix[numvar][kk] = 0;
                countcero++;
            }
        }

    }

    public void ignoreflowAmatrix(int branchout) {
        Amatrix[numunits + numbus + branchout][numunits + numbus + branchout] = 0;
    }

    public void includeflowAmatrix(int branchout) {
        Amatrix[numunits + numbus + branchout][numunits + numbus + branchout] = (-1) * BranchX[branchout];
    }

    public void refreshDCLFAmatrix() {
        for (int ii = numunits; ii < numunits + numbus; ii++) {
            for (int kk = numunits; kk < numunits + numbus; kk++) {
                Amatrix[ii][kk] = (-1) * Bmattemp[ii - numunits][kk - numunits];
            }
        }
    }

//Right Hand Side (RHS) Vector:
    public void createRHS(int tt, byte ubin[][], double pact[][], int branchout) {

        int ii, rows;
        RHSvector = new float[numconstr];

        //RHS 1: Active power duplication variables
        for (ii = 0; ii < numunits; ii++) {
            RHSvector[ii] = (float) pact[ii][tt] * ubin[ii][tt] / sref;
        }

        //RHS 2: DC Power Flow Equations
        for (ii = 0; ii < numbus; ii++) {
            if (ii == 0) {
                RHSvector[ii + numunits] = (getbuspdem(ii, tt) / sref) - tol_b; //OJO: se le resto la tolerancia para hacer un rango
            } else {
                RHSvector[ii + numunits] = getbuspdem(ii, tt) / sref;
            }
        }

        //RHS 3: DC Flow Line Limits
        rows = numunits + numbus;
        for (ii = 0; ii < numbran; ii++) {
            if (branchout == ii) {
                RHSvector[ii + rows] = 100000f;
            } else {
                RHSvector[ii + rows] = 0;
            }

            //RHS 4: Positive Slack Variables included in DC Flow Limits:
            RHSvector[ii + rows + numbran] = Flowmax[ii] / sref;

            //RHS 5: Negative Slack Variables included in DC Flow Limits:
            RHSvector[ii + rows + 2 * numbran] = Flowmax[ii] / sref;
        }

        //RHS 6: Slack Voltage angle equal to cero (arbitrary to busbar 0)
        RHSvector[numvar] = 0;

    }

//Calculate total power demand at each busbar:
    //public float getbuspdem(int bus, int tt, byte ubin[][], double pact[][]){
    public float getbuspdem(int bus, int tt) {
        float buspdem = 0;
        for (int kk = 0; kk < numloads; kk++) {
            if (busloadid[kk] == bus) {
                buspdem += pload[kk][tt];
            }
        }
        return buspdem;

    }

}
