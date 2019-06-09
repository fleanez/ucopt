package cl.flconsulting.ucopt;

//****************************************************************************
//Unit-Commitment Option Frame
//Initially Created: 1/11/2005 - Frank Leanez
//****************************************************************************
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class UCtreeoptionFrame extends JFrame implements ActionListener, TreeSelectionListener {

//------------------
//Class Attributes:
//------------------
    //Frame Dimensions:
    static final int windowHeight = 350;
    static final int leftWidth = 150;
    static final int rightWidth = 320;
    static final int windowWidth = leftWidth + rightWidth;
    JTree treeuc;
    JPanel rightpanel;
    XmlTreeMaker myucoption;

    JavaUC myjavauc;
    JavaUCFrame ucMainFrame;

    //GUI attributtes:
    JButton bglpkdir;
    JButton bminosdir;
    JButton bapply;
    JButton bcancel;
    JButton brestdef;
    JButton bsetdet;
    JButton bok;

    JCheckBox cusefitto;
    JCheckBox cuserepdetail;
    JCheckBox cusepiecewise;
    JCheckBox cuseheuristic;
    JCheckBox cuseaugmen;
    JCheckBox cuseprintlp;

    JComboBox cdbtype;
    JComboBox cglpklpmethod;
    JComboBox cglpkpiftype;
    JComboBox copdef;

    JTextField tinstalldir;
    JTextField tmaxunits;
    JTextField tperiods;
    JTextField titermax;
    JTextField titertime;
    JTextField ttolobj;
    JTextField ttoldg;
    JTextField ttolb;
    JTextField tdbname;
    JTextField tdbloc;
    JTextField tdblog;
    JTextField tglpkdir;
    JTextField tglpkpifname;
    JTextField tglpkdosbat;
    JTextField tglpkpofname;
    JTextField tminosdir;
    JTextField tminoslineabat;
    JTextField tminosquadrabat;
    JTextField tsubalfa;
    JTextField tsubbeta;
    JTextField tccoef;
    JTextField tepsaug;
    JTextField twidth;
    JTextField theight;
    JTextField tsquare;
    JTextField tdbtxt;

    String[] chardatatemp; //String data introduced through JComponents

//------------------
//Frame Constructor:
//------------------
    public UCtreeoptionFrame(JavaUCFrame jucframe) {

        //1--Call Super and create panel:
        super("UC--Advanced");
        JPanel puctree = new JPanel();
        //myjavauc = juc;
        myjavauc = jucframe.MyJavaUC;
        ucMainFrame = jucframe;

        //2--Add a nice border:
        EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
        CompoundBorder cb = new CompoundBorder(eb, bb);
        puctree.setBorder(new CompoundBorder(cb, eb));

        //3--Create Tree Nodes:
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("UC Options");
        createNodes(top);

        //4--Create Tree from Nodes:
        treeuc = new JTree(top);
        treeuc.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        treeuc.addTreeSelectionListener(this);

        //5--Set Display Icons and Fonts:
        ImageIcon optionicon = new ImageIcon(this.getClass().getResource("/cl/flconsulting/ucopt/resources/bop.png"));
        ImageIcon optionfopen = new ImageIcon(this.getClass().getResource("/cl/flconsulting/ucopt/resources/bfopen.png"));
        ImageIcon optionfclose = new ImageIcon(this.getClass().getResource("/cl/flconsulting/ucopt/resources/bfclose.png"));
        DefaultTreeCellRenderer rendleaf = new DefaultTreeCellRenderer();
        rendleaf.setLeafIcon(optionicon);
        rendleaf.setOpenIcon(optionfopen);
        rendleaf.setClosedIcon(optionfclose);
        Font f = new Font("SansSerif", Font.PLAIN, 11);
        rendleaf.setFont(f);
        treeuc.setCellRenderer(rendleaf);

        //6--Build left-side panel:
        JScrollPane treeView = new JScrollPane(treeuc);
        treeView.setPreferredSize(new Dimension(leftWidth, windowHeight));

        //7--Build right-side panel:
        rightpanel = new JPanel(new CardLayout());
        rightpanel.add(defaultpanel(), "default");
        rightpanel.add(generalpanel(), "general");
        rightpanel.add(dbpanel(), "database");
        rightpanel.add(glpkpanel(), "glpk");
        rightpanel.add(minospanel(), "minos");
        rightpanel.add(lrpanel(), "lrtunning");
        rightpanel.add(genoptpanel(), "generalopt");
        rightpanel.add(displaypanel(), "display");

        //8--Build split-pane view:
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeView, rightpanel);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(leftWidth);
        splitPane.setPreferredSize(new Dimension(windowWidth + 10, windowHeight + 10));

        //9--Add Initial Panels:
        puctree.setLayout(new BorderLayout());
        puctree.add(splitPane, BorderLayout.CENTER);
        puctree.add(okPane(), BorderLayout.SOUTH);
        getContentPane().add("Center", puctree);
        pack();

        //10--Prepare Frame:
        addWindowListener(
                new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = windowWidth + 10;
        int h = windowHeight + 10;
        setSize(w, h);
        setLocation(screenSize.width / 3 - w / 2, screenSize.height / 2 - h / 2);
        setVisible(true);

    }

    //Nodes for tree directly from XMLtree object
    private void createNodes(DefaultMutableTreeNode top) {

        myucoption = myjavauc.DataoptionUC;
        int numelements = myucoption.numelements();
        int numdisplay = myucoption.numdisplayelements();
        int contdisplay = 0;
        DefaultMutableTreeNode node[] = new DefaultMutableTreeNode[numelements + 1];
        for (int kk = 0; kk < numelements + 1; kk++) {
            String ename = myucoption.treedata[kk].getelementname();
            String dname = myucoption.treedata[kk].getdisplayname();
            String cdata = myucoption.treedata[kk].getchardata();
            int owni = myucoption.treedata[kk].getownindex();
            node[kk] = new DefaultMutableTreeNode(myucoption.treedata[kk]);
            if (kk == 0) {
            } else {
                if (myucoption.treedata[kk].isdisplayable()) {
                    int parenti = myucoption.treedata[kk].getparentindex();
                    if (parenti == 0) {
                        top.add(node[kk]);
                    } else {
                        node[parenti].add(node[kk]);
                    }
                }
            }
        }

    }

    //Handle Tree Actions:
    public void valueChanged(TreeSelectionEvent e) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeuc.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }
        if (node.isRoot()) {
            CardLayout cl = (CardLayout) (rightpanel.getLayout());
            cl.show(rightpanel, "default");
            return;
        }

        Object nodeinfo = node.getUserObject();
        TreeXml t = (TreeXml) nodeinfo;
        CardLayout cl = (CardLayout) (rightpanel.getLayout());
        if (node.isLeaf()) {
            int owni = t.getownindex();
            int nexti;
            DefaultMutableTreeNode nextn = node.getNextNode();
            if (nextn != null) {
                Object nextnodeinfo = nextn.getUserObject();
                TreeXml nextt = (TreeXml) nextnodeinfo;
                nexti = nextt.getownindex();
            } else {
                nexti = myucoption.numelements() + 1;
            }
            //for (int kk=owni+1; kk<nexti;kk++){
            //System.out.println("nnn:"+myucoption.treedata[kk].getelementname());
            //}
            cl.show(rightpanel, t.getelementname());
        } else {
            //cl.show(rightpanel, "default");
        }

    }

//------------------
//Panels Construtors
//------------------
    //Default Panel:
    public JPanel defaultpanel() {

        //Panel Principal:
        JPanel pane = new JPanel(new GridBagLayout());
        pane.setPreferredSize(new Dimension(rightWidth, windowHeight));
        pane.setBackground(Color.WHITE);

        //Panel de Titulo:
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        JLabel l1 = new JLabel("Unit Commitment Options");
        pane.add(l1, c);
        c.gridy = 1;
        JLabel l2 = new JLabel("(Left-click a component to edit)");
        pane.add(l2, c);
        pane.setVisible(true);
        return pane;

    }

    //General Options Panel:
    public JPanel generalpanel() {

        //Main Panel:
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

        //Panel de Titulo:
        JPanel pintro = new JPanel();
        JLabel l1 = new JLabel(
                "<html><font face=SansSerif color=#686868 size=2><b>GENERAL UNIT COMMITMENT SETTINGS</html>");
        pintro.add(l1);
        pintro.setBorder(bordeintro());

        //Panel de Datos:
        JPanel pdata = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        JLabel l2 = new JLabel("Maximun Units Allowed: ");
        pdata.add(l2, c);
        c.gridx = 1;
        tmaxunits = new JTextField(Short.toString(myjavauc.numaxunits), 5);
        pdata.add(tmaxunits, c);
        c.gridy = 1;
        tperiods = new JTextField(Short.toString(myjavauc.numperiods), 5);
        pdata.add(tperiods, c);
        c.gridx = 0;
        JLabel l3 = new JLabel("Max Time Periods: ");
        pdata.add(l3, c);
        c.gridx = 0;
        c.gridy = 2;
        JLabel l4 = new JLabel("Max Iterations: ");
        pdata.add(l4, c);
        c.gridx = 1;
        c.gridy = 2;
        titermax = new JTextField(Long.toString(myjavauc.itermax), 5);
        pdata.add(titermax, c);
        c.gridx = 0;
        c.gridy = 3;
        JLabel l6 = new JLabel("Dual Gap Tolerance: ");
        pdata.add(l6, c);
        c.gridx = 1;
        c.gridy = 3;
        ttoldg = new JTextField(Float.toString(myjavauc.tol_dg), 5);
        pdata.add(ttoldg, c);
        c.gridx = 0;
        c.gridy = 4;
        JLabel l5 = new JLabel("Objective Function Tolerance: ");
        pdata.add(l5, c);
        c.gridx = 1;
        c.gridy = 4;
        ttolobj = new JTextField(Float.toString(myjavauc.tol_obj), 5);
        pdata.add(ttolobj, c);
        c.gridx = 0;
        c.gridy = 5;
        JLabel l7 = new JLabel("RHS Tolerance: ");
        pdata.add(l7, c);
        c.gridx = 1;
        c.gridy = 5;
        ttolb = new JTextField(Float.toString(myjavauc.tol_b), 5);
        pdata.add(ttolb, c);
        c.gridx = 0;
        c.gridy = 6;
        JLabel l8 = new JLabel("Max. Execution Time: ");
        pdata.add(l8, c);
        c.gridx = 1;
        c.gridy = 6;
        titertime = new JTextField(Long.toString(myjavauc.itertime), 5);
        pdata.add(titertime, c);

        pdata.setBorder(bordedata("General options"));

        //Main panel addition:
        pane.add(pintro, BorderLayout.NORTH);
        pane.add(pdata, BorderLayout.CENTER);
        pane.setVisible(false);
        return pane;

    }

    //DB Options Panel:
    public JPanel dbpanel() {

        //Main Panel:
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

        //Panel de Titulo:
        JPanel pintro = new JPanel();
        JLabel l1 = new JLabel(
                "<html><font face=SansSerif color=#686868 size=2><b>DATA BASE DEFAULT SETTINGS</html>");
        pintro.add(l1);
        pintro.setBorder(bordeintro());

        //Panel de Datos:
        JPanel pdata = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        //add labels:
        c.anchor = GridBagConstraints.LINE_START;
        JLabel l2 = new JLabel("Default DB Name: ");
        pdata.add(l2, c);
        c.gridy = 1;
        JLabel l3 = new JLabel("Default DB Vendor: ");
        pdata.add(l3, c);
        c.gridy = 2;
        JLabel l4 = new JLabel("Default DB Location: ");
        pdata.add(l4, c);
        c.gridy = 3;
        JLabel l5 = new JLabel("Default Text-File Directory: ");
        pdata.add(l5, c);
        c.gridy = 4;
        pdata.add(new JLabel(" "), c);
        //add fields:
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 1;
        c.gridy = 0;
        tdbname = new JTextField(myjavauc.dbname, 7);
        pdata.add(tdbname, c);
        c.gridy = 1;
        String[] dbvendor = {"odbc", "mysql"};
        cdbtype = new JComboBox<String>(dbvendor);
        cdbtype.setSelectedItem(myjavauc.dbtype);
        pdata.add(cdbtype, c);
        c.gridy = 2;
        tdbloc = new JTextField(myjavauc.dbloc, 7);
        c.gridy = 3;
        tdbtxt = new JTextField(myjavauc.txtfiledir, 7);
        pdata.add(tdbtxt, c);
        pdata.setBorder(bordedata("DB options"));

        //Main panel addition:
        pane.add(pintro, BorderLayout.NORTH);
        pane.add(pdata, BorderLayout.CENTER);
        pane.setVisible(false);
        return pane;

    }

    //General Optimizer Options Pane:
    public JPanel genoptpanel() {

        //Main Panel:
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

        //Title Panel:
        JPanel pintro = new JPanel();
        JLabel l1 = new JLabel(
                "<html><font face=SansSerif color=#686868 size=2><b>GENERAL EXTERNAL OPTIMIZER SETTINGS</html>");
        pintro.add(l1);
        pintro.setBorder(bordeintro());

        //Data Panel:
        JPanel pdata = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        //add labels:
        c.anchor = GridBagConstraints.LINE_START;
        JLabel l2 = new JLabel("Default Engine: ");
        pdata.add(l2, c);
        c.gridy = 1;
        JLabel l3 = new JLabel("Piece-wise Linear: ");
        pdata.add(l3, c);
        c.gridy = 2;
        JLabel l4 = new JLabel("Heuristic Dual Aprox: ");
        pdata.add(l4, c);
        c.gridy = 3;
        JLabel l5 = new JLabel("Augmented Lagrangean: ");
        pdata.add(l5, c);
        c.gridy = 4;
        JLabel l6 = new JLabel("Print LP Message: ");
        pdata.add(l6, c);

        //add fields:
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 1;
        c.gridy = 0;
        String[] sdefopt = {"glpk", "minos", "lpsolve"};
        copdef = new JComboBox<String>(sdefopt);
        copdef.setSelectedItem(myjavauc.optimizer);
        pdata.add(copdef, c);
        c.gridy = 1;
        cusepiecewise = new JCheckBox();
        if (myjavauc.usepiecewise) {
            cusepiecewise.setSelected(true);
        } else {
            cusepiecewise.setSelected(false);
        }
        pdata.add(cusepiecewise, c);
        c.gridy = 2;
        cuseheuristic = new JCheckBox();
        if (myjavauc.useheuristic) {
            cuseheuristic.setSelected(true);
        } else {
            cuseheuristic.setSelected(false);
        }
        pdata.add(cuseheuristic, c);
        c.gridy = 3;
        cuseaugmen = new JCheckBox();
        if (myjavauc.useaugmen) {
            cuseaugmen.setSelected(true);
        } else {
            cuseaugmen.setSelected(false);
        }
        pdata.add(cuseaugmen, c);
        c.gridy = 4;
        cuseprintlp = new JCheckBox();
        if (myjavauc.printlp) {
            cuseprintlp.setSelected(true);
        } else {
            cuseprintlp.setSelected(false);
        }
        pdata.add(cuseprintlp, c);

        pdata.setBorder(bordedata("Optimizer defaults:"));

        //Main panel addition:
        pane.add(pintro, BorderLayout.NORTH);
        pane.add(pdata, BorderLayout.CENTER);
        pane.setVisible(false);
        return pane;

    }

    //GLPK Options Panel:
    public JPanel glpkpanel() {

        //Main Panel:
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

        //Panel de Titulo:
        JPanel pintro = new JPanel();
        JLabel l1 = new JLabel(
                "<html><font face=SansSerif color=#686868 size=2><b>GLPK OPTIMIZATION ENGINE DEFAULTS</html>");
        pintro.add(l1);
        pintro.setBorder(bordeintro());

        //Panel de Datos:
        JPanel pdata = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        //add labels:
        c.anchor = GridBagConstraints.LINE_START;
        JLabel l2 = new JLabel("GLPK Installation Directory: ");
        pdata.add(l2, c);
        c.gridy = 2;
        JLabel l3 = new JLabel("LP Method: ");
        pdata.add(l3, c);
        c.gridy = 3;
        JLabel l4 = new JLabel("Input File Format: ");
        pdata.add(l4, c);
        c.gridy = 4;
        JLabel l5 = new JLabel("Input File Name: ");
        pdata.add(l5, c);
        c.gridy = 5;
        JLabel l6 = new JLabel("DOS-Batch File Interface: ");
        pdata.add(l6, c);
        c.gridy = 6;
        JLabel l7 = new JLabel("Output File Name: ");
        pdata.add(l7, c);
        //add fields:
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 1;
        c.gridy = 1;
        bglpkdir = new JButton(" Browse ");
        bglpkdir.setActionCommand("glpkdir");
        bglpkdir.addActionListener(this);
        pdata.add(bglpkdir, c);
        c.gridy = 0;
        tglpkdir = new JTextField(myjavauc.glpkdir, 10);
        pdata.add(tglpkdir, c);
        c.gridy = 2;
        String[] lpmethod = {"simplex", "interior point"};
        cglpklpmethod = new JComboBox<String>(lpmethod);
        cglpklpmethod.setSelectedItem(myjavauc.glpklpmethod);
        pdata.add(cglpklpmethod, c);
        c.gridy = 3;
        String[] piftype = {"freemps", "mps"};
        cglpkpiftype = new JComboBox<String>(piftype);
        pdata.add(cglpkpiftype, c);
        c.gridy = 4;
        tglpkpifname = new JTextField(myjavauc.glpkpifname, 8);
        pdata.add(tglpkpifname, c);
        c.gridy = 5;
        tglpkdosbat = new JTextField(myjavauc.glpkdosbat, 8);
        pdata.add(tglpkdosbat, c);
        c.gridy = 6;
        tglpkpofname = new JTextField(myjavauc.glpkpofname, 8);
        pdata.add(tglpkpofname, c);
        pdata.setBorder(bordedata("GLPK options"));

        //Main panel addition:
        pane.add(pintro, BorderLayout.NORTH);
        pane.add(pdata, BorderLayout.CENTER);
        pane.setVisible(false);
        return pane;

    }

    //MINOS Options Panel:
    public JPanel minospanel() {

        //Main Panel:
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

        //Panel de Titulo:
        JPanel pintro = new JPanel();
        JLabel l1 = new JLabel(
                "<html><font face=SansSerif color=#686868 size=2><b>MINOS OPTIMIZATION ENGINE DEFAULTS</html>");
        pintro.add(l1);
        pintro.setBorder(bordeintro());

        //Panel de Datos:
        JPanel pdata = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        //add labels:
        c.anchor = GridBagConstraints.LINE_START;
        JLabel l2 = new JLabel("MINOS Installation Directory: ");
        pdata.add(l2, c);
        c.gridy = 2;
        JLabel l3 = new JLabel("Linear DOS-Batch File Interface: ");
        pdata.add(l3, c);
        c.gridy = 3;
        JLabel l4 = new JLabel("Quadratic DOS-Batch File Interface: ");
        pdata.add(l4, c);
        //add fields:
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 1;
        c.gridy = 1;
        bminosdir = new JButton(" Browse ");
        bminosdir.setActionCommand("minosdir");
        bminosdir.addActionListener(this);
        pdata.add(bminosdir, c);
        c.gridy = 0;
        tminosdir = new JTextField(myjavauc.minosdir, 8);
        pdata.add(tminosdir, c);
        c.gridy = 2;
        tminoslineabat = new JTextField(myjavauc.minoslineabat, 8);
        pdata.add(tminoslineabat, c);
        c.gridy = 3;
        tminosquadrabat = new JTextField(myjavauc.minosquadrabat, 8);
        pdata.add(tminosquadrabat, c);
        pdata.setBorder(bordedata("MINOS options"));

        //Main panel addition:
        pane.add(pintro, BorderLayout.NORTH);
        pane.add(pdata, BorderLayout.CENTER);
        pane.setVisible(false);
        return pane;

    }

    //Display options Panel:
    public JPanel displaypanel() {

        //Main Panel:
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

        //Panel de Titulo:
        JPanel pintro = new JPanel();
        JLabel l1 = new JLabel(
                "<html><font face=SansSerif color=#686868 size=2><b>RESULTS DISPLAY OPTIONS</html>");
        pintro.add(l1);
        pintro.setBorder(bordeintro());

        //Panel de Datos:
        JPanel pdata = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        //add labels:
        c.anchor = GridBagConstraints.LINE_START;
        JLabel l2 = new JLabel("Result Window Dimension: ");
        pdata.add(l2, c);
        c.gridy = 1;
        JLabel l3 = new JLabel("    Weigth: ");
        pdata.add(l3, c);
        c.gridy = 2;
        JLabel l4 = new JLabel("    Heigth: ");
        pdata.add(l4, c);
        c.gridy = 3;
        JLabel l5 = new JLabel("Fit to Window? ");
        pdata.add(l5, c);
        c.gridy = 4;
        JLabel l6 = new JLabel("Individual Square Size: ");
        pdata.add(l6, c);
        c.gridy = 5;
        JLabel l7 = new JLabel("Report Details? ");
        pdata.add(l7, c);

        //add fields:
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 1;
        c.gridy = 1;
        twidth = new JTextField(Integer.toString(myjavauc.dispwidth), 3);
        pdata.add(twidth, c);
        c.gridy = 2;
        theight = new JTextField(Integer.toString(myjavauc.dispheight), 3);
        pdata.add(theight, c);
        c.gridy = 3;
        cusefitto = new JCheckBox();
        if (myjavauc.usefitto) {
            cusefitto.setSelected(true);
        } else {
            cusefitto.setSelected(false);
        }
        pdata.add(cusefitto, c);
        c.gridy = 4;
        tsquare = new JTextField(Integer.toString(myjavauc.dispsquare), 3);
        pdata.add(tsquare, c);
        c.gridy = 5;
        cuserepdetail = new JCheckBox();
        if (myjavauc.userepdetail) {
            cuserepdetail.setSelected(true);
        } else {
            cuserepdetail.setSelected(false);
        }
        pdata.add(cuserepdetail, c);
        pdata.setBorder(bordedata("Display options"));

        //Main panel addition:
        pane.add(pintro, BorderLayout.NORTH);
        pane.add(pdata, BorderLayout.CENTER);
        pane.setVisible(false);
        return pane;

    }

    //LR Options Panel:
    public JPanel lrpanel() {

        //Main Panel:
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout());

        //Title Panel:
        JPanel pintro = new JPanel();
        JLabel l1 = new JLabel(
                "<html><font face=SansSerif color=#686868 size=2><b>LAGRANGE RELAXATION TUNNING PARAMETERS</html>");
        pintro.add(l1);
        pintro.setBorder(bordeintro());

        //Data Panel:
        JPanel pdata = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        //add labels:
        c.anchor = GridBagConstraints.LINE_START;
        JLabel l2 = new JLabel("alfa coef: ");
        pdata.add(l2, c);
        c.gridy = 1;
        JLabel l3 = new JLabel("beta coef: ");
        pdata.add(l3, c);
        c.gridy = 2;
        JLabel l4 = new JLabel("Augmented c-coef: ");
        pdata.add(l4, c);
        c.gridy = 3;
        JLabel l5 = new JLabel("Augmented epsilon: ");
        pdata.add(l5, c);
        //add fields:
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 1;
        c.gridy = 0;
        tsubalfa = new JTextField(Float.toString(myjavauc.subalfa), 5);
        pdata.add(tsubalfa, c);
        c.gridy = 1;
        tsubbeta = new JTextField(Float.toString(myjavauc.subbeta), 5);
        pdata.add(tsubbeta, c);
        c.gridy = 2;
        tccoef = new JTextField(Float.toString(myjavauc.c_coef), 5);
        pdata.add(tccoef, c);
        c.gridy = 3;
        tepsaug = new JTextField(Float.toString(myjavauc.epsaug), 5);
        pdata.add(tepsaug, c);
        pdata.setBorder(bordedata("LR options"));

        //Main panel addition:
        pane.add(pintro, BorderLayout.NORTH);
        pane.add(pdata, BorderLayout.CENTER);
        pane.setVisible(false);
        return pane;

    }

    //LR Options Panel:
    public JPanel okPane() {

        JPanel pane = new JPanel();

        bcancel = new JButton("Cancel");
        bcancel.addActionListener(this);
        bcancel.setActionCommand("cancel");

        bapply = new JButton("Apply");
        bapply.addActionListener(this);
        bapply.setActionCommand("apply");
        bapply.setPreferredSize(bcancel.getPreferredSize());

        bok = new JButton("OK");
        bok.addActionListener(this);
        bok.setActionCommand("ok");
        bok.setPreferredSize(bcancel.getPreferredSize());

        brestdef = new JButton("Defaults..");
        //brestdef.setEnabled(false);
        brestdef.addActionListener(this);
        brestdef.setActionCommand("restore");
        bsetdet = new JButton("Change Presets");
        bsetdet.setEnabled(false);
        bsetdet.addActionListener(this);
        bsetdet.setActionCommand("change");
        pane.add(bsetdet);
        pane.add(bok);
        pane.add(bcancel);
        pane.add(bapply);
        pane.add(brestdef);
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

    //Read all JComponents Selected Text:
    public void setnewattributes() {

        //Set new batch files:
        try {
            FileOutputStream os = new FileOutputStream(getstringcomp(tglpkdosbat));
            BufferedOutputStream bos = new BufferedOutputStream(os, 32768);
            PrintStream myPrintStream = new PrintStream(bos, false);
            myPrintStream.println("cd\\");
            myPrintStream.println("cd " + getstringcomp(tglpkdir) + "\\bin");
            String lpstr;
            if (getstringcomp(cglpklpmethod).equals("simplex")) {
                lpstr = "--simplex";
            } else {
                lpstr = "--interior";
            }
            String lpfor;
            if (getstringcomp(cglpkpiftype).equals("freemps")) {
                lpfor = "--freemps";
            } else {
                lpfor = "--freemps";  //FUTURO DESARROLLO (AUNQUE IMPROBABLE)
            }
            String commandglpk = "glpsol.exe " + lpfor + " " + getstringcomp(tglpkpifname) + " " + lpstr
                    + " -o " + getstringcomp(tglpkpofname);
            myPrintStream.println(commandglpk);
            myPrintStream.close();
        } catch (Exception e) {
            String message = e.toString();
            System.out.println("File error: " + message);
        }

        //Set javauc object attributes:
        myjavauc.glpklpmethod = myjavauc.getstringcomp(cglpklpmethod);
        myjavauc.glpkpiftype = myjavauc.getstringcomp(cglpkpiftype);
        myjavauc.numaxunits = Short.valueOf(myjavauc.getstringcomp(tmaxunits)).shortValue();
        myjavauc.numperiods = Short.valueOf(myjavauc.getstringcomp(tperiods)).shortValue();
        myjavauc.itermax = Short.valueOf(myjavauc.getstringcomp(titermax)).shortValue();
        myjavauc.itertime = Short.valueOf(myjavauc.getstringcomp(titermax)).shortValue();
        myjavauc.tol_obj = Float.valueOf(myjavauc.getstringcomp(ttolobj)).floatValue();
        myjavauc.tol_dg = Float.valueOf(myjavauc.getstringcomp(ttoldg)).floatValue();
        myjavauc.tol_b = Float.valueOf(myjavauc.getstringcomp(ttolb)).floatValue();
        myjavauc.dbtype = myjavauc.getstringcomp(cdbtype);
        myjavauc.dbname = myjavauc.getstringcomp(tdbname);
        myjavauc.dbloc = myjavauc.getstringcomp(tdbloc);
        myjavauc.txtfiledir = myjavauc.getstringcomp(tdbtxt);
        myjavauc.optimizer = myjavauc.getstringcomp(copdef);
        myjavauc.useheuristic = cuseheuristic.isSelected();
        myjavauc.useaugmen = cuseaugmen.isSelected();
        myjavauc.usepiecewise = cusepiecewise.isSelected();
        myjavauc.printlp = cuseprintlp.isSelected();

        myjavauc.glpkdir = myjavauc.getstringcomp(tglpkdir);
        myjavauc.glpkpifname = myjavauc.getstringcomp(tglpkpifname);
        myjavauc.glpkdosbat = myjavauc.getstringcomp(tglpkdosbat);
        myjavauc.glpkpofname = myjavauc.getstringcomp(tglpkpofname);
        myjavauc.minosdir = myjavauc.getstringcomp(tminosdir);
        myjavauc.minoslineabat = myjavauc.getstringcomp(tminoslineabat);
        myjavauc.minosquadrabat = myjavauc.getstringcomp(tminosquadrabat);
        myjavauc.dispwidth = Integer.valueOf(myjavauc.getstringcomp(twidth)).intValue();
        myjavauc.dispheight = Integer.valueOf(myjavauc.getstringcomp(theight)).intValue();
        myjavauc.usefitto = cusefitto.isSelected();
        myjavauc.dispsquare = Integer.valueOf(myjavauc.getstringcomp(tsquare)).intValue();
        myjavauc.subalfa = Float.valueOf(myjavauc.getstringcomp(tsubalfa)).floatValue();
        myjavauc.subbeta = Float.valueOf(myjavauc.getstringcomp(tsubbeta)).floatValue();
        myjavauc.c_coef = Float.valueOf(myjavauc.getstringcomp(tccoef)).floatValue();
        myjavauc.epsaug = Float.valueOf(myjavauc.getstringcomp(tepsaug)).floatValue();

    }

    //Return String Selected Data in JComponent:
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
    //Apply Changes to JavaUC option atributtes:

    /*
	public void setnewattributes(){
		//1--This will update the XmlTree
		for (int kk=0; kk<=myucoption.numfields(); kk++){
			if (myucoption.treedata[kk].isleaf()){
				myucoption.treedata[kk].setchardata(chardatatemp[kk]);
			}
		}
		//2--This will update all javauc atributtes:
		myjavauc.optionfile();
	}
     */
    //Handle Actions:
    public void actionPerformed(ActionEvent ev) {
        String label = ev.getActionCommand();
        if (label.equals("apply")) {
            setnewattributes();
            ucMainFrame.setFrameValues();
            setFocusable(true);
            toFront();
        } else if (label.equals("cancel")) {
            setVisible(false);							//Provisional
            dispose();
        } else if (label.equals("ok")) {
            setnewattributes();
            setVisible(false);
            ucMainFrame.setFrameValues();
            dispose();
        } else if (label.equals("change")) {
        } else if (label.equals("glpkdir")) {
            JFileChooser fileglpk = new JFileChooser();
            fileglpk.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fileglpk.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("You chose to open this file: "
                        + fileglpk.getSelectedFile().getPath());
                tglpkdir.setText(fileglpk.getSelectedFile().getPath());
            }
        } else if (label.equals("restore")) {
            ReadXmlDefautls();
        }

    }

    public boolean ReadXmlDefautls() {
        //Codigo Defectuoso. Debe cambiarse por metodo de seteo de atributos

        myjavauc.optionfile();
        dispose();
        new UCtreeoptionFrame(ucMainFrame);
        return true;
    }

    public boolean WriteXmlDefautls() {
        return true;
    }

}
