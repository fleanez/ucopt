package cl.flconsulting.ucopt;

//****************************************************************************
//Optimizers caller
//Initially Created: 7/12/2008 - Frank Leanez
//GLPK Version supported: 4.49 (64 bits)
//Lpsolve Version supported:
//****************************************************************************  
import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.glp_prob;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_smcp;
import org.gnu.glpk.glp_iptcp;
import org.gnu.glpk.glp_cpxcp;

import java.io.*;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class OptimizerCaller {

    public static final int OPTIMAL = 0;
    public static final int INFEASIBLE = 1;
    public static final int FEASIBLE = 2;

    public static final int NOPRINT = 3;
    public static final int PRINTDETAIL = 4;

    public static final int EQUAL = 0;
    public static final int LESSTHAN = 1;
    public static final int GREATTHAN = 2;

    public static final int MSG_OFF = 0;
    public static final int MSG_ON = 1;

    public static final String SIMPLEX = "simplex";
    public static final String INTERIOR = "interior";

    public static final String GLPK_SOLVER = "glpk";
    public static final String LPSOLVE = "lpsolve";
    public static final String MINOS53 = "minos5.3";

    private String optimizer;
    private String lpmethod;
    private LpSolve lpsolve;
    private int PrintLevel = 0;
    //GlpkSolver glpsol;
    private glp_prob glpsol;

    private PrintStream myPrintStream;
    private FileOutputStream os;
    private BufferedOutputStream bos;

    //Constructor N1: desde matrices pasadas por memoria:
    public OptimizerCaller(String optTemp, int Nrow, int Ncol) throws Exception {
        optimizer = optTemp;
        if (optTemp.equals(GLPK_SOLVER)) {
            //    		try {
            //    			glpsol = new GlpkSolver();
            //    		}catch(Exception e){
            //    			System.out.println("Error de glpk: " + e.getLocalizedMessage());
            //    		}
            glpsol = GLPK.glp_create_prob();
            //glpsol.setProbName("UCOPT");
            GLPK.glp_set_prob_name(glpsol, "UCOPT");
            //glpsol.setObjName("objucopt");
            GLPK.glp_set_obj_name(glpsol, "objucopt");
            //glpsol.setObjDir(GlpkSolver.LPX_MIN);
            GLPK.glp_set_obj_dir(glpsol, GLPKConstants.GLP_MIN);
            //glpsol.setIntParm(GlpkSolver.LPX_K_MSGLEV, 0);
            //glpsol.addRows(Nrow);
            GLPK.glp_add_rows(glpsol, Nrow);
            //glpsol.addCols(Ncol);
            GLPK.glp_add_cols(glpsol, Ncol);
            lpmethod = SIMPLEX;

        } else if (optTemp.equals(LPSOLVE)) {
            lpsolve = LpSolve.makeLp(Nrow, Ncol);
            lpsolve.setMinim();
            lpsolve.setLpName("UCOPT");
        } else if (optTemp.equals(MINOS53)) {
            os = new FileOutputStream("./economic.dat");
            bos = new BufferedOutputStream(os, 32768);
            myPrintStream = new PrintStream(bos, false);
        }

    }

    //Constructor N2 desde archivo cplex-lp:
    public void setDimension(int Nrow, int Ncol) throws Exception {
        if (optimizer.equals(GLPK_SOLVER)) {
            GLPK.glp_add_rows(glpsol, Nrow);
            GLPK.glp_add_cols(glpsol, Ncol);
//    		glpsol.addRows(Nrow);
//			glpsol.addCols(Ncol);

        } else if (optimizer.equals(LPSOLVE)) {

            if (lpsolve != null) {
                lpsolve.deleteLp();
            }
            lpsolve = LpSolve.makeLp(Nrow, Ncol);

        }
    }

    public void setObjCoefficient(double[] objcoefTemp) throws Exception {
        if (optimizer.equals(GLPK_SOLVER)) {
            for (int ii = 0; ii < objcoefTemp.length; ii++) {
                //glpsol.setObjCoef(ii+1 , objcoefTemp[ii]);
                GLPK.glp_set_obj_coef(glpsol, ii + 1, objcoefTemp[ii]);
            }

        } else if (optimizer.equals(LPSOLVE)) {

            for (int ii = 0; ii < objcoefTemp.length; ii++) {
                lpsolve.setObj(ii + 1, objcoefTemp[ii]);
            }
        }
    }

    public void setRHS(double[] RHSTemp, int[] RHSsenseTemp) throws Exception {

        if (optimizer.equals(GLPK_SOLVER)) {
            for (int ii = 0; ii < RHSTemp.length; ii++) {
                if (RHSsenseTemp[ii] == EQUAL) {
                    //glpsol.setRowBnds(ii+1, GlpkSolver.LPX_FX, RHSTemp[ii], 0);
                    GLPK.glp_set_row_bnds(glpsol, ii + 1, GLPK.GLP_FX, RHSTemp[ii], 0);
                } else if (RHSsenseTemp[ii] == LESSTHAN) {
                    //glpsol.setRowBnds(ii+1, GlpkSolver.LPX_UP, 0, RHSTemp[ii]);
                    GLPK.glp_set_row_bnds(glpsol, ii + 1, GLPK.GLP_UP, 0, RHSTemp[ii]);
                } else if (RHSsenseTemp[ii] == GREATTHAN) {
                    //glpsol.setRowBnds(ii+1, GlpkSolver.LPX_LO, RHSTemp[ii], 0);
                    GLPK.glp_set_row_bnds(glpsol, ii + 1, GLPK.GLP_LO, RHSTemp[ii], 0);
                }
            }
        } else if (optimizer.equals(LPSOLVE)) {
            for (int ii = 0; ii < RHSTemp.length; ii++) {
                lpsolve.setRh(ii + 1, RHSTemp[ii]);
                if (RHSsenseTemp[ii] == EQUAL) {
                    lpsolve.setConstrType(ii + 1, LpSolve.EQ);
                } else if (RHSsenseTemp[ii] == LESSTHAN) {
                    lpsolve.setConstrType(ii + 1, LpSolve.LE);
                } else if (RHSsenseTemp[ii] == GREATTHAN) {
                    lpsolve.setConstrType(ii + 1, LpSolve.GE);
                }
            }
        }

    }

    public void setA(int[] ArowTemp, int[] AcolTemp, double[] AcoefTemp) throws Exception {

        if (optimizer.equals(GLPK_SOLVER)) {

            int numEle = ArowTemp.length;
            SWIGTYPE_p_int Arow;
            SWIGTYPE_p_int Acol;
            SWIGTYPE_p_double Acoef;
            Arow = GLPK.new_intArray(numEle);
            Acol = GLPK.new_intArray(numEle);
            Acoef = GLPK.new_doubleArray(numEle);

//    		int [] Arow = new int [numEle-1];
//		int [] Acol = new int [numEle-1];
//		double [] Acoef = new double [numEle-1];
            for (int ii = 0; ii < numEle - 2; ii++) {
                GLPK.intArray_setitem(Arow, ii + 1, ArowTemp[ii] + 1);
                GLPK.intArray_setitem(Acol, ii + 1, AcolTemp[ii] + 1);
                GLPK.doubleArray_setitem(Acoef, ii + 1, AcoefTemp[ii]);
//    			Arow[ii+1]=ArowTemp[ii]+1;
//    			Acol[ii+1]=AcolTemp[ii]+1;
//    			Acoef[ii+1]=AcoefTemp[ii];
            }
            //glpsol.loadMatrix(numEle-2, Arow, Acol, Acoef);
            GLPK.glp_load_matrix(glpsol, numEle - 2, Arow, Acol, Acoef);
            GLPK.delete_intArray(Arow);
            GLPK.delete_intArray(Acol);
            GLPK.delete_doubleArray(Acoef);

        } else if (optimizer.equals(LPSOLVE)) {
            for (int ii = 0; ii < ArowTemp.length; ii++) {
                lpsolve.setMat(ArowTemp[ii] + 1, AcolTemp[ii] + 1, AcoefTemp[ii]);
            }
        }

    }

    public void setBounds(double[] LB, double[] UB) throws Exception {
        if (optimizer.equals(GLPK_SOLVER)) {
            for (int ii = 0; ii < LB.length; ii++) {
                if (LB[ii] == 0 && UB[ii] == 0) { //This is because GLPK 4.8 simplex did not accept double cero bounds
                    //glpsol.setColBnds(ii+1, GlpkSolver.LPX_FX, 0, 0);
                    GLPK.glp_set_col_bnds(glpsol, ii + 1, GLPK.GLP_FX, 0, 0);
                } else {
                    //glpsol.setColBnds(ii+1, GlpkSolver.LPX_DB, LB[ii], UB[ii]);
                    GLPK.glp_set_col_bnds(glpsol, ii + 1, GLPK.GLP_DB, LB[ii], UB[ii]);
                }
            }

        } else if (optimizer.equals(LPSOLVE)) {
            for (int ii = 0; ii < LB.length; ii++) {
                lpsolve.setLowbo(ii + 1, LB[ii]);
                lpsolve.setUpbo(ii + 1, UB[ii]);
            }
        }
    }

    public void setLPMethod(String glpklpmethodTemp) {

        if (optimizer.equals(GLPK_SOLVER)) {
            if (glpklpmethodTemp.equals(SIMPLEX)) {
                lpmethod = SIMPLEX;
            } else {
                lpmethod = INTERIOR;
            }
        }

    }

    public void setPrintLevel(int printTemp) {

        PrintLevel = printTemp;

//    	if (optimizer.equals(GLPK_SOLVER)){
//    			
//    		if (printTemp==NOPRINT){
//    			//glpsol.setIntParm(GlpkSolver.LPX_K_MSGLEV, 0);
//                    GLPK.glp_java_set_msg_lvl(GLPKConstants.GLP_JAVA_MSG_LVL_OFF);
//                    
//                    //GLPK.glp_java_set_msg_lvl(GLPK.GLP_JAVA_MSG_LVL_ALL);
//    		}else if (printTemp==PRINTDETAIL){
//                    //GLPK.glp_java_set_msg_lvl(GLPK.GLP_JAVA_MSG_LVL_ALL);
//    			//glpsol.setIntParm(GlpkSolver.LPX_K_MSGLEV, 1);
//    		}
//    		
//    	
//    	}else if (optimizer.equals(LPSOLVE)){
//    		if (printTemp==NOPRINT){
//    			lpsolve.setVerbose(LpSolve.MSG_NONE);
//    		}else if (printTemp==PRINTDETAIL){
//    			//lpsolve.setVerbose(LpSolve.MSG_ITERATION);
//    		}
//    	}
    }

    public int getSolutionStatus() throws Exception {
        int statusTemp = INFEASIBLE;

        if (optimizer.equals(GLPK_SOLVER)) {
            if (lpmethod.equals(SIMPLEX)) {
                //glpsol.writeCpxlp("uc.txt");
                //glpsol.simplex();
                //GLPK.glp_write_lp(glpsol, null, "uc.lp");
                glp_smcp parm = new glp_smcp();
                GLPK.glp_init_smcp(parm); //This initializes parameters
                if (PrintLevel == PRINTDETAIL) {
                    parm.setMsg_lev(GLPK.GLP_MSG_ON);
                } else {
                    parm.setMsg_lev(GLPK.GLP_MSG_OFF);
                }

                GLPK.glp_simplex(glpsol, parm);

                if (GLPK.glp_get_status(glpsol) == GLPK.GLP_OPT) {
                    statusTemp = OPTIMAL;
                } else if (GLPK.glp_get_status(glpsol) == GLPK.GLP_FEAS) {
                    statusTemp = FEASIBLE;
                } else {
                    statusTemp = INFEASIBLE;
                }

                //				if(glpsol.getStatus()==GlpkSolver.LPX_OPT){
                //					statusTemp=OPTIMAL;
                //					//System.out.println("optimo");
                //				}else if (glpsol.getStatus()==GlpkSolver.LPX_FEAS){
                //					statusTemp=this.FEASIBLE;
                //					//System.out.println("factible");
                //				}else{
                //					statusTemp=this.INFEASIBLE;
                //					//System.out.println("infactible");
                //				}
            } else {
                glp_iptcp parm = new glp_iptcp();
                GLPK.glp_init_iptcp(parm); //This initializes parameters
                parm.setMsg_lev(GLPK.GLP_MSG_OFF); //Temporal
                if (PrintLevel == PRINTDETAIL) {
                    parm.setMsg_lev(GLPK.GLP_MSG_ON);
                } else {
                    parm.setMsg_lev(GLPK.GLP_MSG_OFF);
                }

                GLPK.glp_interior(glpsol, parm);

                if (GLPK.glp_ipt_status(glpsol) == GLPK.GLP_OPT) {
                    statusTemp = OPTIMAL;
                } else {
                    statusTemp = INFEASIBLE;
                }

                //                    glpsol.interior();
                //                    if(glpsol.iptStatus()==GlpkSolver.LPX_T_OPT){
                //                            statusTemp=this.OPTIMAL;
                //                    }else{
                //                            statusTemp=this.INFEASIBLE;
                //                    }
            }
        } else if (optimizer.equals(LPSOLVE)) {
            int statLP = lpsolve.solve();
            if (PrintLevel == NOPRINT) {
                lpsolve.setVerbose(LpSolve.MSG_NONE);
            } else if (PrintLevel == PRINTDETAIL) {
                lpsolve.setVerbose(LpSolve.MSG_ITERATION);
            }
            //lpsolve.writeLp("uc.txt");
            if (statLP == LpSolve.OPTIMAL) {
                statusTemp = OPTIMAL;
            } else if (statLP == LpSolve.FEASFOUND) {
                statusTemp = FEASIBLE;
            } else if (statLP == LpSolve.NOFEASFOUND) {
                statusTemp = INFEASIBLE;
            } else {
                statusTemp = INFEASIBLE;
            }
        }

        return statusTemp;
    }

    public void writeLPFile(String filePath) {

        if (optimizer.equals(GLPK_SOLVER)) {
            glp_cpxcp par = new glp_cpxcp();
            GLPK.glp_write_lp(glpsol, par, filePath);
        } else if (optimizer.equals(LPSOLVE)) {
            try {
                lpsolve.writeLp(filePath);
            } catch (LpSolveException e) {
                System.err.println(e.getMessage());
            }
        }

    }

    public double[] getPrimalSolution() throws Exception {

        double[] primSolTemp;
        if (optimizer.equals(GLPK_SOLVER)) {

            int numVar = GLPK.glp_get_num_cols(glpsol);
//            int numVar=glpsol.getNumCols();
            primSolTemp = new double[numVar];

            if (lpmethod.equals(SIMPLEX)) {
                for (int ii = 0; ii < numVar; ii++) {
                    //primSolTemp[ii]=glpsol.getColPrim(ii+1);
                    primSolTemp[ii] = GLPK.glp_get_col_prim(glpsol, ii + 1);
                }
            } else {
                for (int ii = 0; ii < numVar; ii++) {
                    primSolTemp[ii] = GLPK.glp_ipt_col_prim(glpsol, ii + 1);
                    //primSolTemp[ii]=glpsol.iptColPrim(ii+1);
                }
            }

        } else if (optimizer.equals(LPSOLVE)) {
            int numVar = lpsolve.getNcolumns();
            primSolTemp = new double[numVar];
            for (int ii = 0; ii < numVar; ii++) {
                primSolTemp[ii] = lpsolve.getVarPrimalresult(ii + 1);
            }
            //primSolTemp=lpsolve.getPtrPrimalSolution();
        } else {
            primSolTemp = null;//temporal
        }
        return primSolTemp;
    }

    public double getObjValue() throws Exception {
        double objValTemp = 0.0;

        if (optimizer.equals(GLPK_SOLVER)) {
            if (lpmethod.equals(SIMPLEX)) {
                //objValTemp=glpsol.getObjVal();
                objValTemp = GLPK.glp_get_obj_val(glpsol);
            } else if (lpmethod.equals(INTERIOR)) {
                //objValTemp=glpsol.iptObjVal();
                objValTemp = GLPK.glp_ipt_obj_val(glpsol);
            }
        } else if (optimizer.equals(LPSOLVE)) {
            objValTemp = lpsolve.getObjective();
        }

        return objValTemp;
    }

    public double[] getDualSolution() throws Exception {
        double[] dualSolTemp;
        //dualSolTemp=null;   //temporal
        if (optimizer.equals(GLPK_SOLVER)) {
            //int numRow=glpsol.getNumRows();
            int numRow = GLPK.glp_get_num_rows(glpsol);
            dualSolTemp = new double[numRow];

            if (lpmethod.equals(SIMPLEX)) {
                for (int ii = 0; ii < numRow; ii++) {
                    //dualSolTemp[ii]=glpsol.getRowDual(ii+1);
                    dualSolTemp[ii] = GLPK.glp_get_row_dual(glpsol, ii + 1);
                }
            } else {
                for (int ii = 0; ii < numRow; ii++) {
                    //dualSolTemp[ii]=glpsol.iptRowDual(ii+1);
                    dualSolTemp[ii] = GLPK.glp_ipt_row_dual(glpsol, ii + 1);
                }
            }

        } else if (optimizer.equals(LPSOLVE)) {
            int numRow = lpsolve.getNcolumns();
            dualSolTemp = new double[numRow];
            for (int ii = 0; ii < numRow; ii++) {
                dualSolTemp[ii] = lpsolve.getVarDualresult(ii + 1);
            }
            //primSolTemp=lpsolve.getPtrPrimalSolution();
        } else {
            dualSolTemp = null;//temporal
        }
        return dualSolTemp;
    }

    public void finalizeOptimizer() throws Exception {

        if (optimizer.equals(GLPK_SOLVER)) {
            GLPK.glp_delete_prob(glpsol);
        } else if (optimizer.equals(LPSOLVE)) {
            lpsolve.deleteLp();
        }

    }

}
