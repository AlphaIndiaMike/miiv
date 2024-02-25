package com.alphaindiamike.miiv.journey;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.alphaindiamike.miiv.App;
import com.alphaindiamike.miiv.services.ApplicationService;
import com.alphaindiamike.miiv.services.GlobalSettingsService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InitCommandIntegrationTest {
	private AnnotationConfigApplicationContext context;
    private ApplicationService applicationService;
    private Path testDirectory;
    private Path newTestDirectory;

    @BeforeEach
    void setUp() throws Exception {
    	// Initialize the Spring application context
        context = new AnnotationConfigApplicationContext(App.class);

        // Retrieve the ApplicationService bean from the application context
        applicationService = context.getBean(ApplicationService.class);

        // Setup test environment: Create a temporary directory
        // Example location: /var/folders/pf/ydbt132x60xbwrxnnxjpy5t00000gn/T/test_dir13902068303723727890
        testDirectory = Files.createTempDirectory("test_dir");
        // Setup test environment: Create another temporary directory
        // Example location: /var/folders/pf/ydbt132x60xbwrxnnxjpy5t00000gn/T/test_dir13902068303723727890
        newTestDirectory = Files.createTempDirectory("new_test_dir");
        
        // Remove workspace setting for a clean shot
        GlobalSettingsService glbSet = context.getBean(GlobalSettingsService.class);
        glbSet.deleteSetting("workspace_dir");
    }

    @AfterEach
    void tearDown() throws Exception {
    	// Close Spring context
        context.close();
        // Clean up the test directory
        Files.walk(testDirectory)
             .sorted((o1, o2) -> -o1.compareTo(o2)) // Sort in reverse order to delete contents before the directory
             .map(Path::toFile)
             .forEach(File::delete); // Delete each file and directory
    }

    @Test
    public void testInitCommandCreatesCorrectStructure() throws Exception {
        // Execute the 'init' command through your ApplicationService
        applicationService.performAction(new String[]{"init", testDirectory.toString()});

        // Verify the expected directory structure.
        // This list should reflect the top-level structure as defined in your 'structure.json'.
        // Expand this list based on your actual expected structure.
        List<String> expectedDirectories = List.of(
            "01 Personal",
            "02 Work",
            "03 Quick Share",
            "04 Software",
            "05 Backups",
            "99 Archive"
        );

        // Check each expected directory exists
        expectedDirectories.forEach(dirName -> {
            Path dirPath = testDirectory.resolve(dirName);
            assertTrue(Files.exists(dirPath), "Missing expected directory: " + dirName);

            // For directories like "04 Software" with expected subdirectories,
            // perform additional checks here.
            if (dirName.equals("04 Software")) {
                List<String> expectedSubDirs = List.of("01 Windows", "02 Linux", "03 Mac", "04 Android");
                expectedSubDirs.forEach(subDir -> assertTrue(
                    Files.exists(dirPath.resolve(subDir)),
                    "Missing expected subdirectory under " + dirName + ": " + subDir
                ));
            }
        });

        // Add more specific checks for file existence, empty directories, etc., as needed.
    }
    
    @Test
    public void testInitCommandShouldNotBeCalledTwice() throws Exception {
        // Call 'init' for the first time
        applicationService.performAction(new String[]{"init", testDirectory.toString()});

        // Attempt to call 'init' a second time
        applicationService.performAction(new String[]{"init", testDirectory.toString()}); // because it's on the other state; we need a state reset here
        
        // Expected message indicating 'init' has already been executed and suggesting the use of 'set'
        String expectedMessage = "Init command has already been executed. Please use 'set' to change the workspace location.";
        assertEquals(expectedMessage, applicationService.getErrorResponse(), "The expected message for calling 'init' a second time was not returned.");
    }
    
    @Test
    public void testSetCommandWithoutPriorInit() throws Exception {
        // Attempt to call 'set' without calling 'init' first
        applicationService.performAction(new String[]{"set", testDirectory.toString()});

        // Expected message indicating 'init' must be called before 'set'
        String expectedMessage = "Please run 'miiv init {path}' before using 'set'.";
        assertEquals(expectedMessage, applicationService.getErrorResponse(), "The expected message for calling 'set' without 'init' was not returned.");
    }
    
    @Test
    public void testSetCommandChangesWorkspaceLocation() throws Exception {
        // Setup: Call 'init' to initialize the workspace
        applicationService.performAction(new String[]{"init", testDirectory.toString()});

        // Execute: Call 'set' to change the workspace location
        applicationService.performAction(new String[]{"set", newTestDirectory.toString()});

        // Verify: Check the workspace location has been updated
        // This step depends on how your application confirms a successful 'set' operation.
        // Assuming there's a method to retrieve the current workspace location:
        String expectedMessage = "Workspace succesfully changed to: "+newTestDirectory.toString();
        assertEquals(expectedMessage, applicationService.getResponse(), "Workspace location was not updated correctly.");
        assertTrue(applicationService.getIsPositiveResponse(), "Workspace location was not updated correctly.");
        
        // Verify the expected directory structure.
        // This list should reflect the top-level structure as defined in your 'structure.json'.
        // Expand this list based on your actual expected structure.
        List<String> expectedDirectories = List.of(
            "01 Personal",
            "02 Work",
            "03 Quick Share",
            "04 Software",
            "05 Backups",
            "99 Archive"
        );

        // Check each expected directory exists
        expectedDirectories.forEach(dirName -> {
            Path dirPath = newTestDirectory.resolve(dirName);
            assertTrue(Files.exists(dirPath), "Missing expected directory: " + dirName);

            // For directories like "04 Software" with expected subdirectories,
            // perform additional checks here.
            if (dirName.equals("04 Software")) {
                List<String> expectedSubDirs = List.of("01 Windows", "02 Linux", "03 Mac", "04 Android");
                expectedSubDirs.forEach(subDir -> assertTrue(
                    Files.exists(dirPath.resolve(subDir)),
                    "Missing expected subdirectory under " + dirName + ": " + subDir
                ));
            }
        });
    }
}
