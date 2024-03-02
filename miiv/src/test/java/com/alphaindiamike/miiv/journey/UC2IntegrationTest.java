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
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UC2IntegrationTest {
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
    public void testCopyFileToDestination() throws Exception {
        // Test copying a file to a destination directory
        applicationService.performAction(new String[]{"copy", sourceFile.toString(), testDirectory.toString()});

        Path copiedFilePath = testDirectory.resolve(sourceFile.getFileName());
        assertTrue(Files.exists(copiedFilePath), "File was not copied successfully.");
    }

    @Test
    public void testCopyFileToNonExistentDestination() throws Exception {
        // Test copying a file to a non-existent destination directory
        Path nonExistentDest = testDirectory.resolve("nonexistent");
        applicationService.performAction(new String[]{"copy", sourceFile.toString(), nonExistentDest.toString()});

        assertFalse(Files.exists(nonExistentDest.resolve(sourceFile.getFileName())), "File should not be copied to a non-existent directory.");
    }

    @Test
    public void testMoveFileToDestination() throws Exception {
        // Test moving a file to a destination directory
        applicationService.performAction(new String[]{"move", sourceFile.toString(), testDirectory.toString()});

        Path movedFilePath = testDirectory.resolve(sourceFile.getFileName());
        assertTrue(Files.exists(movedFilePath), "File was not moved successfully.");
        assertFalse(Files.exists(sourceFile), "Source file still exists after move operation.");
    }

    @Test
    public void testMoveFileToNonExistentDestination() throws Exception {
        // Test moving a file to a non-existent destination directory
        Path nonExistentDest = testDirectory.resolve("nonexistent");
        applicationService.performAction(new String[]{"move", sourceFile.toString(), nonExistentDest.toString()});

        assertFalse(Files.exists(nonExistentDest.resolve(sourceFile.getFileName())), "File should not be moved to a non-existent directory.");
        assertTrue(Files.exists(sourceFile), "Source file should not disappear if move operation fails.");
    }

    @Test
    public void testCopyFileAndOverwriteExisting() throws Exception {
        // Setup: Create a file at the destination with the same name
        Path destinationFile = Files.createFile(testDirectory.resolve(sourceFile.getFileName()));
        Files.writeString(destinationFile, "Old content");

        // Test copying a file and overwriting the existing one
        applicationService.performAction(new String[]{"copy", sourceFile.toString(), testDirectory.toString()});
        
        String content = Files.readString(destinationFile);
        assertEquals("This is a test file.", content, "Destination file content should be overwritten.");
    }

    @Test
    public void testMoveFileAndOverwriteExisting() throws Exception {
        // Setup: Create a file at the destination with the same name
        Path destinationFile = Files.createFile(testDirectory.resolve(sourceFile.getFileName()));
        Files.writeString(destinationFile, "Old content");

        // Test moving a file and overwriting the existing one
        applicationService.performAction(new String[]{"move", sourceFile.toString(), testDirectory.toString()});
        
        assertTrue(Files.exists(destinationFile), "Destination file should exist after move.");
        assertFalse(Files.exists(sourceFile), "Source file should not exist after move.");
        String content = Files.readString(destinationFile);
        assertEquals("This is a test file.", content, "Destination file content should be overwritten.");
    }
    
    @Test
    public void testCopyFileWithSpecialCharacters() throws Exception {
        // Setup: Create a file with special characters in the name
        Path specialCharFile = Files.createFile(testDirectory.resolve("special:file?.txt"));
        Files.writeString(specialCharFile, "Content of special file");

        // Execute: Copy the file
        applicationService.performAction(new String[]{"copy", specialCharFile.toString(), testDirectory.toString()});

        // Verify: The file with special characters is copied
        assertTrue(Files.exists(testDirectory.resolve("special:file?.txt")), "File with special characters was not copied.");
    }

    @Test
    public void testCopyFileWithPermissions() throws Exception {
        // This test assumes a Unix-like system where file permissions are meaningful
        // Setup: Create a file and set permissions
        Path fileWithPermissions = Files.createFile(testDirectory.resolve("fileWithPerms.txt"));
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-----");
        Files.setPosixFilePermissions(fileWithPermissions, perms);

        // Execute: Copy the file
        applicationService.performAction(new String[]{"copy", fileWithPermissions.toString(), testDirectory.toString()});

        // Verify: The file is copied with the same permissions
        Path copiedFile = testDirectory.resolve(fileWithPermissions.getFileName());
        assertTrue(Files.exists(copiedFile), "File with permissions was not copied.");
        assertEquals(perms, Files.getPosixFilePermissions(copiedFile), "Copied file permissions do not match source file.");
    }

    @Test
    public void testHandleSymbolicLink() throws Exception {
        // Setup: Create a symbolic link
        Path targetFile = Files.createFile(testDirectory.resolve("targetFile.txt"));
        Path symlink = Files.createSymbolicLink(testDirectory.resolve("symlink"), targetFile);

        // Execute: Copy the symbolic link
        applicationService.performAction(new String[]{"copy", symlink.toString(), testDirectory.toString()});

        // Verify: The symbolic link is copied (not the target file)
        Path copiedLink = testDirectory.resolve(symlink.getFileName());
        assertTrue(Files.isSymbolicLink(copiedLink), "Symbolic link was not copied as a link.");
    }
    
    @Test
    public void testCopyFileAfterInit() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        applicationService.performAction(new String[]{"copy", sourceFile.toString(), testDirectory.toString()});
        
        assertTrue(Files.exists(testDirectory), "The file should be copied successfully.");
    }
    
    @Test
    public void testMoveFileAfterInit() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        applicationService.performAction(new String[]{"move", sourceFile.toString(), testDirectory.toString()});
        
        assertTrue(Files.exists(testDirectory), "The file should be moved successfully.");
        assertFalse(Files.exists(sourceFile), "The source file should no longer exist after moving.");
    }

    @Test
    public void testAttemptCopyWithoutInit() throws Exception {
        applicationService.performAction(new String[]{"copy", sourceFile.toString(), testDirectory.toString()});
        
        // Assuming applicationService.getErrorResponse() returns an error if 'init' hasn't been performed
        String errorMessage = applicationService.getErrorResponse();
        assertNotNull(errorMessage, "An error message should be returned when attempting to copy without init.");
    }

    @Test
    public void testAttemptMoveWithoutInit() throws Exception {
        applicationService.performAction(new String[]{"move", sourceFile.toString(), testDirectory.toString()});
        
        String errorMessage = applicationService.getErrorResponse();
        assertNotNull(errorMessage, "An error message should be returned when attempting to move without init.");
    }

    @Test
    public void testCopyWithWorkspaceChangedViaSet() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        
        // Changing workspace directory
        Path newWorkspaceDir = Files.createTempDirectory("newMiivWorkspace");
        applicationService.performAction(new String[]{"set", newWorkspaceDir.toString()});
        
        Path newDestinationPath = newWorkspaceDir.resolve("newDestination.txt");
        applicationService.performAction(new String[]{"copy", sourceFile.toString(), newDestinationPath.toString()});
        
        assertTrue(Files.exists(newDestinationPath), "The file should be copied successfully in the new workspace.");
    }

    @Test
    public void testMoveWithWorkspaceChangedViaSet() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        
        Path newWorkspaceDir = Files.createTempDirectory("newMiivWorkspace");
        applicationService.performAction(new String[]{"set", newWorkspaceDir.toString()});
        
        Path newSourceFile = Files.createTempFile(newWorkspaceDir, "newSource", ".txt");
        Files.writeString(newSourceFile, "Content for testing after workspace set");
        Path newDestinationPath = newWorkspaceDir.resolve("newMovedDestination.txt");
        
        applicationService.performAction(new String[]{"move", newSourceFile.toString(), newDestinationPath.toString()});
        
        assertTrue(Files.exists(newDestinationPath), "The file should be moved successfully in the new workspace.");
        assertFalse(Files.exists(newSourceFile), "The new source file should no longer exist after moving.");
    }
    
    @Test
    public void testCopyToNonWritableDestination() throws Exception {
        // Initial setup including 'init'
        // Attempt to copy a file to a non-writable destination
        // Assert that an appropriate error message is returned
    }

    @Test
    public void testCopyNonExistentSourceFile() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        Path nonExistentFile = testDirectory.resolve("nonExistent.txt");
        applicationService.performAction(new String[]{"copy", nonExistentFile.toString(), testDirectory.toString()});
        
        String errorMessage = applicationService.getErrorResponse();
        assertNotNull(errorMessage, "An error message should be returned when attempting to copy a non-existent file.");
    }
    
    @Test
    public void testCopyPdfFileToDesignatedDirectory() throws Exception {
        // Setup: Initialize workspace and simulate creating a PDF file
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        Path pdfFile = testDirectory.resolve("example.pdf");
        Files.createFile(pdfFile);
        // Simulating user input for designated directory for PDFs
        Path designatedPdfDir = testDirectory.resolve("PDFs");
        Files.createDirectory(designatedPdfDir);

        // Execute: Copy the PDF file to the designated directory
        applicationService.performAction(new String[]{"copy", pdfFile.toString(), designatedPdfDir.toString()});

        // Verify: PDF file exists in the designated directory
        assertTrue(Files.exists(designatedPdfDir.resolve(pdfFile.getFileName())));
    }

    @Test
    public void testHandleDuplicateFilesOnCopy() throws Exception {
        // Setup: Initialize workspace and create a source file
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        Path sourceFile = testDirectory.resolve("duplicateFile.txt");
        Files.writeString(sourceFile, "This is a test file.");

        // Simulating the existence of a duplicate file in the destination
        Path destinationDir = testDirectory.resolve("Documents");
        Files.createDirectory(destinationDir);
        Path duplicateFile = destinationDir.resolve(sourceFile.getFileName());
        Files.writeString(duplicateFile, "Existing file content.");

        // Simulate user decision to overwrite the existing file
        // Assuming `performAction` can simulate or receive user input for decisions
        //applicationService.setUserDecision("overwrite");

        // Execute: Attempt to copy the file, triggering the duplicate handling logic
        applicationService.performAction(new String[]{"copy", sourceFile.toString(), destinationDir.toString()});

        // Verify: The original file has been overwritten
        String fileContent = Files.readString(destinationDir.resolve(sourceFile.getFileName()));
        assertEquals("This is a test file.", fileContent, "The duplicate file should have been overwritten with the source content.");
    }

    @Test
    public void testMoveFileAndPreserveOriginalOnUserCancel() throws Exception {
        // Setup: Initialize workspace and create a source file
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        Path sourceFile = testDirectory.resolve("importantFile.txt");
        Files.writeString(sourceFile, "Important content");

        // Destination directory
        Path destinationDir = testDirectory.resolve("Important");
        Files.createDirectory(destinationDir);

        // Simulate user decision to cancel the move due to some reason (e.g., destination issues)
        //applicationService.setUserDecision("cancel");

        // Execute: Attempt to move the file, but user decides to cancel
        applicationService.performAction(new String[]{"move", sourceFile.toString(), destinationDir.toString()});

        // Verify: The source file still exists, and the move was not completed
        assertTrue(Files.exists(sourceFile), "Source file should remain after cancelling the move operation.");
        assertFalse(Files.exists(destinationDir.resolve(sourceFile.getFileName())), "Destination should not have the file after cancellation.");
    }
    
    @Test
    public void testAutoCategorizeAndCopyJpgFile() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        Path jpgFile = Files.createFile(testDirectory.resolve("example.jpg"));
        // Assuming the tool has predefined rules or user settings for categorizing JPG files
        Path imagesDir = testDirectory.resolve("Images");
        Files.createDirectory(imagesDir);

        applicationService.performAction(new String[]{"copy", jpgFile.toString(), imagesDir.toString()});

        assertTrue(Files.exists(imagesDir.resolve(jpgFile.getFileName())), "JPG file should be copied to the Images directory.");
    }
    
    @Test
    public void testPromptUserForMp3FileDestination() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        Path mp3File = Files.createFile(testDirectory.resolve("song.mp3"));
        // Assuming the application prompts the user for a destination directory for MP3 files
        Path musicDir = testDirectory.resolve("Music");
        Files.createDirectory(musicDir);
        
        // Simulate user input or decision for the destination directory
        //applicationService.setUserDecision(musicDir.toString());

        applicationService.performAction(new String[]{"copy", mp3File.toString(), musicDir.toString()});

        assertTrue(Files.exists(musicDir.resolve(mp3File.getFileName())), "MP3 file should be copied to the Music directory based on user input.");
    }
    
    @Test
    public void testDuplicateHandlingForMultipleFileTypes() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        Path pdfFile = Files.createFile(testDirectory.resolve("document.pdf"));
        Path mp3File = Files.createFile(testDirectory.resolve("track.mp3"));

        Path docsDir = testDirectory.resolve("Documents");
        Files.createDirectory(docsDir);
        Path musicDir = testDirectory.resolve("Music");
        Files.createDirectory(musicDir);

        // Create duplicates in the destination directories
        Files.createFile(docsDir.resolve(pdfFile.getFileName()));
        Files.createFile(musicDir.resolve(mp3File.getFileName()));

        // Simulate user decision to overwrite for both types
        // applicationService.setUserDecision("overwrite");

        applicationService.performAction(new String[]{"copy", pdfFile.toString(), docsDir.toString()});
        applicationService.performAction(new String[]{"copy", mp3File.toString(), musicDir.toString()});

        assertTrue(Files.exists(docsDir.resolve(pdfFile.getFileName())), "PDF file duplicate should be overwritten in the Documents directory.");
        assertTrue(Files.exists(musicDir.resolve(mp3File.getFileName())), "MP3 file duplicate should be overwritten in the Music directory.");
    }

    @Test
    public void testUserCancelOperationForFileType() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        Path jpgFile = Files.createFile(testDirectory.resolve("image.jpg"));
        Path destinationDir = testDirectory.resolve("ToBeDetermined");
        Files.createDirectory(destinationDir);

        // Simulate user canceling the operation
        // applicationService.setUserDecision("cancel");

        applicationService.performAction(new String[]{"copy", jpgFile.toString(), destinationDir.toString()});

        assertFalse(Files.exists(destinationDir.resolve(jpgFile.getFileName())), "Operation should be canceled, and JPG file should not be copied.");
    }
    
    @Test
    public void testFileIntegrityAfterCopy() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        Path originalFile = testDirectory.resolve("importantDoc.txt");
        Files.writeString(originalFile, "Critical content for integrity check.");

        Path destinationDir = testDirectory.resolve("Archives");
        Files.createDirectory(destinationDir);

        applicationService.performAction(new String[]{"copy", originalFile.toString(), destinationDir.toString()});
        Path copiedFile = destinationDir.resolve(originalFile.getFileName());

        assertTrue(Files.exists(copiedFile), "Copied file should exist in the destination directory.");
        assertEquals(Files.readString(originalFile), Files.readString(copiedFile), "Content of the copied file should match the original.");
    }

    @Test
    public void testEfficientHandlingOfLargeFiles() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        Path largeFile = testDirectory.resolve("largeVideoFile.mp4");
        // Assuming the creation of a large file is simulated or a large file is available
        Files.createFile(largeFile); // Simplification for example purposes

        Path destinationDir = testDirectory.resolve("Videos");
        Files.createDirectory(destinationDir);

        // The focus here would be on the performance or any errors logged during the operation
        applicationService.performAction(new String[]{"copy", largeFile.toString(), destinationDir.toString()});

        assertTrue(Files.exists(destinationDir.resolve(largeFile.getFileName())), "Large file should be copied efficiently without errors.");
        // Additional checks might include verifying resource usage or operation duration
    }

    @Test
    public void testBatchOperationOnSelectedFiles() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        Path firstFile = Files.createFile(testDirectory.resolve("first.txt"));
        Path secondFile = Files.createFile(testDirectory.resolve("second.txt"));
        // Simulate user selecting specific files for a batch operation
        Path destinationDir = testDirectory.resolve("BatchProcessed");
        Files.createDirectory(destinationDir);

        // Simulating batch copy operation for selected files
        //applicationService.performBatchAction(new String[]{"copy", firstFile.toString(), secondFile.toString(), destinationDir.toString()});

        assertTrue(Files.exists(destinationDir.resolve(firstFile.getFileName())), "First file should be copied in batch operation.");
        assertTrue(Files.exists(destinationDir.resolve(secondFile.getFileName())), "Second file should be copied in batch operation.");
    }
    
    @Test
    public void testAutoCategorizationBasedOnFileExtension() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        Path textFile = Files.createFile(testDirectory.resolve("note.txt"));
        Path imageFile = Files.createFile(testDirectory.resolve("photo.jpg"));
        // Directories for automatic categorization
        Path docsDir = testDirectory.resolve("Documents");
        Files.createDirectory(docsDir);
        Path imagesDir = testDirectory.resolve("Images");
        Files.createDirectory(imagesDir);

        // Assuming the tool is configured to automatically categorize .txt and .jpg files
        applicationService.performAction(new String[]{"auto-categorize", testDirectory.toString()});

        assertTrue(Files.exists(docsDir.resolve(textFile.getFileName())), ".txt file should be automatically moved to Documents directory.");
        assertTrue(Files.exists(imagesDir.resolve(imageFile.getFileName())), ".jpg file should be automatically moved to Images directory.");
    }

    @Test
    public void testOperationHistoryLogging() throws Exception {
        applicationService.performAction(new String[]{"init", testDirectory.toString()});
        Path logFile = testDirectory.resolve("operationLog.txt");
        // Assuming the tool is configured to log operations to a specific file
        
        Path fileToCopy = Files.createFile(testDirectory.resolve("logTest.txt"));
        Files.writeString(fileToCopy, "Test logging");

        Path destinationDir = Files.createDirectory(testDirectory.resolve("Logs"));
        applicationService.performAction(new String[]{"copy", fileToCopy.toString(), destinationDir.toString()});

        // Verify the operation log contains an entry for the performed operation
        String logContent = Files.readString(logFile);
        assertTrue(logContent.contains("copy") && logContent.contains(fileToCopy.toString()) && logContent.contains("success"), "Operation log should contain detailed entry of the copy operation.");
    }


    // Additional tests can include:
    // - Copying/moving directories (if supported by your application)
    // - Copying/moving files with special characters or permissions
    // - Handling of symbolic links (if relevant)
    // - Verifying error messages or application state for failed operations
    // Additional scenarios to consider:
    // - Handling read-only files
    // - Verifying error messages for unsupported operations (if any)
    // - Testing behavior when the file system runs out of space
    // - Cross-platform file path compatibility
    
    
}
