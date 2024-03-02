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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UC3IntegrationTest {
	private AnnotationConfigApplicationContext context;
    private ApplicationService applicationService;
    private Path testDirectory;
    private Path newTestDirectory;
    private Path sourceFile;

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
        
        //Source directory
        sourceFile = Files.createTempFile(testDirectory, "source", ".txt");
        Files.writeString(sourceFile, "This is a test file.");
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
	public void testUserPreferenceForDuplicateFiles() throws Exception {
	    applicationService.performAction(new String[]{"init", testDirectory.toString()});
	    Path originalFile = Files.createFile(testDirectory.resolve("report.txt"));
	    Files.writeString(originalFile, "Original content");

	    // Assuming the destination already has a file with the same name
	    Path destinationDir = testDirectory.resolve("Reports");
	    Files.createDirectory(destinationDir);
	    Path duplicateFile = destinationDir.resolve(originalFile.getFileName());
	    Files.writeString(duplicateFile, "Different content");

	    // Set user preference for handling duplicates to "overwrite"
	    //applicationService.setUserPreferenceForDuplicates("overwrite");

	    applicationService.performAction(new String[]{"copy", originalFile.toString(), destinationDir.toString()});

	    assertTrue(Files.exists(duplicateFile), "Duplicate file should exist post operation.");
	    assertEquals("Original content", Files.readString(duplicateFile), "Duplicate file content should match the source file based on user preference.");
	}
	
	@Test
	public void testFileNotExistAfterMove() throws Exception {
	    applicationService.performAction(new String[]{"init", testDirectory.toString()});
	    Path fileToMove = Files.createFile(testDirectory.resolve("presentation.pptx"));

	    Path destinationDir = testDirectory.resolve("Presentations");
	    Files.createDirectory(destinationDir);

	    applicationService.performAction(new String[]{"move", fileToMove.toString(), destinationDir.toString()});

	    assertFalse(Files.exists(fileToMove), "Original file should not exist after being moved.");
	    assertTrue(Files.exists(destinationDir.resolve(fileToMove.getFileName())), "Moved file should exist in the destination directory.");
	}
	
	@Test
	public void testFeedbackAfterUnsuccessfulOperation() throws Exception {
	    applicationService.performAction(new String[]{"init", testDirectory.toString()});
	    Path fileToMove = Files.createFile(testDirectory.resolve("diary.docx"));

	    Path nonExistentDir = testDirectory.resolve("NonExistent");

	    applicationService.performAction(new String[]{"move", fileToMove.toString(), nonExistentDir.toString()});

	    // Assuming `getFeedback` method retrieves operation feedback for the user
	    //String feedback = applicationService.getFeedback();
	    //assertNotNull(feedback, "Feedback should be provided for the unsuccessful operation.");
	    //assertTrue(feedback.contains("failed") || feedback.contains("non-existent"), "Feedback should indicate the reason for the operation's failure.");
	}
	
	@Test
	public void testHandlingReadOnlyFiles() throws Exception {
	    applicationService.performAction(new String[]{"init", testDirectory.toString()});
	    Path readOnlyFile = Files.createFile(testDirectory.resolve("readonlyfile.txt"));
	    Files.writeString(readOnlyFile, "This is read-only content.");
	    readOnlyFile.toFile().setReadOnly();

	    Path destinationDir = testDirectory.resolve("ReadOnlyFiles");
	    Files.createDirectory(destinationDir);

	    // Attempt to copy the read-only file
	    applicationService.performAction(new String[]{"copy", readOnlyFile.toString(), destinationDir.toString()});

	    // Verify the file was copied successfully, assuming the tool handles read-only attribute appropriately
	    Path copiedFile = destinationDir.resolve(readOnlyFile.getFileName());
	    assertTrue(Files.exists(copiedFile), "Read-only file should be copied successfully.");
	}
	
	@Test
	public void testRetryMechanismForUnavailableResources() throws Exception {
	    applicationService.performAction(new String[]{"init", testDirectory.toString()});
	    Path networkFile = testDirectory.resolve("networkResource.docx");
	    // Simulate creation and immediate unavailability of the file (e.g., locked or on a temporarily unavailable network drive)
	    Files.createFile(networkFile);
	    // Assuming the tool has a retry mechanism for handling such cases
	    
	    Path localDir = testDirectory.resolve("LocalDocuments");
	    Files.createDirectory(localDir);

	    // Attempt to copy the file, expecting the tool to retry upon initial failure
	    applicationService.performAction(new String[]{"copy", networkFile.toString(), localDir.toString()});

	    // Verify the operation eventually succeeds, assuming temporary unavailability resolves
	    assertTrue(Files.exists(localDir.resolve(networkFile.getFileName())), "File should be copied successfully after retrying.");
	}

	@Test
	public void testUserConfirmationForBatchOverwrites() throws Exception {
	    applicationService.performAction(new String[]{"init", testDirectory.toString()});
	    Path sourceDir = Files.createDirectory(testDirectory.resolve("BatchSource"));
	    Path file1 = Files.createFile(sourceDir.resolve("file1.txt"));
	    Files.writeString(file1, "Content of file1");
	    Path file2 = Files.createFile(sourceDir.resolve("file2.txt"));
	    Files.writeString(file2, "Content of file2");

	    Path destinationDir = Files.createDirectory(testDirectory.resolve("BatchDestination"));
	    // Pre-existing files in the destination to simulate overwrite scenario
	    Files.createFile(destinationDir.resolve("file1.txt"));
	    Files.createFile(destinationDir.resolve("file2.txt"));

	    // Simulate user confirmation for overwriting files
	    //applicationService.setUserConfirmationForOverwrites(true);

	    // Perform batch copy, expecting user confirmation to be handled
	    //applicationService.performBatchAction(new String[]{"copy", sourceDir.toString(), destinationDir.toString()});

	    // Verify files were overwritten as per user confirmation
	    assertTrue(Files.readString(destinationDir.resolve("file1.txt")).equals("Content of file1"), "file1.txt should be overwritten.");
	    assertTrue(Files.readString(destinationDir.resolve("file2.txt")).equals("Content of file2"), "file2.txt should be overwritten.");
	}

	


}
