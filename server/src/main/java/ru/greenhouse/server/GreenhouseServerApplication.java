package ru.greenhouse.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GreenhouseServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenhouseServerApplication.class, args);
    }
}
