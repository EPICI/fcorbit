package core;

import javax.swing.text.*;
import javax.swing.undo.*;

public class TimedUndoManager extends UndoManager {
	
	public static final long WAIT_MS = 1000;
	
	public long timeLast = System.currentTimeMillis();
	
	/**
	 * Return how long it has been since the last call to this
	 * method or the object was created
	 * 
	 * @return
	 */
	public long pushTime(){
		long timeNow = System.currentTimeMillis();
		long result = timeNow - timeLast;
		timeLast = timeNow;
		return result;
	}
	
	@Override
	protected UndoableEdit editToBeUndone() {
        int i = (Integer)Main.getField(this, "indexOfNextAdd");
        while (i > 0) {
            SigEdit edit = (SigEdit)edits.elementAt(--i);
            if (edit.starts) {
                return edit;
            }
        }

        return null;
    }

	@Override
	protected UndoableEdit editToBeRedone() {
        int count = edits.size();
        int i = (Integer)Main.getField(this, "indexOfNextAdd");
        while (i < count) {
            SigEdit edit = (SigEdit)edits.elementAt(i++);
            if (edit.ends) {
                return edit;
            }
        }
        return null;
    }
	
	@Override
	public boolean addEdit(UndoableEdit anEdit){
		int ies = edits.size();
		long passed = pushTime();
		boolean newChain = passed>WAIT_MS;
		if(ies>0){
			SigEdit last = (SigEdit)lastEdit();
			last.ends = newChain;
		}
		boolean result = super.addEdit(new SigEdit(anEdit,newChain,true));
		return result;
	}
	
	public static class SigEdit implements UndoableEdit{
		
		public UndoableEdit view;
		public boolean starts;
		public boolean ends;
		
		public SigEdit(UndoableEdit ue,boolean s,boolean e){
			view = ue;
			starts = s;
			ends = e;
		}

		@Override
		public void undo() throws CannotUndoException {
			view.undo();
		}

		@Override
		public boolean canUndo() {
			return view.canUndo();
		}

		@Override
		public void redo() throws CannotRedoException {
			view.redo();
		}

		@Override
		public boolean canRedo() {
			return view.canRedo();
		}

		@Override
		public void die() {
			view.die();
		}

		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			return view.addEdit(anEdit);
		}

		@Override
		public boolean replaceEdit(UndoableEdit anEdit) {
			return view.replaceEdit(anEdit);
		}

		@Override
		public boolean isSignificant() {
			return starts;
		}

		@Override
		public String getPresentationName() {
			return view.getPresentationName();
		}

		@Override
		public String getUndoPresentationName() {
			return view.getUndoPresentationName();
		}

		@Override
		public String getRedoPresentationName() {
			return view.getRedoPresentationName();
		}
	}

}
