package fr.esgi.hla.itadaki.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point of the Itadaki application.
 * scanBasePackages ensures all sub-packages under the root are discovered.
 */
@SpringBootApplication(scanBasePackages = "fr.esgi.hla.itadaki")
public class ItadakiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItadakiApplication.class, args);
    }
}
