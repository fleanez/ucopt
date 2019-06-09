package cl.flconsulting.ucopt;

/*****************************************************************************  
 * Written by:             Frank Leanez
 * Initial release:        November 27, 2005
 * Linear Programming JAVA-GLPK Interface Class
 * Runs external application using native "runGLPKsol" method (written in C++)
 * Associated code file: runGLPKsol.cpp
 * 
 *  Definition:
 *  **********
 * This is the interface class with c++ runGLPKsol.dll and glpk48.dll (or superior) 
 * for LP and MIP problems of the following type:
 * 
 * min/max: Cx
 * st:      Ax<b
 *          x- <x< x+
 * 
 *   Inputs:
 *   ******
 *
 * 	- LPmethod:
 *		0: LP  Simplex
 *		1: LP  Interior Point
 *		2: MIP Branch and BOund
 * 	- objcoefn:
 *		Objective function linear coeficient vector (C)
 * 	- Arow:
 *		A matrix not-null coeficient row index (Only Sparse matrix are allowed)
 * 	- Acol:
 *		A matrix not-null coeficient column index (Only Sparse matrix are allowed)
 * 	- Acoef:
 *		A matrix not-null coeficient (Only Sparse matrix are allowed)
 * 	- RHS:
 *		Right-Hand-Side contraint vector (b)
 * 	- ctype: (Vector of constraints type)
 *		0: Equality
 *		1: Lower than -- ie. sum(a(i)*x(i))<=b
 *		2: Greater than -- ie. sum(a(i)*x(i))<=b
 * 	- LB:
 *		Column or variable Lower Bounds (x-)
 * 	- UB:
 *		Column or variable Upper Bounds (x+)		
 *****************************************************************************/  
@Deprecated
public class runGLPKsol {

    public native void runGLPK(int LPmethodn[], double objcoefn[], int Arown[], int Acoln[],
            double Acoefn[], double RHSn[], byte ctypen[], double LBn[], double UBn[]);

    public void exec(String glpkdir, String LPmethodstring, double objcoef[], int Arow[], int Acol[],
            double Acoef[], double RHS[], byte ctype[], double LB[], double UB[]) {

        //Variable Definition:
        int LPmethod[] = new int[1];

        //Use Simplex Method:
        if (LPmethodstring.equals("simplex")) {
            LPmethod[0] = 0;

            //Use Interior Point Method:
        } else if (LPmethodstring.equals("interior point")) {
            LPmethod[0] = 1;

            //Use Branch and Bound Method:
        } else if (LPmethodstring.equals("B&B")) {
            LPmethod[0] = 2;
        }

        //Execute Native Methods:
        try {
            System.load(glpkdir + "\\bin\\glpk48.dll"); 	//Load GLPK48.dll
            System.loadLibrary("./libs/runGLPKsol"); 	//Load runGLPKsol.dll PROVISIONAL!!
            runGLPK(LPmethod, objcoef, Arow, Acol, Acoef, RHS, ctype, LB, UB);
        } catch (Exception e) {
            String message = e.toString();
            System.out.println("Error Calling GLPK: " + message);
        }

    }

}
