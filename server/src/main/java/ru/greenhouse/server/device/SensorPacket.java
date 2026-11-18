package ru.greenhouse.server.device;

import java.time.LocalDateTime;

public record SensorPacket(
        long seq,
        int soilRaw,
        int lightRaw,
        Double temperature,
        Double humidity,
        boolean pumpOn,
        boolean lampOn,
        boolean waterOk,
        int lampMode,
        boolean timeOk,
        LocalDateTime receivedAt) {
}
