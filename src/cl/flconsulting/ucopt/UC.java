package cl.flconsulting.ucopt;

/**
 *
 * @author Frank Leanez
 */
public class UC {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JavaUCFrame showMainPanel = new JavaUCFrame();
                showMainPanel.setVisible(true);
            }
        });
    }
}
