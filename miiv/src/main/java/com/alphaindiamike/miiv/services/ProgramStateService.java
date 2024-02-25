package com.alphaindiamike.miiv.services;

import java.util.ArrayList;
import java.util.List;

import com.alphaindiamike.miiv.services.workflow.ProgramState;
import com.alphaindiamike.miiv.services.workflow.ProgramStateContext;

/**
 * Singleton class for managing application state.
 * Ensures that only one instance of this class is created and provides a global point of access to it.
 */
public final class ProgramStateService {
    // The single instance of the class
    private static volatile ProgramStateService instance;
    
    private final ProgramStateContext stateContext;

    // Fields to hold the current state and its history
    private ProgramState prevState;
    private ProgramState currentState;
    private final List<String> stateHistory;

    // Listeners that are notified when the state changes
    private final List<StateChangeListener> listeners;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the state and its history.
     */
    private ProgramStateService() {
    	stateContext = new ProgramStateContext();
        stateHistory = new ArrayList<>();
        listeners = new ArrayList<>();
        addStateToHistory(stateContext.getCurrentStateName());
    }

    /**
     * Provides global access to the single instance of the class.
     * Uses double-checked locking to ensure thread safety.
     *
     * @return the single instance of ProgramStateService
     */
    public static ProgramStateService getInstance() {
        if (instance == null) {
            synchronized (ProgramStateService.class) {
                if (instance == null) {
                    instance = new ProgramStateService();
                }
            }
        }
        return instance;
    }

    /**
     * Sets the current state to a new value.
     * Validates the new state before updating.
     * Notifies listeners of the state change.
     *
     * @param newState the new state to be set
     * @param parameters: parameters required for state transition
     */
    public synchronized void triggerStateTransition(ProgramState newState, String[] parameters) {
    	prevState = currentState;
    	currentState = stateContext.changeState(newState, parameters);
    	if (true == validateState())
    	{
    		addStateToHistory(newState.getStateName());
    		notifyListeners();
        	logStateChange();
    	}
    }

    /**
     * Returns the current state.
     *
     * @return the current state
     */
    public synchronized String getState() {
        return stateContext.getCurrentStateName();
    }

    /**
     * Resets the state to its initial condition.
     */
    public synchronized void resetState() {
        stateContext.resetState();
        stateHistory.clear();
        notifyListeners();
    }
    
    /**
     * Adds a state to the history.
     * This method allows programmatically adding states to the history,
     * simulating state transitions for testing or initialization purposes.
     *
     * @param stateName The name of the state to add to the history.
     */
    public synchronized void addStateToHistory(String stateName) {
        if (stateName == null || stateName.trim().isEmpty()) {
            throw new IllegalArgumentException("State name cannot be null or empty.");
        }
        stateHistory.add(stateName);
    }

    /**
     * Returns a copy of the state history.
     *
     * @return a copy of the state history
     */
    public synchronized List<String> getStateHistory() {
        return new ArrayList<>(stateHistory);
    }

    /**
     * Adds a listener to be notified of state changes.
     *
     * @param listener the listener to add
     */
    public synchronized void addStateChangeListener(StateChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Notifies all registered listeners of a state change.
     */
    private void notifyListeners() {
        for (StateChangeListener listener : listeners) {
            listener.onStateChange(currentState.getStateName());
        }
    }

    /**
     * Validates the new state.
     * This method can be extended to include complex validation logic.
     *
     * @param newState the new state to validate
     * @return true if the new state is valid, false otherwise
     */
    private boolean validateState() {
        /* in Java, the != operator is used to compare references, 
         * not the content of the objects. If you have two different 
         * objects (instances) of the same class, even if they contain 
         * the same data, using != to compare them will return true 
         * because they are different objects with different memory 
         * addresses in the heap. */
        return currentState != prevState;
    }

    /**
     * Logs the state change to the console.
     * This method can be modified to log to a file or external system.
     */
    private void logStateChange() {
        System.out.println("State changed to: " + currentState);
    }

    /**
     * Interface for state change listeners.
     */
    public interface StateChangeListener {
        void onStateChange(String newState);
    }
}
