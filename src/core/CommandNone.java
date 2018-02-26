package core;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import core.GraphicEditorPane.*;

/**
 * Not a real command, this goes in when there's no command active
 * 
 * @author EPICI
 * @version 1.0
 */
public class CommandNone implements ActiveCommand{
	
	public final GraphicEditorPane view;
	
	public CommandNone(GraphicEditorPane view){
		this.view = view;
	}
	
	@Override
	public void cancel(){
		view.restoreBackupSel();
		view.repaint();
	}
	
	@Override
	public void render(Graphics2D g){
		if(view.mouseDown==1&&view.mouseDragged){
			// What would be selected
			final int mx = view.lastMousex, my = view.lastMousey, omx = view.originMousex, omy = view.originMousey;
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, GraphicEditorPane.OVERLAY_ALPHA));
			g.setColor(GraphicEditorPane.SELECTED_OVERLAY);
			g.fill(new Rectangle2D.Double(Math.min(mx, omx),Math.min(my, omy),Math.abs(mx-omx),Math.abs(my-omy)));
		}
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
			// Select
			boolean shift = view.keys.get(KeyEvent.VK_SHIFT);
			view.restoreBackupSel();
			if(view.mouseDragged){
				int mx = e.getX(), my = e.getY();
				ArrayList<FCObj> candidates = view.getSelectionArea(mx,my);
				if(candidates.size()>0){// Something will be selected
					for(FCObj obj:candidates){
						// No shift -> select all
						// Shift -> deselect all
						boolean oc = view.objSel.contains(obj);
						if(!oc&&!shift){
							view.objSel.add(obj);
						}else if(oc&&shift){
							view.objSel.remove(obj);
						}
					}
					Main.updateTextSelectionFromObj();
					view.repaint();
				}
			}else{
				int mx = e.getX(), my = e.getY();
				FCObj sel = view.getSelectionPoint(mx,my);
				if(sel!=null){// Something will be selected
					if(shift){
						// Try to remove it, and if it wasn't removed, add it
						if(!view.objSel.remove(sel))view.objSel.add(sel);
					}else{
						// Replace the current selection with the new one
						view.objSel.clear();
						view.objSel.add(sel);
					}
					Main.updateTextSelectionFromObj();
					view.repaint();
				}
			}
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
		switch(view.mouseDown){
		case 2:{
			// Pan view
			int x = e.getX();
			int y = e.getY();
			view.panned(view.lastMousex-x,view.lastMousey-y);
			break;
		}
		case 1:{
			// Selection preview
			view.restoreBackupSel();
			boolean shift = view.keys.get(KeyEvent.VK_SHIFT);
			int mx = e.getX(), my = e.getY();
			ArrayList<FCObj> candidates = view.getSelectionArea(mx,my);
			if(candidates.size()>0){// Something will be selected
				for(FCObj obj:candidates){
					// No shift -> select all
					// Shift -> deselect all
					boolean oc = view.objSel.contains(obj);
					if(!oc&&!shift){
						view.objSel.add(obj);
					}else if(oc&&shift){
						view.objSel.remove(obj);
					}
				}
			}
			view.repaint();
			break;
		}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// Selection preview
		view.restoreBackupSel();
		boolean shift = view.keys.get(KeyEvent.VK_SHIFT);
		int mx = e.getX(), my = e.getY();
		FCObj sel = view.getSelectionPoint(mx,my);
		if(sel!=null){// Something will be selected
			if(shift){
				// Try to remove it, and if it wasn't removed, add it
				if(!view.objSel.remove(sel))view.objSel.add(sel);
			}else{
				// Replace the current selection with the new one
				view.objSel.clear();
				view.objSel.add(sel);
			}
		}
		view.repaint();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		double by = e.getPreciseWheelRotation();
		if(view.keys.get(KeyEvent.VK_ALT)){// Pan
			// Hold shift to pan x instead of y
			boolean usex = view.keys.get(KeyEvent.VK_SHIFT);
			by *= GraphicEditorPane.PAN_RATE;
			if(usex){
				view.panned(by,0);
			}else{
				view.panned(0,by);
			}
			view.uanchorx=view.anchorx;
			view.uanchory=view.anchory;
		}else{// Zoom
			view.scaled(by*GraphicEditorPane.SCALE_RATE);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()){
		case KeyEvent.VK_SHIFT:{
			view.repaint();
			break;
		}
		case KeyEvent.VK_CONTROL:{
			view.repaint();
			break;
		}
		case KeyEvent.VK_ALT:{
			view.repaint();
			break;
		}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		boolean shift = view.keys.get(KeyEvent.VK_SHIFT);
		boolean ctrl = view.keys.get(KeyEvent.VK_CONTROL);
		boolean alt = view.keys.get(KeyEvent.VK_ALT);
		switch(e.getKeyCode()){
		case KeyEvent.VK_0:{
			// 0 -> center the view
			double tx=0,ty=0;
			if(view.objSel.size()>0){// If object is selected, go to that object instead
				FCObj obj = view.objSel.get(0);
				tx=obj.x;
				ty=obj.y;
			}
			view.anchorx=view.uanchorx=tx;
			view.anchory=view.uanchory=ty;
			view.repaint();
			break;
		}
		case KeyEvent.VK_Z:{
			if(!alt){
				if(ctrl){
					if(shift){// Redo
						view.tryRedo();
					}else{// Undo
						view.tryUndo();
					}
				}else{
					if(shift){// Toggle show grid and bounds
						view.showGrid=!view.showGrid;
					}else{// Toggle show wireframe
						view.showWireframe=!view.showWireframe;
					}
					view.repaint();
				}
			}
			break;
		}
		case KeyEvent.VK_Y:{
			if(ctrl&&!shift&&!alt){// Redo
				view.tryRedo();
			}
		}
		case KeyEvent.VK_X:{
			// Delete selection
			view.restoreBackupSel();
			if(view.objSel.size()>0){
				view.objDoc.removeAll(new HashSet<>(view.objSel));
				view.objSel.clear();
				Main.updateTextFromObj();
				view.repaint();
			}
			break;
		}
		case KeyEvent.VK_G:{
			// Translate selection
			if(view.objSel.size()>0){
				view.setCommand(new CommandTranslate(view));
			}
			break;
		}
		case KeyEvent.VK_SHIFT:{
			view.repaint();
			break;
		}
		case KeyEvent.VK_CONTROL:{
			view.repaint();
			break;
		}
		case KeyEvent.VK_ALT:{
			view.repaint();
			break;
		}
		}
	}
	
}