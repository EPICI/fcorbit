package core;

import java.awt.event.*;

/**
 * Represents either an active command or the root listener set
 * 
 * @author EPICI
 * @version 1.0
 */
public interface ActiveCommand extends MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	
	/**
	 * Cancel the command
	 */
	public void cancel();
	
}
