package cl.flconsulting.ucopt;

/*****************************************************************************  
 * Java Panel for Dynamic Programming Visualization
 * Written by:             Frank Leanez
 * Initial release:        September, 2005
 *****************************************************************************/  

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.*;

class UCmatrixPanel extends JPanel implements KeyListener, ActionListener, ItemListener {
	
	
//------------------
//Class Attributes:
//------------------
	
	private JavaUC myjavauc;
	
	//General attributes:
	Dimension dim;  //Provicional
	int numunits;
	int numperiods;
	double squareheight;
	double squarewidth;
	int squareubin;
	int xlabel;
	int ylabel;
	
	byte uprint[][];
	byte uprior[][];
	
//------------------
//Panel Constructor:
//------------------

	public UCmatrixPanel(JavaUC uctemp){
		super();
		myjavauc=uctemp;						
		numunits=myjavauc.numunits;
		numperiods=myjavauc.numperiods;
		uprint=new byte[numunits][numperiods];
		uprior=new byte[numunits][numperiods];
		for (int tt=0;tt<numperiods;tt++){
			for (int ii=0;ii<numunits;ii++){
				uprint[ii][tt]=myjavauc.ubin[ii][tt];
				uprior[ii][tt]=uprint[ii][tt];
			}
		}
		if (numunits>=100){
			xlabel=8*getlongestname();
		}else{
			xlabel=8*getlongestname();
		}
        ylabel=10;
		
		if (myjavauc.usefitto){
			int ytemp=myjavauc.dispheight;
			int xtemp=myjavauc.dispwidth;
			squareheight=(myjavauc.dispheight-(ylabel*2))/numunits;
			squarewidth=(myjavauc.dispwidth-xlabel-20)/numperiods;
		}else{
			squareheight=myjavauc.dispsquare;
			squarewidth=myjavauc.dispsquare;
		}
        //squareubin=myjavauc.dispsquare; 		//Pixel size square Unit representation
		//dim=new Dimension(numperiods*squareubin+xlabel+1,numunits*squareubin+ylabel+1);
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		Dimension prefSize = new Dimension((int)squarewidth*numperiods+xlabel*2,(int)squareheight*numunits+ylabel*2);
		//Dimension maxSize = new Dimension(Short.MAX_VALUE, 100);
		add(new Box.Filler(prefSize, prefSize, prefSize));
		//addMouseListener(this);
		JLabel bla = new JLabel ("Unit Commitment");
		//add(bla);
		setOpaque(true);
		setBackground(Color.white);
		//setPreferredSize(dim);
		setVisible(true);
	}
	
//------------------
//Panel Methods:
//------------------

	public void keyPressed(KeyEvent event){}
  	public void keyReleased(KeyEvent event){}
  	public void keyTyped(KeyEvent event){}
	public void itemStateChanged(ItemEvent e){}
	public void actionPerformed(ActionEvent ev){}
	
	
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
        	//Short id = new Short (myjavauc.Unitdata.readunitid[ii]);
        	//g2.drawString(id.toString(), x, y+20);
        	String wtemp="                                                                        ";
        	String unitlabel=formatlabel2(myjavauc.Unitdata.readname[ii]+wtemp,this.getlongestname());
        	g2.drawString(" "+unitlabel, x, y+(int)gridHeight);
        	x+=xlabel;
        	for (int tt=0; tt<numperiods; tt++){
        		//g2.draw(new Rectangle2D.Double(x, y, gridWidth, gridHeight));
        		
        		g2.setPaint(setquarecolor(ii,tt));
        		/*
        		if (uprint[ii][tt]==1){
        			g2.setPaint(Color.red);
        		}else{
        			g2.setPaint(Color.white);
        		}
        		*/
        		g2.fill(new Rectangle2D.Double(x, y, gridWidth, gridHeight));
        		g2.setPaint(Color.black);
        		g2.draw(new Rectangle2D.Double(x, y, gridWidth, gridHeight));
        		//g2.drawString((" "+tt+" "), x+10, y+numunits*squareubin+ylabel);
        		x+=gridWidth;
        	}
        	y+=gridHeight;
        }
        x=xlabel;
        for (int tt=0; tt<numperiods; tt++){
        	g2.drawString((" "+(tt+1)+" "), x+2, y+ylabel);
        	x+=gridWidth;
        }
	}
	
	public void updatebinary(String typeindex){
		int ii,tt;
		for (tt=0;tt<numperiods;tt++){
			for (ii=0;ii<numunits;ii++){
				if (typeindex.equals("last")){
					uprint[ii][tt]=myjavauc.ubin[ii][tt];
				}else if (typeindex.equals("best")){
					uprint[ii][tt]=myjavauc.ubest[ii][tt];
				}else{
					uprint[ii][tt]=uprior[ii][tt];
				}
			}
		}
	}
	
	public Color setquarecolor(int ii, int tt){
		Color finalcolor;
		if (uprint[ii][tt]==0){
			finalcolor=Color.white;
		}else{
			double tdown=gettdown(ii,tt);
			//Cold StartUp:
			if (tdown>=myjavauc.Unitdata.readtstartcold[ii]){
				finalcolor=new Color(0x3366FF);
			//Warm StartUp:
			}else if ((tdown<myjavauc.Unitdata.readtstartcold[ii]) &&
					  (tdown>myjavauc.Unitdata.readtstarthot[ii]) ){
				finalcolor=new Color(0x330099);
			//Hot StartUp:
			}else if ((tdown<=myjavauc.Unitdata.readtstarthot[ii]) && tdown>0){
				finalcolor=new Color(0x660033);
			}else{
				finalcolor=Color.red;
			}
		}
		return finalcolor;
	}
	
	//Get Time unit is Offline - Same method as Javauc.java:
	public short gettdown(int ii, int tt){
		short tdown=0;
		int trun=myjavauc.Unitdata.readtrunini[ii];
		//Si hablamos de una unidad despachada en ese tiempo (tt):
		if (uprint[ii][tt]==1){
			int cont=1;
			//Mientras no estemos en un tiempo negativo:
			boolean ttpos=true;
			if (tt==0){
				cont=0;
				if (trun<0){
					tdown=(short)Math.abs(trun);
				}
				ttpos=false;
			}
			//Mientras sigamos encontrando q la unidad estaba apagada tiempo atras:
			while ((uprint[ii][tt-cont]==0) && ttpos){
				tdown+=1;
				if (tt-(cont+1)<0){
					if (trun<0){
						tdown+=(short)Math.abs(trun);
					}
					ttpos=false;
				}else{
					cont++;
				}
			}
		}
		return tdown;
	}
	
	
	//2--Determine characters of the largest unit name:
	public int getlongestname(){
		int longestname=0;
		for (int ii=0;ii<numunits;ii++){
			
			int longtemp;
			String nametemp=myjavauc.Unitdata.readname[ii];
			if (nametemp==null){// || !nametemp.equals("")){
				longtemp=0;
			}else{
				longtemp=nametemp.length();
			}
			if (longtemp>longestname){
				longestname=longtemp;
			}
		}
		return longestname;
	}
	
	
	public String formatlabel(String originallabel, int maxdig){
		int dig = originallabel.length();
		String digstring=originallabel;
		if (dig>maxdig){
			digstring=originallabel.substring(dig-maxdig,dig);
		}
		return digstring;
	}
	
	public String formatlabel2(String originallabel, int maxdig){
		String digstring;
		digstring=originallabel.substring(0,maxdig);
		return digstring;
	}
	/*
	public void mouseClicked(MouseEvent me){}
	//Invoked when the mouse button has been clicked (pressed and released) on a component. 
	public void mouseEntered(MouseEvent me){}
	//Invoked when the mouse enters a component. 
	public void mouseExited(MouseEvent me){}
	//Invoked when the mouse exits a component. 
	public void mousePressed(MouseEvent me){
		int clickperiod=(me.getX()-xlabel)/numperiods;
		int clickunit=(me.getY()-ylabel)/numunits;
		System.out.println("Unidad: "+clickunit+"Tiempo:"+clickperiod);
	}
	//Invoked when a mouse button has been pressed on a component. 
	public void mouseReleased(MouseEvent e){}
	//Invoked when a mouse button has been released on a component.
	*/
	
}//end class
