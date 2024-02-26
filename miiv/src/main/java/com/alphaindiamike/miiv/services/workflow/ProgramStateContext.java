package com.alphaindiamike.miiv.services.workflow;

public class ProgramStateContext {
	    private ProgramState currentState;

	    public ProgramStateContext() {
	        this.currentState = new InitialState(); // Starting state
	    }
	    
	    public void resetState() {
	    	this.currentState = new InitialState(); // Starting state
	    }
	    
	    /* Used by the state transition to register the new state. */
	    public void setState(ProgramState state) {
	    	currentState = state;
	    }

	    public ProgramState changeState(ProgramState nextState, String[] params) {
	    	return currentState.handleStateChange(this, nextState, params);
	    }

	    public String getCurrentStateName() {
	        return currentState.getStateName();
	    }

}
