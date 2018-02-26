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
	
	public static final Color TRANSPARENT = new Color(0,0,0,0);
	public static final Color[] ATRANSPARENT = {TRANSPARENT,TRANSPARENT};
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
	public static final Color SELECTED_OVERLAY = Color.decode("#bbbbbb");
	public static final Color SELECTED_OVERLAY_FIRST = Color.decode("#dddddd");
	public static final float OVERLAY_ALPHA = 0.5f;
	public static final Color GRID = Color.decode("#444444");
	public static final Color AXISX = Color.decode("#cc3333");
	public static final Color AXISY = Color.decode("#33cc33");
	public static final Color AXISXY = Color.decode("#aaaa44");
	
	public static final double SCALE_RATE = -1d/8;
	public static final double SCALE_MIN = -3;
	public static final double SCALE_MAX = 3;
	public static final double PAN_RATE = 32;
	public static final double ANCHORX_MAX = 1000;
	public static final double ANCHORY_MAX = 725;
	public static final int BOUNDX = 1000;
	public static final int BOUNDY = 725;
	
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
	
	/**
	 * Show as wireframe?
	 */
	public boolean showWireframe;
	/**
	 * Show axes and grid?
	 */
	public boolean showGrid;
	
	public ArrayList<FCObj> objDoc;
	public ArrayList<FCObj> objSel;
	public ArrayList<FCObj> backupSel = new ArrayList<>();
	
	public ActiveCommand command = new CommandNone(this);
	
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
		double scale = getScale();
		double anchorx = this.anchorx;
		double anchory = this.anchory;
		boolean showWireframe = this.showWireframe;
		boolean showGrid = this.showGrid;
		// Make layers
		RenderLayer design = new RenderLayer(3,width,height);
		design.translate(cx, cy);
		design.scale(scale, scale);
		design.translate(-anchorx, -anchory);
		RenderLayer level = new RenderLayer(2,width,height);
		level.translate(cx, cy);
		level.scale(scale, scale);
		level.translate(-anchorx, -anchory);
		RenderLayer overlays = new RenderLayer(1,width,height);
		overlays.translate(cx, cy);
		overlays.scale(scale, scale);
		overlays.translate(-anchorx, -anchory);
		int leveln = -2;
		// Sort objects
		objDoc.sort(FCObj.Z_COMPARE);
		// Grab selection
		HashMap<FCObj,Integer> selectedIndex = new HashMap<>();
		int i=0;
		for(FCObj obj:objSel){
			selectedIndex.put(obj, i);
			i++;
		}
		// Background
		g.setColor(BACKGROUND);
		g.fillRect(0, 0, width, height);
		// Iterate and draw onto layers
		for(FCObj obj:objDoc){
			if(tracker!=Main.ticker)break;
			int z = obj.z;
			double x = obj.x, y = obj.y, w = obj.w, h = obj.h, r = obj.r;
			int typeData = obj.getTypeData();
			Integer osel = selectedIndex.get(obj);
			boolean isselected = osel!=null;
			int sel = isselected?osel:-1;
			boolean isselectedfirst = sel==0;
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
					level.translate(-anchorx, -anchory);
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
				if(showWireframe){
					ig.draw(new Ellipse2D.Double(ow*-0.5, oh*-0.5, ow, oh));
				}else{
					ig.fill(new Ellipse2D.Double(ow*-0.5, oh*-0.5, ow, oh));
				}
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
				if(!showWireframe){
					ig = target.graphics[1];
					ot = ig.getTransform();
					ig.translate(x, y);
					ig.rotate(Math.toRadians(r));
					ig.setColor(colFill);
					ig.fill(new Ellipse2D.Double(iw*-0.5, ih*-0.5, iw, ih));
					ig.setTransform(ot);
				}
				if(isselected){
					ig = overlays.graphics[0];
					ig.translate(x, y);
					ig.rotate(Math.toRadians(r));
					ig.setColor(isselectedfirst?SELECTED_OVERLAY_FIRST:SELECTED_OVERLAY);
					ig.fill(new Ellipse2D.Double(ow*-0.5, oh*-0.5, ow, oh));
					ig.setTransform(ot);
				}
			}else{// Rectangle or rod
				double ow,oh;
				if(isrod){
					ow = w;
					oh = h;
					if(iswater&&!showWireframe)oh*=2;
					iw = Math.abs(ow-4);
					ih = Math.abs(oh-4);
				}else{
					ow = Math.max(w, iw+2);
					oh = Math.max(h, ih+2);
					if(isstatic&&!showWireframe){
						ow+=1;
						oh+=1;
						ig.translate(0.5, 0.5);
					}
				}
				ig.translate(x, y);
				ig.rotate(Math.toRadians(r));
				ig.setColor(colOutline);
				if(showWireframe){
					ig.draw(new Rectangle2D.Double(ow*-0.5, oh*-0.5, ow, oh));
				}else{
					ig.fill(new RoundRectangle2D.Double(ow*-0.5, oh*-0.5, ow, oh, Math.PI/2, ROUND_RADIUS));
				}
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
				if(!showWireframe){
					ig = target.graphics[1];
					ot = ig.getTransform();
					ig.translate(x, y);
					ig.rotate(Math.toRadians(r));
					ig.setColor(colFill);
					ig.fill(new Rectangle2D.Double(iw*-0.5, ih*-0.5, iw, ih));
					ig.setTransform(ot);
				}
				if(isselected){
					ig = overlays.graphics[0];
					ig.translate(x, y);
					ig.rotate(Math.toRadians(r));
					ig.setColor(isselectedfirst?SELECTED_OVERLAY_FIRST:SELECTED_OVERLAY);
					ig.fill(new RoundRectangle2D.Double(ow*-0.5, oh*-0.5, ow, oh, Math.PI/2, ROUND_RADIUS));
					ig.setTransform(ot);
				}
			}
		}
		// Grid
		if(showGrid){
			Graphics2D ig = overlays.graphics[0];
			ig.setColor(GRID);
			ig.drawLine(-BOUNDX, -BOUNDY, BOUNDX, -BOUNDY);
			ig.drawLine(-BOUNDX, 0, BOUNDX, 0);
			ig.drawLine(-BOUNDX, BOUNDY, BOUNDX, BOUNDY);
			ig.drawLine(-BOUNDX, -BOUNDY, -BOUNDX, BOUNDY);
			ig.drawLine(0, -BOUNDY, 0, BOUNDY);
			ig.drawLine(BOUNDX, -BOUNDY, BOUNDX, BOUNDY);
		}
		// Finally, render all
		level.renderTo(g);
		design.renderTo(g);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, OVERLAY_ALPHA));
		overlays.renderTo(g);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
		command.render(g);
	}
	
	public void drawJoint(Graphics2D g,double x,double y){
		drawJoint(g,JOINT,x,y);
	}
	public void drawJoint(Graphics2D g,Color color,double x,double y){
		final double jro = JOINT_RADIUS_OUTER;
		final double jri = JOINT_RADIUS_INNER;
		Area area = new Area(new Ellipse2D.Double(x-jro, y-jro, jro*2, jro*2));
		g.setColor(color);
		if(showWireframe){
			g.draw(area);
		}else{
			area.subtract(new Area(new Ellipse2D.Double(x-jri, y-jri, jri*2, jri*2)));
			g.fill(area);
		}
	}
	
	public void drawCenteredString(Graphics2D g,String text,int x,int y){
		FontMetrics metrics = g.getFontMetrics(g.getFont());
	    x -= metrics.stringWidth(text)*0.5;
	    y -= metrics.getHeight()*0.5 - metrics.getAscent();
	    g.drawString(text, x, y);
	}
	
	public double getScale(){
		return Math.pow(2, logScale);
	}
	
	public double getInvScale(){
		return Math.pow(2, -logScale);
	}
	
	public void scaled(double amount){
		double newLogScale = Floats.median(SCALE_MIN, logScale+amount, SCALE_MAX);
		if(logScale==newLogScale)return;
		logScale = newLogScale;
		repaint();
	}
	
	public void panned(double byx,double byy){
		double invScale = getInvScale();
		uanchorx += byx*invScale;
		uanchory += byy*invScale;
		double newAnchorx = Floats.median(-ANCHORX_MAX, uanchorx, ANCHORX_MAX);
		double newAnchory = Floats.median(-ANCHORY_MAX, uanchory, ANCHORY_MAX);
		if(anchorx==newAnchorx&&anchory==newAnchory)return;
		anchorx = newAnchorx;
		anchory = newAnchory;
		repaint();
	}
	
	public static Color[] getColorsFor(int typeData){
		if(typeData==0)return ATRANSPARENT;// Empty
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
		setCommand(new CommandNone(this));
	}
	
	public void setCommand(ActiveCommand replacement){
		command.cancel();
		command = replacement;
		repaint();
	}
	
	/**
	 * Modified distance comparator used by selection stuff so it's possible to select
	 * objects which are hidden by other objects
	 * 
	 * @author EPICI
	 * @version 1.0
	 */
	public static class ModDistance implements Mapping<FCObj,Double>{

		final double px,py;
		public ModDistance(double x,double y){
			px=x;
			py=y;
		}
		
		@Override
		public Double map(FCObj obj) {
			int typeData = obj.getTypeData();
			boolean iscircle = Bits.readBit(typeData, FCObj.TYPE_CIRCLE);
			if(iscircle){
				return Math.hypot(px-obj.x, py-obj.y)/Math.abs(obj.w*0.5);
			}else{
				double px = this.px, py = this.py;
				final double x = obj.x,
						y = obj.y,
						rx = Math.abs(obj.w*0.5),
						ry = Math.abs(obj.h*0.5),
						r = -Math.toRadians(obj.r),// Invert rotation
						cr = Math.cos(r),
						sr = Math.sin(r);
				px -= x;
				py -= y;
				final double nx = px*cr-py*sr;
				py = px*sr+py*cr;
				px = nx;
				return Math.hypot(px/rx, py/ry);
			}
		}
		
	}
	
	public FCObj getSelectionPoint(){
		return getSelectionPoint(lastMousex,lastMousey);
	}
	/**
	 * Given the current mouse position, get the single
	 * object that would be selected by a right click
	 * 
	 * @param mx
	 * @param my
	 * @return
	 */
	public FCObj getSelectionPoint(int mx,int my){
		int width = getWidth(), height = getHeight();
		double invScale = getInvScale();
		final double wmx = (mx-width*0.5)*invScale+anchorx;
		final double wmy = (my-height*0.5)*invScale+anchory;
		return getSelectionPointWorld(wmx,wmy);
	}
	public FCObj getSelectionPointWorld(double wmx,double wmy){
		ArrayList<FCObj> candidates = new ArrayList<>();
		for(FCObj obj:objDoc){
			if(obj.contains(wmx,wmy)){
				candidates.add(obj);
			}
		}
		if(candidates.isEmpty())return null;
		Mapping.sort(candidates, new ModDistance(wmx,wmy), Comparator.<Double>naturalOrder());
		return candidates.get(0);
	}
	
	public ArrayList<FCObj> getSelectionArea(){
		return getSelectionArea(lastMousex,lastMousey);
	}
	/**
	 * Given the current mouse position, get the objects
	 * that would be selected or deselected by a right click
	 * 
	 * @param mx
	 * @param my
	 * @return
	 */
	public ArrayList<FCObj> getSelectionArea(int mx,int my){
		final int omx = originMousex, omy = originMousey;
		return getSelectionArea(mx,my,omx,omy);
	}
	public ArrayList<FCObj> getSelectionArea(int mx,int my,int omx,int omy){
		int width = getWidth(), height = getHeight();
		double invScale = getInvScale();
		final double wmx = (mx-width*0.5)*invScale+anchorx;
		final double wmy = (my-height*0.5)*invScale+anchory;
		final double womx = (omx-width*0.5)*invScale+anchorx;
		final double womy = (omy-height*0.5)*invScale+anchory;
		return getSelectionAreaWorld(wmx,wmy,womx,womy);
	}
	public ArrayList<FCObj> getSelectionAreaWorld(double wmx,double wmy,double womx,double womy){
		FCObj dummy = new FCObj();
		dummy.type = "Empty";
		dummy.x = (wmx+womx)*0.5;
		dummy.y = (wmy+womy)*0.5;
		dummy.w = wmx-womx;
		dummy.h = wmy-womy;
		ArrayList<FCObj> candidates = new ArrayList<>();
		for(FCObj obj:objDoc){
			if(dummy.intersects(obj)){
				candidates.add(obj);
			}
		}
		return candidates;
	}
	
	public void setBackupSel(){
		backupSel.clear();
		backupSel.addAll(objSel);
	}
	
	public void restoreBackupSel(){
		objSel.clear();
		objSel.addAll(backupSel);
	}
	
	public void tryUndo(){
		Main.overrideUoft = true;
		Main.tryUndo();
		Main.overrideUoft = false;
	}
	
	public void tryRedo(){
		Main.overrideUoft = true;
		Main.tryRedo();
		Main.overrideUoft = false;
	}

}
