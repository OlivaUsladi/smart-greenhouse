package ru.greenhouse.server.device;

import java.time.LocalDateTime;

public record DeviceEvent(String device, boolean on, boolean manual, int seconds, LocalDateTime at) {
}
