package ru.greenhouse.server.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "greenhouse")
public record GreenhouseProperties(
        @DefaultValue("test-data/packets.log") Path sourceFile,
        @DefaultValue("10") double speed,
        @DefaultValue("10") int windowSec,
        @DefaultValue("60") int bufferSize,
        @DefaultValue("5") int emaN,
        @DefaultValue("520") int soilDryRaw,
        @DefaultValue("260") int soilWetRaw,
        @DefaultValue("true") boolean lightInverted,
        @DefaultValue("results") Path outputDir,
        @DefaultValue("admin") String user,
        String password) {
}
