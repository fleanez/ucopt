package cl.flconsulting.ucopt;

/*****************************************************************************  
 * Java Panel for Dynamic Programming Visualization
 * Written by:             Frank Leanez
 * Initial release:        September, 2005
 *****************************************************************************/  

import java.awt.*;
//import java.awt.event.*;
import javax.swing.*;
//import java.awt.geom.*;

class UClegendPanel extends JPanel {
	
	
//------------------
//Class Attributes:
//------------------
	
	
//------------------
//Panel Constructor:
//------------------

	public UClegendPanel(){
		super();
		
		setOpaque(true);
		//setBackground(Color.white);
		setVisible(true);
	}
	
//------------------
//Panel Methods:
//------------------
	
	/*
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		
        double gridHeight = squareheight;
        double gridWidth = squarewidth;
        
        int x=0;
        int y=ylabel;
        
		//g2.setStroke(stroke);
        for (int ii=0; ii<numunits; ii++){
        	x=0;
        	//Short id = new Short (MyJavaUC.Unitdata.readunitid[ii]);
        	//g2.drawString(id.toString(), x, y+20);
        	String unitlabel=formatlabel(MyJavaUC.Unitdata.readname[ii],5);
        	g2.drawString(" "+unitlabel, x, y+20);
        	x+=xlabel;
        	for (int tt=0; tt<numperiods; tt++){
        		g2.draw(new Rectangle2D.Double(x, y, gridWidth, gridHeight));
        		//if (MyJavaUC.ubin[ii][tt]==1){
        		if (uprint[ii][tt]==1){
        			g2.setPaint(Color.red);
        		}else{
        			g2.setPaint(Color.white);
        		}
        		g2.fill(new Rectangle2D.Double(x, y, gridWidth, gridWidth));
        		g2.setPaint(Color.black);
        		g2.draw(new Rectangle2D.Double(x, y, gridWidth, gridHeight));
        		//g2.drawString((" "+tt+" "), x+10, y+numunits*squareubin+ylabel);
        		x+=gridWidth;
        	}
        	y+=gridHeight;
        }
        x=xlabel;
        for (int tt=0; tt<numperiods; tt++){
        	g2.drawString((" "+(tt+1)+" "), x+((int)gridWidth/2), y+ylabel);
        	x+=gridWidth;
        }
	}
	*/
        
	public void paint(Graphics g){
	
		String []legendname  = {"Offline","Online","Hot startup", "Warm startup",
							   "Cold startup"  };
		Color  []legendcolor = {Color.white,Color.red,new Color(0x660033),
								new Color(0x330099),new Color(0x3366FF) };
		
		for (int ii=0; ii<legendname.length; ++ii) {
		
			g.setColor(Color.blue);
			g.drawString("Unit Commitment Color-Specification:",20,18);
			
			g.setColor(Color.black);
			g.drawRect(10,26+20*ii,16,16);
			
			g.setColor(legendcolor[ii]);
			g.fillRect(11,27+20*ii,15,15);
			
			g.setColor(Color.black);
			g.drawString(legendname[ii],40,40+20*ii);
		}
	}
	
	
}//end class
