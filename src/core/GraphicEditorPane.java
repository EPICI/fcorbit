package core;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.color.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.*;

/**
 * The graphical editor component
 * 
 * @author EPICI
 * @version 1.0
 */
public class GraphicEditorPane extends JPanel implements KeyTracker {
	
	public static final double ROUND_RADIUS = 2;
	public static final double JOINT_RADIUS_OUTER = 4;
	public static final double JOINT_RADIUS_INNER = 2;
	
	public static final Color BACKGROUND = Color.decode("#87bdf1");
	public static final Color JOINT = Color.decode("#808080");
	public static final Color JOINT_CENTER = Color.decode("#ffffff");
	public static final Color[] GOAL_OBJECT = {Color.decode("#b86461"),Color.decode("#fe6766")};
	public static final Color[] U_WHEEL = {Color.decode("#0b6afc"),Color.decode("#8cfce4")};
	public static final Color[] BUILD_AREA = {Color.decode("#7878ee"),Color.decode("#bedcf8")};
	public static final Color[] DYNAMIC_RECTANGLE = {Color.decode("#c5550e"),Color.decode("#f8dc2f")};
	public static final Color[] CW_WHEEL = {Color.decode("#fd8003"),Color.decode("#ffed00")};
	public static final Color[] WATER_ROD = {Color.decode("#feffff"),Color.decode("#0001fe")};
	public static final Color[] GOAL_AREA = {Color.decode("#bc6667"),Color.decode("#f29291")};
	public static final Color[] DYNAMIC_CIRCLE = {Color.decode("#bd591b"),Color.decode("#f98931")};
	public static final Color[] CCW_WHEEL = {Color.decode("#ce49a3"),Color.decode("#ffcfce")};
	public static final Color[] WOOD_ROD = {Color.decode("#6a3502"),Color.decode("#b55a04")};
	public static final Color[] STATIC_OBJECT = {Color.decode("#007f09"),Color.decode("#01be02")};
	
	public static final double SCALE_RATE = -1d/8;
	public static final double SCALE_MIN = -3;
	public static final double SCALE_MAX = 3;
	public static final double PAN_RATE = 32;
	public static final double ANCHORX_MAX = 1000;
	public static final double ANCHORY_MAX = 725;
	
	/**
	 * Which keys are held down
	 */
	public final BitSet keys = new BitSet();
	/**
	 * Uniform world to pixel scale for x and y axes
	 */
	public double logScale = -2;
	/**
	 * Where in the world the center of the window is, x value
	 */
	public double anchorx = 0;
	/**
	 * Where in the world the center of the window is, y value
	 */
	public double anchory = 0;
	/**
	 * Temporary unbounded anchorx
	 */
	public double uanchorx;
	/**
	 * Temporary unbounded anchory
	 */
	public double uanchory;
	/**
	 * Mouse button being held down (0 = not pressed, 1 = left, 2 = middle, 3 = right)
	 */
	public int mouseDown;
	/**
	 * Has the mouse moved since it was clicked down?
	 */
	public boolean mouseDragged;
	/**
	 * The mouse x where dragging began
	 */
	public int originMousex;
	/**
	 * The mouse y where dragging began
	 */
	public int originMousey;
	/**
	 * The mouse x, where it was last seen
	 */
	public int lastMousex;
	/**
	 * The mouse y, where it was last seen
	 */
	public int lastMousey;
	
	public ArrayList<FCObj> objDoc;
	public ArrayList<FCObj> objSel;
	
	public ActiveCommand command = new CommandNone();
	
	public BitSet getKeys(){
		return keys;
	}

	public GraphicEditorPane() {
	}

	public GraphicEditorPane(LayoutManager layout) {
		super(layout);
	}

	public GraphicEditorPane(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public GraphicEditorPane(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}
	
	/**
	 * Call after done initializing fields
	 */
	public void init(){
		
	}
	
	@Override
	public void paint(Graphics og){
		// Fetch values
		Graphics2D g = (Graphics2D) og;
		int width = getWidth(), height = getHeight();
		double cx = width*0.5, cy = height*0.5;
		long tracker = Main.ticker;
		double scale = Math.pow(2, logScale);
		double anchorx = this.anchorx;
		double anchory = this.anchory;
		// Make layers
		RenderLayer design = new RenderLayer(3,width,height);
		design.translate(cx, cy);
		design.scale(scale, scale);
		design.translate(anchorx, anchory);
		RenderLayer level = new RenderLayer(2,width,height);
		level.translate(cx, cy);
		level.scale(scale, scale);
		level.translate(anchorx, anchory);
		int leveln = -2;
		// Sort objects
		objDoc.sort(FCObj.Z_COMPARE);
		// Background
		g.setColor(BACKGROUND);
		g.fillRect(0, 0, width, height);
		// Iterate and draw onto layers
		for(FCObj obj:objDoc){
			if(tracker!=Main.ticker)break;
			int z = obj.z;
			double x = obj.x, y = obj.y, w = obj.w, h = obj.h, r = obj.r;
			int typeData = FCObj.nameToType.get(obj.type);
			boolean isdesign = Bits.readBit(typeData, FCObj.TYPE_DESIGN);
			boolean iscircle = Bits.readBit(typeData, FCObj.TYPE_CIRCLE);
			boolean isgoal = Bits.readBit(typeData, FCObj.TYPE_GOAL);
			boolean isjointable = Bits.readBit(typeData, FCObj.TYPE_JOINTABLE);
			boolean iscollide = Bits.readBit(typeData, FCObj.TYPE_COLLIDES);
			boolean ismovable = Bits.readBit(typeData, FCObj.TYPE_MOVABLE);
			boolean isrod = isdesign&&!iscircle&&!isgoal;
			boolean iswater = isrod&&Bits.readBit(typeData, FCObj.TYPE_NOSELFCOLLIDE);
			boolean isstatic = !isdesign&&iscollide&&!ismovable;
			Color[] colors = getColorsFor(typeData);
			Color colOutline = colors[0];
			Color colFill = colors[1];
			RenderLayer target = design;// Where to render to
			if(!isdesign){// Level
				if(z>leveln){
					// New layer
					leveln=z;
					level.renderTo(g);
					level = new RenderLayer(2,width,height);
					level.translate(cx, cy);
					level.scale(scale, scale);
					level.translate(anchorx, anchory);
				}
				target = level;
			}
			double iw = Math.abs(w-8);
			double ih = Math.abs(h-8);
			Graphics2D ig = target.graphics[0];
			AffineTransform ot = ig.getTransform();
			if(iscircle){// Circle
				double ow = Math.max(w, iw+2);
				double oh = Math.max(h, ih+2);
				ig.translate(x, y);
				ig.rotate(Math.toRadians(r));
				ig.setColor(colOutline);
				ig.fill(new Ellipse2D.Double(ow*-0.5, oh*-0.5, ow, oh));
				ig.setTransform(ot);
				if(isjointable){
					ig = target.graphics[2];
					ot = ig.getTransform();
					ig.translate(x, y);
					ig.rotate(Math.toRadians(r));
					drawJoint(ig,JOINT_CENTER,0,0);
					drawJoint(ig,ow*0.5,0);
					drawJoint(ig,ow*-0.5,0);
					drawJoint(ig,0,oh*0.5);
					drawJoint(ig,0,oh*-0.5);
					if(iw>40){
						drawJoint(ig,20,0);
						drawJoint(ig,-20,0);
					}
					if(ih>40){
						drawJoint(ig,0,20);
						drawJoint(ig,0,-20);
					}
					ig.setTransform(ot);
				}
				ig = target.graphics[1];
				ot = ig.getTransform();
				ig.translate(x, y);
				ig.rotate(Math.toRadians(r));
				ig.setColor(colFill);
				ig.fill(new Ellipse2D.Double(iw*-0.5, ih*-0.5, iw, ih));
				ig.setTransform(ot);
			}else{// Rectangle or rod
				double ow,oh;
				if(isrod){
					ow = w;
					oh = h;
					if(iswater)oh*=2;
					iw = Math.abs(ow-4);
					ih = Math.abs(oh-4);
				}else{
					ow = Math.max(w, iw+2);
					oh = Math.max(h, ih+2);
					if(isstatic){
						ow+=1;
						oh+=1;
						ig.translate(0.5, 0.5);
					}
				}
				ig.translate(x, y);
				ig.rotate(Math.toRadians(r));
				ig.setColor(colOutline);
				ig.fill(new RoundRectangle2D.Double(ow*-0.5, oh*-0.5, ow, oh, Math.PI/2, ROUND_RADIUS));
				ig.setTransform(ot);
				if(isjointable){
					ig = target.graphics[2];
					ot = ig.getTransform();
					ig.translate(x, y);
					ig.rotate(Math.toRadians(r));
					if(isrod){
						drawJoint(ig,ow*0.5,0);
						drawJoint(ig,ow*-0.5,0);
					}else{
						drawJoint(ig,0,0);
						drawJoint(ig,ow*0.5,oh*0.5);
						drawJoint(ig,ow*-0.5,oh*0.5);
						drawJoint(ig,ow*0.5,-oh*0.5);
						drawJoint(ig,ow*-0.5,-oh*0.5);
					}
					ig.setTransform(ot);
				}
				ig = target.graphics[1];
				ot = ig.getTransform();
				ig.translate(x, y);
				ig.rotate(Math.toRadians(r));
				ig.setColor(colFill);
				ig.fill(new Rectangle2D.Double(iw*-0.5, ih*-0.5, iw, ih));
				ig.setTransform(ot);
			}
		}
		level.renderTo(g);
		design.renderTo(g);
	}
	
	public static void drawJoint(Graphics2D g,double x,double y){
		drawJoint(g,JOINT,x,y);
	}
	public static void drawJoint(Graphics2D g,Color color,double x,double y){
		final double jro = JOINT_RADIUS_OUTER;
		final double jri = JOINT_RADIUS_INNER;
		Area area = new Area(new Ellipse2D.Double(x-jro, y-jro, jro*2, jro*2));
		area.subtract(new Area(new Ellipse2D.Double(x-jri, y-jri, jri*2, jri*2)));
		g.setColor(color);
		g.fill(area);
	}
	
	public void scrolled(double amount){
		double newLogScale = Floats.median(SCALE_MIN, logScale+amount, SCALE_MAX);
		if(logScale==newLogScale)return;
		logScale = newLogScale;
		repaint();
	}
	
	public void panned(double byx,double byy){
		double invScale = Math.pow(2, -logScale);
		uanchorx -= byx*invScale;
		uanchory -= byy*invScale;
		double newAnchorx = Floats.median(-ANCHORX_MAX, uanchorx, ANCHORX_MAX);
		double newAnchory = Floats.median(-ANCHORY_MAX, uanchory, ANCHORY_MAX);
		if(anchorx==newAnchorx&&anchory==newAnchory)return;
		anchorx = newAnchorx;
		anchory = newAnchory;
		repaint();
	}
	
	public static Color[] getColorsFor(int typeData){
		if(Bits.readBit(typeData, FCObj.TYPE_DESIGN)){// Design
			if(Bits.readBit(typeData, FCObj.TYPE_GOAL)){// Goal override
				return GOAL_OBJECT;
			}else{
				if(Bits.readBit(typeData, FCObj.TYPE_CIRCLE)){// Wheel
					if(Bits.readBit(typeData, FCObj.TYPE_POWERED)){// Powered wheel
						if(Bits.readBit(typeData, FCObj.TYPE_CLOCKWISE)){// CW wheel
							return CW_WHEEL;
						}else{// CCW wheel
							return CCW_WHEEL;
						}
					}else{
						return U_WHEEL;
					}
				}else{// Rod
					if(Bits.readBit(typeData, FCObj.TYPE_NOSELFCOLLIDE)){// water
						return WATER_ROD;
					}else{// wood
						return WOOD_ROD;
					}
				}
			}
		}else{// Not design
			if(Bits.readBit(typeData, FCObj.TYPE_COLLIDES)){// Static or dynamic
				if(Bits.readBit(typeData, FCObj.TYPE_MOVABLE)){// Dynamic
					if(Bits.readBit(typeData, FCObj.TYPE_CIRCLE)){// Dynamic circle
						return DYNAMIC_CIRCLE;
					}else{// Dynamic rectangle
						return DYNAMIC_RECTANGLE;
					}
				}else{// Static
					return STATIC_OBJECT;
				}
			}else{// Build area or goal area
				if(Bits.readBit(typeData, FCObj.TYPE_GOAL)){// Goal area
					return GOAL_AREA;
				}else{// Build area
					return BUILD_AREA;
				}
			}
		}
	}
	
	public void forget(){
		cancelCommand();
		keys.clear();
		mouseDown = 0;
		mouseDragged = false;
	}
	
	/**
	 * Cancel whatever the current command is
	 */
	public void cancelCommand(){
		command.cancel();
		command = new CommandNone();
	}
	
	/**
	 * Not a real command, this goes in when there's no command active
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public class CommandNone implements ActiveCommand{
		
		@Override
		public void cancel(){
			
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			switch(mouseDown){
			case 2:{
				// Pan view
				int x = e.getX();
				int y = e.getY();
				panned(lastMousex-x,lastMousey-y);
				break;
			}
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			double by = e.getPreciseWheelRotation();
			if(keys.get(KeyEvent.VK_ALT)){// Pan
				// Hold shift to pan x instead of y
				boolean usex = keys.get(KeyEvent.VK_SHIFT);
				by *= PAN_RATE;
				if(usex){
					panned(by,0);
				}else{
					panned(0,by);
				}
				uanchorx=anchorx;
				uanchory=anchory;
			}else{// Zoom
				scrolled(by*SCALE_RATE);
			}
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
		
	}

}
