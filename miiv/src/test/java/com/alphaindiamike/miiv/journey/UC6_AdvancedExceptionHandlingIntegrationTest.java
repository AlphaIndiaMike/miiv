package com.alphaindiamike.miiv.journey;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UC6_AdvancedExceptionHandlingIntegrationTest {
	private Path testDirectory;
    private Path operationalDirectory;
    private FileSystem mockFileSystem;

    @BeforeEach
    void setUp() throws Exception {
        testDirectory = Files.createTempDirectory("testDir");
        operationalDirectory = testDirectory.resolve("operationalDir");
        Files.createDirectories(operationalDirectory);
        // Mocking the file system to simulate the behavior of a drive disconnection
        mockFileSystem = mock(FileSystem.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.walk(testDirectory)
             .sorted(Comparator.reverseOrder())
             .map(Path::toFile)
             .forEach(File::delete);
    }

    @Test
    void testOperationFailsOnDriveDisconnection() throws Exception {
        Path fileToOperate = operationalDirectory.resolve("importantDoc.txt");
        Files.createFile(fileToOperate);

        // Simulating the drive disconnection by making the directory temporarily inaccessible
        // Note: For simplicity, we're changing permissions, which is a form of making it inaccessible
        // In a real scenario, this would represent a drive becoming unavailable
        Files.setPosixFilePermissions(operationalDirectory, PosixFilePermissions.fromString("---------"));

        IOException exception = assertThrows(IOException.class, () -> {
            // Attempting an operation that would fail due to the "disconnection"
            Files.copy(fileToOperate, operationalDirectory.resolve("backup.txt"));
        });

        // Verifying that an operation during a "disconnection" results in an IOException
        assertNotNull(exception, "Operation during drive disconnection should throw IOException.");

        // Restoring access for cleanup
        Files.setPosixFilePermissions(operationalDirectory, PosixFilePermissions.fromString("rwxr-xr-x"));
    }
}
