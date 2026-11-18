package ru.greenhouse.server.device;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

// Источник данных для ЛР2: журнал строк, которые плата передаёт в COM-порт.
// Формат строки журнала: время_приёма;строка_от_платы
// speed - во сколько раз быстрее реального времени воспроизводится журнал (0 - без пауз).
public class FileDeviceLink implements DeviceLink {

    private static final long MAX_PAUSE_MS = 10_000;

    private final Path file;
    private final double speed;
    private volatile boolean running;

    public FileDeviceLink(Path file, double speed) {
        this.file = file;
        this.speed = speed;
    }

    @Override
    public void start(DeviceListener listener) throws IOException {
        running = true;
        LocalDateTime previous = null;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while (running && (line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("//")) {
                    continue;
                }
                int sep = line.indexOf(';');
                if (sep < 0) {
                    listener.onRejected(line, "нет времени приёма", previous);
                    continue;
                }
                LocalDateTime at;
                try {
                    at = LocalDateTime.parse(line.substring(0, sep).trim());
                } catch (DateTimeParseException e) {
                    listener.onRejected(line, "неверное время приёма", previous);
                    continue;
                }
                if (previous != null) {
                    pause(Duration.between(previous, at));
                }
                previous = at;
                PacketParser.dispatch(line.substring(sep + 1), at, listener);
            }
        }
    }

    @Override
    public void stop() {
        running = false;
    }

    private void pause(Duration journalTime) {
        if (speed <= 0 || journalTime.isNegative()) {
            return;
        }
        long ms = Math.min(MAX_PAUSE_MS, (long) (journalTime.toMillis() / speed));
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }
}
