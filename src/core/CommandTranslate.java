package core;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * 
 * 
 * @author EPICI
 * @version 1.0
 */
public class CommandTranslate implements ActiveCommand{
	
	public final GraphicEditorPane view;
	
	public boolean done = false;
	public IdentityHashMap<FCObj,FCObj> backupDoc;
	public int initialx;
	public int initialy;
	
	/**
	 * Modify the translation direction
	 * <br>
	 * 0 is none, 1 is x local, 2 is x global,
	 * -1 is y local, -2 is y global
	 */
	public int direction = 0;
	
	public CommandTranslate(GraphicEditorPane view){
		this.view = view;
		initialx = view.lastMousex;
		initialy = view.lastMousey;
		backupDoc = new IdentityHashMap<>();
		for(FCObj obj:view.objSel){
			backupDoc.put(obj, new FCObj(obj));
		}
	}
	
	public void restoreBackupDoc(){
		for(FCObj obj:view.objSel){
			FCObj copy = backupDoc.get(obj);
			obj.copyFrom(copy);
		}
	}
	
	public double[] getTranslation(int mx,int my){
		final int direction = this.direction;
		int dx = mx-initialx;
		int dy = my-initialy;
		final double invScale = view.getInvScale();
		double wdx = dx*invScale;
		double wdy = dy*invScale;
		switch(direction){
		case 1:{
			final double r = Math.toRadians(view.objSel.get(0).r),
					cr = Math.cos(r),
					sr = Math.sin(r);
			wdx = wdx*cr;
			wdy = wdx*sr;
			break;
		}
		case 2:{
			wdy=0;
			break;
		}
		case -1:{
			final double r = Math.toRadians(view.objSel.get(0).r),
					cr = Math.cos(r),
					sr = Math.sin(r);
			wdy = wdy*cr;
			wdx = -wdy*sr;
			break;
		}
		case -2:{
			wdx=0;
			break;
		}
		}
		return new double[]{wdx,wdy};
	}
	
	public void updateMove(int mx,int my){
		restoreBackupDoc();
		double[] wdxy = getTranslation(mx,my);
		double wdx = wdxy[0];
		double wdy = wdxy[1];
		HashSet<FCObj> hashSel = new HashSet<>(view.objSel);
		for(FCObj obj:view.objDoc){
			if(hashSel.contains(obj)){
				obj.x += wdx;
				obj.y += wdy;
			}
		}
	}

	@Override
	public void cancel() {
		if(!done)restoreBackupDoc();
		view.repaint();
	}

	@Override
	public void render(Graphics2D g) {
		final double RAYLENGTH = 10000;
		final double scale = view.getScale();
		int width = view.getWidth(), height = view.getHeight();
		double cx = width*0.5, cy = height*0.5;
		AffineTransform ot = g.getTransform();
		g.translate(cx, cy);
		g.scale(scale, scale);
		g.translate(-view.anchorx, -view.anchory);
		FCObj first = view.objSel.get(0);
		double ox = first.x;
		double oy = first.y;
		if(direction==0){
			double[] wdxy = getTranslation(view.lastMousex,view.lastMousey);
			double wdx = wdxy[0];
			double wdy = wdxy[1];
			g.setColor(GraphicEditorPane.AXISXY);
			g.drawLine((int)(ox), (int)(oy), (int)(ox-wdx), (int)(oy-wdy));
		}else{
			double wdx,wdy;
			if(direction>0){
				g.setColor(GraphicEditorPane.AXISX);
				if(direction>1){
					wdx=1;
					wdy=0;
				}else{
					FCObj obj = view.objSel.get(0);
					final double r = Math.toRadians(obj.r);
					wdx = Math.cos(r);
					wdy = Math.sin(r);
				}
			}else{
				g.setColor(GraphicEditorPane.AXISY);
				if(direction<-1){
					wdx=0;
					wdy=1;
				}else{
					FCObj obj = view.objSel.get(0);
					final double r = Math.toRadians(obj.r);
					wdx = -Math.sin(r);
					wdy = Math.cos(r);
				}
			}
			Main.console.println(wdx+" "+wdy);
			wdx*=RAYLENGTH;
			wdy*=RAYLENGTH;
			g.drawLine((int)(ox-wdx), (int)(oy-wdy), (int)(ox+wdx), (int)(oy+wdy));
		}
		g.setTransform(ot);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		switch(view.mouseDown){
		case 1:{
			// Left click to confirm
			done=true;
			Main.updateTextFromObj();
			view.cancelCommand();
			break;
		}
		case 3:{
			// Right click to cancel
			view.cancelCommand();
			break;
		}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// Do movement
		updateMove(e.getX(),e.getY());
		view.repaint();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()){
		case KeyEvent.VK_ESCAPE:{
			view.cancelCommand();
			break;
		}
		case KeyEvent.VK_X:{
			direction = direction>0?direction-1:2;
			updateMove(view.lastMousex,view.lastMousey);
			view.repaint();
			break;
		}
		case KeyEvent.VK_Y:{
			direction = direction<0?direction+1:-2;
			updateMove(view.lastMousex,view.lastMousey);
			view.repaint();
			break;
		}
		}
	}
	
}
