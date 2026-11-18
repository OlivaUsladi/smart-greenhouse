package ru.greenhouse.server.service;

import java.time.LocalDateTime;

public record LogEntry(LocalDateTime time, String kind, String text) {
}
