package ru.greenhouse.server.web;

import java.time.LocalDateTime;
import java.util.List;

public final class Dto {

    private Dto() {
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String token) {
    }

    public record Error(String message) {
    }

    public record SensorValue(String type, String name, String unit, Double raw, Double value, boolean inNorm,
                              double normMin, double normMax) {
    }

    public record Current(LocalDateTime time, boolean receiving, String source, List<SensorValue> sensors,
                          boolean pumpOn, boolean lampOn, boolean waterOk, int accepted, int rejected) {
    }

    public record Point(LocalDateTime t, Double raw, Double filtered) {
    }

    public record SensorStats(String type, String name, String unit, Double min, Double avg, Double max,
                              long count, Double inNormPct) {
    }

    public record Stats(List<SensorStats> sensors, int accepted, int rejected, int events, int samples, long gaps,
                        long waterings) {
    }
}
