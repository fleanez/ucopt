package cl.flconsulting.ucopt;

/**
 * ***************************************************************************
 * Written by: Frank Leanez Initial release: September, 2005
 ****************************************************************************
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.border.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import javax.swing.ImageIcon;

class UCmatrixFrame extends JFrame implements KeyListener, ActionListener, ItemListener {

//------------------
//Class Attributes:
//------------------
    private JavaUC myjavauc;

    //Button attributes:
    JButton bcontinue;
    JTextField tcontrolgap;

    //General attributes:
    int windowHeight;
    int leftWidth;
    int rightWidth;
    int windowWidth;
    int numunits;
    int numperiods;
    String[] sstatus;
    boolean iterrun;
    boolean firstreport = true;

    //Simulation Results Labels:
    JLabel ltotalcost;
    JLabel liter;
    JLabel ldg;
    JLabel lstatus;
    JLabel lcuts;
    JLabel ltimetotal;
    JLabel ltimedual;
    JLabel ltimeprimal;
    JLabel ltimealr;
    JLabel ltimesubp;
    JLabel ltimedata;
    JLabel lreport;

    //Control Buttons:
    JButton bcontin;
    JButton bexit;
    JButton bopt;
    JButton bbest;
    JButton bprior;
    JButton breport;

    //Textfield Parameters:
    JTextField tsubalfa;
    JTextField tsubbeta;
    JTextField tc_coef;
    JTextField tepsaug;
    JTextField ttolset;
    JTextField titermax;
    JTextField ttoldg;
    JTextField ttolb;

    //Combo-boxes:
    JComboBox cprior;

    //Drawing Panel attributes:
    Color oricolor;
    private UCmatrixPanel myMatrix;

//------------------
//Frame Constructor:
//------------------
    public UCmatrixFrame(JavaUC uctemp) {

        //1--Call Super, create necessary objects and create main panel:
        super("Dynamic Programming Visualization");
        myjavauc = uctemp;
        myMatrix = new UCmatrixPanel(myjavauc);
        JPanel pmain = new JPanel();
        pmain.setLayout(new BorderLayout());
        iterrun = false;
        firstreport = true;

        //2--Set proper Dimension:
        leftWidth = myjavauc.dispwidth;
        rightWidth = 200;
        windowHeight = myjavauc.dispheight;
        windowWidth = leftWidth + rightWidth;
        numunits = myjavauc.numunits;
        numperiods = myjavauc.numperiods;

        //3--LEFT-SIDE PANEL (units status window):
        JScrollPane mainView = new JScrollPane(myMatrix);
        mainView.setPreferredSize(new Dimension(leftWidth, windowHeight));

        /* No me funciono!! damm!
		mainView.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		JScrollBar vert=new JScrollBar();
		vert.setMinimum(windowHeight);
		vert.setMaximum(windowHeight+1000);
		mainView.setVerticalScrollBar(vert);
		mainView.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
         */
        //4--RIGHT-SIDE PANEL (buttons and options):
        JTabbedPane rightpanel = new JTabbedPane(JTabbedPane.TOP);//, JTabbedPane.SCROLL_TAB_LAYOUT);

        //4.1--Results Panel:
        rightpanel.addTab("Results", respanel());

        //4.2--Parameter Panel:
        rightpanel.addTab("Parameters", parameterpanel());

        //4.3--Leyend Panel:
        rightpanel.addTab("Legend", legendpanel());

        //4.4--Build split-pane view:
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainView, rightpanel);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(leftWidth);
        splitPane.setResizeWeight(1);
        //splitPane.setPreferredSize(new Dimension(windowWidth+10,windowHeight+10));

        //5--CONTROL FLOW AND BUTTONS PANEL:
        JPanel pcontrolflow = new JPanel();

        bcontin = new JButton(" Execute ");
        bcontin.addActionListener(this);
        bcontin.setActionCommand("continue");

        bexit = new JButton("Exit");
        bexit.addActionListener(this);
        bexit.setActionCommand("exit");
        bexit.setPreferredSize(bcontin.getPreferredSize());

        bbest = new JButton("Best Feasible");
        bbest.addActionListener(this);
        bbest.setActionCommand("best");
        bbest.setEnabled(false);
        bbest.setForeground(Color.gray);

        bopt = new JButton("Final Solution");
        bopt.addActionListener(this);
        bopt.setActionCommand("last");
        bopt.setPreferredSize(bbest.getPreferredSize());
        bopt.setEnabled(false);
        bopt.setForeground(Color.gray);

        bprior = new JButton("Priority List");
        bprior.addActionListener(this);
        bprior.setActionCommand("prior");
        bprior.setPreferredSize(bopt.getPreferredSize());
        bprior.setEnabled(true);
        oricolor = bprior.getBackground();
        bprior.setBackground(Color.black);

        pcontrolflow.add(bprior);
        pcontrolflow.add(bbest);
        pcontrolflow.add(bopt);
        pcontrolflow.add(bcontin);
        pcontrolflow.add(bexit);

        //6--STATUS BAR PANEL:
        JPanel pstatusbar = new JPanel();
        pstatusbar.setLayout(new BoxLayout(pstatusbar, BoxLayout.LINE_AXIS));
        String sstatus = "<html><font face=Times color=#606060  size=2>";
        JLabel lini = new JLabel(sstatus + "Status: </html>");
        pstatusbar.add(lini);
        pstatusbar.setBorder(bordeintro());

        //7--ADD CREATED SUB-PANELS TO THE MAIN PANEL (PMAIN):
        JPanel pprovi = new JPanel();
        pprovi.setLayout(new BorderLayout());
        pprovi.add(pcontrolflow, BorderLayout.CENTER);
        pprovi.add(pstatusbar, BorderLayout.SOUTH);

        pmain.add(splitPane, BorderLayout.CENTER);
        pmain.add(pprovi, BorderLayout.SOUTH);
        getContentPane().add("Center", pmain);
        pack();

        //8--Prepare Frame:
        addWindowListener(
                new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                dispose();
            }
        });
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        windowHeight += pprovi.getPreferredSize().height + 60;
        windowWidth += 30;
        setSize(windowWidth, windowHeight);
        setLocation(screenSize.width / 3, screenSize.height / 3);
        setVisible(true);

        /*
		
		//Prepare Frame's LookandFeel:
        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        try {
		    UIManager.setLookAndFeel(lookAndFeel);
		}catch (Exception e){}
		
		//Create Panels:
		JPanel back = new JPanel();
		
		
		//Add DP Graphic:
		myMatrix = new UCmatrixPanel(MyJavaUC);
		back.add(myMatrix);
		
		
		//Add Control Buttons:
		JPanel pflow = new JPanel();
		pflow.setLayout(new BoxLayout(pflow, BoxLayout.PAGE_AXIS));
		
		
		tcontrolgap = new JTextField(formatgap(MyJavaUC.dualgap),7);
		tcontrolgap.setMaximumSize(new Dimension(125,18));
		
		pflow.add(tcontrolgap);
		pflow.add(Box.createRigidArea(new Dimension(0,7)));
		
		Font f= new Font("SansSerif", Font.PLAIN, 9);
		bcontinue = new JButton("Continue");
		bcontinue.setBackground(Color.white);
		bcontinue.setFont(f);
		bcontinue.addActionListener(this);
		bcontinue.setActionCommand("Continue");
		bcontinue.setPreferredSize(new Dimension(75,18));
		
		pflow.add(bcontinue);
		back.add(pflow);
		
		//Preparing Window:
		getContentPane().add(back);
		pack();
		setLocation(200,50);
		setResizable(false);
		setVisible(true);
		
         */
    }

    public void keyPressed(KeyEvent event) {
    }

    public void keyReleased(KeyEvent event) {
    }

    public void keyTyped(KeyEvent event) {
    }

    public void itemStateChanged(ItemEvent e) {
    }

    public void actionPerformed(ActionEvent ev) {
        String actionlabel = ev.getActionCommand();
        if (actionlabel.equals("continue")) {

            setJavaUCattribute();
            myjavauc.iteratescuc();
            setstatussolution();
            iterrun = true;

            //Paint last solution found:
            myMatrix.updatebinary("last");
            myMatrix.repaint();

            //Set Result Labels:
            ltotalcost.setText(formatreport(myjavauc.primalfunction, 10));
            if (myjavauc.totaliteruc >= myjavauc.itermax) {
                liter.setForeground(Color.red);
            }
            liter.setText(formatfloat(myjavauc.totaliteruc));
            ldg.setText(formatfloat(myjavauc.dualgap));
            lcuts.setText(formatfloat(myjavauc.numcuts));
            lstatus.setText(sstatus[0]);
            lstatus.setForeground(getcolorstatus(sstatus[0]));

            ltimetotal.setText(formatfloat(myjavauc.timetotal));
            ltimedual.setText(formatfloat(myjavauc.timedual));
            ltimeprimal.setText(formatfloat(myjavauc.timeprimal));
            ltimealr.setText(formatfloat(myjavauc.timealr));
            ltimesubp.setText(formatfloat(myjavauc.timesubp));
            ltimedata.setText(formatfloat(myjavauc.timedata));

            breport.setEnabled(true);
            lreport.setEnabled(true);
            bcontin.setEnabled(false); //PROVISIONAL!!!

            //Set buttons enabled as needed:
            bopt.setEnabled(true);
            bopt.setForeground(Color.black);
            bopt.setBackground(Color.black);
            bbest.setEnabled(true);
            bbest.setForeground(Color.black);
            bprior.setBackground(oricolor);

        } else if (actionlabel.equals("report")) {
            if (firstreport) {
                myjavauc.makereportfile();
                //officialreport();
                firstreport = false;
            }

            try {
                Runtime r = Runtime.getRuntime();
                r.exec("notepad " + myjavauc.LogFilePath());
            } catch (Exception e) {
                String message = e.toString();
                System.out.println("File error: " + message);
            }
        } else if (actionlabel.equals("last")) {
            //Set Labels:
            ltotalcost.setText(formatreport(myjavauc.primalfunction, 10));
            lstatus.setText(sstatus[0]);
            ldg.setText(formatfloat(myjavauc.dualgap));
            //primalbest
            lstatus.setForeground(getcolorstatus(sstatus[0]));
            bopt.setBackground(Color.black);
            bbest.setBackground(oricolor);
            bprior.setBackground(oricolor);
            //Update Matrix:
            myMatrix.updatebinary("last");
            myMatrix.repaint();
        } else if (actionlabel.equals("best")) {
            lstatus.setText(sstatus[1]);
            lstatus.setForeground(getcolorstatus(sstatus[1]));
            ltotalcost.setText(formatreport(myjavauc.primalbest, 10));
            //liter.setText(sstatus[1]);
            ldg.setText(formatfloat(myjavauc.dgbest));
            bopt.setBackground(oricolor);
            bbest.setBackground(Color.black);
            bprior.setBackground(oricolor);
            myMatrix.updatebinary("best");
            myMatrix.repaint();
        } else if (actionlabel.equals("prior")) {
            if (iterrun) {
                lstatus.setText(sstatus[2]);
                lstatus.setForeground(getcolorstatus(sstatus[2]));
                bopt.setBackground(oricolor);
                bbest.setBackground(oricolor);
                bprior.setBackground(Color.black);
                myMatrix.updatebinary("prior");
                myMatrix.repaint();
            }
        } else if (actionlabel.equals("exit")) {
            setVisible(false);
            dispose();
        }

    }

    public JPanel respanel() {

        //Main Panel:
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

        //Title Panel:
        JPanel pintro = new JPanel();
        JLabel l1 = new JLabel(
                "<html><font face=SansSerif color=#686868 size=2><b>SIMULATION RESULTS</html>");
        pintro.add(l1);
        pintro.setBorder(bordeintro());

        //Data Panel:
        JPanel pdata = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        //add labels:
        c.anchor = GridBagConstraints.LINE_START;
        JLabel lcost = new JLabel("Total Cost: ");
        pdata.add(lcost, c);
        c.gridy = 1;
        JLabel l2 = new JLabel("Total Iterations: ");
        pdata.add(l2, c);
        c.gridy = 2;
        JLabel l3 = new JLabel("Total Cuts: ");
        pdata.add(l3, c);
        c.gridy = 3;
        JLabel l4 = new JLabel("Dual Gap: ");
        pdata.add(l4, c);
        c.gridy = 4;
        JLabel l5 = new JLabel("Status: ");
        pdata.add(l5, c);
        c.gridy = 5;
        c.gridwidth = 2;
        JLabel lwhite = new JLabel(" ");
        pdata.add(lwhite, c);
        c.gridy = 6;
        JLabel lt = new JLabel("  Execution Times: [ms]");
        pdata.add(lt, c);
        c.gridy = 7;
        c.gridwidth = 1;
        JLabel l6 = new JLabel("Total: ");
        pdata.add(l6, c);
        c.gridy = 8;
        JLabel l7 = new JLabel("Dual: ");
        pdata.add(l7, c);
        c.gridy = 9;
        JLabel l8 = new JLabel("Primal: ");
        pdata.add(l8, c);
        c.gridy = 10;
        JLabel l9 = new JLabel("Lagrangian Relax.: ");
        pdata.add(l9, c);
        c.gridy = 11;
        JLabel l10 = new JLabel("Trans. Sub-problem: ");
        pdata.add(l10, c);
        c.gridy = 12;
        JLabel l11 = new JLabel("Data Read: ");
        pdata.add(l11, c);

        //add fields:
        ltotalcost = formatreslabel(formatfloat((float) myjavauc.primalfunction));
        liter = formatreslabel(formatfloat((float) myjavauc.totaliteruc));
        ldg = formatreslabel(formatfloat(myjavauc.dualgap));
        lstatus = formatreslabel("INITIAL");
        lcuts = formatreslabel(formatfloat((float) myjavauc.numcuts));
        ltimetotal = formatreslabel(formatfloat((float) myjavauc.timetotal));
        ltimedual = formatreslabel(formatfloat((float) myjavauc.timedual));
        ltimeprimal = formatreslabel(formatfloat((float) myjavauc.timeprimal));
        ltimealr = formatreslabel(formatfloat((float) myjavauc.timealr));
        ltimesubp = formatreslabel(formatfloat((float) myjavauc.timesubp));
        ltimedata = formatreslabel(formatfloat((float) myjavauc.timedata));
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 1;
        c.gridy = 0;
        pdata.add(ltotalcost, c);
        c.gridy = 1;
        pdata.add(liter, c);
        c.gridy = 2;
        pdata.add(lcuts, c);
        c.gridy = 3;
        pdata.add(ldg, c);
        c.gridy = 4;
        pdata.add(lstatus, c);
        c.gridy = 7;
        pdata.add(ltimetotal, c);
        c.gridy = 8;
        pdata.add(ltimedual, c);
        c.gridy = 9;
        pdata.add(ltimeprimal, c);
        c.gridy = 10;
        pdata.add(ltimealr, c);
        c.gridy = 11;
        pdata.add(ltimesubp, c);
        c.gridy = 12;
        pdata.add(ltimedata, c);

        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 14;
        pdata.add(new JLabel(" "), c);
        c.gridy = 15;
        c.anchor = GridBagConstraints.CENTER;

        JPanel auxpane = new JPanel();

        Font f = new Font("SansSerif", Font.PLAIN, 9);
        ImageIcon reporticon = new ImageIcon(this.getClass().getResource("/cl/flconsulting/ucopt/resources/report.png"));
        breport = new JButton("", reporticon);
        //bcontinue.setBackground(Color.white);
        breport.setFont(f);
        breport.addActionListener(this);
        breport.setActionCommand("report");
        //breport.setPreferredSize(new Dimension((int)(rightWidth*0.8),25));
        breport.setPreferredSize(new Dimension(14, 20));
        breport.setEnabled(false);
        lreport = new JLabel("Show Report:  ");
        lreport.setEnabled(false);
        auxpane.add(lreport);
        auxpane.add(breport);
        Border leb = BorderFactory.createLineBorder(Color.gray, 1);
        auxpane.setBorder(leb);

        pdata.add(auxpane, c);

        //Main panel addition and border:
        pdata.setBorder(bordedata("Simulation Results:"));
        pane.add(pintro, BorderLayout.NORTH);
        pane.add(pdata, BorderLayout.CENTER);
        pane.setVisible(false);
        return pane;

    }

    public JPanel parameterpanel() {

        //Prepare Textfields:
        tsubalfa = new JTextField(formatfloat((float) myjavauc.subalfa), 5);
        tsubbeta = new JTextField(formatfloat((float) myjavauc.subbeta), 5);
        tc_coef = new JTextField(formatfloat((float) myjavauc.c_coef), 5);
        tepsaug = new JTextField(formatfloat((float) myjavauc.epsaug), 5);
        titermax = new JTextField(Long.toString(myjavauc.itermax), 5);
        ttoldg = new JTextField(formatfloat((float) myjavauc.tolset), 5);
        ttolb = new JTextField(formatfloat((float) myjavauc.tol_b), 5);

        //Main Panel:
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

        //Title Panel:
        JPanel pintro = new JPanel();
        JLabel l1 = new JLabel(
                "<html><font face=SansSerif color=#686868 size=2><b>ALGORITHM PARAMETERS</html>");
        pintro.add(l1);
        pintro.setBorder(bordeintro());

        //Data Panel:
        JPanel pdata = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        //add labels:
        c.anchor = GridBagConstraints.LINE_START;
        c.gridwidth = 2;
        JLabel llr = new JLabel("  LR Subgradient Parameters: ");
        llr.setForeground(new Color(0x686868));
        pdata.add(llr, c);
        c.gridy = 1;
        c.gridwidth = 1;
        JLabel l2 = new JLabel("Subgradient alpha: ");
        pdata.add(l2, c);
        c.gridy = 2;
        JLabel l3 = new JLabel("Subgradient beta: ");
        pdata.add(l3, c);
        c.gridy = 3;
        c.gridwidth = 2;
        JLabel lw2 = new JLabel(" ");
        pdata.add(lw2, c);
        c.gridy = 4;
        JLabel lalr = new JLabel("  Augmented LR Parameters:");
        lalr.setForeground(new Color(0x686868));
        pdata.add(lalr, c);
        c.gridy = 5;
        c.gridwidth = 1;
        JLabel l4 = new JLabel("C-Coeficient: ");
        pdata.add(l4, c);
        c.gridy = 6;
        JLabel l5 = new JLabel("Eo:");
        pdata.add(l5, c);
        c.gridy = 7;
        c.gridwidth = 2;
        JLabel lw3 = new JLabel(" ");
        pdata.add(lw3, c);
        c.gridy = 8;
        JLabel l6 = new JLabel("  LR General Parameters:");
        l6.setForeground(new Color(0x686868));
        pdata.add(l6, c);
        c.gridy = 9;
        c.gridwidth = 1;
        JLabel l7 = new JLabel("Max Iterations: ");
        pdata.add(l7, c);
        c.gridy = 10;
        JLabel l8 = new JLabel("Dual-Gap Tol: ");
        pdata.add(l8, c);
        c.gridy = 11;
        JLabel l9 = new JLabel("RHS Gap Tol: ");
        pdata.add(l9, c);
        c.gridy = 12;
        JLabel l10 = new JLabel("Prior Obj: ");
        pdata.add(l10, c);
        //add fields:
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 1;
        c.gridy = 1;
        pdata.add(tsubalfa, c);
        c.gridy = 2;
        pdata.add(tsubbeta, c);
        c.gridy = 5;
        pdata.add(tc_coef, c);
        c.gridy = 6;
        pdata.add(tepsaug, c);
        c.gridy = 9;
        pdata.add(titermax, c);
        c.gridy = 10;
        pdata.add(ttoldg, c);
        c.gridy = 11;
        pdata.add(ttolb, c);

        c.gridy = 12;
        String[] prioroption = {"optimal", "feasible"};
        cprior = new JComboBox<String>(prioroption);
        cprior.setSelectedItem(myjavauc.priorsearch);
        pdata.add(cprior, c);

        //Main panel addition and border:
        pdata.setBorder(bordedata("LR Tuning Parameters:"));
        pane.add(pintro, BorderLayout.NORTH);
        pane.add(pdata, BorderLayout.CENTER);
        pane.setVisible(false);
        return pane;

    }

    public JPanel legendpanel() {

        //Main Panel:
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

        //Title Panel:
        JPanel pintro = new JPanel();
        JLabel l1 = new JLabel(
                "<html><font face=SansSerif color=#686868 size=2><b>VISUAL COLOR DEFINITION:</html>");
        pintro.add(l1);
        pintro.setBorder(bordeintro());

        //Data Panel:
        UClegendPanel pdata = new UClegendPanel();

        //Main panel addition and border:
        pane.add(pintro, BorderLayout.NORTH);
        pane.add(pdata, BorderLayout.CENTER);
        pane.setVisible(false);
        return pane;

    }

    //Intro Panel Border:
    public Border bordeintro() {
        Border leb = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        Border eb = BorderFactory.createEmptyBorder(4, 10, 2, 10);
        return new CompoundBorder(leb, eb);
    }

    //Data Panel Border:
    public Border bordedata(String s) {
        Border leb = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder tb = BorderFactory.createTitledBorder(leb, s);
        return tb;
    }

    //Float Formatter (5 decimal digits):
    public String formatfloat(float dg) {
        Float gap = new Float(dg);
        String amountOut;
        NumberFormat numberFormatter = NumberFormat.getInstance(Locale.ENGLISH);
        numberFormatter.setMaximumFractionDigits(5);
        amountOut = numberFormatter.format(gap);
        return amountOut;
    }

    //Result Labels formatter:
    public JLabel formatreslabel(String orilabel) {
        JLabel lback = new JLabel(
                "<html><font face=SansSerif color=#000000 size=3><b>" + orilabel + "</html>");
        return lback;
    }

    //Status of the current solution:
    public void setstatussolution() {
        sstatus = new String[3];
        if (myjavauc.ucconvergence && myjavauc.txconvergence) {
            sstatus[0] = "OPTIMAL";
            sstatus[1] = "OPTIMAL";
        } else {
            if (myjavauc.onefeasible) {
                sstatus[1] = "FEASIBLE";
                if (myjavauc.lastfeasible) {
                    sstatus[0] = "NON-OPTIMAL";
                } else {
                    sstatus[0] = "INFEASIBLE";
                }
            } else {
                sstatus[0] = "INFEASIBLE";
                sstatus[1] = "INFEASIBLE";
            }
        }
        sstatus[2] = "INITIAL";
    }

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

    public Color getcolorstatus(String sresstatus) {
        if (sresstatus.equals("OPTIMAL")) {
            return Color.blue;
        } else if (sresstatus.equals("FEASIBLE") || sresstatus.equals("NON-OPTIMAL")
                || sresstatus.equals("INITIAL")) {
            return Color.black;
        } else {
            return Color.red;
        }
    }

    //Official Report Creation:
    public boolean officialreport() {
        int ii, tt, cont = 0;
        FileOutputStream os;
        BufferedOutputStream bos;
        PrintStream reportline;

        try {
            os = new FileOutputStream("UCcomplete.txt", true);
            reportline = new PrintStream(os);

            //1--Report Simulations Results:
            reportline.println("=======UNIT COMMITMENT REPORT FILE========");
            reportline.println(" ");
            if (myjavauc.usequadra && myjavauc.useaugmen) {
                reportline.println("  Non-Linear Augmented Lagrange Relaxation Methodology");
            } else {
                reportline.println("  Linear Lagrange Relaxation Methodology");
            }
            reportline.println(" ");
            reportline.println("  Total Cost              : " + formatreport(myjavauc.primalbest, 10));
            reportline.println("  Final Solution          : " + sstatus[1]);
            reportline.println("  Solution Quality        : " + myjavauc.solquality);
            reportline.println("  External Optimizer      : " + myjavauc.optimizer);
            if (myjavauc.usequadra) {
                reportline.println("  Quadratic Convex Optimization");
            } else {
                if (myjavauc.optimizer.equals("glpk")) {
                    reportline.println("  LP External Method      : " + myjavauc.glpklpmethod);
                } else {
                    reportline.println("  LP External Method      : simplex (minos 5.0)");
                }
            }
            reportline.println("  Iterations              : " + myjavauc.totaliteruc);
            reportline.println("  Execution Time (mseg)   : " + myjavauc.timetotal);
            reportline.println("  Dual Solving Time (mseg): " + myjavauc.timedual);
            reportline.println("  Cuts Created            : " + myjavauc.numcuts);
            reportline.println(" ");
            reportline.println(" ");

            reportline.println(printdivline(numperiods * 2 + 35));

            //2--Determine characters of the largest unit name:
            int longestname = 0;
            for (ii = 0; ii < numunits; ii++) {
                int longtemp = myjavauc.Unitdata.readname[ii].length();
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
                        if (myjavauc.ubest[ii - 2][tt] == 1) {
                            auxchar[cont + 1] = '1';
                        } else {
                            auxchar[cont + 1] = '0';
                        }
                        cont += 2;
                    }
                    String wtmp = "                                                    ";
                    String unitnametemp = myMatrix.formatlabel(wtmp + myjavauc.Unitdata.readname[ii - 2], longestname);
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
                    String pout = formatreport(myjavauc.pbest[ii][tt], 5);
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
                String unitnametemp = myMatrix.formatlabel("          " + myjavauc.Unitdata.readname[ii], 10);
                reportline.println(unitnametemp + "     " + plineout);
            }
            reportline.println(printdivline(numperiods * 8 + 25));

            //4--Print energy demands and reserves:
            String plineout = " ";
            for (tt = 0; tt < numperiods; tt++) {
                cont = 0;
                String pout = formatreport(myjavauc.hourlypdem[tt], 5);
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
                    totalres += myjavauc.pmax[ii][tt] * myjavauc.ubest[ii][tt];
                }
                totalres = (totalres - myjavauc.hourlypdem[tt]) * 100 / (myjavauc.hourlypdem[tt]);
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
            os.close();
            return true;
        } catch (Exception e) {
            System.out.println("File report error: " + e);
            return false;
        }
    }

    //Update Attributes in JavaUC object:
    public void setJavaUCattribute() {

        myjavauc.subalfa = Float.valueOf(myjavauc.getstringcomp(tsubalfa)).floatValue();
        myjavauc.subbeta = Float.valueOf(myjavauc.getstringcomp(tsubbeta)).floatValue();
        myjavauc.c_coef = Float.valueOf(myjavauc.getstringcomp(tc_coef)).floatValue();
        myjavauc.epsaug = Float.valueOf(myjavauc.getstringcomp(tepsaug)).floatValue();
        myjavauc.tolset = Float.valueOf(myjavauc.getstringcomp(ttoldg)).floatValue();
        myjavauc.itermax = Long.valueOf(myjavauc.getstringcomp(titermax)).longValue();
        myjavauc.tol_dg = Float.valueOf(myjavauc.getstringcomp(ttoldg)).floatValue();
        myjavauc.tol_b = Float.valueOf(myjavauc.getstringcomp(ttolb)).floatValue();
        myjavauc.priorsearch = myjavauc.getstringcomp(cprior);

    }

}//end class
