package cl.flconsulting.ucopt;

/*****************************************************************************  
 * Written by:             Frank Leanez
 * Initial release:        November 7, 2005
 * Runs external application using native "runExternal" method (written in C++)
 * Asociated file: RunexternalFile.cpp
 *****************************************************************************/  

public class runExternalFile {

    public native void runExternal(String js);
    
    @Deprecated
    public void exec(String exeprog) {
//        System.load(".\\lib\\RunExternalFile.dll");
        System.loadLibrary("runExternalFile"); //Load RunExternalFile.dll
        runExternal(exeprog);
    }
    
    public static void doRun(String progname_pc, String progname_unix, boolean print) throws Exception {
        String OSName = System.getProperty("os.name");
        
        //Call solver. Currently only Runtime exec() method supported
        Runtime r = Runtime.getRuntime();
        Process p;
        r.gc();
        if (OSName.startsWith("Windows")) {
            if (print) {
                System.out.println("Call Command: " + progname_pc);
            }
            p = r.exec(progname_pc);
        } else if (OSName.equals("Linux") || OSName.startsWith("Mac OS X")) {
            if (print) {
                System.out.println("Call Command: /bin/sh -c " + progname_unix);
            }
            p = r.exec(new String[]{"/bin/sh", "-c", progname_unix});
        } else if (OSName.equals("IRIX")) {
            String[] commando = {"xterm", "-e", progname_unix};
            if (print) {
                System.out.println("Dorun:" + commando);
            }
            p = r.exec(commando);
        } else {
            if (print) {
                System.out.println("Call Command: " + progname_pc);
            }
            p = r.exec(progname_pc);
        }
        p.waitFor(); //TODO: Improve this!!
        r.runFinalization();
    }

}
