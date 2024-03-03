package com.alphaindiamike.miiv;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.alphaindiamike.miiv.view.JavaFXMain;
import com.alphaindiamike.miiv.services.ApplicationService;

/**
 * Main application class that serves as the entry point for the application.
 * It initializes the Spring application context and decides whether to run in GUI mode or CLI mode.
 */
@Configuration
@ComponentScan(basePackages = "com.alphaindiamike.miiv")
public class App 
{
    public static void main( String[] args )
    {
        // Initialize the Spring application context
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(App.class);

        // Check for GUI mode flag and launch JavaFX UI if present
        if (args.length > 0 && "--gui".equals(args[0])) {
        	JavaFXMain.launchApp(args, context);
        }
        else {
	        // Retrieve the ApplicationService bean from the application context
	        ApplicationService applicationService = context.getBean(ApplicationService.class);
	
	        // Use the applicationService as needed
	        // For example, triggering some functionality that uses the injected Controller
	        applicationService.performAction(args);
	
	        // Close the application context
	        context.close();
        }
	    // Register shutdown hook to ensure context closes gracefully on application exit
	    context.registerShutdownHook();
    }
}
