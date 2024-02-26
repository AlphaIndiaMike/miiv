package com.alphaindiamike.miiv.services.workflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.alphaindiamike.miiv.privatelib.exceptions.DirectoryCreationException;
import com.alphaindiamike.miiv.privatelib.exceptions.InvalidPathException;

/**
 * Represents the initial state of the program, where initialization has not yet occurred.
 */
public class InitialState implements ProgramState {
    
    /**
     * Gets the current state's name.
     *
     * @return the name of the current state.
     */
    @Override
    public String getStateName() {
        return "Pending init";
    }

    /**
     * Handles the transition from the current state to the next, based on provided parameters.
     * In this case, transitions to a WorkspaceReadyState if the provided path is valid.
     *
     * @param context    The context in which the state operates, allowing for broader control.
     * @param nextState  The next state to transition to, not used in this implementation.
     * @param parameters Parameters required for the state transition, expects a valid path as the first parameter.
     * @return A new ProgramState representing the next state of the program.
     * @throws IllegalArgumentException if the parameters are not valid or do not meet the required conditions.
     */
    @Override
    public ProgramState handleStateChange(ProgramStateContext context, ProgramState nextState, String[] parameters) {
        if (parameters == null || parameters.length == 0) {
            throw new InvalidPathException("Expected a valid path as the first parameter.");
        }

        String pathString = parameters[0];
        Path path = Paths.get(pathString);

        // Check if the path exists; if not, attempt to create the directory.
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new DirectoryCreationException("Failed to create the directory at the provided path: " + pathString, e);
            }
        } else if (!Files.isDirectory(path)) {
            throw new InvalidPathException("The provided path exists but is not a directory: " + pathString);
        }

        // Create the .miiv subdirectory within the provided path
        Path miivPath = path.resolve(".miiv");
        if (!Files.exists(miivPath)) {
            try {
                Files.createDirectory(miivPath);
            } catch (IOException e) {
                throw new DirectoryCreationException("Failed to create the .miiv directory at the provided path: " + miivPath, e);
            }
        }
        // Assuming setupLocalDatabase and other necessary setup tasks are performed here
        
        // Transition to the provided nextState if all validations and setup are successful
        context.setState(nextState);
        return nextState;
    }
}