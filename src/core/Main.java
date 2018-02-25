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
		addKeyTracker(graphicEditor);
		addKeyTracker(textEditor);
		addUndoTracker(textEditor);
		addGraphicForwardListeners();
		textDoc.addDocumentListener(new DocumentListener(){

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateObjDocumentFromText();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateObjDocumentFromText();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateObjDocumentFromText();
			}
			
		});
		textEditor.addCaretListener(new CaretListener(){
			// Caret is the text cursor, so this does selection listening as well

			@Override
			public void caretUpdate(CaretEvent e) {
				updateObjSelectionFromText();
			}
		
		});
		
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
				graphicEditor.mouseDown = 0;
				graphicEditor.command.mouseReleased(e);
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
		ticker++;
		graphicEditor.repaint();
	}
	
	public static void updateTextFromObj(){
		
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
	 */
	public static void addKeyTracker(JComponent comp){
		if(!(comp instanceof KeyTracker))throw new IllegalArgumentException("Does not implement the KeyTracker interface");
		KeyTracker kt = (KeyTracker) comp;
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
			}

			@Override
			public void mouseExited(MouseEvent e) {
				kt.forget();
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
