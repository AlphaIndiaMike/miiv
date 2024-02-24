package com.alphaindiamike.miiv.units;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alphaindiamike.miiv.services.ProgramStateService;
import com.alphaindiamike.miiv.services.ProgramStateService.StateChangeListener;
import com.alphaindiamike.miiv.services.pstate.WorkspaceReadyState;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class ProgramStateServiceUT {
	ProgramStateService service;
    private final Path testFolderPath = Paths.get(".//test");

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
        service.setState(new WorkspaceReadyState(".//test"),new String[]{".//test"});
        assertEquals("Ready", service.getState(), "The current state should be updated to the new state");
    }

    @Test
    void testSetStateWithInvalidValue() {
        String initialState = service.getState();
        service.setState(null, null); // Attempt to set an invalid state
        assertEquals(initialState, service.getState(), "The state should not change when given an invalid new state");
        
        service.setState(new WorkspaceReadyState(".//test"),new String[]{""}); // Another attempt with a different invalid value
        assertEquals(initialState, service.getState(), "The state should remain unchanged with empty new state");
    }

    @Test
    void testStateHistory() {
        String[] states = {"Pending init", "Ready"};
        service.setState(new WorkspaceReadyState(".//test"),new String[]{".//test"});

        List<String> stateHistory = service.getStateHistory();
        assertArrayEquals(states, stateHistory.toArray(), "The state history should match the states set");
    }

    @Test
    void testResetState() {
    	service.setState(new WorkspaceReadyState(".//test"),new String[]{".//test"});
        service.resetState();
        assertEquals("Pending init", service.getState(), "The state should be reset to the initial state");
        assertTrue(service.getStateHistory().isEmpty(), "The state history should be cleared after reset");
    }

    @Test
    void testListenerNotification() {
        final String[] notifiedState = {null};
        StateChangeListener listener = newState -> notifiedState[0] = newState;
        service.addStateChangeListener(listener);

        String newState = "Listener State";
        service.setState(new WorkspaceReadyState(".//test"),new String[]{".//test"});
        assertEquals(newState, notifiedState[0], "The listener should be notified of the state change");
    }
}
