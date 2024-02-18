package com.alphaindiamike.miiv;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.alphaindiamike.miiv.services.ApplicationService;

@Configuration
@ComponentScan(basePackages = "com.alphaindiamike.miiv")
public class App 
{
    public static void main( String[] args )
    {
        // Initialize the Spring application context
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(App.class);

        // Retrieve the ApplicationService bean from the application context
        ApplicationService applicationService = context.getBean(ApplicationService.class);

        // Use the applicationService as needed
        // For example, triggering some functionality that uses the injected Controller
        applicationService.performAction(args);

        // Close the application context
        context.close();
    }
}
