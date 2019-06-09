package cl.flconsulting.ucopt;

/**
 * ***************************************************************************
 * Written by: Frank Leanez Initial release: September, 2005
 ****************************************************************************
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class JavaUCFrame extends JFrame implements KeyListener, ActionListener, ItemListener {

//------------------
//Class Attributes:
//------------------
    public JavaUC MyJavaUC;

    //Dynamic Label attributes:
    JLabel lusen1;
    JLabel litersc;

    //Button attributes:
    JButton bok;
    JButton bcancel;
    JButton badv;
    JRadioButton bfulln1;
    JRadioButton bhalfn1;
    JRadioButton bheavyn1;
    JRadioButton bnonen1;

    //General attributes:
    JTextField tnumunits;
    JTextField tnumperiods;

    //DB attributes:
    JButton bbrowse;

    JCheckBox cusesch;
    JCheckBox cusedb;
    JCheckBox cusepiecewise;
    JCheckBox cuseheuristic;
    JCheckBox cusequadra;
    JCheckBox cuseaugmen;
    JCheckBox cuseramp;
    JCheckBox cuseplimit;
    JCheckBox cusetxconstraint;

    JCheckBox cusespin;
    JCheckBox cusepres;

    JComboBox cdbvendor;
    JComboBox cpriorsearch;
    JComboBox copt;
    JComboBox ctol;
    JTextField tdbubica;
    JTextField tdbname;
    JTextField tdblogin;
    JTextField titermax;
    JTextField titersc;
    JTextField tfiledir;

    JPanel phold;

    JPasswordField tdbpass;

//------------------
//Frame Constructor:
//------------------
    public JavaUCFrame() {

        super("Unit Commitment Options");

        //CREATE JAVAUC OBJECT (INITIALIZE ENVIROMENT)
        MyJavaUC = new JavaUC();

        //Prepare Frame's LookandFeel:
        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
        }

        //SUPREME PANEL:
        JPanel psupreme = new JPanel();
        psupreme.setLayout(new BoxLayout(psupreme, BoxLayout.PAGE_AXIS));

        //GENERAL PANEL:
        JPanel pgeneral = new JPanel();
        pgeneral.setLayout(new BorderLayout());
        JPanel pg1 = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        //Left Columns (labels):
        c.anchor = GridBagConstraints.LINE_START;
        JLabel lnumunits = new JLabel("Max Units: ");
        pg1.add(lnumunits, c);
        c.gridy = 1;
        JLabel lnumperiods = new JLabel("Time Periods: ");
        pg1.add(lnumperiods, c);
        c.gridy = 2;
        JLabel litermax = new JLabel("Max Iterations: ");
        pg1.add(litermax, c);
        c.gridy = 3;
        JLabel ltol = new JLabel("Tolerance: ");
        pg1.add(ltol, c);
        c.gridy = 0;
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_END;
        tnumunits = new JTextField("", 5);
        pg1.add(tnumunits, c);
        c.gridy = 1;
        tnumperiods = new JTextField("", 5);
        pg1.add(tnumperiods, c);
        c.gridy = 2;
        titermax = new JTextField("", 5);
        pg1.add(titermax, c);
        c.gridy = 3;
        String[] tolset = {"1E-4", "0.001", "0.01", "0.1"};
        ctol = new JComboBox<String>(tolset);

        pg1.add(ctol, c);
        pgeneral.add(pg1, BorderLayout.NORTH);

        //DATA ORIGIN PANEL:
        JPanel pdata = new JPanel();
        pdata.setLayout(new BorderLayout());
        phold = new JPanel(new CardLayout());

        //Text/DB selection Panel:
        JPanel pd0 = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        JLabel lusedb = new JLabel("Read From DB");
        pd0.add(lusedb, c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_END;
        cusedb = new JCheckBox();
        cusedb.setName("usedatabase");
        cusedb.addItemListener(this);
        cusedb.setSelected(true);
        pd0.add(cusedb, c);

        cusesch = new JCheckBox();

        //DB sub-panel:
        JPanel pd1 = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        //Left Columns (labels):
        c.anchor = GridBagConstraints.LINE_START;
        JLabel ldbvendor = new JLabel("DB Vendor:");
        pd1.add(ldbvendor, c);
        c.gridy = 1;
        JLabel ldbubica = new JLabel("DB Location:");
        pd1.add(ldbubica, c);
        c.gridy = 2;
        JLabel ldbname = new JLabel("DB Name:");
        pd1.add(ldbname, c);
        c.gridy = 3;
        JLabel ldblogin = new JLabel("DB Login:");
        pd1.add(ldblogin, c);
        c.gridy = 4;
        JLabel ldbpass = new JLabel("DB Password:");
        pd1.add(ldbpass, c);
        //Right Columns (textfields and combo-boxes):
        c.anchor = GridBagConstraints.LINE_END;
        c.gridy = 0;
        c.gridx = 1;
        String[] dbvendor = {"mysql", "odbc"};
        cdbvendor = new JComboBox<String>(dbvendor);
        pd1.add(cdbvendor, c);
        c.gridy = 1;
        tdbubica = new JTextField("", 7);
        pd1.add(tdbubica, c);
        c.gridy = 2;
        tdbname = new JTextField("", 7);
        pd1.add(tdbname, c);
        c.gridy = 3;
        tdblogin = new JTextField("", 7);
        pd1.add(tdblogin, c);
        c.gridy = 4;
        tdbpass = new JPasswordField("", 7);
        tdbpass.setPreferredSize(tdblogin.getPreferredSize());
        pd1.add(tdbpass, c);

        //TXT sub-panel:
        JPanel pd2 = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        JLabel lfiledir = new JLabel("Input Files Directory: ");
        pd2.add(lfiledir, c);
        c.gridy = 1;
        tfiledir = new JTextField("", 15);
        pd2.add(tfiledir, c);
        c.gridy = 2;
        c.anchor = GridBagConstraints.LINE_END;
        bbrowse = new JButton("  Browse  ");
        bbrowse.addActionListener(this);
        bbrowse.setActionCommand("Browse");
        pd2.add(bbrowse, c);

        //Text/DB holding Panel:
        //phold = new JPanel(new CardLayout());
        phold.add(pd1, "dbpanel");
        phold.add(pd2, "txtpanel");
        phold.setVisible(true);

        pdata.add(pd0, BorderLayout.NORTH);
        pdata.add(phold, BorderLayout.CENTER);

        //OPTIMIZER PANEL:
        JPanel popt = new JPanel();
        popt.setLayout(new BorderLayout());

        JPanel op1 = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();

        //Left Columns (labels):
        c.anchor = GridBagConstraints.LINE_START;
        JLabel lopt = new JLabel("External Optimizer: ");
        op1.add(lopt, c);
        c.gridy = 1;
        c.gridwidth = 2;
        cuseaugmen = new JCheckBox("Augmented lagrangian", false);
        op1.add(cuseaugmen, c);
        c.gridy = 2;
        cuseheuristic = new JCheckBox("Heuristic search", false);
        op1.add(cuseheuristic, c);
        c.gridy = 3;
        cusepiecewise = new JCheckBox("Piece-wise linear costs", false);
        cusepiecewise.setEnabled(false);
        cuseaugmen.setName("piece");
        cuseaugmen.addItemListener(this);
        op1.add(cusepiecewise, c);
        c.gridy = 4;
        cusequadra = new JCheckBox("Use quadratic costs?", false);
        cusequadra.setName("cuadra");
        cusequadra.addItemListener(this);
        cusequadra.setVisible(false);
        op1.add(cusequadra, c);

        //Right Columns (Choice):
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 1;
        c.gridy = 0;
        String[] avaiopt = {"minos", "glpk", "lpsolve"};
        copt = new JComboBox<String>(avaiopt);
        copt.addItemListener(this);
        op1.add(copt, c);
        c.gridy = 1;
        JLabel laux = new JLabel(" ");
        op1.add(laux, c);
        popt.add(op1, BorderLayout.NORTH);

        //ADVANCED PANEL:
        JPanel padv = new JPanel();
        padv.setLayout(new BorderLayout());
        JPanel padv1 = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;

        //c.gridwidth=2;
        //cuseramp = new JCheckBox("Consider ramps",useramp);
        cuseramp = new JCheckBox("Consider ramps", false);
        cuseramp.setEnabled(false);
        padv1.add(cuseramp, c);
        c.gridy = 1;
        //cusespin = new JCheckBox("Spining reserve",usespin);
        cusespin = new JCheckBox("Spining reserve", false);
        cusespin.setEnabled(false);
        padv1.add(cusespin, c);
        c.gridy = 2;
        //cusepres = new JCheckBox("Primary reserve",usepres);
        cusepres = new JCheckBox("Primary reserve", false);
        cusepres.setEnabled(false);
        padv1.add(cusepres, c);
        c.gridy = 3;
        //cuseplimit = new JCheckBox("Generation Limit",useplimit);
        cuseplimit = new JCheckBox("Generation Limit", false);
        cuseplimit.setEnabled(false);
        padv1.add(cuseplimit, c);

        c.gridy = 4;
        JLabel lprior = new JLabel("Solution Seek Priority:");
        padv1.add(lprior, c);
        c.gridy = 5;
        c.anchor = GridBagConstraints.CENTER;
        String[] spriorsearch = {"optimal", "feasible"};
        cpriorsearch = new JComboBox<String>(spriorsearch);
        //cpriorsearch.setSelectedItem(priorsearch);
        padv1.add(cpriorsearch, c);

        c.gridy = 6;
        c.anchor = GridBagConstraints.LINE_START;
        //JLabel ladv=new JLabel ("Advanced Options Window:");
        JLabel ladv = new JLabel(" ");
        padv1.add(ladv, c);
        c.gridy = 7;
        c.anchor = GridBagConstraints.CENTER;
        badv = new JButton("Advanced Options");
        badv.setActionCommand("Advanced");
        badv.addActionListener(this);
        padv1.add(badv, c);
        //c.gridy=5;
        //padv1.add(new JLabel (" "),c);

        padv.add(padv1, BorderLayout.NORTH);

        //SECURITY-CONSTRAINT PANEL:
        JPanel pscuc = new JPanel();
        pscuc.setLayout(new BorderLayout());

        JPanel pscuc1 = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        JLabel lusetx = new JLabel("Transmission effects");
        pscuc1.add(lusetx, c);
        c.gridy = 1;
        JLabel lwhite = new JLabel(" ");
        pscuc1.add(lwhite, c);
        c.anchor = GridBagConstraints.LINE_START;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 2;
        lusen1 = new JLabel("N-1 Criteria:");
        lusen1.setEnabled(false);
        pscuc1.add(lusen1, c);

        c.gridx = 1;
        c.gridy = 0;
        cusetxconstraint = new JCheckBox();
        cusetxconstraint.setSelected(false);
        cusetxconstraint.setEnabled(false);
        cusetxconstraint.setName("tx");
        cusetxconstraint.addItemListener(this);
        pscuc1.add(cusetxconstraint, c);

        bfulln1 = new JRadioButton("All Lines");
        bfulln1.setActionCommand("full");
        bfulln1.addActionListener(this);
        bfulln1.setEnabled(false);

        bhalfn1 = new JRadioButton("Over Half Capacity");
        bhalfn1.setActionCommand("half");
        bhalfn1.addActionListener(this);
        bhalfn1.setEnabled(false);

        bheavyn1 = new JRadioButton("Heaviest Congested");
        bheavyn1.setActionCommand("heavy");
        bheavyn1.addActionListener(this);
        bheavyn1.setEnabled(false);

        bnonen1 = new JRadioButton("None");
        bnonen1.setActionCommand("none");
        bnonen1.setSelected(true);
        bnonen1.addActionListener(this);
        bnonen1.setEnabled(false);

        ButtonGroup bn1 = new ButtonGroup();
        bn1.add(bfulln1);
        bn1.add(bhalfn1);
        bn1.add(bheavyn1);
        bn1.add(bnonen1);

        c.anchor = GridBagConstraints.LINE_START;
        c.gridx = 0;
        c.gridy = 3;
        pscuc1.add(bfulln1, c);
        c.gridy = 4;
        pscuc1.add(bhalfn1, c);
        c.gridy = 5;
        pscuc1.add(bheavyn1, c);
        c.gridy = 6;
        pscuc1.add(bnonen1, c);

        c.gridy = 7;
        litersc = new JLabel("Max Iterations:");
        litersc.setVisible(false);
        pscuc1.add(litersc, c);
        c.gridx = 1;
        titersc = new JTextField("", 2);
        titersc.setVisible(false);
        pscuc1.add(titersc, c);

        pscuc.add(pscuc1, BorderLayout.NORTH);

        //TABS PANEL:
        JTabbedPane opciontabs = new JTabbedPane(JTabbedPane.TOP);//, JTabbedPane.SCROLL_TAB_LAYOUT);
        opciontabs.addTab("General", pgeneral);
        opciontabs.addTab("Data Origin", pdata);
        opciontabs.addTab("Optimizer", popt);
        opciontabs.addTab("Advanced", padv);
        opciontabs.addTab("Security-Constrained", pscuc);
        opciontabs.setSelectedIndex(0);

        //OK-CANCEL PANEL:
        JPanel pokcancel = new JPanel();
        bok = new JButton("Calculate");
        bok.addActionListener(this);
        bok.setActionCommand("Calculate");
        bcancel = new JButton("  Exit  ");
        bcancel.addActionListener(this);
        bcancel.setActionCommand("Cancel");
        pokcancel.add(bok);
        pokcancel.add(bcancel);

        //Preparing Window:
        setFrameValues();
        psupreme.add(opciontabs);
        psupreme.add(pokcancel);
        getContentPane().add(psupreme);
        pack();
        setLocation(200, 50);
        setResizable(false);
        setVisible(true);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);  //(INDEPENDANT)
                MyJavaUC = null;
                dispose();
            }
        });

    }

    public void keyPressed(KeyEvent event) {
    }

    public void keyReleased(KeyEvent event) {
    }

    public void keyTyped(KeyEvent event) {
    }

//----------------------
//Handle Choices' Actions:
//----------------------
    public void itemStateChanged(ItemEvent e) {

        Object event = e.getItemSelectable();
        //Object event = e.getItem();

        if (event instanceof JCheckBox) {
            JCheckBox checkevent = (JCheckBox) event;
            if (checkevent.getName().equals("tx")) {
                if (checkevent.isSelected()) {
                    lusen1.setEnabled(true);
                    bfulln1.setEnabled(true);
                    bhalfn1.setEnabled(false);	//PROVISIONAL!!!
                    bheavyn1.setEnabled(false);	//PROVISIONAL!!!
                    bnonen1.setEnabled(true);
                    litersc.setVisible(true);
                    titersc.setVisible(true);

                } else {
                    lusen1.setEnabled(false);
                    bfulln1.setEnabled(false);
                    bhalfn1.setEnabled(false);
                    bheavyn1.setEnabled(false);
                    bnonen1.setEnabled(false);
                    litersc.setVisible(false);
                    titersc.setVisible(false);
                }
            } else if (checkevent.getName().equals("usedatabase")) {
                CardLayout cl = (CardLayout) (phold.getLayout());
                if (!checkevent.isSelected()) {
                    cl.show(phold, "txtpanel");
                } else {
                    cl.show(phold, "dbpanel");
                }
            } else if (checkevent.getName().equals("cuadra")) {
                if (checkevent.isSelected()) {
                    //cusepiecewise.setSelected(false); //PROVISIONAL!!!
                    //cusepiecewise.setEnabled(false);  //PROVISIONAL!!!
                } else {
                    //cusepiecewise.setSelected(true);  //PROVISIONAL!!!
                    //cusepiecewise.setEnabled(true);   //PROVISIONAL!!!
                }
            }

        } else if (event instanceof JComboBox) {
            JComboBox comboevent = (JComboBox) event;
            if (comboevent.getSelectedIndex() == 0) {
                //cusepiecewise.setEnabled(true);
                cusequadra.setVisible(true);
            } else if (comboevent.getSelectedIndex() == 1 && cusequadra != null) {
                cusequadra.setVisible(false);
            }
        } else {
            System.out.println(event.toString());
        }

    }

//----------------------
//Handle Button Actions:
//----------------------
    public void actionPerformed(ActionEvent ev) {
        String label = ev.getActionCommand();
        if (label.equals("Calculate")) {
            String selectopt = (String) copt.getSelectedItem();
            boolean usequadra, useaugmen = false, useramp;
            if (selectopt.equals("minos") && cusequadra.isSelected()) {
                usequadra = true;
                //if (cuseaugmen.isSelected()){
                //	useaugmen=true;
                //}
            } else {
                usequadra = false;
            }

            //N-1 constraints codification:
            short typen1;
            if (bfulln1.isSelected()) {
                typen1 = 3;
            } else if (bhalfn1.isSelected()) {
                typen1 = 2;
            } else if (bheavyn1.isSelected()) {
                typen1 = 1;
            } else {
                typen1 = 0;
            }
            MyJavaUC.setgeneral(getshorttext(tnumunits), getshorttext(tnumperiods),
                    getlongtext(titermax), copt.getSelectedItem().toString(),
                    ctol.getSelectedItem().toString(), cuseaugmen.isSelected(),
                    cuseheuristic.isSelected(), cusepiecewise.isSelected(),
                    usequadra, cuseramp.isSelected(),
                    cusetxconstraint.isSelected(), typen1,
                    getshorttext(titersc), cusesch.isSelected(),
                    cpriorsearch.getSelectedItem().toString(),
                    cusespin.isSelected(), cusepres.isSelected(),
                    cuseplimit.isSelected(), getstringtext(tfiledir),
                    cusedb.isSelected(), cdbvendor.getSelectedItem().toString(), getstringtext(tdbubica),
                    getstringtext(tdbname), getstringtext(tdblogin),
                    getstringtext(tdbpass));
//			if (cusedb.isSelected()){
//				MyJavaUC.initconnection(cdbvendor.getSelectedItem().toString(), getstringtext(tdbubica),
//								getstringtext(tdbname),getstringtext(tdblogin),
//								getstringtext(tdbpass));
//			}
            MyJavaUC.calculate();
            UCmatrixFrame mymatrix = new UCmatrixFrame(MyJavaUC);

        } else if (label.equals("Cancel")) {
            System.exit(0);  //(INDEPENDANT)
            MyJavaUC = null;
            dispose();
        } else if (label.equals("Advanced")) {
            UCtreeoptionFrame mytreeoptionframe = new UCtreeoptionFrame(this);
        } else if (label.equals("Browse")) {
            JFileChooser txtfilechooser = new JFileChooser();
            txtfilechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = txtfilechooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                tfiledir.setText(txtfilechooser.getSelectedFile().getPath());
            }
        }

    }

    public short getshorttext(JTextField jtext) {
        jtext.selectAll();
        return Short.valueOf(jtext.getSelectedText()).shortValue();
    }

    public long getlongtext(JTextField jtext) {
        jtext.selectAll();
        return Long.valueOf(jtext.getSelectedText()).longValue();
    }

    public String getstringtext(JTextField jtext) {
        jtext.selectAll();
        return jtext.getSelectedText();
    }

    public void setFrameValues() {

        //LOAD MAIN DEFAULTS FROM OPTION FILE:
        //General Defaults:
        short numu = MyJavaUC.numunits;
        short numt = MyJavaUC.numperiods;
        long iterm = MyJavaUC.itermax;
        long itersc = MyJavaUC.itermaxnet;
        float toldg = MyJavaUC.tol_dg;
        String priorsearch = MyJavaUC.priorsearch;

        //DB Defaults:
        boolean usesch = MyJavaUC.usesch; //provisional!!!
        boolean usedb = MyJavaUC.usedb;
        String db = MyJavaUC.dbtype;
        String dbnam = MyJavaUC.dbname;
        String dbloc = MyJavaUC.dbloc;
        String dblog = MyJavaUC.dblog;
        String dbpas = MyJavaUC.dbpass;
        String txtfiledir = MyJavaUC.txtfiledir;
        //External Optimizer:
        boolean useramp = MyJavaUC.useramp;
        boolean useheuristic = MyJavaUC.useheuristic;
        boolean usespin = MyJavaUC.usespin;
        boolean usepres = MyJavaUC.usepres;
        boolean useaugmen = MyJavaUC.useaugmen;
        boolean usepiecewise = MyJavaUC.usepiecewise;
        boolean useplimit = MyJavaUC.useplimit;
        String oppt = MyJavaUC.optimizer;

        //GENERAL PANEL:
        tnumunits.setText(Short.toString(numu));
        tnumperiods.setText(Short.toString(numt));
        titermax.setText(Long.toString(iterm));
        String[] tolset = {"1E-4", "0.001", "0.01", "0.1"};
        if (toldg <= 1e-4) {
            ctol.setSelectedItem(tolset[0]);
        } else if (toldg <= 0.001 && toldg > 1e-4) {
            ctol.setSelectedItem(tolset[1]);
        } else if (toldg <= 0.01 && toldg > 0.001) {
            ctol.setSelectedItem(tolset[2]);
        } else {
            ctol.setSelectedItem(tolset[3]);
        }

        //DATA ORIGIN PANEL:
        cusedb.setSelected(usedb);

        //DB sub-panel:
        String[] dbvendor = {"mysql", "odbc"};
        cdbvendor.setSelectedItem(db);
        tdbubica.setText(dbloc);
        tdbname.setText(dbnam);
        tdblogin.setText(dblog);
        tdbpass.setText(dbpas);

        //TXT sub-panel:
        tfiledir.setText(txtfiledir);

        //OPTIMIZER PANEL:
        String[] avaiopt = {"minos", "glpk"};
        copt.setSelectedItem(oppt);
        cuseaugmen.setSelected(useaugmen);
        cuseheuristic.setSelected(useheuristic);
        cusepiecewise.setSelected(usepiecewise);

        //ADVANCED PANEL:
        cuseramp.setSelected(useramp);
        cusespin.setSelected(usespin);
        cusepres.setSelected(usepres);
        cuseplimit.setSelected(useplimit);
        cpriorsearch.setSelectedItem(priorsearch);

        //SECURITY-CONSTRAINT PANEL:
        titersc.setText(Long.toString(itersc));
        //pendiente todo lo demas incluyendo XML

        //TABS PANEL:
        //OK-CANCEL PANEL:
    }

}//end class
