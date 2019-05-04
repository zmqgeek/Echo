package reduction.ttg;

import java.io.Serializable;

import reduction.ttg.node.ErrorState;

/**
 * TTGNode represents testing state during testing. 
 * It can be a NormalState or a ErrorState.
 * 
 * @author echo
 */
public abstract class TTGNode implements Serializable {
	public boolean isEntry() {
		return entry;
	}
	
	public void setAsEntry() {
		entry = true;
	}
	
	public int getID() {
		return id;
	}
	
	// Whether the TTGNode is the error state.
	public boolean isErrorState() {
		return this.getClass().equals(ErrorState.class);
	}
	
	// Whether the TTGNode is the entry node.
	protected boolean entry;
	protected int id;
}
