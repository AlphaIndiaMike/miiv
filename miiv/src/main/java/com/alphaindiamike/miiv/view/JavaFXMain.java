package com.alphaindiamike.miiv.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.alphaindiamike.miiv.controllers.GUI_Controlller;

/**
 * JavaFX application class responsible for launching the JavaFX UI.
 * This class initializes the JavaFX framework and sets up the primary stage for the UI.
 */
public class JavaFXMain extends Application {
    private static String[] savedArgs;
    private static AnnotationConfigApplicationContext context;

    /**
     * Launches the JavaFX application.
     * @param args Command-line arguments passed to the application.
     * @param ctx Spring application context to enable dependency injection in JavaFX controllers.
     */
    public static void launchApp(String[] args, AnnotationConfigApplicationContext ctx) {
        savedArgs = args;
        context = ctx;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/MainView.fxml"));
            GUI_Controlller controller = context.getBean(GUI_Controlller.class); // Adjust based on your context and controller
            loader.setController(controller);

            Parent root = loader.load(); // Load the FXML file
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            primaryStage.setTitle("Miiv Application GUI");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace(); // This will print the stack trace of the exception to standard error.
            System.out.println("Error during FXML loading: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        if (context != null) {
            context.close(); // Ensure the Spring context is properly closed when the application stops
        }
        super.stop();
    }
}