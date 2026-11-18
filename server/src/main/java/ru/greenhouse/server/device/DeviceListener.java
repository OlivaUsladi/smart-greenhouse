package ru.greenhouse.server.device;

import java.time.LocalDateTime;

public interface DeviceListener {

    void onPacket(SensorPacket packet);

    void onEvent(DeviceEvent event);

    void onMessage(String message, LocalDateTime at);

    void onRejected(String line, String reason, LocalDateTime at);
}
