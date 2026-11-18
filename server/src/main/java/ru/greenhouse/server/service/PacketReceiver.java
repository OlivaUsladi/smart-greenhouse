package ru.greenhouse.server.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ru.greenhouse.server.device.DeviceEvent;
import ru.greenhouse.server.device.DeviceListener;
import ru.greenhouse.server.device.SensorPacket;
import ru.greenhouse.server.processing.Sampler;
import ru.greenhouse.server.processing.SensorBuffer;
import ru.greenhouse.server.report.ConsoleReport;

public class PacketReceiver implements DeviceListener {

    private final SensorBuffer buffer;
    private final Sampler sampler;
    private final ConsoleReport console;
    private final List<LogEntry> log = new ArrayList<>();
    private volatile int accepted;
    private volatile int rejected;
    private volatile int events;
    private volatile int messages;

    public PacketReceiver(SensorBuffer buffer, Sampler sampler, ConsoleReport console) {
        this.buffer = buffer;
        this.sampler = sampler;
        this.console = console;
    }

    @Override
    public void onPacket(SensorPacket packet) {
        sampler.advanceTo(packet.receivedAt());
        buffer.add(packet);
        accepted++;
    }

    @Override
    public void onEvent(DeviceEvent e) {
        events++;
        boolean pump = "pump".equals(e.device());
        String state = e.on() ? (pump ? "Насос включён" : "Фитолента включена") : (pump ? "Насос выключен" : "Фитолента выключена");
        String text = state + (e.manual() ? " вручную" : " автоматически")
                + (e.on() && e.seconds() > 0 ? " на " + e.seconds() + " с" : "");
        add(new LogEntry(e.at(), e.device(), text));
        console.event(e.at(), text);
    }

    @Override
    public void onMessage(String message, LocalDateTime at) {
        messages++;
        add(new LogEntry(at, "message", "Сообщение платы: " + message));
        console.message(message);
    }

    @Override
    public void onRejected(String line, String reason, LocalDateTime at) {
        rejected++;
        add(new LogEntry(at, "rejected", "Отброшена строка (" + reason + "): " + line));
        console.rejected(line, reason);
    }

    private synchronized void add(LogEntry entry) {
        log.add(entry);
    }

    public synchronized List<LogEntry> log() {
        return List.copyOf(log);
    }

    public int accepted() {
        return accepted;
    }

    public int rejected() {
        return rejected;
    }

    public int events() {
        return events;
    }

    public int messages() {
        return messages;
    }
}
