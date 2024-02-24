package com.alphaindiamike.miiv.services.pstate;

import java.util.ArrayList;
import java.util.List;

import com.alphaindiamike.miiv.services.ProgramStateService;

public class ProgramStateContext {
	    private ProgramState currentState;
	    private final List<String> stateHistory = new ArrayList<>();
	    private final List<ProgramStateService.StateChangeListener> listeners = new ArrayList<>();

	    public ProgramStateContext() {
	        this.currentState = new InitialState(); // Starting state
	    }
	    
	    public void resetState() {
	    	this.currentState = new InitialState(); // Starting state
	    }

	    public void setState(ProgramState newState) {
	        this.currentState = newState;
	        stateHistory.add(newState.getState());
	    }

	    public void changeState(ProgramState nextState, String[] params) {
	    	currentState.handleStateChange(this, nextState, params);
	        notifyListeners();
	    }

	    public String getCurrentState() {
	        return currentState.getState();
	    }

	    public List<String> getStateHistory() {
	        return new ArrayList<>(stateHistory);
	    }

	    public void addStateChangeListener(ProgramStateService.StateChangeListener listener) {
	        listeners.add(listener);
	    }

	    private void notifyListeners() {
	        for (ProgramStateService.StateChangeListener listener : listeners) {
	            listener.onStateChange(getCurrentState());
	        }
	    }

		public void WorkspaceReadyState(String pathString) {
			// TODO Auto-generated method stub
			
		}
}
