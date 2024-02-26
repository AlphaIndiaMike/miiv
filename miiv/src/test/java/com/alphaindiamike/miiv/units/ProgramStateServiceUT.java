package com.alphaindiamike.miiv.units;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alphaindiamike.miiv.privatelib.exceptions.DirectoryCreationException;
import com.alphaindiamike.miiv.privatelib.exceptions.InvalidPathException;
import com.alphaindiamike.miiv.services.ProgramStateService;
import com.alphaindiamike.miiv.services.ProgramStateService.StateChangeListener;
import com.alphaindiamike.miiv.services.workflow.WorkspaceReadyState;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgramStateServiceUT {
	ProgramStateService service;
    private final Path testFolderPath = Paths.get(".//test");
    private static final Logger logger = LoggerFactory.getLogger(ProgramStateServiceUT.class);

    @BeforeEach
    void setUp() throws Exception {
        resetSingleton(ProgramStateService.class);
        service = ProgramStateService.getInstance();
        // Any additional setup can go here
    }

    @AfterEach
    void tearDown() throws Exception {
        // Resetting the singleton after each test ensures test isolation
        resetSingleton(ProgramStateService.class);
        deleteTestFolder();
    }
    
    private void deleteTestFolder() throws IOException {
        if (Files.exists(testFolderPath)) {
            Files.walk(testFolderPath)
                 .sorted((o1, o2) -> -o1.compareTo(o2)) // Sort in reverse order
                 .map(Path::toFile)
                 .forEach(file -> {
                     if (!file.delete()) {
                         System.err.println("Failed to delete " + file + " during teardown.");
                     }
                 });
            
            if (Files.exists(testFolderPath)) {
                throw new IOException("Failed to delete the test folder at: " + testFolderPath);
            }
        }
    }

    // Utility method to reset the singleton instance using reflection
    private void resetSingleton(Class<?> clazz) throws Exception {
        Field instance = clazz.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
    
    @Test
    void testSingletonBehavior() {
        ProgramStateService anotherInstance = ProgramStateService.getInstance();
        assertSame(service, anotherInstance, "Expected both instances to be the same");
    }

    @Test
    void testSetState() {
        service.triggerStateTransition(new WorkspaceReadyState(".//test"),new String[]{".//test"});
        assertEquals("Ready", service.getState(), "The current state should be updated to the new state");
    }

    @Test
    void testSetStateWithInvalidValue() {
    	try {
	        String initialState = service.getState();
	        service.triggerStateTransition(null, null); // Attempt to set an invalid state
	        assertEquals(initialState, service.getState(), "The state should not change when given an invalid new state");
	        
	        service.triggerStateTransition(new WorkspaceReadyState(".//test"),new String[]{""}); // Another attempt with a different invalid value
	        assertEquals(initialState, service.getState(), "The state should remain unchanged with empty new state");
    	} catch (InvalidPathException | DirectoryCreationException e) {
            // Log the exception using your preferred logging framework
    		logger.error("Caught exception: {}", e.getMessage());
        }
    }

    @Test
    void testStateHistory() {
        String[] states = {"Pending init", "Ready"};
        service.triggerStateTransition(new WorkspaceReadyState(".//test"),new String[]{".//test"});

        List<String> stateHistory = service.getStateHistory();
        assertArrayEquals(states, stateHistory.toArray(), "The state history should match the states set");
    }

    @Test
    void testResetState() {
    	service.triggerStateTransition(new WorkspaceReadyState(".//test"),new String[]{".//test"});
        service.resetState();
        assertEquals("Pending init", service.getState(), "The state should be reset to the initial state");
        assertTrue(service.getStateHistory().isEmpty(), "The state history should be cleared after reset");
    }

    @Test
    void testListenerNotification() {
        // An array to hold the notified state. Arrays are used here to allow modification within the lambda.
        final String[] notifiedState = {null};

        // Implementing StateChangeListener using a lambda expression.
        // This listener updates notifiedState[0] when a state change occurs.
        StateChangeListener listener = newState -> notifiedState[0] = newState;
        
        // Registering the listener with the service. This ensures that our listener
        // will be notified of any state changes that occur in the service.
        service.addStateChangeListener(listener);

        // The expected new state after the transition.
        String newState = "Ready";

        // Triggering a state transition in the service. This should cause the service
        // to notify all registered listeners, including ours, about the change.
        service.triggerStateTransition(new WorkspaceReadyState(".//test"), new String[]{".//test"});

        // Asserting that our listener was indeed notified of the state change
        // and that the notified state matches the expected new state ("Ready").
        // This assertion verifies that the listener mechanism is functioning as expected.
        assertEquals(newState, notifiedState[0], "The listener should be notified of the state change");
    }

}
