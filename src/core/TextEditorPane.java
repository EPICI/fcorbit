package core;

import java.util.*;
import java.awt.*;
import java.awt.color.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.*;

/**
 * The text editor component
 * 
 * @author EPICI
 * @version 1.0
 */
public class TextEditorPane extends JTextArea implements KeyTracker {
	
	/**
	 * Which keys are held down
	 */
	public final BitSet keys = new BitSet();
	
	public BitSet getKeys(){
		return keys;
	}

	public TextEditorPane() {
	}

	public TextEditorPane(String text) {
		super(text);
	}

	public TextEditorPane(Document doc) {
		super(doc);
	}

	public TextEditorPane(int rows, int columns) {
		super(rows, columns);
	}

	public TextEditorPane(String text, int rows, int columns) {
		super(text, rows, columns);
	}

	public TextEditorPane(Document doc, String text, int rows, int columns) {
		super(doc, text, rows, columns);
	}
	
	/**
	 * Call after done initializing fields
	 */
	public void init(){
		
	}
	
	public void forget(){
		keys.clear();
	}

}
