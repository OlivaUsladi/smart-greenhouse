package ru.greenhouse.server.processing;

import java.time.LocalDateTime;
import java.util.Map;

public record Sample(LocalDateTime start, int packets, Map<SensorType, Reading> values,
                     boolean waterOk, boolean pumpOn, boolean lampOn) {

    public static Sample gap(LocalDateTime start) {
        return new Sample(start, 0, Map.of(), false, false, false);
    }

    public boolean isGap() {
        return packets == 0;
    }
}
