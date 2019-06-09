package cl.flconsulting.ucopt;

//****************************************************************************
//Unit-Commitment by Lagrange Relaxation
//Initially Created: 17/8/2005 - Frank Leanez
//****************************************************************************  
import java.io.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

//public class JavaUC extends AnalysisTool{		//(DEEPEDIT)
public class JavaUC {							//(INDEPENDANT)

//------------------------------------------------------------------------------
//Class Attributes:
//------------------------------------------------------------------------------
//General class attributes:
    Connection mylink;
    short numunits;
    short nummaint;
    short numuserout;
    boolean txconstraint;
    boolean useaugmen;
    boolean useheuristic;
    boolean usepiecewise;
    boolean usequadra;
    boolean useramp;
    boolean usedb;
    boolean usesch;
    boolean usespin;
    boolean usepres;
    boolean useplimit;
    public String txtfiledir; //Temporal workaround to load cases

    short typen1;				//Type of n-1 check: 0=none; 1=heaviest; 2=half; 3=all lines
    int numbrasch;			//Number of branch readed from Schematic

//Main Method Matrices:
    float hourlypdem[]; 		//Hourly active power load
    float hourlyspin[]; 		//Hourly Spinning reserve requirement
    float hourlypres[]; 		//Hourly Primary reserve requirement
    float multdem[];			//Lagrange Multiplier for demand constraint
    float multres[];			//Lagrange Multiplier for reserv constraint
    float multcut[];			//Lagrange Multiplier for Benders cut constraints
    float multcuttime[];  	//Benders cut constraints time index
    boolean feasable[];		//Feaseable Primal Solution Indicator
    short priorlist[][]; 		//Priority list: Order by Beta
    short indexpos[][];		//Index of Units position in Unitdata
    short marginal[];			//Index of Marginal Unit
    boolean able[][]; 		//Units Discarded (unavailable or expensive configuration)
    boolean must[][];  		//Must run binary indicator
    float pmax[][]; 			//Pmax
    float pmin[][]; 			//Pmin
    float beta[][]; 			//Beta
    float fuelprice[][]; 		//Time-dependant Fuel Prices
    byte ubin[][];  			//On/off binary decision variable
    float spinres = 0f; 		//PROVISIONAL!!
    float primres = 0f; 		//PROVISIONAL!!
    double pact[][];  		//Actual Generating Power (calculated in the dual process)
    double plast[][]; 		//Last Dual Solution found (Active power injections)
    byte ulast[][];   		//Last Dual Solution found (binary decision vector)
    double pbest[][]; 		//Best Dual or Primal Solution found (Active power injections)
    byte ubest[][];  		//Best Dual or Primal Solution found (binary decision vector)
    double ppri[][];			//Primal Feasible Generating Power
    double lambdapri[]; 		//Primal Feasible Lambda

    boolean ucconvergence = false; //Unit Commitment convergence indicator
    boolean txconvergence = false; //Transmission subproblem convergence indicator
    boolean onefeasible = false; //At least one feasible solution found indicator
    String solquality = "N/A"; 	//Solution quality expression (qualitative)
    long ucitercont = 0;		//Iteration counter
    double startcost; 		//Total Startup Cost
    double primalfunction = 0; 	//Primal Function
    boolean lastfeasible = false;
    float dualgap = -1f; 		//Iterative duality gap
    double dualbest = 0; 		//Best Dual Lagrange function
    double dualfunction = 0; 	//Dual Lagrange function
    double primalbest = 0; 		//Best feasible primal function value
    float dgbest = 1; 			//Best feasible duality gap

    long timedata = 0;			//Data read time (mseg)
    long timealr = 0;			//ALR executing time (mseg)
    long timesubp = 0;			//Subproblem check executing time (mseg)
    long timetotal = 0;			//Total time (mseg)
    long timeprimal = 0;		//Total Primal time (mseg)
    long timedual = 0;			//Total Dual time (mseg)
    int totaliteruc = 0;		//Total Unit Commitment iterations (does not include subproblems)
    int cont100iter = 1;		//Auxiliary integer counter (it takes value 1 when counting 100)

//Piece-wise linear related attributes:
    short activebloc[][]; //Selected piece-wise linear block
    float activepmax[][]; //Selected piece-wise linear pmax
    float activepmin[][]; //Selected piece-wise linear pmin
    float activebeta[][]; //Selected piece-wise linear incremental cost

//Atributtes from Option File:
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
    boolean printlp;

//Atributos probablemente provisionales:
    public DataUnitUC Unitdata;
    DataMaintUC Maintdata;
    DataUserUC Userdata;
    DataLoadUC Loaddata;
    DataNetUC Networkdata;
    DataMaintnetUC Netmaintdata;
    DataSystemUC Systemdata;
    XmlTreeMaker DataoptionUC;
    //JavaUCFrame MyJavaUCFrame;
    NetUCcut mynetcut = null;
    UCBenderCut bendercut = null;
    //Schematic mysch=null;
    OptimizerCaller opt;

//Atributos del DeepEdit:
    double[] BranchR;
    double[] BranchX;
    int[] BranchI;
    int[] BranchJ;
    String[] NomBranch;
    double[] BranchPmax;

//Benders Cuts related attributes:
    int numcuts = 0;
    int cutid[];
    float cutobjval[];
    float cutsimplexval[][];
    int cuttime[];
    float cutpit[][];
    float cutmultiplier[];

    //IMPRESIONES DE PRUEBA A ARCHIVO (PROVISIONAL):
    FileOutputStream os;
//	BufferedOutputStream bos;
    PrintStream reportline;

//------------------------------------------------------------------------------
//Class Constructors:
//------------------------------------------------------------------------------
    public JavaUC() {					//(INDEPENDANT)

        System.out.println(" ");
        System.out.println("-------Initilizing Unit Commitment---------");
        System.out.println(" ");

        //c2--Read Option File:
        System.out.println("Start reading option file... <UCOptions.xml>");
        optionfile();
        System.out.println("Done reading option file...");
        System.out.println(" ");

        //c3--Make Unit Commitment Option Frame visible
        //MyJavaUCFrame=null;
        //if (MyJavaUCFrame == null) {
        //	MyJavaUCFrame = new JavaUCFrame(this);
        //} else {
        //	MyJavaUCFrame.setVisible(true);
        //}
        usesch = false;				//(INDEPENDANT)

    }

    /*
    public JavaUC(Schematic sch){	//(DEEPEDIT)
    	
	    //c1--Super call and :
	    super(sch);
    	mysch = sch;
    	System.out.println (" ");
    	System.out.println ("-------Initilizing Unit Commitment---------");
    	System.out.println (" ");
		//c2--Read Option File:
		optionfile();
		
		//c3--Make Unit Commitment Option Frame visible
    	usesch=true;
    	if (MyJavaUCFrame == null) {
			MyJavaUCFrame = new JavaUCFrame(this);
		} else {
			MyJavaUCFrame.setVisible(true);
		}
    	
    }
     */
//------------------------------------------------------------------------------
//Main Class Methods:
//------------------------------------------------------------------------------
    //Initial Calculations:
    public void calculate() {

        //PROVISIONAL HACER REPORTE DETALLADO 	//PROVISIONAL!!!
        boolean makereport = true;				//PROVISIONAL!!!
        if (makereport) {
            try {

                os = new FileOutputStream(LogFilePath());
                //bos = new BufferedOutputStream(os,32768);
                reportline = new PrintStream(os);
                //reportline.println ("*************REPORTE PROVISIONAL UNIT COMMITMENT (SPANISH)*************");
            } catch (Exception e) {
                System.out.println("Error Creando Archivo Provisional");
            }
        }
        //FIN REPORTE PROVISIONAL:

        initializeUC();

        //Read Database:
        long recordtime = System.currentTimeMillis();
        if (usedb) {
            initconnection(dbtype, dbloc, dbname, dblog, dbpass);
            inidataread();
            closedbconnection();
        } else {
            inifileread();
        }
        timedata = System.currentTimeMillis() - recordtime;
        System.out.println("Data Read Complete....");
        System.out.println("read time= " + timedata + "ms.");

        //Calculate Initial Lagrange Multipliers:
        recordtime = System.currentTimeMillis();
        inilagrangemult();
        long timeinilagrange = System.currentTimeMillis() - recordtime;
        System.out.println("Lagrange Multiplier Initialization done....");
        System.out.println("Initializing time= " + timeinilagrange + "ms.");
        timetotal += timedata + timeinilagrange;

        /*//IMPRESIONES A REPORTE PROVISIONAL:
    	reportline.println (" ");
    	reportline.println ("Datos de Entrada:");
    	reportline.println ("*****************");
    	reportline.println (" ");
    	reportline.println ("* DATOS DE LAS UNIDADES: ");
    	reportline.println (" ");
    	reportline.println (" #    ID:      Nombre:       Barra:");
    	for (int ii=0;ii<numunits;ii++){
    	reportline.println (" "+ii+"    "+Unitdata.readunitid[ii]+"        "+Unitdata.readname[ii]+"         "+Unitdata.readbusbarto[ii]);
    	}
    	reportline.println (" ");
    	reportline.println ("* COSTOS: ");
		for (int tt=0;tt<numperiods;tt++){
    	for (int ii=0;ii<numunits;ii++){
		reportline.println ("beta["+ii+"]["+tt+"]: "+beta[ii][tt]);
		}}
		for (int tt=0;tt<numperiods;tt++){
    	for (int ii=0;ii<numunits;ii++){
		reportline.println ("pmax["+ii+"]["+tt+"]:  "+pmax[ii][tt]);
		}}
    	for (int ii=0;ii<numunits;ii++){
		for (int tt=0;tt<numperiods;tt++){
		reportline.println ("pmin["+ii+"]["+tt+"]:  "+pmin[ii][tt]);
		}}
		reportline.println (" ");
		reportline.println (" ");
    	reportline.println ("**********************************************");
		reportline.println ("INICIALIZACION DE LOS MULTIPLICADORES DE LAGRANGE:");
    	reportline.println ("**********************************************");
		
		for (int tt=0;tt<1;tt++){
		for (int ii=0;ii<numunits;ii++){
		reportline.println ("Priority list["+ii+"]["+tt+"]: "+priorlist[ii][tt]);
		}}

		reportline.println (" ");
		reportline.println ("* Multiplicadores de restriccion de potencia:");
		for (int tt=0;tt<numperiods;tt++){
		reportline.println ("lambda["+tt+"]: "+multdem[tt]);
		}
		reportline.println ("* Multiplicadores de restriccion de reserva en giro:");
		for (int tt=0;tt<numperiods;tt++){
		reportline.println ("mu's["+tt+"]: "+multres[tt]);
		}
    	//FIN REPORTE PROVISIONAL:
         */
    }

    public void initializeUC() {

        //Initialize main iterate variables:
        ucconvergence = false;
        txconvergence = false;
        onefeasible = false;
        solquality = "N/A";
        ucitercont = 0;
        startcost = 0;
        primalfunction = 0;
        lastfeasible = false;
        dualgap = -1f;
        dualfunction = 0;
        primalbest = 0;
        dualbest = 0;
        dgbest = 1;
        timedata = 0;
        timealr = 0;
        timesubp = 0;
        timetotal = 0;
        timeprimal = 0;
        timedual = 0;
        totaliteruc = 0;
        cont100iter = 1;

    }

    //Iterative Augmented Lagrangian Relaxation UC Process:
    public boolean iteratealr() {

        long recordtime = System.currentTimeMillis();
        dualgap = -1;
        ucitercont = 1;
        long aux = 0;
        boolean feasesol = false;
        boolean trueconver = false;

        /*//IMPRESIONES A REPORTE PROVISIONAL:
			reportline.println (" ");
	    	reportline.println (" ");
	    	reportline.println ("**********************************************");
			reportline.println ("INICIO DE LA RELAJACION LAGRANGEANA AUMENTADA:");
	    	reportline.println ("**********************************************");
	    	//FIN REPORTE PROVISIONAL:
         */
        if (userepdetail) {
            reportline.println("==========================================================");
            reportline.println("DETAILED LAGRANGEAN AUGMENTATION REPORT: (CVS DATA FORMAT)");
            reportline.println("ITER, DUAL BEST, PRIM BEST, GAP (pu), ELAPSED TIME (mseg)");
            reportline.println("==========================================================");
        }

        //1--Iterative Procedure for Unit Commitment:
        while ((ucitercont <= itermax)
                && ((aux = System.currentTimeMillis() - recordtime) < itertime * 60000)) {
            ucitercont++;

            //--1.1.Individual Unit Dynamic Programming Algoritm:
            //**************
            individualdp();
            //**************

            //--1.2.Dual Function Evaluation:
            //**************
            dualeval();
            //**************
            long recordtime2 = System.currentTimeMillis();

            //--1.3.Primal Function Evaluation:
            //**************
            feasesol = primaleval();
            //**************
            timeprimal += System.currentTimeMillis() - recordtime2;

            /*//IMPRESIONES A REPORTE PROVISIONAL:
			reportline.println (" ");
	    	reportline.println (" ");
	    	reportline.println ("ITERACION #"+(ucitercont-1)+" (UC)");
	    	reportline.println (" ");
	    	reportline.println ("***Resultados del DUAL:");
	    	reportline.println (" ");
	    	reportline.println ("*Vector de Decision Binario:");
	    	for (int ii=0;ii<numunits;ii++){
			for (int tt=0;tt<numperiods;tt++){
			reportline.println ("ubin["+ii+"]["+tt+"]:  "+ubin[ii][tt]);
			}}
	    	reportline.println (" ");
	    	reportline.println ("*Potencias Generadas (duales):");
	    	for (int ii=0;ii<numunits;ii++){
			for (int tt=0;tt<numperiods;tt++){
			reportline.println ("pact["+ii+"]["+tt+"]:  "+pact[ii][tt]);
			}}
	    	reportline.println ("*Funcion Dual: "+dualfunction);
	    	reportline.println (" ");
	    	reportline.println ("***Resultados del PRIMAL:");
	    	reportline.println (" ");
	    	reportline.println ("*Potencias Generadas (primales):");
	    	for (int ii=0;ii<numunits;ii++){
			for (int tt=0;tt<numperiods;tt++){
			reportline.println ("Pprimal["+ii+"]["+tt+"]:  "+ppri[ii][tt]);
			}}
	    	reportline.println ("*Funcion Primal: "+primalfunction);
	    	reportline.println (" ");
    		//FIN REPORTE PROVISIONAL:
             */
            //--1.4.Duality Gap and/or Convergence Check:
            //**************
            trueconver = convergencecheck(feasesol);
            //**************

            //Print details if selected:
            if (userepdetail) {
                //timealr+=System.currentTimeMillis()-recordtime;
                reportline.println((ucitercont - 1) + ", " + dualbest + ", " + primalbest + ", " + dgbest + ", " + (System.currentTimeMillis() - recordtime));
            }

            if (trueconver) {
//                            try {
//                                /*//IMPRESIONES A REPORTE PROVISIONAL:
//                                reportline.println ("*Tolerancia:(%) "+(tolset*100));
//                                reportline.println ("*Gap DUAL:  (%) "+(dualgap*100));
//                                reportline.println ("!!!!CONVERGENCIA ENCONTRADA!!!!");
//                                //FIN REPORTE PROVISIONAL:
//                                    */
//                                opt.finalize();
//                            } catch (Exception ex) {
//                                Logger.getLogger(JavaUC.class.getName()).log(Level.SEVERE, null, ex);
//                            }
                break;
            }

            /*//IMPRESIONES A REPORTE PROVISIONAL:
	    	reportline.println ("*Tolerancia:(%) "+(tolset*100));
	    	reportline.println ("*Gap DUAL:  (%) "+(dualgap*100));
    		//FIN REPORTE PROVISIONAL:
             */
            //--1.5.Lagrange Multiplier Maximization:
            //**************
            dualupdate();
            //**************

            /*//IMPRESIONES A REPORTE PROVISIONAL:
	    	reportline.println (" ");
	    	reportline.println ("***Nuevas Variables Duales: ");
			for (int tt=0;tt<numperiods;tt++){
			reportline.println ("lambda["+tt+"]:  "+multdem[tt]);
			}
			for (int tt=0;tt<numperiods;tt++){
			reportline.println ("mu's["+tt+"]:    "+multres[tt]);
			}
			for (int kk=0;kk<numcuts;kk++){
			reportline.println ("Hora: "+cuttime[kk]+"   Multiplicador: "+cutmultiplier[kk]);
			}
    		//FIN REPORTE PROVISIONAL:
             */
        }

        if (userepdetail) {
//                      reportline.println (" ");
//			reportline.println ("==========================================================");
//			reportline.println (" ");
//			reportline.println (" ");
            reportline.flush();
            reportline.close();
        }

        timealr += System.currentTimeMillis() - recordtime;
        timedual += timealr - timeprimal;
        totaliteruc += (ucitercont - 1);

        //2--Handle Stoping Criteria: (Deprecated)
        /*
			//2.1--If excedeeded execution time:
		if (aux>=itertime*60000){
			return false;
			//2.2--If excedeeded iterations:
		}else if (ucitercont>=itermax){
			return false;
		//2.3--If succesful convergence:
		}else if (trueconver){
			return true;
		}else{
			return false;
		}
         */
        return trueconver;

    }

    //Iterative Security Constraint UC with Benders Feasesibility Cuts Algorithm:
    public void iteratescuc() {

        int numnewcuts = 0, contnetiter = 0, contcut = 0;
        boolean ucsolution = true;
        short cutneed = 1;
        long recordtime = System.currentTimeMillis();

        //EXCLUDE Transmission Contraints:
        if (!txconstraint) {
            ucsolution = iteratealr();
            txconvergence = true;
            //INCLUDE Transmission Contraints:
        } else {

            while (cutneed != 0 && ucsolution && contnetiter < itermaxnet) {
                ucsolution = iteratealr();
                txconvergence = false;

                /*//IMPRESIONES A REPORTE PROVISIONAL:
				reportline.println (" ");
		    	reportline.println (" ");
		    	reportline.println ("**********************************************");
				reportline.println ("CHEQUEO DE RESTRICCIONES DE TRANSMISION:");
		    	reportline.println ("**********************************************");
				reportline.println (" ");
    			//FIN REPORTE PROVISIONAL:
                 */
                if (ucsolution) {

                    //1--Actualizacion de los cortes de Benders:
                    long recordtime2 = System.currentTimeMillis();

                    //1.1--Back up provisional:
                    if (contcut != 0) {
                        contcut = cutid.length;
                    }
                    float[] multtemp = new float[contcut];
                    for (int kk = 0; kk < contcut; kk++) {
                        multtemp[kk] = cutmultiplier[kk];
                    }

                    //1.2--Inicializa las matrices:
                    //1.3--Calcula el nuevo corte y lo agrega al objeto tipo corte:
                    cutneed = mynetcut.calculatecuts(glpklpmethod, ubin, ppri, glpkdir,
                            glpkpifname, glpkdosbat, glpkpofname, optimizer, typen1,
                            tol_b, minoslineabat);
                    bendercut = mynetcut.returnlastcut();

                    //1.4--Chequeo de convergencia e Inicializacion de las nuevas matrices:
                    if (cutneed == 0) {
                        txconvergence = true;
                        break;
                    } else {
                        txconvergence = false;
                        contcut += cutneed;
                        numcuts = contcut;
                        cutid = new int[contcut];
                        cutobjval = new float[contcut];
                        cutsimplexval = new float[contcut][numunits];
                        cuttime = new int[contcut];
                        cutpit = new float[contcut][numunits];
                        cutmultiplier = new float[contcut];

                        /*//IMPRESIONES A REPORTE PROVISIONAL:
						reportline.println (" ");
				    	reportline.println ("**********************************************");
						reportline.println ("LIMITES TRANSMISION EXCEDIDOS!");
						reportline.println ("Cortes Creados: "+cutneed);
						reportline.println ("Total Cortes Acumulados: "+numcuts);
						reportline.println ("Chequeo de Transmision numero: "+(contnetiter+1));
				    	reportline.println ("**********************************************");
						reportline.println (" ");
		    			//FIN REPORTE PROVISIONAL:
                         */
                    }

                    //1.5--Llenado de las nuevas matrices
                    for (int kk = 0; kk < contcut; kk++) {
                        if (kk < multtemp.length) {
                            cutmultiplier[kk] = multtemp[kk];
                        } else {
                            cutmultiplier[kk] = 0f;
                        }
                        cutid[kk] = kk;
                        cutobjval[kk] = bendercut.getobjvalue(kk);
                        float[] simplextemp = bendercut.getsimplexarray(kk);
                        float[] pittemp = bendercut.getpitarray(kk);
                        for (int ii = 0; ii < simplextemp.length; ii++) {
                            cutsimplexval[kk][ii] = simplextemp[ii];
                            cutpit[kk][ii] = pittemp[ii];
                        }
                        cuttime[kk] = bendercut.gettimeperiod(kk);
                    }
                    timesubp += System.currentTimeMillis() - recordtime2;

                }
                contnetiter++;
            }
        }
        timetotal = System.currentTimeMillis() - recordtime;

        /*//IMPRESIONES A REPORTE PROVISIONAL:
				reportline.println (" ");
				if (txconstraint){
				if (numcuts==0){
				reportline.println ("**********************************************");
				reportline.println ("NO SE ENCONTRARON LIMITES EXCEDIDOS!");
				reportline.println ("**********************************************");
				}else{
				reportline.println ("**********************************************");
				reportline.println ("LIMITES TRANSMISION EXCEDIDOS! Imprimiendo Cortes de Benders:");
				reportline.println ("**********************************************");
				}
				reportline.println (" ");
				reportline.println (" ");
		    	for (int kk=0;kk<numcuts;kk++){
				reportline.println ("Corte numero:      "+cutid[kk]);
				reportline.println ("Funcion Objetivo:  "+cutobjval[kk]);
				reportline.println ("Periodo de Tiempo: "+cuttime[kk]);
				reportline.println ("Mult. Simplex:");
				for (int ii=0;ii<numunits;ii++){
				reportline.println ("                   "+cutsimplexval[kk][ii]);
				}
				reportline.println ("P[i][t]:");
				for (int ii=0;ii<numunits;ii++){
				reportline.println ("                   "+cutpit[kk][ii]);
				}
				reportline.println (" ");
				}
				try{
				os.close();
				}catch(Exception e) {
				System.out.println("File report error: "+e);
				}
				}
		    	//FIN REPORTE PROVISIONAL:
         */
        //CONSOLE FINAL PRINTLINES(probable PROVISIONAL!!):
        System.out.println("*******************************************************");
        System.out.println("*      UNIT COMMITMENT BY LAGRANGIAN RELAXATION       *");
        System.out.println("*                                    |Solution Report|*");
        System.out.println("*******************************************************");
        if (ucsolution) {
            System.out.println("* FINAL Solution Status:  OPTIMAL SOLUTION FOUND!!!   *");
            System.out.println("* Execution time:                                     *");
            System.out.println("* Total Iterations:                                   *");
            System.out.println("* Total Bender Cuts:                                  *");
        } else {
            System.out.println("* FINAL Solution Status:  STOPPED!!!! no optimal found*");
            System.out.println("* Showing Last Solution   (It may be not feasible)    *");
            System.out.println("* Try adjusting Tolerance and/or Max. Iterations      *");
        }
        System.out.println("*******************************************************");

        //END CONSOLE PRINTLINES         
        try {
            if (opt != null) {
                opt.finalizeOptimizer();
            }
        } catch (Exception ex) {
            Logger.getLogger(JavaUC.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void inidataread() {

        //2--Make the Connection:
        //initconnection("mysql", "ucdata", "root", ""); Overrrided by Frame!
        //3--Create objects to read from DB:
        Unitdata = new DataUnitUC(mylink);
        Maintdata = new DataMaintUC(mylink);
        Userdata = new DataUserUC(mylink);
        Loaddata = new DataLoadUC(mylink);
        Systemdata = new DataSystemUC(mylink);

        //4--Initialize Objects:
        short numunitsread = Unitdata.numtotalunit();
        short numheatread = Unitdata.numtotalheat();
        if (numunits > numunitsread) {
            numunits = numunitsread;
        }
        Unitdata.inidataunit();
        nummaint = Maintdata.numtotalmaint();
        Maintdata.inidatamaint();
        numuserout = Userdata.numtotaluser();
        Userdata.inidatauser();
        short numload = Loaddata.numtotalload();
        Loaddata.inidataload(numperiods);
        Systemdata.inidatasystem(numperiods);

        //5--Filling Objects atributtes from DB:
        Unitdata.completeselect();
        Maintdata.completeselect();
        Userdata.completeselect();
        Loaddata.completeselect();
        Systemdata.completeselect();

        //6--Get Total demand and Reserve:
        //6.1--Reserva en giro PROVISIONAL!!!
        hourlypdem = new float[numperiods];
        hourlyspin = new float[numperiods];
        hourlypres = new float[numperiods];
        for (int tt = 0; tt < numperiods; tt++) {
            hourlypdem[tt] = Loaddata.gethourpdem(tt);
            if (usespin) {
                hourlyspin[tt] = (Systemdata.readspin[tt] / 100) * hourlypdem[tt];
                //hourlyspin[tt]=0.05f*hourlypdem[tt];
            } else {
                hourlyspin[tt] = 0;
            }
            hourlypres[tt] = primres * hourlypdem[tt];  //PROVISIONAL
        }

        /*Impresiones Provisionales:
				for (int tt=0; tt<numperiods; tt++){
				System.out.println ("pdem["+tt+"]="+hourlypdem[tt]);
				}
				for (int tt=0; tt<numperiods; tt++){
				System.out.println ("Readspin["+tt+"]="+Systemdata.readspin[tt]);
				System.out.println ("spin["+tt+"]="+hourlyspin[tt]);
				}
         */
        //7--(optional)Read Network Components:
        if (txconstraint) {
            Networkdata = new DataNetUC(mylink);
            short numbradb = Networkdata.numtotalnet();
            Networkdata.inidatanet();
            Networkdata.completeselect();

            Netmaintdata = new DataMaintnetUC(mylink);
            short nummaintnet = Netmaintdata.numtotalmaintnet();
            Netmaintdata.inidatamaintnet();
            Netmaintdata.completeselect();

            /*Impresiones Provisionales:
				for (int ii=0;ii<nummaintnet;ii++){
				System.out.println("netid: "+Netmaintdata.readnetid[ii]);
				System.out.println("from: "+Netmaintdata.readtomaint[ii]);
				System.out.println("to: "+Netmaintdata.readfrommaint[ii]);
				System.out.println("R: "+Netmaintdata.readR[ii]);
				System.out.println("X: "+Netmaintdata.readX[ii]);
				System.out.println("Pmax: "+Netmaintdata.readpmax[ii]);
				System.out.println("inservice: "+Netmaintdata.readinservice[ii]);
				}
             */
            //Update with Schematic values:  (DEEPEDIT)
            /*
			if (usesch){
				InitNetwork();
				for (int ii=0;ii<numbradb;ii++){
					for (int kk=0;kk<numbrasch;kk++){
						if (Networkdata.readname[ii].equals(NomBranch[kk])){
							Networkdata.readname[ii]=NomBranch[kk];
							Networkdata.readr[ii]=(float)BranchR[kk];
							Networkdata.readx[ii]=(float)BranchX[kk];
							Networkdata.readpmax[ii]=(float)BranchPmax[kk];
							break;
			}}}}
             */
            mynetcut = new NetUCcut(numunits, numperiods, Networkdata, Unitdata, Loaddata, Netmaintdata);
        }

        //8--Use piece-wise linear heat rates:
        /*if (usepiecewise){
			for (int ii=0;ii<numunits;ii++){
				for (int kk=0;kk<Unitdata.getnumblocks(ii);kk++){
					System.out.println("Pmax["+ii+"]["+kk+"]: "+Unitdata.getblockpmax(ii,kk));
					System.out.println("Pmin["+ii+"]["+kk+"]: "+Unitdata.getblockpmin(ii,kk));
					System.out.println("Heat["+ii+"]["+kk+"]: "+Unitdata.getblockheat(ii,kk));
		}}}
         */
    }

    public void inifileread() {

        //3f--Create objects to read from DB:
        Unitdata = new DataUnitUC();
        Maintdata = new DataMaintUC();
        Userdata = new DataUserUC();
        Loaddata = new DataLoadUC();
        Systemdata = new DataSystemUC();

        //4f--Initialize Objects:
        short numunitsread = Unitdata.datalines(txtfiledir);
        if (numunits > numunitsread) {
            numunits = numunitsread;
        }
        Unitdata.inidataunit();

        nummaint = Maintdata.datalines(txtfiledir);
        Maintdata.inidatamaint();

        numuserout = Userdata.datalines(txtfiledir);
        Userdata.inidatauser();

        short numload = Loaddata.loaddatalines(txtfiledir);
        int numcurvepoint = Loaddata.curvedatalines(txtfiledir);
        Loaddata.inidataload(numperiods);

        Systemdata.inidatasystem(numperiods);

        //5f--Filling Objects atributtes from DB:
        Unitdata.loaddatafromfile(txtfiledir);
        Maintdata.loaddatafromfile(txtfiledir);
        Userdata.loaddatafromfile(txtfiledir);
        Loaddata.loaddatafromfile(txtfiledir);
        Systemdata.loaddatafromfile(txtfiledir);

        //6f--Get Total demand and Reserve:
        //6f.1--Reserva en giro PROVISIONAL!!!
        hourlypdem = new float[numperiods];
        hourlyspin = new float[numperiods];
        hourlypres = new float[numperiods];
        for (int tt = 0; tt < numperiods; tt++) {
            hourlypdem[tt] = Loaddata.gethourpdem(tt);
            if (usespin) {
                hourlyspin[tt] = (Systemdata.readspin[tt] / 100) * hourlypdem[tt];
            } else {
                hourlyspin[tt] = 0;
            }
            hourlypres[tt] = primres * hourlypdem[tt];  //PROVISIONAL
        }

        /*Impresiones Provisionales:
				for (int tt=0; tt<numperiods; tt++){
				System.out.println ("pdem["+tt+"]="+hourlypdem[tt]);
				}
				for (int tt=0; tt<numperiods; tt++){
				System.out.println ("Readspin["+tt+"]="+Systemdata.readspin[tt]);
				System.out.println ("spin["+tt+"]="+hourlyspin[tt]);
				}
         */
        //7f--(optional)Read Network Components:
        if (txconstraint) {
            Networkdata = new DataNetUC();
            short numbradb = Networkdata.datalines(txtfiledir);
            Networkdata.inidatanet();
            Networkdata.loaddatafromfile(txtfiledir);

            Netmaintdata = new DataMaintnetUC();
            short nummaintnet = Netmaintdata.datalines(txtfiledir);
            Netmaintdata.inidatamaintnet();
            Netmaintdata.loaddatafromfile(txtfiledir);
            mynetcut = new NetUCcut(numunits, numperiods, Networkdata, Unitdata, Loaddata, Netmaintdata);
        }

        //8f--Use piece-wise linear heat rates:
        /*//Impresiones Provisionales:
				if (usepiecewise){
				for (int ii=0;ii<numunits;ii++){
				for (int kk=0;kk<Unitdata.getnumblocks(ii);kk++){
				System.out.println("Pmax["+ii+"]["+kk+"]: "+Unitdata.getblockpmax(ii,kk));
				System.out.println("Pmin["+ii+"]["+kk+"]: "+Unitdata.getblockpmin(ii,kk));
				System.out.println("Heat["+ii+"]["+kk+"]: "+Unitdata.getblockheat(ii,kk));
				}}}
         */
    }

    /*
	public void InitNetwork() {  //(DEEPEDIT)
		
		comp		TempComponent	=	null;		// Componente general
		ndb_comp	TempNdb_comp	=	null;		// Componente de la ndb
		Busbar		TempBusbar		=	null;		// Barra
		ndb_branch	TempBran		=	null;		// Rama
		Line		TempLine		=	null;		// Line
		Trafo		TempTrafo		=	null;		// Trafo
		Generator	TempGenerator	=	null;		// Generator
		ndb_inj		TempNdb_inj		=	null;		// Injection
		Load		TempLoad		=	null;		// Load
		int			countBus		=	0;			// Contador que asigna nmero a Nodos
		int			countBra		=	0;			// Contador que asigna nmero a Ramas
		int			countLin		=	0;			// Contador que asigna nmero a Lneas
		int			countTra		=	0;			// Contador que asigna nmero a Trafos
		int			countGen		=	0;			// Contador que asigna nmero a Generadores
		int			countLoad		=	0;			// Contador que asigna nmero a Cargas
		//BusbarVectorI				=	new Vector();
		double		ueb				=	0;
		double		res				=	0;
		double		z				=	0;
		double		reac			=	0;
		double		b				=	0;
		int			countbusgen 	=	0;			// Contador de Barras de Generacion
		double		vref;							// voltaje nominal de barra
		double		Pgen;
		double		Qgen;
		double		Pload;
		double		Qload;
		
		for (int ix = 0; ix < ActSchematic.Components.size(); ix++) { 
	    	TempComponent = (comp) ActSchematic.Components.elementAt(ix);
			// Only elements in selected Control Area (deprecated)
			//if(TempComponent instanceof ndb_comp){
	    	//	TempNdb_comp= (ndb_comp) TempComponent;
	    	//	if(!(TempNdb_comp.getctrla().equals(ctrla) || ctrla.equals("ALL CONTROL AREAS"))) {
	    	//		continue;
	    	//	}
	    	//}
	    	//else {
	    	//	continue;
	    	//}
	    	
	    	if(TempComponent.getactive() && TempComponent.getvalid() && TempComponent.getinservice() ) {    		
				if(TempComponent instanceof Busbar){
					//TempComponent.InformAllComponents();
		    		//TempBusbar = (Busbar)TempComponent;
		    		//BusbarVectorI.addElement(TempComponent);
					countBus++;
				}
				if(TempComponent instanceof Load){
					countLoad++;
				}
				if(TempComponent instanceof ndb_branch){
					TempBran		= (ndb_branch) TempComponent;
					if(TempComponent instanceof Line){
						TempLine	= (Line) TempBran;
	    				TempLine.InformPartner(TempLine.ConnComps1, TempLine.getx1(), TempLine.gety1(), 1); 
						TempLine.InformPartner(TempLine.ConnComps2, TempLine.getx2(), TempLine.gety2(), 2);
	    				countLin++;
					}
	    			if(TempComponent instanceof Trafo){
	    				TempComponent.InformAllComponents();
	    				TempTrafo	= (Trafo) TempBran;
	    				countTra++;
					}
	    			countBra++;
				}
				if(TempComponent instanceof ndb_inj){
		    		TempComponent.InformAllComponents();
		    		if(TempComponent instanceof Generator){
						TempGenerator	= (Generator) TempComponent;
						countGen++;
					}
				}
			}
		}
		
		int numnod		=	countBus;
		numbrasch		=	countBra;
		int numlin		=	countLin;
		int numtra		=	countTra;
		int numgen		=	countGen;
		int numload		=	countLoad;				// Total Loads
		//NumBusGen	=	new int[numgen];		// Barra de ubicacion del generador
		//String[] NomGen		=	new String[numgen];		// Nombre de los Generadores
		BranchR	=	new double[numbrasch];
		BranchX		=	new double[numbrasch];
		BranchI		=	new int[numbrasch];
		BranchJ		=	new int[numbrasch];
		NomBranch	=	new String[numbrasch];		// Nombre de las ramas
		BranchPmax = new double[numbrasch];
		//String[] NomNod		=	new	String[numnod];
		//NumBusLoad	=	new int[numload];
		//BusLoad		=	new boolean[numnod];
		countBus	=	0;
		countBra	=	0;
		countLin	=	0;
		countTra	=	0;
		countGen	=	0;
		countLoad	=	0;
		
		for (int ix = 0; ix < ActSchematic.Components.size(); ix++) {
	    	TempComponent = (comp) ActSchematic.Components.elementAt(ix);
			
			// Only elements in selected Control Area
	    	//
			//if(TempComponent instanceof ndb_comp){
	    	//	TempNdb_comp= (ndb_comp) TempComponent;
	    	//	if(!(TempNdb_comp.getctrla().equals(ctrla) || ctrla.equals("ALL CONTROL AREAS"))) {
	    	//		continue;
	    	//	}
	    	//} else {
	    	//	continue;
	    	//}
			
		    if(TempComponent.getactive() && TempComponent.getvalid() && TempComponent.getinservice()) {
				
				
				if(TempComponent instanceof Busbar){
					TempBusbar = (Busbar)TempComponent;
					//NomNod[countBus]	=	TempComponent.getName();
				countBus++;
				}
				
				if(TempComponent instanceof Load){
					TempComponent.InformAllComponents();
					TempLoad=(Load)TempComponent;
					Pload=TempLoad.getp0();
					Qload=TempLoad.getq0();
					countLoad++;
				}
				
				if(TempComponent instanceof ndb_branch){
					if(TempComponent instanceof Line) {
				    	TempLine = (Line)TempComponent;
						//con1=numbus(TempLine.getcon1());
						//con2=numbus(TempLine.getcon2());
						//BranchX[countBra] = (double)(TempLine.getxx1()*TempLine.getlen()*sref/(TempLine.getun1()*TempLine.getun1()));
						//BranchR[countBra] = (double)(TempLine.getr1()*TempLine.getlen()*sref/(TempLine.getun1()*TempLine.getun1()));
						//BranchI[countBra] = con1;
						//BranchJ[countBra] = con2;
						BranchR[countBra] = (double)(TempLine.getr1()*TempLine.getlen()*sref/(TempLine.getun1()*TempLine.getun1()));
						BranchX[countBra] = (double)(TempLine.getxx1()*TempLine.getlen()*sref/(TempLine.getun1()*TempLine.getun1()));
						BranchPmax[countBra]=TempLine.getsmax();
						NomBranch[countBra] = (String) TempLine.getName();
					    countLin++;
					}
					
					if(TempComponent instanceof Trafo) {
					    TempTrafo = (Trafo)TempComponent;
					    TempTrafo.actualizet();
							ueb = (double)((TempTrafo.getun1()/TempTrafo.getun2())/(TempTrafo.getur1()/TempTrafo.getur2()));
							res = (double)(TempTrafo.getpcu()*0.001*ueb*ueb/TempTrafo.getsn()*sref/TempTrafo.getsn());
							z   = (double)(TempTrafo.getuk()*0.01*ueb*ueb*sref/TempTrafo.getsn());
							b   = (float)(TempTrafo.geti0()*0.01*0.5/sref*TempTrafo.getsn());
							// Simulacin de efecto de reactancia negativa en trafo de tres enrollados
							if(TempTrafo.getuk() >= 0) {
						    	reac= (double)(Math.sqrt(z*z-res*res));
							} else {
						    	reac= (double)(-Math.sqrt(z*z-res*res));			
							}
							BranchX[countBra] = (double)reac;
							BranchR[countBra] = (double)res;
							//BranchI[countBra] = con1;
							//BranchJ[countBra] = con2;
							NomBranch[countBra] = (String) TempTrafo.getName();
						countTra++;
					}
					countBra++;
				}
				if(TempComponent instanceof ndb_inj){
					TempComponent.InformAllComponents();
					TempGenerator	=	(Generator)	TempComponent;
					TempNdb_inj		=	(ndb_inj)	TempComponent;
					//NomGen[countGen]	=	TempGenerator.getName();
					countGen++;
				}
			}
		}
		
	}
     */
    public void inilagrangemult() {

        //7.2--Initialize priorlist arrays:
        float readbetatemp[] = new float[numunits];
        short priorpostemp[] = new short[numunits];
        short prioridtemp[] = new short[numunits];
        priorlist = new short[numunits][numperiods];
        indexpos = new short[numunits][numperiods];
        able = new boolean[numunits][numperiods];
        must = new boolean[numunits][numperiods];
        pmax = new float[numunits][numperiods];
        pmin = new float[numunits][numperiods];
        beta = new float[numunits][numperiods];
        ubin = new byte[numunits][numperiods];
        pact = new double[numunits][numperiods];
        ulast = new byte[numunits][numperiods];
        plast = new double[numunits][numperiods];
        ubest = new byte[numunits][numperiods];
        pbest = new double[numunits][numperiods];
        fuelprice = new float[numunits][numperiods];

        if (usepiecewise) {
            activebloc = new short[numunits][numperiods];
            activepmax = new float[numunits][numperiods];
            activepmin = new float[numunits][numperiods];
            activebeta = new float[numunits][numperiods];
        }

        for (short ii = 0; ii < numunits; ii++) {
            priorpostemp[ii] = ii;
            //readbetatemp[ii] = Unitdata.readbeta[ii]*Unitdata.readfuelprice[ii];
        }

        //8--Building the First Priority list for each period:
        for (int tt = 0; tt < numperiods; tt++) {
            for (int ii = 0; ii < numunits; ii++) {

                priorlist[ii][tt] = (short) ii;
                indexpos[ii][tt] = (short) ii;
                must[ii][tt] = false;
                pmax[ii][tt] = Unitdata.readpmax[ii] - Unitdata.readauxserv[ii];
                pmin[ii][tt] = Unitdata.readpmin[ii];
                ubin[ii][tt] = 0;
                pact[ii][tt] = 0.0;
                ubest[ii][tt] = 0;
                pbest[ii][tt] = 0.0;
                fuelprice[ii][tt] = Unitdata.readfuelprice[ii];

                //Set incremental cost "beta":
                if (usepiecewise) {
                    float blockheattemp = Unitdata.getblockheat(ii, Unitdata.getnumblocks(ii) - 1);
                    beta[ii][tt] = blockheattemp * Unitdata.readfuelprice[ii];
                } else {
                    beta[ii][tt] = Unitdata.readbeta[ii] * Unitdata.readfuelprice[ii];
                }
                readbetatemp[ii] = beta[ii][tt];

                //Discard if unit has its own unavailable flag "on":
                if (Unitdata.readinservice[ii]) {
                    able[ii][tt] = true;
                } else {
                    able[ii][tt] = false;
                    priorlist[ii][tt] = -1;
                }

                //Discard or update if Maintenance is programmed:
                for (int kk = 0; kk < nummaint; kk++) {
                    if ((Unitdata.readunitid[ii] == Maintdata.readunitid[kk])
                            && (Maintdata.readfrommaint[kk] <= tt)
                            && (tt <= Maintdata.readtomaint[kk])) {
                        if (Maintdata.readinservice[kk]) {
                            able[ii][tt] = true;
                            pmax[ii][tt] = Maintdata.readpmax[kk];
                            pmin[ii][tt] = Maintdata.readpmin[kk];
                            fuelprice[ii][tt] = Maintdata.readbeta[kk];
                            beta[ii][tt] = Unitdata.readbeta[ii] * fuelprice[ii][tt];//OJO!!
                            readbetatemp[ii] = Maintdata.readbeta[kk] * fuelprice[ii][tt];
                        } else {
                            able[ii][tt] = false;
                            priorlist[ii][tt] = -1;
                            pmax[ii][tt] = 0;
                            pmin[ii][tt] = 0;
                            beta[ii][tt] = Float.MAX_VALUE;
                            readbetatemp[ii] = Float.MAX_VALUE;
                            fuelprice[ii][tt] = Float.MAX_VALUE;//OJO!!
                        }
                        break;
                    }
                }

                //Discard or update if User inputs invalidate:
                for (int kk = 0; kk < numuserout; kk++) {
                    if ((Unitdata.readunitid[ii] == Userdata.readunitid[kk])
                            && (Userdata.readfromout[kk] <= tt)
                            && ((tt) <= Userdata.readtoout[kk])) {
                        if (Userdata.readinservice[kk]) {
                            able[ii][tt] = true;
                            must[ii][tt] = Userdata.readmustrun[kk];
                            pmax[ii][tt] = Userdata.readpmax[kk];
                            pmin[ii][tt] = Userdata.readpmin[kk];
                            fuelprice[ii][tt] = Userdata.readbeta[kk];
                            beta[ii][tt] = Unitdata.readbeta[ii] * fuelprice[ii][tt];
                            readbetatemp[ii] = Userdata.readbeta[kk] * fuelprice[ii][tt];
                        } else {
                            able[ii][tt] = false;
                            must[ii][tt] = false;
                            priorlist[ii][tt] = -1;
                            pmax[ii][tt] = 0;
                            pmin[ii][tt] = 0;
                            beta[ii][tt] = Float.MAX_VALUE;
                            readbetatemp[ii] = Float.MAX_VALUE;
                            fuelprice[ii][tt] = Float.MAX_VALUE;//OJO!!
                        }
                        break;
                    }
                }

                //Discard if minimun output unit is above system limit:
                if (useplimit) {
                    float psystem = Systemdata.readpmax[tt];
                    if (pmin[ii][tt] > psystem) {
                        able[ii][tt] = false;
                        priorlist[ii][tt] = -1;

                        //Adjust output limit if needed:
                    } else {
                        if (pmax[ii][tt] > psystem + tol_b) {
                            pmax[ii][tt] = psystem;
                        }
                    }
                }
            }

            //Order by cost (beta) and discard expensier configurations:
            //cont=0;
            for (int ii = 0; ii < numunits; ii++) {
                for (int kk = ii + 1; kk < numunits; kk++) {

                    if (readbetatemp[kk] < readbetatemp[ii]) {

                        if (Unitdata.readunitcentralid[ii] == Unitdata.readunitcentralid[kk]) {

                            short swapauxshort = priorlist[ii][tt];
                            float swapbeta = readbetatemp[ii];
                            priorlist[ii][tt] = priorlist[kk][tt];
                            readbetatemp[ii] = readbetatemp[kk];
                            priorlist[kk][tt] = swapauxshort;
                            //priorlist[kk][tt]=-1;
                            readbetatemp[kk] = Float.MAX_VALUE;

                        } else {

                            short swapauxshort = priorlist[ii][tt];
                            float swapbeta = readbetatemp[ii];
                            priorlist[ii][tt] = priorlist[kk][tt];
                            readbetatemp[ii] = readbetatemp[kk];
                            priorlist[kk][tt] = swapauxshort;
                            readbetatemp[kk] = swapbeta;

                        }
                        short swapauxshort = indexpos[ii][tt];
                        indexpos[ii][tt] = indexpos[kk][tt];
                        indexpos[kk][tt] = swapauxshort;

                    } else {

                    }
                }
            }

        }

        //9--Initializing Lagrange Multipliers:
        int cont = 0;
        multdem = new float[numperiods];
        multres = new float[numperiods];
        for (int tt = 0; tt < numperiods; tt++) {
            double pcommit = 0;
            cont = 0;

            //9.1--DEMAND BALANCE CONSTRAINT--
            //9.1.1--Filling Demand Requirements:
            while ((pcommit < hourlypdem[tt])) {

                if (priorlist[cont][tt] != -1) {
                    ubin[priorlist[cont][tt]][tt] = 1;
                    pcommit += pmax[priorlist[cont][tt]][tt];
                    pact[priorlist[cont][tt]][tt] = pmax[priorlist[cont][tt]][tt]; //CAMBIAR!! 	PQ??
                }
                cont++;
                if (cont == numunits) {
                    //DAR AVISO DE INFACTIBLE!!
                    System.out.println("WARNING!! Impossible to satisfy demand at hour: " + tt);
                    break;
                }

            }

            //9.1.2--Adjust Marginal Unit Output to satisfy Demand Constraint (it may be unnecessary):
            if (cont != 0 && priorlist[cont - 1][tt] != -1) {
                double pactaux = pmax[priorlist[cont - 1][tt]][tt] - pcommit + hourlypdem[tt];
                if (pactaux >= (double) pmin[priorlist[cont - 1][tt]][tt]) {
                    pact[priorlist[cont - 1][tt]][tt] = pactaux;
                } else {
                    pact[priorlist[cont - 1][tt]][tt] = (double) pmin[priorlist[cont - 1][tt]][tt];
                }

                //9.1.3--Define lambda equal to marginal unit cost(beta):
                if (pcommit == 0 || cont == 0) {
                    multdem[tt] = 0;
                } else {
                    multdem[tt] = beta[priorlist[cont - 1][tt]][tt]; //Marginal unit
                }
            }

            //9.2--SPINNING RESERV BALANCE CONSTRAINT--
            //9.2.1--Filling Reserve Requirements:
            if (usespin) {
                //cont=0;
                //pcommit=0;
                if (pcommit < hourlypdem[tt] + hourlyspin[tt]) {
                    if (cont == numunits) {
                        System.out.println("WARNING!! Impossible to satisfy spinning reserve at hour: " + tt);
                        //DAR AVISO DE INFACTIBLE!!
                    } else {
                        while ((pcommit < hourlypdem[tt] + hourlyspin[tt])) {

                            if (priorlist[cont][tt] != -1) {
                                ubin[priorlist[cont][tt]][tt] = 1;
                                pcommit += pmax[priorlist[cont][tt]][tt];
                                pact[priorlist[cont][tt]][tt] = pmin[priorlist[cont][tt]][tt]; //ojo!!
                            }
                            cont++;
                            if (pcommit < hourlypdem[tt] + hourlyspin[tt] && cont == numunits) {
                                //DAR AVISO DE INFACTIBLE!!
                                System.out.println("WARNING!! Impossible to satisfy spinning reserve at hour: " + (tt + 1));
                                break;
                            }

                        }
                    }
                }

                //9.2.2--Define mu's equal to marginal unit if reserv is needed:
                if (pcommit == 0 || cont == 0) {
                    multres[tt] = 0;
                } else {
                    float pactaux = pmin[priorlist[cont - 1][tt]][tt] / pmax[priorlist[cont - 1][tt]][tt];
                    multres[tt] = Math.max(pactaux * (beta[priorlist[cont - 1][tt]][tt] - multdem[tt]), 0);
                }

            }

        }

        /*Impresiones Provisionales:
					//Print Initial Lagrange Multipliers: 		Provisional!!!
					for (int tt=0; tt<numperiods; tt++){
					System.out.println ("lambda["+tt+"]="+multdem[tt]);
					}
					for (int tt=0; tt<numperiods; tt++){
					System.out.println ("mu's["+tt+"]="+multres[tt]);
					}
					
					for (int tt=0; tt<numperiods; tt++){
					for (int ii=0; ii<numunits; ii++){
					System.out.println ("ubin["+ii+"]["+tt+"]="+ubin[ii][tt]);
					}}
					for (int tt=0; tt<numperiods; tt++){
					for (int ii=0; ii<numunits; ii++){
					System.out.println ("pact["+ii+"]["+tt+"]="+pact[ii][tt]);
					}}
         */
 /*
		//Imprime la potencia de despacho lineal por trozos: 		Provisional!!!
		for (int ii=0; ii<numunits; ii++){
		for (int tt=0; tt<numperiods; tt++){
			boolean islow=false;
			double popt=0;
			int mup=0,mdown=0;
			float pmaxdown=0,pminup=Float.MAX_VALUE;
			float pmindown=0,pmaxup=0;
			float blockpmin;
			float blockpmax;
			float blockbeta;
			float[] mcost= new float[(int)Unitdata.getnumblocks(ii)];
			//float mbestd=Float.MAX_VALUE, mbestu=Float.MAX_VALUE;
			
			for (int kk=0;kk<Unitdata.getnumblocks(ii);kk++){
				
				blockpmin=Unitdata.getblockpmin(ii,kk);
				blockpmax=Unitdata.getblockpmax(ii,kk);
				blockbeta=Unitdata.getblockheat(ii,kk)*fuelprice[ii][tt];
				
				//3.1.1--Descarta bloque fuera de rangos:
				if (blockpmax<pmin[ii][tt] || blockpmin>pmax[ii][tt]){
					continue;
				}
				
				//3.1.2--Ajustar potencia MAXIMA a establecida por mantenimiento o especial:
				if (blockpmax>pmax[ii][tt]){
					blockpmax=pmax[ii][tt];
				}
				
				//3.1.3--Ajustar potencia MINIMA a establecida por mantenimiento o especial:
				if (blockpmin<pmin[ii][tt]){
					blockpmin=pmin[ii][tt];
				}
				
				mcost[kk]=(Unitdata.getblockheat(ii,kk)*fuelprice[ii][tt])-(multdem[tt]);
				
				
			//--NON-CONVEX CASE-- piece-wise linear optimal power output:
				if (mcost[kk]>0){
					if (blockpmin<pminup){
						pminup=blockpmin;
						pmaxup=blockpmax;
						mup=kk;
					}
				}else{
					islow=true;
					if (blockpmax>pmaxdown){
						pmaxdown=blockpmax;
						pmindown=blockpmin;
						mdown=kk;
					}
				}
			}
			
			//Set active linear block:
			if (islow){
				activebloc[ii][tt]=(short)mdown;
				activepmax[ii][tt]=pmaxdown;
				activepmin[ii][tt]=pmindown;
				activebeta[ii][tt]=Unitdata.getblockheat(ii,mdown)*fuelprice[ii][tt];
				popt=(double)pmaxdown;
				//beta[ii][tt]=Unitdata.getblockheat(ii,mdown)*fuelprice[ii][tt];
			}else{
				activebloc[ii][tt]=(short)mup;
				activepmax[ii][tt]=pmaxup;
				activepmin[ii][tt]=pminup;
				activebeta[ii][tt]=Unitdata.getblockheat(ii,mup)*fuelprice[ii][tt];
				popt=(double)pminup;
				//beta[ii][tt]=Unitdata.getblockheat(ii,mup)*fuelprice[ii][tt];
			}
			System.out.println("Popt["+ii+"]["+tt+"]="+popt);
			System.out.println("Active Block ="+activebloc[ii][tt]);
			System.out.println("Active Pmax ="+activepmax[ii][tt]);
			System.out.println("Active Pmin ="+activepmin[ii][tt]);
			System.out.println("Active Beta ="+activebeta[ii][tt]);
		}}
         */
    }

    public void individualdp() {

        //10--Individual Unit Dynamic Programming Schedule:
        byte tray[][];// = new byte [2][numperiods];
        double cumulcost[][];// = new double[2][numperiods];
        boolean estado[][];// = new boolean[2][numperiods];
        double operacost;
        double popt[];// = new double [numperiods];
        short cumultime[][];// = new short[2][numperiods];
        marginal = new short[numperiods];

        for (int ii = 0; ii < numunits; ii++) {

            popt = new double[numperiods];
            tray = new byte[2][numperiods];
            cumulcost = new double[2][numperiods];
            estado = new boolean[2][numperiods];
            cumultime = new short[2][numperiods];

            for (int tt = 0; tt < numperiods; tt++) {

                //Initialize arrays:
                plast[ii][tt] = pact[ii][tt];
                ulast[ii][tt] = ubin[ii][tt];
                boolean camino[][] = new boolean[2][2];
                camino[0][0] = true;
                camino[0][1] = true;
                camino[1][0] = true;
                camino[1][1] = true;
                estado[0][tt] = true;
                estado[1][tt] = true;

                //1--EL INICIO (tt==0) ES UN CASO ESPECIAL:
                if (tt == 0) {

                    //1.2--IDENTIFICA CUALES DE LOS DOS ESTADOS SON POSIBLES (existe):
                    //1.2.1--Por indisponibilidad:
                    if (!able[ii][tt]) {
                        estado[1][0] = false;
                    }
                    //1.2.2--Por tiempo minimo de partida:
                    if (Unitdata.readtrunini[ii] > 0) {
                        if (Unitdata.readtrunini[ii] < Unitdata.readtminup[ii]) {
                            estado[0][0] = false;
                        }
                    } //1.2.3--Por tiempo minimo de parada:
                    else {
                        if (Math.abs(Unitdata.readtrunini[ii]) < Unitdata.readtmindown[ii]) {
                            estado[1][0] = false;
                        }
                    }
                    //1.2.4--Si es una unidad mustrun:
                    if (must[ii][tt]) {
                        if (!estado[1][0]) {
                            System.out.println("IMPOSIBLE TO CONPLAIN MUSTRUN!");
                            //LANZAR UN WARNING QUE NO SE PUDO CUMPLIR EL MUSTRUN! RANAS Y SAPOS!!
                        } else {
                            estado[0][0] = false;
                        }
                    }

                    //1.3--DETERMINA LOS COSTOS DE OPERACION:
                    if (estado[1][0]) {
                        popt[tt] = getpopt(ii, tt);
                        operacost = getoperacost(ii, tt, popt[tt]);
                    } else {
                        popt[tt] = 0;
                        operacost = 0;
                    }

                    //1.4--IDENTIFICA CUALES CAMINOS SON POSIBLES:
                    //1.4.1--Descarta si estaba ENCENDIDA segun condiciones iniciales:
                    if (Unitdata.readtrunini[ii] > 0) {
                        camino[0][0] = false;
                        camino[0][1] = false;
                        //1.4.2--Descarta si estaba APAGADA segun condiciones iniciales:
                    } else {
                        camino[1][0] = false;
                        camino[1][1] = false;
                    }
                    //1.4.3--Descarta si el estado [0] es imposible:
                    if (!estado[0][0]) {
                        camino[0][0] = false;
                        camino[1][0] = false;
                    }
                    //1.4.4--Descarta si el estado [1] es imposible:
                    if (!estado[1][0]) {
                        camino[0][1] = false;
                        camino[1][1] = false;
                    }

                    //1.5--DETERMINA LAS MEJORES TRAYECTORIAS:
                    //1.5.1--Si se viene desde apagado:
                    if (camino[0][0] || camino[0][1]) {
                        tray[0][0] = 0;
                        tray[1][0] = 0;
                    }
                    //1.5.2--Si se viene desde encendido:
                    if (camino[1][0] || camino[1][1]) {
                        tray[0][0] = 1;
                        tray[1][0] = 1;
                    }

                    //1.6--DETERMINA LOS COSTOS ACUMULADOS DE T==0:
                    //1.6.1--Costos si se viene desde apagado:
                    if (camino[0][0] || camino[0][1]) {
                        cumulcost[0][0] = 0;
                        cumulcost[1][0] = operacost + getcst(ii, tt);
                    }
                    //1.6.2--Costo si viene desde encendido:
                    if (camino[1][0] || camino[1][1]) {
                        cumulcost[0][0] = 0;
                        cumulcost[1][0] = operacost;
                    }

                    //1.7--DETERMINA EL TIEMPO ACUMULADO DE OPERACION EN T==0:
                    //1.7.1--Tiempo de apagado (si existe el estado=0)
                    if (camino[0][0]) {
                        cumultime[0][0] = (short) (Math.abs(Unitdata.readtrunini[ii]) + 1);
                    }
                    if (camino[1][0]) {
                        cumultime[0][0] = 1;
                    }

                    //1.7.2--Tiempo de encendido (si existe el estado=1)
                    if (camino[0][1]) {
                        cumultime[1][0] = 1;
                    }
                    if (camino[1][1]) {
                        cumultime[1][0] = (short) (Unitdata.readtrunini[ii] + 1);
                    }

                    //2--TIEMPO DISTINTO DE CERO (tt>0):
                } else {

                    //2.2--IDENTIFICA CUALES DE LOS 2 ESTADOS SON POSIBLES EN T>0:
                    //2.2.1--Por indisponibilidad:
                    if (!able[ii][tt]) {
                        estado[1][tt] = false;
                    }

                    //2.2.2--Descartar el estado 0 porque no se puede apagar la unidad:
                    if (!estado[0][tt - 1] && estado[1][tt - 1] && (cumultime[1][tt - 1] < Unitdata.readtminup[ii])) {
                        estado[0][tt] = false;
                    }

                    //2.2.3--Descartar el estado 1 porque no se puede encender la unidad:
                    if (!estado[1][tt - 1] && estado[0][tt - 1] && (cumultime[0][tt - 1] < Unitdata.readtmindown[ii])) {
                        estado[1][tt] = false;
                    }

                    //2.2.4--Si es una unidad mustrun:
                    if (must[ii][tt]) {
                        if (!estado[1][tt]) {
                            System.out.println("NO SE PUDO CUMPLIR EL MUSTRUN!");
                            //LANZAR UN WARNING QUE NO SE PUDO CUMPLIR EL MUSTRUN!
                        } else {
                            estado[0][tt] = false;
                            System.out.println("SE RESPETA EL MUSTRUN");
                        }
                    }

                    //2.3--DETERMINA LOS COSTOS DE OPERACION EN T>0:
                    if (estado[1][tt]) {
                        popt[tt] = getpopt(ii, tt);
                        //operacost=beta[ii][tt]*popt[tt]-multdem[tt]*popt[tt];
                        operacost = getoperacost(ii, tt, popt[tt]);
                    } else {
                        popt[tt] = 0;
                        operacost = 0;
                    }

                    //2.4--IDENTIFICA CUALES CAMINOS SON POSIBLES:
                    //2.4.1--Descarta camino [0]->[0]:
                    if (!estado[0][tt - 1] || !estado[0][tt]) {
                        camino[0][0] = false;
                    }

                    //2.4.2--Descarta camino [0]->[1]:
                    //Opcion 1: Que no exista alguno de los estados a los extremos:
                    if (!estado[0][tt - 1] || !estado[1][tt]) {
                        camino[0][1] = false;
                    }
                    //Opcion 2: Que no se pueda por tiempo minimo de apagado
                    if (cumultime[0][tt - 1] < Unitdata.readtmindown[ii]) {
                        camino[0][1] = false;
                    }

                    //2.4.3-Descarta camino [1]->[0]:
                    //Opcion 1: Que no exista alguno de los estados a los extremos:
                    if (!estado[1][tt - 1] || !estado[0][tt]) {
                        camino[1][0] = false;
                    }
                    //Opcion 2: Que no se pueda por tiempo minimo de encendido
                    if (cumultime[1][tt - 1] < Unitdata.readtminup[ii]) {
                        camino[1][0] = false;
                    }

                    //2.4.4--Descarta camino [1]->[1]:
                    if (!estado[1][tt - 1] || !estado[1][tt]) {
                        camino[1][1] = false;
                    }

                    //2.5--DETERMINA LAS MEJORES TRAYECTORIAS:
                    //5.1--La mejor opcion para apagar la unidad es: (Calculo de tray[0] y cumulcost[0])
                    if (camino[0][0] && camino[1][0]) {
                        if (cumulcost[0][tt - 1] <= cumulcost[1][tt - 1]) {
                            cumulcost[0][tt] = cumulcost[0][tt - 1];
                            tray[0][tt] = 0;
                        } else {
                            cumulcost[0][tt] = cumulcost[1][tt - 1];
                            tray[0][tt] = 1;
                        }
                    } else if (camino[0][0] && !camino[1][0]) {
                        cumulcost[0][tt] = cumulcost[0][tt - 1];
                        tray[0][tt] = 0;
                    } else if (!camino[0][0] && camino[1][0]) {
                        cumulcost[0][tt] = cumulcost[1][tt - 1];
                        tray[0][tt] = 1;
                    } else if (!camino[0][0] && !camino[1][0]) {
                        cumulcost[0][tt] = 0;
                        tray[0][tt] = tray[0][tt - 1];
                        //System.out.println("por aqui esta pasando la cosa11!!");
                    }

                    //2.5.2--La mejor opcion para encender la unidad es:
                    if (camino[0][1] && camino[1][1]) {
                        if (cumulcost[0][tt - 1] + transcost(ii, cumultime[0][tt - 1]) <= cumulcost[1][tt - 1]) {
                            cumulcost[1][tt] = cumulcost[0][tt - 1] + transcost(ii, cumultime[0][tt - 1]) + operacost;
                            tray[1][tt] = 0;
                        } else {
                            cumulcost[1][tt] = cumulcost[1][tt - 1] + operacost;
                            tray[1][tt] = 1;
                        }
                    } else if (camino[0][1] && !camino[1][1]) {
                        cumulcost[1][tt] = cumulcost[0][tt - 1] + transcost(ii, cumultime[0][tt - 1]) + operacost;
                        tray[1][tt] = 0;
                    } else if (!camino[0][1] && camino[1][1]) {
                        cumulcost[1][tt] = cumulcost[1][tt - 1] + operacost;
                        tray[1][tt] = 1;
                    } else if (!camino[0][1] && !camino[1][1]) {
                        cumulcost[1][tt] = 0;
                        tray[1][tt] = tray[1][tt - 1];
                        //System.out.println("por aqui esta pasando la cosa!! unidad: "+ii);
                    }

                    //2.6--DETERMINA EL TIEMPO ACUMULADO DE OPERACION EN T>0:
                    //2.7.1--Tiempo de apagado si se llega al estado estado=0:
                    if ((tray[0][tt] == 0) && estado[0][tt - 1]) {
                        cumultime[0][tt] = (short) (cumultime[0][tt - 1] + 1);
                    } else {
                        cumultime[0][tt] = 1;
                    }

                    //2.7.2--Tiempo de encendido si se llega al estado estado=1:
                    if ((tray[1][tt] == 1) && estado[1][tt - 1]) {
                        cumultime[1][tt] = (short) (cumultime[1][tt - 1] + 1);
                    } else {
                        cumultime[1][tt] = 1;
                    }

                }

                /*
		System.out.println ("estado["+0+"]["+tt+"]="+estado[0][tt]);
		System.out.println ("estado["+1+"]["+tt+"]="+estado[1][tt]);
		
		System.out.println ("camino[0][0]="+camino[0][0]);
		System.out.println ("camino[0][1]="+camino[0][1]);
		System.out.println ("camino[1][0]="+camino[1][0]);
		System.out.println ("camino[1][1]="+camino[1][1]);
		
		System.out.println ("operacost="+operacost);
		
		System.out.println ("tray["+0+"]["+tt+"]="+tray[0][tt]);
		System.out.println ("tray["+1+"]["+tt+"]="+tray[1][tt]);
		
		System.out.println ("cumulcost["+0+"]["+tt+"]="+cumulcost[0][tt]);
		System.out.println ("cumulcost["+1+"]["+tt+"]="+cumulcost[1][tt]);
		
		System.out.println ("cumultime["+0+"]["+tt+"]="+cumultime[0][tt]);
		System.out.println ("cumultime["+1+"]["+tt+"]="+cumultime[1][tt]);
                 */
            }//end for tt

            //Se devuelve a asignar la mejor opcion:
            if (cumulcost[0][numperiods - 1] <= cumulcost[1][numperiods - 1]) {
                if (estado[0][numperiods - 1]) {
                    ubin[ii][numperiods - 1] = 0;
                } else {
                    ubin[ii][numperiods - 1] = 1;
                }
            } else {
                if (estado[1][numperiods - 1]) {
                    ubin[ii][numperiods - 1] = 1;
                } else {
                    ubin[ii][numperiods - 1] = 0;
                }
                //ubin[ii][numperiods-1]=1;
            }

            pact[ii][numperiods - 1] = ubin[ii][numperiods - 1] * popt[numperiods - 1];
            for (int tt = numperiods - 1; tt > 0; tt--) {
                ubin[ii][tt - 1] = tray[ubin[ii][tt]][tt];
                pact[ii][tt - 1] = tray[ubin[ii][tt]][tt] * popt[tt - 1];
            }

        }//end for ii

        //3--Adjust Marginal Unit Output Heuristic (ONLY WITH LP)
        if (useheuristic && !usequadra) {
            float deltadem = 0;
            for (int tt = 0; tt < numperiods; tt++) {
                deltadem = 0;

                //3.1--Find Marginal Unit:
                marginal[tt] = getmarginalunit(tt);
                for (int ii = 0; ii < numunits; ii++) {
                    deltadem += ubin[ii][tt] * pact[ii][tt];
                }
                deltadem = deltadem - hourlypdem[tt];

                //3.2--If insuficient generation -> do nothing (por lo pronto)
                if (deltadem < 0) {

                    //3.3--If excess of commited generation -> adjust marginal output:
                } else {

                    if (deltadem > pmax[marginal[tt]][tt] - pmin[marginal[tt]][tt]) {
                        if (usepiecewise) {
                            pact[marginal[tt]][tt] = activepmin[marginal[tt]][tt];
                        } else {
                            pact[marginal[tt]][tt] = pmin[marginal[tt]][tt];
                        }
                    } else {
                        if (usepiecewise) {
                            pact[marginal[tt]][tt] = activepmax[marginal[tt]][tt] - deltadem;
                        } else {
                            pact[marginal[tt]][tt] = pmax[marginal[tt]][tt] - deltadem;
                        }

                        //3.4--Correct if ramp limit found:
                        if (useramp && tt > 0 && Unitdata.readgradup[marginal[tt]] != 0 && ubin[marginal[tt]][tt] == 1) {
                            if (pact[marginal[tt]][tt] - pact[marginal[tt]][tt - 1] > Unitdata.readgradup[marginal[tt]] * timediv) {
                                pact[marginal[tt]][tt] = pact[marginal[tt]][tt - 1] + Unitdata.readgradup[marginal[tt]] * timediv;
                            }
                        }
                    }
                }
            }

        }

    }

    public void dualeval() {

        //11--Dual Function Evaluation:
        dualfunction = 0;
        startcost = 0;
        double pdual, rdual, deltapboth = 0, sumpact = 0, sumplast = 0;

        for (int tt = 0; tt < numperiods; tt++) {
            pdual = 0;
            rdual = 0;
            deltapboth = 0;
            sumpact = 0;
            sumplast = 0;

            for (int ii = 0; ii < numunits; ii++) {

                //11.1--Total produccion cost:
                //11.1.1--If non-linear augmented lagrangian is selected:
                if (useaugmen) {
                    deltapboth += Math.pow(pact[ii][tt] * ubin[ii][tt] - plast[ii][tt] * ulast[ii][tt], 2);
                    sumpact += pact[ii][tt] * ubin[ii][tt];
                    sumplast += plast[ii][tt] * ulast[ii][tt];
                }

                //11.1.2--If quadratic costs are selected: OJO Unitdata.readalpha[ii] NO ESTA INCLUIDO
                if (usequadra) {
                    dualfunction += (double) (beta[ii][tt] * pact[ii][tt] * ubin[ii][tt]
                            + Unitdata.readgamma[ii] * pact[ii][tt] * pact[ii][tt]) * ubin[ii][tt];
                    //11.1.3--If linear costs are selected:
                } else {
                    if (usepiecewise) {
                        dualfunction += (double) (activebeta[ii][tt] * pact[ii][tt] * ubin[ii][tt]);
                    } else {
                        dualfunction += (double) (beta[ii][tt] * pact[ii][tt] * ubin[ii][tt]);
                    }
                }

                //11.2--Startup Cost: (added to dualfunction in convergencecheck method)
                startcost += getcst(ii, tt);

                //11.3--Lagrange's Linear Augmentation:
                pdual += pact[ii][tt] * ubin[ii][tt];
                rdual += (double) pmax[ii][tt] * ubin[ii][tt];
            }//end ii

            dualfunction += multdem[tt] * ((double) hourlypdem[tt] - pdual);
            dualfunction += multres[tt] * ((double) (hourlyspin[tt] - (rdual - pdual)));

            //11.4--Augmented quadratic term inclusion:	
            if (useaugmen) {
                dualfunction += (1 / (2 * epsaug)) * deltapboth;
                dualfunction += (-1) * c_coef * (sumpact - hourlypdem[tt]) * (sumplast - hourlypdem[tt]);
            }

        }//end tt

        //11.5--Transmission Sub-problem Bender's cuts:
        if (txconstraint) {
            for (int kk = 0; kk < numcuts; kk++) {
                float deltaint = 0;
                for (int ii = 0; ii < numunits; ii++) {
                    deltaint += cutsimplexval[kk][ii] * (pact[ii][cuttime[kk]] * ubin[ii][cuttime[kk]] - cutpit[kk][ii]);
                }
                dualfunction += cutmultiplier[kk] * (cutobjval[kk] + deltaint);
            }
        }

    }

    public boolean primaleval() {

        //12--Primal Function Evaluation -> Economic Dispatch:
        //FileOutputStream os;
        //BufferedOutputStream bos;
        //FileInputStream is1;
        //BufferedReader is;
        //int numbasic=1;
        //if (usespin){
        //	numbasic+=1;
        //}
        //runExternalFile rr = new runExternalFile();  //(INDEPENDANT)
        double pmaxcommit = 0;
        boolean feasprimal = true;
        boolean feasesol = true;
        feasable = new boolean[numperiods];
        ppri = new double[numunits][numperiods];
        lambdapri = new double[numperiods];
        primalfunction = 0;

        int Arow[], Acol[];
        double Acoef[];
        double objcoef[];
        double LBvector[];
        double UBvector[];
        double RHS[];				//Provisional (Relaxed constraints)
        int constrtype[];			//Provisional (Relaxed constraints)
        //byte constrtype[];

        for (int tt = 0; tt < numperiods; tt++) {

            //12.1.1--Prepare Matrices:
            pmaxcommit = 0;
            int cont = 0;

            objcoef = new double[numunits];
            LBvector = new double[numunits];
            UBvector = new double[numunits];
            RHS = new double[2];				//Provisional (Relaxed constraints)
            constrtype = new int[2];			//Provisional (Relaxed constraints)
            //constrtype=new byte[2];
            for (int ii = 0; ii < numunits; ii++) {
                if (ubin[ii][tt] != 0) {
                    cont++;
                    pmaxcommit += pmax[ii][tt];
                }
            }
            Arow = new int[(cont + 1) * 2];
            Acol = new int[(cont + 1) * 2];
            Acoef = new double[(cont + 1) * 2];
            //int auxcont=cont;
            cont = 0;
            for (int ii = 0; ii < numunits; ii++) {
                objcoef[ii] = beta[ii][tt] * ubin[ii][tt];
                LBvector[ii] = pmin[ii][tt] * ubin[ii][tt];
                UBvector[ii] = pmax[ii][tt] * ubin[ii][tt];
                if (ubin[ii][tt] != 0) {
                    Arow[cont] = 0;
                    Arow[cont + 1] = 1;
                    Acol[cont] = ii;
                    Acol[cont + 1] = ii;
                    Acoef[cont] = 1;
                    Acoef[cont + 1] = -1;
                    cont += 2;
                }
            }

            constrtype[0] = OptimizerCaller.EQUAL;
            constrtype[1] = OptimizerCaller.LESSTHAN;
            RHS[0] = hourlypdem[tt];
            if (usespin) {
                RHS[1] = hourlyspin[tt] - pmaxcommit;
            } else {
                RHS[1] = 0;
            }

            //------GLPK OPTIMIZATION SOFTWARE------
            if (optimizer.equals(OptimizerCaller.GLPK_SOLVER) || optimizer.equals(OptimizerCaller.LPSOLVE)) { //&& feasprimal){
                /*==INICIO ANTIGUO FUNCIONA==	
				pmaxcommit=0;
				//short cont=0;
				//cont=0;
				short ttcuts=0;
				feasable[tt]=false;
				
			==FIN ANTIGUO FUNCIONA==*/

                //DIRECT GLPK CALL
                //12.1.1--Prepare Matrices:
                /*
				objcoef=new double[numunits];
				LBvector=new double[numunits];
				UBvector=new double[numunits];
				RHS=new double[2];				//Provisional (Relaxed constraints)
				constrtype=new byte[2];			//Provisional (Relaxed constraints)
				for (int ii=0;ii<numunits;ii++){
					if (ubin[ii][tt]!=0){
						cont++;
						pmaxcommit+=pmax[ii][tt];
					}
				}
				Arow = new int [(cont+1)*2];
				Acol = new int [(cont+1)*2];
				Acoef = new double [(cont+1)*2];
				cont=0;
				for (int ii=0;ii<numunits;ii++){
					objcoef[ii]=beta[ii][tt]*ubin[ii][tt];
					LBvector[ii]=pmin[ii][tt];
					UBvector[ii]=pmax[ii][tt];
					if (ubin[ii][tt]!=0){
						Arow[cont+1]=1;
						Arow[cont+2]=2;
						Acol[cont+1]=ii+1;
						Acol[cont+2]=ii+1;
						Acoef[cont+1]=1;
						Acoef[cont+2]=-1;
						cont+=2;
					}
				}
				constrtype[0]=0;
				constrtype[1]=1; //Activar para restriccion de reserva
				RHS[0]=hourlypdem[tt];
				RHS[1]=hourlyspin[tt]-pmaxcommit; //Activar para restriccion de reserva
				
				
				
				
				/*GLPK EXECUTABLE CALL USING CUSTOM C++ (DEPRECATED FOR A WHILE)
				
				try{
					runGLPKsol runglpk = new runGLPKsol();
					runglpk.exec(glpkdir,glpklpmethod,objcoef,Arow,Acol,Acoef,
					             RHS,constrtype,LBvector,UBvector);
				}catch(Exception e){
					String message = e.toString();
					System.out.println("Error Calling GLPK: "+message);
				}
                 */
 /*==INICIO ANTIGUO FUNCIONA==
				
				//GLPK EXECUTABLE CALL USING "org.gnu.glpk" LIBRARY:
				//12.1.2--Fill LP Problem:
				GlpkSolver glpsol = new GlpkSolver();
				glpsol.setProbName("UCOPT");
				glpsol.setObjName("objucopt");
				glpsol.setObjDir(GlpkSolver.LPX_MIN);
				
				glpsol.addRows(2);
				glpsol.addCols(numunits);
				glpsol.setIntParm(GlpkSolver.LPX_K_MSGLEV, 0);
				
				for (int ii=0;ii<numunits;ii++){
					glpsol.setObjCoef(ii+1 , objcoef[ii]);
					if (ubin[ii][tt]==0){
						glpsol.setColBnds(ii+1, GlpkSolver.LPX_FX, 0, 0);
					}else{
						glpsol.setColBnds(ii+1, GlpkSolver.LPX_DB, LBvector[ii], UBvector[ii]);
					}
				}
				
				glpsol.setRowBnds(1, GlpkSolver.LPX_FX, RHS[0], 0);
				glpsol.setRowBnds(2, GlpkSolver.LPX_LO, RHS[1], 0);
				glpsol.loadMatrix(cont, Arow, Acol, Acoef);
				
				//12.1.3--Call glpk linear programming methodology
				if (glpklpmethod.equals("simplex")){
					glpsol.simplex();
				}else{
					glpsol.interior();
				}
				//System.out.println("TT = "+tt);
				//12.1.4--Write problem to a CPLEX file (optional)
				//if (tt==3){
				//	glpsol.writeCpxlp("CplexFormat.txt");
				//}
				
				//12.1.5--Reading Results:
				feasprimal=false;
				if (glpklpmethod.equals("simplex")){
				
					//If optimal solution found by simplex:
					if(glpsol.getStatus()==GlpkSolver.LPX_OPT){
			    		feasable[tt]=true;
			    		feasprimal=true;
			    		//Read Primal Function Value:
					    primalfunction+=glpsol.getObjVal();
					    //Read Lagrange Multipliers:
					    lambdapri[tt]=glpsol.getRowDual(1);
					    //Read primal values:
						for (int ii=0;ii<numunits;ii++){
					    	ppri[ii][tt]=glpsol.getColPrim(ii+1)*ubin[ii][tt];
						}
					}else{
			    		feasable[tt]=false;
			    		feasprimal=false;
			    		feasesol=false;
					}
								
				}else{
					
					//If optimal solution found by interior point:
					if(glpsol.iptStatus()==GlpkSolver.LPX_T_OPT){
			    		feasable[tt]=true;
			    		feasprimal=true;
			    		//Read Primal Function Value:
					    primalfunction+=glpsol.iptObjVal();
					    //Read Lagrange Multipliers:
					    lambdapri[tt]=glpsol.iptRowDual(1);
					    //Read primal values:
						for (int ii=0;ii<numunits;ii++){
					    	ppri[ii][tt]=glpsol.iptColPrim(ii+1)*ubin[ii][tt];
						}
					}else{
			    		feasable[tt]=false;
			    		feasprimal=false;
			    		feasesol=false;
					}
					
				}
				
				
				
				glpsol.deleteProb();
				==FIN ANTIGUO FUNCIONA==*/
 /*
				//12.1--Linear Optimization File Creation:
				try{
					
					os  = new FileOutputStream(glpkdir+"\\bin\\"+glpkpifname);
					bos = new BufferedOutputStream(os,32768);
					PrintStream myPrintStream = new PrintStream(bos, false);
					
					
					myPrintStream.println("NAME UCOPT");
					
					//1--Objective function Name:
					myPrintStream.println("ROWS");
					
					//2--Row Definition:
					myPrintStream.println(" N OBJ");
					myPrintStream.println(" E POW");
					if (usespin){
					myPrintStream.println(" L SPR");
					}
					for (int kk=0;kk<numcuts;kk++){
						if (cuttime[kk]==tt){
						myPrintStream.println(" L CUT"+kk);
						ttcuts++;
						}
					}
					
					//3--Constraint Matrix "A":
					myPrintStream.println("COLUMNS");
					for (int ii=0; ii<numunits; ii++){
					if (usepiecewise){
					myPrintStream.println(" P"+ii+"t"+tt+" OBJ "+ubin[ii][tt]*activebeta[ii][tt]);
					}else{
					myPrintStream.println(" P"+ii+"t"+tt+" OBJ "+ubin[ii][tt]*beta[ii][tt]);
					}
					myPrintStream.println(" P"+ii+"t"+tt+" POW "+ubin[ii][tt]);
					if (usespin){
					myPrintStream.println(" P"+ii+"t"+tt+" SPR "+ubin[ii][tt]);
					}
					for (int kk=0;kk<numcuts;kk++){
						if (cuttime[kk]==tt){
						myPrintStream.println(" P"+ii+"t"+tt+" CUT"+kk+" "+ubin[ii][tt]*cutsimplexval[kk][ii]);
						}
					}
					}
					
					//4--RHS Vector:
					myPrintStream.println("RHS");
					myPrintStream.println(" B1 POW "+hourlypdem[tt]);
					if (usespin){
					float pmaxtemp=0;
					for (int ii=0;ii<numunits;ii++){
					pmaxtemp+=pmax[ii][tt]*ubin[ii][tt];
					}
					myPrintStream.println(" B1 SPR "+(pmaxtemp-hourlyspin[tt]));
					}
					for (int kk=0;kk<numcuts;kk++){
						if (cuttime[kk]==tt){
						float rhscut=0;
						for (int ii=0;ii<numunits;ii++){
							rhscut+=cutpit[kk][ii]*cutsimplexval[kk][ii];
						}
						myPrintStream.println(" B1 CUT"+kk+" "+(rhscut-cutobjval[kk]));
						}
					}
					
					//5--Bounds (includes ramp check:):
					myPrintStream.println("BOUNDS");
					for (int ii=0; ii<numunits; ii++){
					if (usepiecewise){
						myPrintStream.println(" LO LIM P"+ii+"t"+tt+" "+activepmin[ii][tt]*ubin[ii][tt]);
					}else{
						myPrintStream.println(" LO LIM P"+ii+"t"+tt+" "+pmin[ii][tt]*ubin[ii][tt]);
					}
						if(useramp && tt>0 && Unitdata.readgradup[ii]!=0 && ubin[ii][tt]==1){
						if (pmax[ii][tt]-ppri[ii][tt-1]>Unitdata.readgradup[ii]*timediv){
						myPrintStream.println(" UP LIM P"+ii+"t"+tt+" "+Unitdata.readgradup[ii]*timediv);
						}else{
						myPrintStream.println(" UP LIM P"+ii+"t"+tt+" "+pmax[ii][tt]*ubin[ii][tt]);
						}
						}else{
							if (usepiecewise){
							myPrintStream.println(" UP LIM P"+ii+"t"+tt+" "+activepmax[ii][tt]*ubin[ii][tt]);
							}else{
							myPrintStream.println(" UP LIM P"+ii+"t"+tt+" "+pmax[ii][tt]*ubin[ii][tt]);
							}
						}
					}
					
					//6--Enddata:
					myPrintStream.println("ENDATA");
					myPrintStream.close();
					
				} catch(Exception e) {
					String message = e.toString();
					System.out.println("File error: "+message);
				}
				
				//12.2--Call GLPK executable:
			    try {
			    	
			    	//--Actual GLPK call-- (windows os and unix)
			    	//Dorun(DeepEdit.PathDOS+glpkdosbat,DeepEdit.PathDOS+glpkdosbat);  //(DEEPEDIT)
			    	rr.exec(glpkdosbat);			 //(INDEPENDANT)
			    	
			    	/*--Deprecated GLPK call-- (only WindowsXP)
			    	Runtime r = Runtime.getRuntime();
			    	r.gc();
			    	Process p = r.exec(glpkdosbat);
			    	r.runFinalization();
			    	r.exec("endprimal.bat"); //Fixed!
			    	p.waitFor();
			    	
			    } catch(Exception e) {
					String message = e.toString();
					System.out.println("Error Calling GLPK: "+message);
			    }
		    
				//12.3--Reading Results from File:
			    try {
					is1 = new FileInputStream(glpkdir+"\\bin\\"+glpkpofname);
					is = new BufferedReader(new InputStreamReader(is1));
					String ln;
					feasprimal=false;
					cont=0;
					int contunits=0;
					while((ln = is.readLine()) != null) {
					    //Check for feaseble solution:
					    if (cont==4){
					    	StringTokenizer reslin = new StringTokenizer(ln);
					    	int conttok=reslin.countTokens();
					    	String res[]=new String[conttok];
					    	for (int ii=0;ii<conttok;ii++){
					    		res[ii]=reslin.nextToken();
					    	}
					    	if (res[conttok-1].equals("UNDEFINED")){
					    		feasable[tt]=false;
					    		feasprimal=false;
					    		feasesol=false;
					    		break;
					    	}else if (res[conttok-1].equals("OPTIMAL")){
					    		feasable[tt]=true;
					    		feasprimal=true;
					    	}
					    }
					    if (feasprimal){
					    //Read Primal Function Value:
					    if(cont==5){
					    	StringTokenizer reslin = new StringTokenizer(ln);
					    	String res1=reslin.nextToken();
					    	res1 = reslin.nextToken();
					    	res1 = reslin.nextToken();
					    	res1 = reslin.nextToken();
					    	primalfunction+=Float.valueOf(res1).floatValue();
					    }
					    //Read Lagrange Multipliers:
					    if (cont==10){
					    	StringTokenizer reslin = new StringTokenizer(ln);
					    	String res1="";
					    	while (reslin.hasMoreTokens()){
					    		res1 = reslin.nextToken();
					    	}
					    	lambdapri[tt]=Double.valueOf(res1).doubleValue();
					    }
					    //Read primal values:
					    if (cont>12+numbasic+ttcuts && cont<=12+numbasic+ttcuts+numunits){
					    	StringTokenizer reslin = new StringTokenizer(ln);
					    	String res1;
					    	res1 = reslin.nextToken();
					    	res1 = reslin.nextToken();
					    	res1 = reslin.nextToken();
					    	if (glpklpmethod.equals("simplex")){
					    		res1 = reslin.nextToken();
					    	}
					    	ppri[contunits][tt]=Double.valueOf(res1).doubleValue();
					    	contunits++;
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
 /*GENERAL SOLVER CALL:*/
                /**
                 * ******************************
                 */
                try {
                    if (opt == null) {
                        opt = new OptimizerCaller(optimizer, 2, numunits);
                    }

                    opt.setObjCoefficient(objcoef);
                    opt.setA(Arow, Acol, Acoef);
                    opt.setBounds(LBvector, UBvector);
                    opt.setRHS(RHS, constrtype);
                    opt.setLPMethod(glpklpmethod);
                    if (printlp) {
                        opt.setPrintLevel(OptimizerCaller.PRINTDETAIL);
                    } else {
                        opt.setPrintLevel(OptimizerCaller.NOPRINT);
                    }

                    if (opt.getSolutionStatus() == OptimizerCaller.OPTIMAL || opt.getSolutionStatus() == OptimizerCaller.FEASIBLE) {
                        feasable[tt] = true;
                        feasprimal = true;
                        primalfunction += opt.getObjValue();

                        double[] primTemp = opt.getPrimalSolution();
                        for (int ii = 0; ii < primTemp.length; ii++) {
                            ppri[ii][tt] = primTemp[ii];
                            //System.out.println("Ppri["+ii+"]"+ ppri[ii][tt]);
                        }
                        double[] dualTemp = opt.getDualSolution();
                        for (int ii = 0; ii < dualTemp.length; ii++) {
                            lambdapri[tt] = dualTemp[ii];
                        }

                    } else {
                        feasable[tt] = false;
                        feasprimal = false;
                        feasesol = false;
                    }
                    //opt.finalize();

                } catch (Exception lpE) {
                    //System.out.println("Error de glpk");
                    System.out.println("Error: " + lpE.getMessage());
                    lpE.printStackTrace();
                }

            } //------LPSOLVE OPTIMIZATION SOFTWARE------
            //else if (optimizer.equals("lpsolve") ){ //&& feasprimal){
            /*	
					
					
					
					LpSolve lp = LpSolve.makeLp(2, numunits);
					lp.setMinim();
					lp.setVerbose(LpSolve.MSG_NONE);
					//lp.setObjFn(objcoef);
					
					for (int ii=0;ii<numunits;ii++){
						
						lp.setLowbo(ii+1,LBvector[ii]);
						lp.setUpbo(ii+1,UBvector[ii]);
						
					}

					for (int ii=0; ii<Arow.length;ii++){
						lp.setMat(Arow[ii], Acol[ii]+1, Acoef[ii]);
					}
					
					lp.setRhVec(RHS);
					
					lp.setConstrType(1,LpSolve.EQ);
					lp.setConstrType(2,LpSolve.LE);
					
					lp.writeLp("uc.txt");
					int solvestat=lp.solve();
					
					//If optimal solution found by simplex:
					if(solvestat==LpSolve.OPTIMAL){
			    		feasable[tt]=true;
			    		feasprimal=true;
			    		//Read Primal Function Value:
					    primalfunction+=lp.getObjective();
					    //Read Lagrange Multipliers:
					    lambdapri[tt]=lp.getVarDualresult(1);
					    //Read primal values:
						for (int ii=0;ii<numunits;ii++){
					    	ppri[ii][tt]=lp.getVarPrimalresult(ii+1)*ubin[ii][tt];
						}
					}else{
			    		feasable[tt]=false;
			    		feasprimal=false;
			    		feasesol=false;
					}
					
					lp.deleteLp();
						
					
				}catch (LpSolveException lpE){
					System.out.println("Error: "+ lpE.getMessage());
					lpE.printStackTrace();
				}
             */ //}
            //------MINOS OPTIMIZATION SOFTWARE------
            else if (optimizer.equals("minos")) {

                //int numres=2; //Provisional!!!
                PrintStream myPrintStream;
                FileOutputStream os;
                BufferedOutputStream bos;
                FileInputStream is1;
                BufferedReader is;

                int numbasic = 2;
                runExternalFile rr = new runExternalFile();
                try {
                    os = new FileOutputStream("./economic.dat");
                    bos = new BufferedOutputStream(os, 32768);
                    myPrintStream = new PrintStream(bos, false);

                    //12.1--Optimization File Creation:
                    //12.1.1--Num of Uncertanties:
                    myPrintStream.println(numunits);
                    myPrintStream.println(" ");

                    //12.1.2--Num of Constraints:
                    int numcuttime = 0;
                    for (int kk = 0; kk < numcuts; kk++) {
                        if (cuttime[kk] == tt) {
                            numcuttime++;
                        }
                    }
                    myPrintStream.println(numbasic + numcuttime);
                    myPrintStream.println(" ");

                    //12.1.3--Linear Objective Function Coeficients:
                    for (int ii = 0; ii < numunits; ii++) {
                        myPrintStream.println(beta[ii][tt] * ubin[ii][tt]);
                        if (usepiecewise) {
                            myPrintStream.println(activebeta[ii][tt] * ubin[ii][tt]);
                        }
                    }
                    myPrintStream.println(" ");

                    //12.1.4--RHS "Right-Hand" constraint vector:
                    myPrintStream.println(hourlypdem[tt]);
                    float pmaxtemp = 0;
                    for (int ii = 0; ii < numunits; ii++) {
                        pmaxtemp += pmax[ii][tt] * ubin[ii][tt];
                    }

                    if (usespin) {
                        myPrintStream.println(pmaxtemp - hourlyspin[tt]);
                    }

                    for (int kk = 0; kk < numcuts; kk++) {
                        if (cuttime[kk] == tt) {
                            float rhscut = 0;
                            for (int ii = 0; ii < numunits; ii++) {
                                rhscut += cutpit[kk][ii] * cutsimplexval[kk][ii];
                            }
                            myPrintStream.println(rhscut - cutobjval[kk]);
                        }
                    }
                    myPrintStream.println(" ");

                    //12.1.5--Linear Constraint Matrix "A":
                    //Power Balance:
                    for (int ii = 0; ii < numunits; ii++) {
                        myPrintStream.println(ubin[ii][tt]);
                    }
                    myPrintStream.println(" ");

                    //Spinning Reserve:
                    if (usespin) {
                        for (int ii = 0; ii < numunits; ii++) {
                            myPrintStream.println(ubin[ii][tt]);
                        }
                        myPrintStream.println(" ");
                    }

                    //Benders Cuts:
                    for (int kk = 0; kk < numcuts; kk++) {
                        if (cuttime[kk] == tt) {
                            for (int ii = 0; ii < numunits; ii++) {
                                myPrintStream.println(ubin[ii][tt] * cutsimplexval[kk][ii]);
                            }
                            myPrintStream.println(" ");
                        }
                    }
                    myPrintStream.println(" ");

                    //12.1.7--Constraints type definition:
                    myPrintStream.println(1);   //OJO PROBABLE PROVISIONAL!!
                    if (usespin) {
                        myPrintStream.println(2);
                    }
                    for (int kk = 0; kk < numcuts; kk++) {
                        if (cuttime[kk] == tt) {
                            myPrintStream.println(2);
                        }
                    }
                    myPrintStream.println(" ");

                    //12.1.8--Lower Bounds:
                    for (int ii = 0; ii < numunits; ii++) {
                        if (usepiecewise) {
                            myPrintStream.println(activepmin[ii][tt] * ubin[ii][tt]);
                        } else {
                            myPrintStream.println(pmin[ii][tt] * ubin[ii][tt]);
                        }
                    }
                    myPrintStream.println(" ");

                    //12.1.9--Upper Bounds:
                    for (int ii = 0; ii < numunits; ii++) {
                        //Ramp Check:
                        if (useramp && tt > 0 && Unitdata.readgradup[ii] != 0 && ubin[ii][tt] == 1) {
                            if (pmax[ii][tt] - ppri[ii][tt - 1] > Unitdata.readgradup[ii] * timediv) {
                                myPrintStream.println(Unitdata.readgradup[ii] * timediv);
                            } else {
                                myPrintStream.println(pmax[ii][tt] * ubin[ii][tt]);
                            }
                        } else {
                            if (usepiecewise) {
                                myPrintStream.println(activepmax[ii][tt] * ubin[ii][tt]);
                            } else {
                                myPrintStream.println(pmax[ii][tt] * ubin[ii][tt]);
                            }
                        }
                    }
                    myPrintStream.println(" ");

                    //12.1.10--Optimization flag (min, max):
                    myPrintStream.println("min");
                    myPrintStream.println("UC");
                    myPrintStream.close();

                    //12.2--Hessian File Creation:
                    os = new FileOutputStream("./matw.dat");
                    bos = new BufferedOutputStream(os, 32768);
                    myPrintStream = new PrintStream(bos, false);

                    //Num of Uncertanties:
                    myPrintStream.println(numunits);
                    myPrintStream.println(" ");
                    //Hessian Matrix (null for LP)
                    for (int ii = 0; ii < numunits; ii++) {
                        for (int kk = 0; kk < numunits; kk++) {
                            if (ii == kk && usequadra) {
                                myPrintStream.println((float) 2 * Unitdata.readgamma[kk] * ubin[kk][tt]);
                            } else {
                                myPrintStream.println(0.0);
                            }
                        }
                        myPrintStream.println(" ");
                    }
                    myPrintStream.close();
                    myPrintStream.println(" ");
                } catch (Exception e) {
                    System.out.println("File error");
                }

                //12.3--Call Minos Executable:
//			    System.out.println("Call Minos");
                try {
                    if (usequadra) {
                        //Dorun(DeepEdit.PathDOS+minosquadrabat,DeepEdit.PathDOS+minosquadrabat); 	//(DEEPEDIT)
//                                    rr.exec(minosquadrabat);		 			//(INDEPENDANT)
                        runExternalFile.doRun("cmd /C " + File.separator + minosquadrabat, "cuadra > sys.log", false);
                    } else {
                        //Dorun(DeepEdit.PathDOS+minoslineabat,DeepEdit.PathDOS+minoslineabat); 	//(DEEPEDIT)
//                                    rr.exec(minoslineabat);		 			//(INDEPENDANT)
                        runExternalFile.doRun("cmd /C " + File.separator + minoslineabat, "lineal > sys.log", false);
                    }
                } catch (Exception e) {
                    String message = e.toString();
                }

                //12.4--Reading Results from File:
                try {

                    //12.4.1--Read feasability from sys.log:
                    is1 = new FileInputStream("./sys.log");
                    int kk = 0;
                    is = new BufferedReader(new InputStreamReader(is1));
                    String ln;
                    int minoserror = 999;
                    while ((ln = is.readLine()) != null) {
                        if ((kk == 7 && !usequadra) || (kk == 3 && usequadra)) {
                            StringTokenizer reslin = new StringTokenizer(ln);
                            String res1;
                            for (int ii = 0; ii < 6; ii++) {
                                res1 = reslin.nextToken();
                            }
                            res1 = reslin.nextToken();
                            minoserror = Integer.valueOf(res1).intValue();
                            break;
                        }
                        kk++;
                    }
                    if (minoserror == 1) {
                        feasprimal = false;
                        feasable[tt] = false;
                        feasesol = false;
                    } else {
                        feasprimal = true;
                        feasable[tt] = true;
                    }
                    //12.4.2--Read numerical results from soluci.dat:
                    is1 = new FileInputStream("./soluci.dat");
                    kk = 0;
                    is = new BufferedReader(new InputStreamReader(is1));
                    while ((ln = is.readLine()) != null) {
                        if (kk != 0) {
                            if (kk <= numunits) {
                                ppri[kk - 1][tt] = Double.valueOf(ln).doubleValue();
                            } else if (kk == (numunits + 1)) {
                                lambdapri[tt] = Double.valueOf(ln).doubleValue();
                            }
                        } else {
                            primalfunction += Float.valueOf(ln).floatValue();
                        }
                        kk++;
                    }
                    is.close();
                    is = null;
                } catch (Exception e) {
                    String message = e.toString();
                }
            }

        }//tt

        //Save results if feasible:
        if (feasesol) {
            onefeasible = true;
        }

        lastfeasible = feasesol;
        return feasesol;
    }

    public void dualupdate() {

        //13--Lagrange Multipliers Update: Subgradient Method
        double deltadem[] = new double[numperiods];
        double deltaspin[] = new double[numperiods];
        double normpdem = 0;
        double normspin = 0;
        float sref = 100;								//(INDEPENDIENTE)

        for (int tt = 0; tt < numperiods; tt++) {
            deltadem[tt] = 0;
            deltaspin[tt] = 0;

            for (int ii = 0; ii < numunits; ii++) {
                deltadem[tt] += ubin[ii][tt] * pact[ii][tt];
                deltaspin[tt] += ubin[ii][tt] * (pmax[ii][tt] - pact[ii][tt]);
            }

            deltadem[tt] = hourlypdem[tt] - deltadem[tt];
            deltaspin[tt] = hourlyspin[tt] - deltaspin[tt];
            normpdem += (deltadem[tt] * deltadem[tt]);
            normspin += (deltaspin[tt] * deltaspin[tt]);
        }
        if (normpdem == 0) {
            normpdem = tol_b;
        }
        if (normspin == 0) {
            normspin = tol_b;
        }

        for (int tt = 0; tt < numperiods; tt++) {

            //13.1--CASE1: Prior solution search set to OPTIMAL -> Diminishing step rule
            if (priorsearch.equals("optimal")) {

                if (deltadem[tt] >= 0) {
                    multdem[tt] = multdem[tt] + (float) (deltadem[tt] / ((subalfa + subbeta * ucitercont) * Math.sqrt(normpdem)));
                    multres[tt] = Math.max(multres[tt] + (float) (deltaspin[tt] / ((subalfa + subbeta * ucitercont) * Math.sqrt(normspin))), 0);
                } else {
                    if (deltaspin[tt] > 0) {
                        multres[tt] = Math.max(multres[tt] + (float) (deltaspin[tt] * (0.1f) / ((subalfa + subbeta * ucitercont) * Math.sqrt(normspin))), 0);
                    } else {
                        multdem[tt] = multdem[tt] + (float) (deltadem[tt] * (0.1f) / ((subalfa + subbeta * ucitercont) * Math.sqrt(normpdem)));
                        multres[tt] = Math.max(multres[tt] + (float) (deltaspin[tt] * (0.1f) / ((subalfa + subbeta * ucitercont) * Math.sqrt(normspin))), 0);
                    }
                }

                //13.2--CASE2: Prior solution search is FEASIBLE -> Constant Step length approach
            } else if (priorsearch.equals("feasible")) {

                if (!feasable[tt]) {
                    if (deltadem[tt] >= 0) {
                        multdem[tt] = multdem[tt] + (float) (deltadem[tt] * subalfa / Math.sqrt(normpdem));
                        multres[tt] = Math.max(multres[tt] + (float) (deltaspin[tt] * subalfa / Math.sqrt(normspin)), 0);
                    } else {
                        if (deltaspin[tt] > tol_b) {
                            multres[tt] = Math.max(multres[tt] + (float) (deltaspin[tt] * (0.2f) * subalfa / Math.sqrt(normspin)), 0);
                        } else {
                            multdem[tt] = multdem[tt] + (float) (deltadem[tt] * (0.2f) * subalfa / Math.sqrt(normpdem));
                            multres[tt] = Math.max(multres[tt] + (float) (deltaspin[tt] * (0.2f) * subalfa / Math.sqrt(normspin)), 0);
                        }
                    }
                } else {
                    if (deltadem[tt] >= 0) {
                        multdem[tt] = multdem[tt] + (float) (deltadem[tt] * (0.1f) * subalfa / Math.sqrt(normpdem));
                        multres[tt] = Math.max(multres[tt] + (float) (deltaspin[tt] * (0.1f) * subalfa / Math.sqrt(normspin)), 0);
                    } else {
                        if (deltaspin[tt] > tol_b) {
                            multres[tt] = Math.max(multres[tt] + (float) (deltaspin[tt] * (0.2f) * subalfa / Math.sqrt(normspin)), 0);
                        } else {
                            multdem[tt] = multdem[tt] + (float) (deltadem[tt] * (0.05f) * subalfa / Math.sqrt(normpdem));
                            multres[tt] = Math.max(multres[tt] + (float) (deltaspin[tt] * (0.05f) * subalfa / Math.sqrt(normspin)), 0);
                        }
                    }
                }

            }

            /*
			if (!feasable[tt]){
				if (deltadem[tt]>=0){
				//multdem[tt]=multdem[tt]+(float)(deltadem[tt]*0.02);
				multdem[tt]=multdem[tt]+(float)(deltadem[tt]/((subalfa+subbeta*ucitercont)*Math.sqrt(normpdem)));
				}else{
				//multdem[tt]=multdem[tt]+(float)(deltadem[tt]*0.005);
				multdem[tt]=multdem[tt]+(float)(deltadem[tt]*(0.1f)/((subalfa+subbeta*ucitercont)*Math.sqrt(normpdem)));
				//multpres[tt]=multres[tt]+(float)(deltares[tt]/((subalfa+subbet*ucitercont)*Math.sqrt(normpres)));
				}
			}else{
				if (deltadem[tt]>=0){
				//multdem[tt]=multdem[tt]+(float)(deltadem[tt]*0.02);
				multdem[tt]=multdem[tt]+(float)(deltadem[tt]*(0.1f)/((subalfa+subbeta*ucitercont)*Math.sqrt(normpdem)));
				//multpres[tt]=multres[tt]+(float)(deltares[tt]/((subalfa+subbet*ucitercont)*Math.sqrt(normpres)));
				}else{
				//multdem[tt]=multdem[tt]+(float)(deltadem[tt]*0.005);
				multdem[tt]=multdem[tt]+(float)(deltadem[tt]*(0.05f)/((subalfa+subbeta*ucitercont)*Math.sqrt(normpdem)));
				//multpres[tt]=multres[tt]+(float)(deltares[tt]/((subalfa+subbet*ucitercont)*Math.sqrt(normpres)));
				}
			}
			
             */
        }

        //13.3-- Benders Cuts multipliers update: -> Constant Step length Rule
        if (txconstraint) {
            float normdiffcutmult = 0;
            float[] deltappound = new float[numcuts];

            //13.3.1--Calculate Norm:
            for (int kk = 0; kk < numcuts; kk++) {
                deltappound[kk] = 0;
                for (int ii = 0; ii < numunits; ii++) {
                    deltappound[kk] += cutsimplexval[kk][ii] * (pact[ii][cuttime[kk]] * ubin[ii][cuttime[kk]] - cutpit[kk][ii]);
                }
                if (cutobjval[kk] + deltappound[kk] < 0) {
                    normdiffcutmult += (cutobjval[kk] + deltappound[kk]) * (cutobjval[kk] + deltappound[kk]);
                }
            }
            if (normdiffcutmult == 0) {
                normdiffcutmult = tol_b;
            }

            //13.3.2--Update Multipliers:
            for (int kk = 0; kk < numcuts; kk++) {
                if (cutobjval[kk] + deltappound[kk] < 0) {
                    cutmultiplier[kk] = cutmultiplier[kk] + (float) ((cutobjval[kk] + deltappound[kk]) * (subalfa) / (Math.sqrt(normdiffcutmult)));
                } else {
                    cutmultiplier[kk] = cutmultiplier[kk] + (float) ((cutobjval[kk] + deltappound[kk]) * (subalfa * 0.2) / (Math.sqrt(normdiffcutmult)));
                }
            }

        }

    }

    public boolean convergencecheck(boolean feasibleprimal) {

        //14--Convergence Analisis Toolbox:
        //14.1--Total Dual Cost:
        dualfunction += startcost;

        //14.2--Primal Function Startup Cost Addition:
        primalfunction += startcost;

        //14.3--Duality Gap Estimation:
        dualgap = (float) ((primalfunction - dualfunction) / dualfunction);

        //14.4--Best Dual Function:
        if (dualfunction > dualbest) {
            dualbest = dualfunction;
        }

        //14.5--Best Primal Function:
        if (feasibleprimal) {
            if (primalbest == 0) {
                primalbest = primalfunction;
            } else {
                if (primalfunction < primalbest) {
                    primalbest = primalfunction;
                }
            }
        }

        //14.6--Current Best Dual Gap:
        if (dualbest != 0) {
            dgbest = (float) ((primalbest - dualbest) / dualbest);
        }

        //14.4--Register Solution: (Revised 03-06-08)
        if (feasibleprimal) {

            for (int tt = 0; tt < numperiods; tt++) {
                for (int ii = 0; ii < numunits; ii++) {
                    ubest[ii][tt] = ubin[ii][tt];
                    pbest[ii][tt] = ppri[ii][tt];
                }
            }

            //if (dualgap<dgbest && dualgap>=0){
            //	primalbest=primalfunction;
            //	dgbest=dualgap;
            //	for (int tt=0; tt<numperiods; tt++){
            //		for (int ii=0; ii<numunits; ii++){
            //			ubest[ii][tt]=ubin[ii][tt];
            //			pbest[ii][tt]=ppri[ii][tt];
            //	}}
            //}
        }

        //14.5--Special printing:
        int itertemp = (int) (ucitercont) / (cont100iter * 50);

        if (itertemp == 1) {
            cont100iter++;
            System.out.println(" ");
            System.out.println("--Iteration " + ucitercont + "--");
            System.out.println("Best DG = " + dgbest);
        }

        //14.6--Tolerance Comparison:
        if (Math.abs(dgbest) <= tolset && dualgap >= 0 && feasibleprimal) {
            ucconvergence = true;

            //14.6.1.--Solution quality veredict:
            if (tolset >= tol_b) {
                solquality = "GOOD";
                if (dualgap < (tolset / 100) || dualgap <= 0.0001) {
                    solquality = "EXCELENT";
                }
            } else {
                solquality = "POOR";
            }
        } else {
            ucconvergence = false;
        }

        return ucconvergence;

    }

    /*Impresiones de Prueba
		for (int tt=0; tt<numperiods; tt++){
			for (int ii=0; ii<numunits; ii++){
				System.out.println ("beta["+ii+"]["+tt+"]="+beta[ii][tt]);
		}}
		for (int tt=0; tt<numperiods; tt++){
			for (int ii=0; ii<numunits; ii++){
				System.out.println ("priorlist["+ii+"]["+tt+"]="+priorlist[ii][tt]);
		}}
     */
 /*-------Impresiones de Prueba DATOS DE ENTRADA---------
		for (int tt=0; tt<numperiods; tt++){
			for (int ii=0; ii<numunits; ii++){
				System.out.println ("ubin["+ii+"]["+tt+"]="+ubin[ii][tt]);
		}}
		for (int tt=0; tt<numperiods; tt++){
			for (int ii=0; ii<numunits; ii++){
				System.out.println ("pact["+ii+"]["+tt+"]="+pact[ii][tt]);
		}}
		for (int tt=0; tt<numperiods; tt++){
			for (int ii=0; ii<numunits; ii++){
				System.out.println ("ubin["+ii+"]["+tt+"]="+ubin[ii][tt]);
		}}
		System.out.println ("pmax[3]= "+Unitdata.readpmax[3]);
		System.out.println ("beta[2]= "+Unitdata.readbeta[2]);
		System.out.println ("inservice[0]= "+Unitdata.readinservice[0]);
		System.out.println ("pmax[0]= "+Maintdata.readpmax[0]);
		System.out.println ("type[0]= "+Maintdata.readtype[0]);
		System.out.println ("inservice[0]= "+Maintdata.readinservice[0]);
		System.out.println ("From[0]= "+Userdata.readfromout[0]);
		System.out.println ("TO[0]= "+Userdata.readtoout[0]);
		System.out.println ("useroutid[0]= "+Userdata.readuseroutid[0]);
		for (int kk=0; kk<numperiods; kk++){
			System.out.println ("pdem[0]["+kk+"]="+Loaddata.readpdem[0][kk]);
		}
		System.out.println ("Initial Lagrange Multipliers Set");
		for (int tt=0; tt<numperiods; tt++){
			System.out.println ("lambda["+tt+"]="+multdem[tt]);
		}
		for (int tt=0; tt<numperiods; tt++){
			System.out.println ("mucero["+tt+"]="+multres[tt]);
		}
		for (int tt=0; tt<numperiods; tt++){
			for (int ii=0; ii<numunits; ii++){
				System.out.println ("priorlist["+ii+"]["+tt+"]="+priorlist[ii][tt]);
			}
		}
     */
 /*Impresiones de Prueba PROGRAMACION DINAMICA:
		System.out.println ("estado["+0+"]["+tt+"]="+estado[0][tt]);
		System.out.println ("estado["+1+"]["+tt+"]="+estado[1][tt]);
		
		System.out.println ("camino[0][0]="+camino[0][0]);
		System.out.println ("camino[0][1]="+camino[0][1]);
		System.out.println ("camino[1][0]="+camino[1][0]);
		System.out.println ("camino[1][1]="+camino[1][1]);
		
		System.out.println ("operacost="+operacost);
		
		System.out.println ("tray["+0+"]["+tt+"]="+tray[0][tt]);
		System.out.println ("tray["+1+"]["+tt+"]="+tray[1][tt]);
		
		System.out.println ("cumulcost["+0+"]["+tt+"]="+cumulcost[0][tt]);
		System.out.println ("cumulcost["+1+"]["+tt+"]="+cumulcost[1][tt]);
		
		System.out.println ("cumultime["+0+"]["+tt+"]="+cumultime[0][tt]);
		System.out.println ("cumultime["+1+"]["+tt+"]="+cumultime[1][tt]);
     */
//------------------------------------------------------------------------------
//Xtra Methods:
//------------------------------------------------------------------------------
    //--XMethod1> OPEN DB Connection: Mysql, ODBC allowed.
    public void initconnection(String typedb, String location, String namedb, String userdb, String passdb) {

        String completenamedb = null;

        //Open MYSQL DB:
        if (typedb.equals("mysql")) {
            completenamedb = "jdbc:mysql://" + location + "/" + namedb;
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }

            //Open MSACCESS DB:
        } else if (typedb.equals("odbc")) {
            //namedb="C://Users//Frank Leanez//Documents//TESIS//MIPvsLR//Simulaciones//SING//caso27032008//"+namedb+".mdb";
            completenamedb = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + namedb + ";";
            //"jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=myDB.mdb;";
            try {
                Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            } catch (java.lang.ClassNotFoundException e) {
                System.err.print("ClassNotFoundException: ");
                System.err.println(e.getMessage());
            }
        }
        try {
            mylink = DriverManager.getConnection(completenamedb, userdb, passdb);
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }

    }

    //--XMethod2> Close DB Connection "mylink"
    public boolean closedbconnection() {
        boolean closeok = false;
        try {
            mylink.close();
            if (mylink.isClosed()) {
                closeok = true;
            }
        } catch (SQLException ex) {
            System.err.println("SQLException: " + ex.getMessage());
        }
        return closeok;
    }

    //--XMethod3> Loading general Tab data from Frame:(Can override defaults previously readed)
    public void setgeneral(short nummaxunitstemp, short numperiodstemp, long itermaxtemp,
            String opttemp, String tol, boolean useaugmentemp, boolean useheuristictemp,
            boolean usepiecewisetemp, boolean usequadratemp,
            boolean useramptemp, boolean txconstrainttemp, short typen1temp,
            short itermaxnettemp, boolean useschtemp, String priorsearchtemp,
            boolean usespintemp, boolean useprestemp, boolean useplimittemp,
            String txtfiledirtemp, boolean usedbtemp, String cdbvendortemp, String tdbubicatemp,
            String tdbnametemp, String tdblogintemp, String tdbpasstemp) {

        numunits = nummaxunitstemp;
        numperiods = numperiodstemp;
        itermax = itermaxtemp;
        optimizer = opttemp;
        tolset = Float.valueOf(tol).floatValue();

        useaugmen = useaugmentemp;
        useheuristic = useheuristictemp;
        usepiecewise = usepiecewisetemp;
        usequadra = usequadratemp;

        useramp = useramptemp;
        txconstraint = txconstrainttemp;
        typen1 = typen1temp;
        itermaxnet = itermaxnettemp;
        usesch = useschtemp;
        priorsearch = priorsearchtemp;
        usespin = usespintemp;
        usepres = useprestemp;
        useplimit = useplimittemp;
        usedb = usedbtemp;
        txtfiledir = txtfiledirtemp;
        dbtype = cdbvendortemp;
        dbloc = tdbubicatemp;
        dbname = tdbnametemp;
        dblog = tdblogintemp;
        dbpass = tdbpasstemp;

    }

    //--XMethod4> Calculate startup cost of unit "ii" at stage "tt":
    public double getcst(int ii, int tt) {
        double cst;
        //Si hablamos de una unidad disponible:
        if (able[ii][tt]) {
            short tdown = gettdown(ii, tt);
            //Cold StartUp:
            if (tdown >= Unitdata.readtstartcold[ii]) {
                cst = Unitdata.readcstcold[ii];
                //Warm StartUp:
            } else if ((tdown < Unitdata.readtstartcold[ii])
                    && (tdown > Unitdata.readtstarthot[ii])) {
                cst = Unitdata.readcstwarm[ii];
                //Hot StartUp:
            } else if ((tdown <= Unitdata.readtstarthot[ii]) && tdown > 0) {
                cst = Unitdata.readcsthot[ii];
            } else {
                cst = 0.0;
            }
        } else {
            cst = 0.0;
        }
        return cst;
    }

    //--XMethod5> Calculte tdown of unit "ii" at stage "tt":
    public short gettdown(int ii, int tt) {
        short tdown = 0;
        int trun = Unitdata.readtrunini[ii];
        //Si hablamos de una unidad despachada en ese tiempo (tt):
        if (ubin[ii][tt] == 1) {
            int cont = 1;
            //Mientras no estemos en un tiempo negativo:
            boolean ttpos = true;
            if (tt == 0) {
                cont = 0;
                if (trun < 0) {
                    tdown = (short) Math.abs(trun);
                }
                ttpos = false;
            }
            //Mientras sigamos encontrando q la unidad estaba apagada tiempo atras:
            while ((ubin[ii][tt - cont] == 0) && ttpos) {
                tdown += 1;
                if (tt - (cont + 1) < 0) {
                    if (trun < 0) {
                        tdown += (short) Math.abs(trun);
                    }
                    ttpos = false;
                } else {
                    cont++;
                }
            }
        }
        return tdown;
    }

    //--XMethod6.1> Calcute Operating Costs (Static costs in single unit dp stage)
    public double getoperacost(int ii, int tt, double popt) {
        int kk = 0;
        float multval = 0;
        double multaug = 0;
        double opc = 0;
        double multspin = 0;

        //1--Transmission considerations:
        if (txconstraint) {
            for (kk = 0; kk < numcuts; kk++) {
                if (cuttime[kk] == tt) {
                    multval += cutmultiplier[kk] * cutsimplexval[kk][ii];
                }
            }
        }

        //2--Include Augmented Lagrangian Term:
        if (useaugmen) {
            double sumplast = 0;
            for (kk = 0; kk < numunits; kk++) {
                sumplast += plast[kk][tt] * ulast[kk][tt];
            }
            multaug = (double) (1 / (2 * epsaug)) * popt * popt - (1 / epsaug) * plast[ii][tt] * ulast[ii][tt] * popt
                    - c_coef * (sumplast) * popt + c_coef * hourlypdem[tt] * popt;
        }

        //3--Include Spinning Reserv Component:
        if (usespin) {
            multspin = multres[tt] * (pmax[ii][tt] - popt);
        }

        //4--Total Cost Evaluation:
        //4.1--Quadratic costs (constant term leaved)
        if (usequadra) {
            opc = beta[ii][tt] * popt + (Unitdata.readgamma[ii] * popt * popt)
                    - multdem[tt] * popt + multval * popt + multaug - multspin;

            //4.2--Linear costs
        } else {

            //4.2.1--Piece-wise Linear:
            if (usepiecewise) {
                opc = activebeta[ii][tt] * popt - multdem[tt] * popt + multval * popt + multaug - multspin;

                //4.2.2--Linearized Costs:
            } else {
                opc = beta[ii][tt] * popt - multdem[tt] * popt + multval * popt + multaug - multspin;
            }

        }

        //opc=beta[ii][tt]*popt-multdem[tt]*popt-multpres[tt]*(pmax[tt]-popt)+multval;
        return opc;
    }

    //--XMethod6.2> Calcute optimal power output(in single unit dp stage)
    public double getpopt(int ii, int tt) {
        double popt = 0.0;
        int kk = 0;
        float multval = 0;
        double sumplast = 0;

        //1--Transmission considerations:
        if (txconstraint) {
            for (kk = 0; kk < numcuts; kk++) {
                if (cuttime[kk] == tt) {
                    multval += cutmultiplier[kk] * cutsimplexval[kk][ii];
                }
            }
        }

        //2--Cuadratic Costs:
        if (usequadra && Unitdata.readgamma[ii] != 0) {
            //2.1--Augmented Lagrangian Optimal Power:
            if (useaugmen) {
                for (kk = 0; kk < numunits; kk++) {
                    sumplast += plast[kk][tt] * ulast[kk][tt];
                }
                popt = (double) (multdem[tt] - multres[tt] - multval - beta[ii][tt] + (1 / epsaug) * plast[ii][tt] * ulast[ii][tt]
                        + c_coef * (sumplast - hourlypdem[tt])) / (2 * Unitdata.readgamma[ii] + (1 / epsaug));
                //2.2--Convencional quadratic costs Optimal Power:
            } else {
                popt = (multdem[tt] - multres[tt] - multval - beta[ii][tt]) / (2 * Unitdata.readgamma[ii]);
            }
            //2.3--Output Limits consideration:
            if (popt > pmax[ii][tt]) {
                popt = pmax[ii][tt];
            } else if (popt < pmin[ii][tt]) {
                popt = pmin[ii][tt];
            }

            //3--Linear Costs:
        } else {

            //3.1--Piece-wise linear Costs:
            if (usepiecewise) {

                boolean islow = false;
                int mup = 0, mdown = 0;
                float pmaxdown = 0, pminup = Float.MAX_VALUE;
                float pmindown = 0, pmaxup = 0;
                float blockpmin;
                float blockpmax;
                float blockbeta;
                float[] mcost = new float[(int) Unitdata.getnumblocks(ii)];
                //float mbestd=Float.MAX_VALUE, mbestu=Float.MAX_VALUE;

                for (kk = 0; kk < Unitdata.getnumblocks(ii); kk++) {

                    blockpmin = Unitdata.getblockpmin(ii, kk);
                    blockpmax = Unitdata.getblockpmax(ii, kk);
                    blockbeta = Unitdata.getblockheat(ii, kk) * fuelprice[ii][tt];

                    //3.1.1--Descarta bloque fuera de rangos:
                    if (blockpmax < pmin[ii][tt] || blockpmin > pmax[ii][tt]) {
                        continue;
                    }

                    //3.1.2--Ajustar potencia MAXIMA (tipico establecida por mantenimiento o especial)
                    if (blockpmax > pmax[ii][tt]) {
                        blockpmax = pmax[ii][tt];
                    }

                    //3.1.3--Ajustar potencia MINIMA (tipico establecida por mantenimiento o especial)
                    if (blockpmin < pmin[ii][tt]) {
                        blockpmin = pmin[ii][tt];
                    }

                    mcost[kk] = (Unitdata.getblockheat(ii, kk) * fuelprice[ii][tt]) - (multdem[tt] - multres[tt]);

                    /*--CONVEX CASE-- piece-wise linear optimal power output:
					if (mcost[kk]>0){
						if (Math.abs(mcost[kk])<mbestu){
							mbestu=Math.abs(mcost[kk]);
							mup=kk;
						}
					}else{
						islow=true;
						if (Math.abs(mcost[kk])<mbestd){
							mbestd=Math.abs(mcost[kk]);
							mdown=kk;
						}
					}
                     */
                    //--NON-CONVEX CASE-- piece-wise linear optimal power output:
                    if (mcost[kk] > 0) {
                        if (blockpmin < pminup) {
                            pminup = blockpmin;
                            pmaxup = blockpmax;
                            mup = kk;
                        }
                    } else {
                        islow = true;
                        if (blockpmax > pmaxdown) {
                            pmaxdown = blockpmax;
                            pmindown = blockpmin;
                            mdown = kk;
                        }
                    }
                }

                //3.1.4--Set active linear block:
                if (islow) {
                    activebloc[ii][tt] = (short) mdown;
                    activepmax[ii][tt] = pmaxdown;
                    activepmin[ii][tt] = pmindown;
                    activebeta[ii][tt] = Unitdata.getblockheat(ii, mdown) * fuelprice[ii][tt];
                    //popt=(double)pmaxdown;

                    //beta[ii][tt]=Unitdata.getblockheat(ii,mdown)*fuelprice[ii][tt];
                } else {
                    activebloc[ii][tt] = (short) mup;
                    activepmax[ii][tt] = pmaxup;
                    activepmin[ii][tt] = pminup;
                    activebeta[ii][tt] = Unitdata.getblockheat(ii, mup) * fuelprice[ii][tt];
                    //popt=(double)pminup;
                    //beta[ii][tt]=Unitdata.getblockheat(ii,mup)*fuelprice[ii][tt];
                }

                //3.1.5--Determine Optimal Power using augmented lagrangian:
                if (useaugmen) {
                    for (kk = 0; kk < numunits; kk++) {
                        sumplast += plast[kk][tt] * ulast[kk][tt];
                    }
                    popt = (double) (multdem[tt] - multres[tt] - multval - activebeta[ii][tt] + (1 / epsaug) * plast[ii][tt] * ulast[ii][tt]
                            + c_coef * (sumplast - hourlypdem[tt])) / (1 / epsaug);
                }

                //3.1.6--Check Limits:
                if (popt > activepmax[ii][tt]) {
                    popt = activepmax[ii][tt];
                }
                if (popt < activepmin[ii][tt]) {
                    popt = activepmin[ii][tt];
                }

                //3.2--linearized Costs (one-piece cost)
            } else {

                //3.2.1--Augmented Lagrangian Optimal Power:
                if (useaugmen) {
                    for (kk = 0; kk < numunits; kk++) {
                        sumplast += plast[kk][tt] * ulast[kk][tt];
                    }
                    popt = (double) (multdem[tt] - multres[tt] - multval - beta[ii][tt] + (1 / epsaug) * plast[ii][tt] * ulast[ii][tt]
                            + c_coef * (sumplast - hourlypdem[tt])) / (1 / epsaug);
                    //3.2.1.1--Adjust Limits
                    if (popt > pmax[ii][tt]) {
                        popt = pmax[ii][tt];
                    }
                    if (popt < pmin[ii][tt]) {
                        popt = pmin[ii][tt];
                    }

                    //3.2.2--Clasical Lagrangian Optimal Power:
                } else {
                    if (beta[ii][tt] <= multdem[tt] - multres[tt] - multval) {
                        popt = (double) pmax[ii][tt];
                    } else {
                        popt = (double) pmin[ii][tt];
                    }
                }

            }

        }

        //4--Ramp rate check: (OJO!! Falta que salte de bloque)
        if (useramp && tt != 0 && Unitdata.readgradup[ii] != 0) {
            if ((popt - pact[ii][tt - 1]) > (Unitdata.readgradup[ii] * timediv)) {
                popt = pact[ii][tt - 1] + Unitdata.readgradup[ii] * timediv;
            }
        }

        return popt;
    }

    //--XMethod7> Calculate Transition Costs:
    public double transcost(int ii, int tdown) {
        double cst = 0;
        //Cold StartUp:
        if (tdown >= Unitdata.readtstartcold[ii]) {
            cst = Unitdata.readcstcold[ii];
            //Warm StartUp:
        } else if ((tdown < Unitdata.readtstartcold[ii])
                && (tdown > Unitdata.readtstarthot[ii])) {
            cst = Unitdata.readcstwarm[ii];
            //Hot StartUp:
        } else if ((tdown <= Unitdata.readtstarthot[ii]) && tdown > 0) {
            cst = Unitdata.readcsthot[ii];
        } else {
            cst = 0.0;
        }
        return cst;
    }

    //--XMethod8> Destroy Unnecessary Data Objects: (Deprecated)
    public void finalizedata() {
        Maintdata = null;
        Userdata = null;
        Loaddata = null;
    }

    //--XMethod9.0> Read Option XML file "UCOption.xml"	
    public void optionfile() {

        if (DataoptionUC == null) {
            DataoptionUC = new XmlTreeMaker("UCOptions.xml");
        }

        //x9.1--General Options:
        DataOptionUC opcion = new DataOptionUC();
        opcion.setoption("installdir", (Object) DataoptionUC.returnchardata("direct"));

        installdir = DataoptionUC.returnchardata("direct");
        numunits = Short.valueOf(DataoptionUC.returnchardata("numunits")).shortValue();
        //numunits=numaxunits;
        numperiods = Short.valueOf(DataoptionUC.returnchardata("numperiods")).shortValue();
        timediv = Short.valueOf(DataoptionUC.returnchardata("timediv")).shortValue();
        itermax = Long.valueOf(DataoptionUC.returnchardata("itermax")).shortValue();
        itermaxnet = Long.valueOf(DataoptionUC.returnchardata("itermaxnet")).shortValue();
        itertime = Long.valueOf(DataoptionUC.returnchardata("itertime")).shortValue();
        tol_obj = Float.valueOf(DataoptionUC.returnchardata("tol-obj")).floatValue();
        tol_dg = Float.valueOf(DataoptionUC.returnchardata("tol-dg")).floatValue();
        tolset = tol_dg;
        tol_b = Float.valueOf(DataoptionUC.returnchardata("tol-b")).floatValue();
        priorsearch = DataoptionUC.returnchardata("priorsearch");

        //x9.2--Database Options:
        usedb = getstatusonoff(DataoptionUC.returnchardata("usedb"));
        dbtype = DataoptionUC.returnchardata("db-type");
        dbname = DataoptionUC.returnchardata("db-name");
        dbloc = DataoptionUC.returnchardata("db-loc");
        dblog = DataoptionUC.returnchardata("db-log");
        dbpass = DataoptionUC.returnchardata("db-pass");
        txtfiledir = DataoptionUC.returnchardata("db-file");

        //x9.3--Optimizer Options:
        optimizer = DataoptionUC.returnchardata("opt-def");
        if (optimizer.equals("minos")) {
            usequadra = true;
        } else {
            usequadra = false;
        }
        printlp = getstatusonoff(DataoptionUC.returnchardata("printlp"));
        useramp = getstatusonoff(DataoptionUC.returnchardata("ramp"));
        usespin = getstatusonoff(DataoptionUC.returnchardata("spin"));
        useheuristic = getstatusonoff(DataoptionUC.returnchardata("heuristic"));
        useaugmen = getstatusonoff(DataoptionUC.returnchardata("augmented"));
        usepiecewise = getstatusonoff(DataoptionUC.returnchardata("piece-wise"));
        usepres = getstatusonoff(DataoptionUC.returnchardata("primary-res"));
        useplimit = getstatusonoff(DataoptionUC.returnchardata("powerlimit"));

        glpkdir = DataoptionUC.returnchardata("glpk-direct");
        glpklpmethod = DataoptionUC.returnchardata("glpk-popt");
        glpkpiftype = DataoptionUC.returnchardata("glpk-popt");
        glpkpifname = DataoptionUC.returnchardata("glpk-pifname");
        glpkdosbat = DataoptionUC.returnchardata("glpk-dosbat");
        glpkpofname = DataoptionUC.returnchardata("glpk-pofname");
        minosdir = DataoptionUC.returnchardata("minos-direct");
        minoslineabat = DataoptionUC.returnchardata("minos-lineabat");
        minosquadrabat = DataoptionUC.returnchardata("minos-quadra");

        //x9.4--Display Options:
        dispwidth = Integer.valueOf(DataoptionUC.returnchardata("width")).intValue();
        dispheight = Integer.valueOf(DataoptionUC.returnchardata("height")).intValue();
        dispsquare = Integer.valueOf(DataoptionUC.returnchardata("square")).intValue();
        String fittemp = DataoptionUC.returnchardata("fitto");
        usefitto = getstatusonoff(fittemp);
        String repdetailtemp = DataoptionUC.returnchardata("repdetail");
        userepdetail = getstatusonoff(repdetailtemp);

        //x9.5--Lagrange Relaxation Tunning Parameters:
        subalfa = Float.valueOf(DataoptionUC.returnchardata("sub-alfa")).floatValue();
        subbeta = Float.valueOf(DataoptionUC.returnchardata("sub-beta")).floatValue();
        c_coef = Float.valueOf(DataoptionUC.returnchardata("c-coef")).floatValue();
        epsaug = Float.valueOf(DataoptionUC.returnchardata("eps-aug")).floatValue();

        //X9.6--Create batch files:
        try {
            FileOutputStream os = new FileOutputStream(glpkdosbat);
            BufferedOutputStream bos = new BufferedOutputStream(os, 32768);
            PrintStream myPrintStream = new PrintStream(bos, false);
            myPrintStream.println("cd\\");
            myPrintStream.println("cd " + glpkdir + "\\bin");
            String lpstr;
            if (glpklpmethod.equals("simplex")) {
                lpstr = "--simplex";
            } else {
                lpstr = "--interior";
            }
            String lpfor;
            if (glpkpiftype.equals("freemps")) {
                lpfor = "--freemps";
            } else {
                lpfor = "--freemps";  //FUTURO DESARROLLO (AUNQUE IMPROBABLE)
            }
            String commandglpk = "glpsol.exe " + lpfor + " " + glpkpifname + " " + lpstr
                    + " -o " + glpkpofname;
            myPrintStream.println(commandglpk);
            myPrintStream.close();
        } catch (Exception e) {
            String message = e.toString();
            System.out.println("File error: " + message);
        }

    }

    //--XMethod10> Read Option XML file "UCOption.xml"
    /*
	public void refreshucframe(){
		Point p = MyJavaUCFrame.getLocationOnScreen();
		MyJavaUCFrame.setVisible(false);
		MyJavaUCFrame=null;
    	if (MyJavaUCFrame == null) {
			MyJavaUCFrame = new JavaUCFrame(this);
			MyJavaUCFrame.setLocation(p);
		} else {
			MyJavaUCFrame.setVisible(true);
			MyJavaUCFrame.setLocation(p);
		}
	}
     */
    public String formatreport(double reportfloat, int numinteger) {
        Double gap = new Double(reportfloat);
        String amountOut;
        NumberFormat numberFormatter = NumberFormat.getInstance(Locale.ENGLISH);
        numberFormatter.setMaximumFractionDigits(2);
        numberFormatter.setMaximumIntegerDigits(numinteger);
        amountOut = numberFormatter.format(gap);
        return amountOut;
    }

    public String printdivline(int numcharacters) {

        char[] auxchar2 = new char[numcharacters];
        for (int tt = 0; tt < numcharacters; tt++) {
            auxchar2[tt] = '-';
        }
        String resline = new String(auxchar2);
        return resline;
    }

    public String formatlabel(String originallabel, int maxdig) {
        int dig = originallabel.length();
        String digstring = originallabel;
        if (dig > maxdig) {
            digstring = originallabel.substring(dig - maxdig, dig);
        }
        return digstring;
    }

    //--XMethod11> Make Report File: (revived!!)
    public void makereportfile() {

        //Official Report Creation:
        int ii, tt, cont = 0;
        //FileOutputStream os;
        //BufferedOutputStream bos;
        //PrintStream reportline;

        try {
            os = new FileOutputStream(LogFilePath(), true);
            reportline = new PrintStream(os);

            //1--Report Simulations Results:
            reportline.println("=======UNIT COMMITMENT REPORT FILE========");
            reportline.println(" ");
            if (usequadra && useaugmen) {
                reportline.println("  Non-Linear Augmented Lagrange Relaxation Methodology");
            } else {
                reportline.println("  Linear Lagrange Relaxation Methodology");
            }
            reportline.println(" ");
            reportline.println("  Total Cost              : " + formatreport(primalbest, 10));
            //reportline.println("  Final Solution          : "+sstatus[1]); //This is a crappy measure anyways
            reportline.println("  Solution Quality        : " + solquality);
            reportline.println("  External Optimizer      : " + optimizer);
            if (usequadra) {
                reportline.println("  Quadratic Convex Optimization");
            } else {
                if (optimizer.equals("glpk")) {
                    reportline.println("  LP External Method      : " + glpklpmethod);
                } else {
                    reportline.println("  LP External Method      : simplex (minos 5.0)");
                }
            }
            reportline.println("  Iterations              : " + totaliteruc);
            reportline.println("  Execution Time (mseg)   : " + timetotal);
            reportline.println("  Dual Solving Time (mseg): " + timedual);
            reportline.println("  Cuts Created            : " + numcuts);
            reportline.println(" ");
            reportline.println(" ");

            reportline.println(printdivline(numperiods * 2 + 35));

            //2--Determine characters of the largest unit name:
            int longestname = 0;
            for (ii = 0; ii < numunits; ii++) {
                int longtemp = Unitdata.readname[ii].length();
                if (longtemp > longestname) {
                    longestname = longtemp;
                }
            }

            //2--Report Unit Status (binary vector)
            char[] auxchar = new char[numperiods * 2];
            for (ii = 0; ii < numunits + 2; ii++) {
                cont = 0;
                if (ii == 0) {
                    auxchar = new char[numperiods];
                    for (tt = 0; tt < (numperiods * 2 - 22) / 2; tt++) {
                        auxchar[tt] = ' ';
                    }
                    reportline.println("     " + (new String(auxchar)) + "Unit Operating Status");
                    reportline.println("             " + (new String(auxchar)) + "(1-" + (numperiods) + " hrs)");
                    reportline.println(printdivline(numperiods * 2 + 35));

                } else if (ii > 1) {
                    auxchar = new char[numperiods * 2 + 5];
                    for (tt = 0; tt < numperiods; tt++) {
                        auxchar[cont] = ' ';
                        if (ubest[ii - 2][tt] == 1) {
                            auxchar[cont + 1] = '1';
                        } else {
                            auxchar[cont + 1] = '0';
                        }
                        cont += 2;
                    }
                    String wtmp = "                                                    ";
                    String unitnametemp = formatlabel(wtmp + Unitdata.readname[ii - 2], longestname);
                    reportline.println(unitnametemp + "       " + new String(auxchar));
                }
            }
            reportline.println(printdivline(numperiods * 2 + 35));
            reportline.println(" ");
            reportline.println(" ");
            reportline.println(" ");

            //3--Report Unit Power outputs (primal values)
            auxchar = new char[1];
            if (numperiods * 8 - 26 > 0) {
                auxchar = new char[(numperiods * 8 - 26) / 2];
                for (tt = 0; tt < (numperiods * 8 - 26) / 2; tt++) {
                    auxchar[tt] = ' ';
                }
            }
            reportline.println(printdivline(numperiods * 8 + 25));
            reportline.println("              " + (new String(auxchar)) + "Units Power Output [MW]");
            reportline.println("                    " + (new String(auxchar)) + "(1-" + (numperiods) + " hrs)");
            reportline.println(printdivline(numperiods * 8 + 25));

            for (ii = 0; ii < numunits; ii++) {
                String plineout = " ";
                auxchar = new char[numperiods * 2 + 5];

                for (tt = 0; tt < numperiods; tt++) {
                    cont = 0;
                    String pout = formatreport(pbest[ii][tt], 5);
                    int longp = pout.length();
                    auxchar = new char[8];
                    for (int kk = 0; kk < 8; kk++) {
                        if (kk < (8 - longp)) {
                            auxchar[kk] = ' ';
                        } else {
                            auxchar[kk] = pout.charAt(cont);
                            cont++;
                        }
                    }
                    plineout += (new String(auxchar));
                }
                String unitnametemp = formatlabel("          " + Unitdata.readname[ii], 10);
                reportline.println(unitnametemp + "     " + plineout);
            }
            reportline.println(printdivline(numperiods * 8 + 25));

            //4--Print energy demands and reserves:
            String plineout = " ";
            for (tt = 0; tt < numperiods; tt++) {
                cont = 0;
                String pout = formatreport(hourlypdem[tt], 5);
                int longp = pout.length();
                auxchar = new char[8];
                for (int kk = 0; kk < 8; kk++) {
                    if (kk < (8 - longp)) {
                        auxchar[kk] = ' ';
                    } else {
                        auxchar[kk] = pout.charAt(cont);
                        cont++;
                    }
                }
                plineout += (new String(auxchar));
            }
            reportline.println(" Demand  [MW]  " + plineout);

            plineout = " ";
            for (tt = 0; tt < numperiods; tt++) {
                cont = 0;
                float totalres = 0;
                for (ii = 0; ii < numunits; ii++) {
                    totalres += pmax[ii][tt] * ubest[ii][tt];
                }
                totalres = (totalres - hourlypdem[tt]) * 100 / (hourlypdem[tt]);
                String pout = formatreport(totalres, 5);
                int longp = pout.length();
                auxchar = new char[8];
                for (int kk = 0; kk < 8; kk++) {
                    if (kk < (8 - longp)) {
                        auxchar[kk] = ' ';
                    } else {
                        auxchar[kk] = pout.charAt(cont);
                        cont++;
                    }
                }
                plineout += (new String(auxchar));
            }
            reportline.println("S. Reserve [%] " + plineout);

            //reportline.println(printdivline(numperiods*8+25));
            //4--Close File Connection
            reportline.close();
            os.close();
            //return true;
        } catch (Exception e) {
            System.out.println("File report error: " + e);
            //return false;
        }

    }

    //--XMethod12> Seek Dual Marginal Unit:
    public short getmarginalunit(int tt) {
        short ii = 0;
        short marginalunit = 0;
        float betamax = 0;
        for (ii = 0; ii < numunits; ii++) {
            if (beta[ii][tt] > betamax && ubin[ii][tt] == 1) {   //OJO!!
                betamax = beta[ii][tt];
                marginalunit = ii;
            }
        }
        return marginalunit;
    }

    //--XMethod13> XML String/Boolean converter: (yes->true; no->false)
    public boolean getstatusonoff(String statecheck) {
        if (statecheck.equals("yes")) {
            return true;
        } else {
            return false;
        }
    }

    //--XMethod14> Return an string with the content of the JComponent
    public String getstringcomp(JComponent jc) {
        if (jc instanceof JTextField) {
            JTextField jtext = (JTextField) jc;
            jtext.selectAll();
            return jtext.getSelectedText();
        } else if (jc instanceof JComboBox) {
            JComboBox jcomb = (JComboBox) jc;
            return jcomb.getSelectedItem().toString();
        } else {
            return "";
        }
    }

    public String LogFilePath() {
        String CompletePathLogFile;
        if (usedb) {
            CompletePathLogFile = "UCcomplete.txt";
        } else {
            CompletePathLogFile = txtfiledir + "//UCcomplete.txt";
        }
        return CompletePathLogFile;
    }

}
