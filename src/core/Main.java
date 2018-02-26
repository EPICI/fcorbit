package core;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import java.awt.*;
import java.awt.color.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

/**
 * The main class
 * 
 * @author EPICI
 * @version 1.0
 */
public class Main {
	
	/**
	 * How long to wait until the user is considered idle
	 */
	public static final long IDLE_MS = 100;
	
	/**
	 * Main window
	 */
	public static JFrame frame;
	/**
	 * Pane that holds the two editor views
	 */
	public static JSplitPane splitPane;
	/**
	 * Editor graphic view
	 */
	public static GraphicEditorPane graphicEditor;
	/**
	 * Scroll pane for editor text view
	 */
	public static JScrollPane textEditorScroll;
	/**
	 * Editor text view
	 */
	public static TextEditorPane textEditor;
	/**
	 * Editor text view document
	 */
	public static Document textDoc;
	/**
	 * Editor text view selection
	 */
	public static String textSel;
	/**
	 * Undo manager object which tracks changes and handles undo/redo
	 */
	public static TimedUndoManager textUndo;
	/**
	 * Editor internal document
	 */
	public static ArrayList<FCObj> objDoc;
	/**
	 * Editor internal selection
	 */
	public static ArrayList<FCObj> objSel;
	/**
	 * Changes every update, used to track idling
	 */
	public static long ticker;
	
	public static final PrintStream console = System.out;

	/**
	 * The main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Make the graphical objects
		frame = new JFrame("Orbit - Editor for Fantastic Contraption");
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		textEditorScroll = new JScrollPane();
		graphicEditor = new GraphicEditorPane();
		textDoc = new PlainDocument();
		textEditor = new TextEditorPane(textDoc,"",150,150);
		textSel = textEditor.getSelectedText();
		objDoc = new ArrayList<>();
		objSel = new ArrayList<>();
		textUndo = new TimedUndoManager();
		// Do layout
		textEditorScroll.setViewportView(textEditor);
		splitPane.add(graphicEditor);
		splitPane.add(textEditorScroll);
		frame.add(splitPane);
		// Set fields and initialize
		graphicEditor.objDoc = objDoc;
		graphicEditor.objSel = objSel;
		graphicEditor.setBackupSel();
		textUndo.useHook = true;
		textUndo.setLimit(100000);
		textDoc.addUndoableEditListener(textUndo);
		addListeners();
		graphicEditor.init();
		textEditor.init();
		// Finalize and show
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.pack();
		frame.setVisible(true);
	}
	
	public static void addListeners(){
		addKeyTracker(graphicEditor,textEditor);
		addKeyTracker(textEditor,graphicEditor);
		addUndoTracker(textEditor);
		addGraphicForwardListeners();
		textDoc.addDocumentListener(new DocumentListener(){

			@Override
			public void insertUpdate(DocumentEvent e) {
				if(allowUpdateObjFromText())updateObjDocumentFromText();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				if(allowUpdateObjFromText())updateObjDocumentFromText();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				if(allowUpdateObjFromText())updateObjDocumentFromText();
			}
			
		});
		textEditor.addCaretListener(new CaretListener(){
			// Caret is the text cursor, so this does selection listening as well

			@Override
			public void caretUpdate(CaretEvent e) {
				if(allowUpdateObjFromText())updateObjSelectionFromText();
			}
		
		});
		
	}
	
	/**
	 * Should we allow updating object from text?
	 * 
	 * @return
	 */
	public static boolean allowUpdateObjFromText(){
		return textEditor.isFocusOwner();
	}
	
	public static void addGraphicForwardListeners(){
		graphicEditor.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				// Don't use mouse click
				graphicEditor.command.mouseClicked(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				graphicEditor.mouseDown = e.getButton();
				graphicEditor.mouseDragged = false;
				graphicEditor.originMousex = graphicEditor.lastMousex = e.getX();
				graphicEditor.originMousey = graphicEditor.lastMousey = e.getY();
				graphicEditor.uanchorx = graphicEditor.anchorx;
				graphicEditor.uanchory = graphicEditor.anchory;
				graphicEditor.command.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				graphicEditor.command.mouseReleased(e);
				graphicEditor.mouseDown = 0;
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				graphicEditor.command.mouseEntered(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				graphicEditor.command.mouseExited(e);
			}
			
		});
		graphicEditor.addMouseMotionListener(new MouseMotionListener(){

			@Override
			public void mouseDragged(MouseEvent e) {
				graphicEditor.mouseDragged = true;
				graphicEditor.command.mouseDragged(e);
				graphicEditor.lastMousex = e.getX();
				graphicEditor.lastMousey = e.getY();
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				graphicEditor.command.mouseMoved(e);
				graphicEditor.lastMousex = e.getX();
				graphicEditor.lastMousey = e.getY();
			}
			
		});
		graphicEditor.addMouseWheelListener(new MouseWheelListener(){

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				graphicEditor.command.mouseWheelMoved(e);
			}
			
		});
		graphicEditor.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {
				graphicEditor.command.keyTyped(e);
			}

			@Override
			public void keyPressed(KeyEvent e) {
				graphicEditor.command.keyPressed(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {
				graphicEditor.command.keyReleased(e);
			}
			
		});
	}
	
	public static void parseTextTo(String source,ArrayList<FCObj> target,String format){
		if(source==null)return;
		for(String line:source.split("\n")){
			try{
				target.add(new FCObj(line,format));
			}catch(Exception e){
				
			}
		}
	}
	
	public static void updateObjFromText(){
		updateObjDocumentFromText();
		updateObjSelectionFromText();
	}
	
	public static void updateObjDocumentFromText(){
		String text = textEditor.getText();
		objDoc.clear();
		parseTextTo(text,objDoc,"fcml");
		ticker++;
		graphicEditor.repaint();
	}
	
	public static void updateObjSelectionFromText(){
		String text = textEditor.getSelectedText();
		objSel.clear();
		parseTextTo(text,objSel,"fcml");
		int sn = objSel.size();
		// Ensure objects are same reference
		HashMap<FCObj,FCObj> swap = new HashMap<>();
		for(FCObj obj:objDoc){
			swap.put(obj, obj);
		}
		for(int i=0;i<sn;i++){
			FCObj obj = swap.get(objSel.get(i));
			if(obj!=null)objSel.set(i, obj);
		}
		ticker++;
		graphicEditor.setBackupSel();
		graphicEditor.repaint();
	}
	
	public static void updateTextFromObj(){
		updateTextDocumentFromObj();
		updateTextSelectionFromObj();
	}
	
	public static void updateTextDocumentFromObj(){
		StringBuilder sb = new StringBuilder();
		for(String line:textEditor.getText().split("\n")){
			try{
				// Dummy object
				new FCObj(line,"fcml");
			}catch(Exception e){
				sb.append(line);
				sb.append('\n');
			}
		}
		for(FCObj obj:objDoc){
			sb.append(obj.toString("fcml"));
			sb.append('\n');
		}
		textEditor.setText(sb.toString());
		ticker++;
		textEditor.repaint();
	}
	
	public static void updateTextSelectionFromObj(){
		graphicEditor.setBackupSel();
		int sn = objSel.size();
		if(sn>0){
			ArrayList<FCObj> sortedSel = new ArrayList<>(objSel);
			Mapping.sort(sortedSel, (FCObj value)->objDoc.indexOf(value), Comparator.<Integer>naturalOrder());
			int i = 0;
			FCObj ref = sortedSel.get(i);
			int first = 0;
			boolean chain = false;
			int pos = 0;
			for(String line:textEditor.getText().split("\n")){
				try{
					FCObj other = new FCObj(line,"fcml");
					if(ref.equals(other)){
						if(!chain)first = pos;
						chain = true;
						i++;
						if(i==sn){
							textEditor.select(first, pos+line.length());
							break;
						}
						ref = sortedSel.get(i);
					}else if(chain){
						break;
					}
				}catch(Exception e){
					
				}
				pos += line.length()+1;
			}
		}
		ticker++;
		textEditor.repaint();
	}
	
	public static void tryUndo(){
		if(textUndo.canUndo()){
			textUndo.undo();
			updateObjFromText();
		}
	}
	
	public static void tryRedo(){
		if(textUndo.canRedo()){
			textUndo.redo();
			updateObjFromText();
		}
	}
	
	public static void addUndoTracker(JComponent comp){
		if(!(comp instanceof KeyTracker))throw new IllegalArgumentException("Does not implement the KeyTracker interface");
		KeyTracker kt = (KeyTracker) comp;
		comp.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				BitSet keys = kt.getKeys();
				boolean ctrl = keys.get(KeyEvent.VK_CONTROL);
				boolean shift = keys.get(KeyEvent.VK_SHIFT);
				boolean alt = keys.get(KeyEvent.VK_ALT);
				switch(e.getKeyCode()){
				case KeyEvent.VK_Z:{
					if(ctrl&&!alt){
						if(shift){// Ctrl+Shift+Z -> redo
							tryRedo();
						}else{// Ctrl+Z -> undo
							tryUndo();
						}
					}
					break;
				}
				case KeyEvent.VK_Y:{
					if(ctrl&&!shift&&!alt){// Ctrl+Y -> redo
						tryRedo();
					}
					break;
				}
				}
			}
			
		});
	}
	
	/**
	 * Add key tracker
	 * 
	 * @param comp
	 * @param other what to pair with
	 */
	public static void addKeyTracker(JComponent comp,JComponent other){
		if(!(comp instanceof KeyTracker))throw new IllegalArgumentException("Does not implement the KeyTracker interface");
		if(!(other instanceof KeyTracker))throw new IllegalArgumentException("Does not implement the KeyTracker interface");
		KeyTracker kt = (KeyTracker) comp;
		KeyTracker kto = (KeyTracker) other;
		comp.addMouseListener(new MouseListener(){

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
				// Can't receive key events if not focused
				e.getComponent().requestFocusInWindow();
				// Only forget when switching between editors
				kto.forget();
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
			
		});
		comp.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				kt.getKeys().set(e.getKeyCode());
			}

			@Override
			public void keyReleased(KeyEvent e) {
				kt.getKeys().clear(e.getKeyCode());
			}
			
		});
	}
	
	/**
	 * Sleep for some number of milliseconds
	 * 
	 * @param ms how long to sleep for
	 */
	public static void sleep(long ms){
		try{
			Thread.sleep(ms);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Schedule something to happen after a delay
	 * 
	 * @param ms how long to wait
	 * @param r what to do after the delay
	 */
	public static void runAfterDelay(long ms,Runnable r){
		new java.util.Timer().schedule(new TimerTask(){
			public void run(){
				r.run();
			}
		}, ms);
	}
	
	public static boolean setField(Object targetObject, String fieldName, Object fieldValue) {
	    Field field;
	    try {
	        field = targetObject.getClass().getDeclaredField(fieldName);
	    } catch (NoSuchFieldException e) {
	        field = null;
	    }
	    Class superClass = targetObject.getClass().getSuperclass();
	    while (field == null && superClass != null) {
	        try {
	            field = superClass.getDeclaredField(fieldName);
	        } catch (NoSuchFieldException e) {
	            superClass = superClass.getSuperclass();
	        }
	    }
	    if (field == null) {
	        return false;
	    }
	    field.setAccessible(true);
	    try {
	        field.set(targetObject, fieldValue);
	        return true;
	    } catch (IllegalAccessException e) {
	        return false;
	    }
	}
	
	public static Object getField(Object targetObject, String fieldName){
		Field field;
	    try {
	        field = targetObject.getClass().getDeclaredField(fieldName);
	    } catch (NoSuchFieldException e) {
	        field = null;
	    }
	    Class superClass = targetObject.getClass().getSuperclass();
	    while (field == null && superClass != null) {
	        try {
	            field = superClass.getDeclaredField(fieldName);
	        } catch (NoSuchFieldException e) {
	            superClass = superClass.getSuperclass();
	        }
	    }
	    if (field == null) {
	        return null;
	    }
	    field.setAccessible(true);
	    try {
	        return field.get(targetObject);
	    } catch (IllegalAccessException e) {
	        return null;
	    }
	}

}
