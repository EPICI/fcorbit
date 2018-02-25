package core;

import java.util.*;

/**
 * Component that tracks for itself which keys are pressed
 * 
 * @author EPICI
 * @version 1.0
 */
public interface KeyTracker {
	
	/**
	 * Get the {@link BitSet} for which if a key is pressed
	 * then the bit corresponding to its keycode is set
	 * 
	 * @return
	 */
	public BitSet getKeys();
	
	/**
	 * Reset whatever state matters
	 */
	public void forget();

}
