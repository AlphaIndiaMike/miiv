package com.alphaindiamike.miiv.services;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class for managing application state.
 * Ensures that only one instance of this class is created and provides a global point of access to it.
 */
public final class ProgramStateService {
    // The single instance of the class
    private static volatile ProgramStateService instance;

    // Fields to hold the current state and its history
    private String currentState;
    private final List<String> stateHistory;

    // Listeners that are notified when the state changes
    private final List<StateChangeListener> listeners;

    /**
     * Private constructor to prevent instantiation.
     * Initializes the state and its history.
     */
    private ProgramStateService() {
        currentState = "Initial State";
        stateHistory = new ArrayList<>();
        listeners = new ArrayList<>();
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
     */
    public synchronized void setState(String newState) {
        if (validateState(newState)) {
            currentState = newState;
            stateHistory.add(newState);
            logStateChange();
            notifyListeners();
        }
    }

    /**
     * Returns the current state.
     *
     * @return the current state
     */
    public synchronized String getState() {
        return currentState;
    }

    /**
     * Resets the state to its initial condition.
     */
    public synchronized void resetState() {
        currentState = "Initial State";
        stateHistory.clear();
        notifyListeners();
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
            listener.onStateChange(currentState);
        }
    }

    /**
     * Validates the new state.
     * This method can be extended to include complex validation logic.
     *
     * @param newState the new state to validate
     * @return true if the new state is valid, false otherwise
     */
    private boolean validateState(String newState) {
        // Simple validation: new state must not be null or empty
        return newState != null && !newState.trim().isEmpty();
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
