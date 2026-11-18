package ru.greenhouse.server.processing;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.greenhouse.server.device.SensorPacket;

public class SensorBuffer {

    private final int capacity;
    private final ArrayDeque<SensorPacket> packets;
    private long dropped;

    public SensorBuffer(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Размер буфера должен быть больше нуля");
        }
        this.capacity = capacity;
        this.packets = new ArrayDeque<>(capacity);
    }

    public synchronized void add(SensorPacket packet) {
        if (packets.size() == capacity) {
            packets.removeFirst();
            dropped++;
        }
        packets.addLast(packet);
    }

    // Пакеты с временем приёма from <= t < to
    public synchronized List<SensorPacket> between(LocalDateTime from, LocalDateTime to) {
        List<SensorPacket> result = new ArrayList<>();
        for (SensorPacket p : packets) {
            if (!p.receivedAt().isBefore(from) && p.receivedAt().isBefore(to)) {
                result.add(p);
            }
        }
        return result;
    }

    public synchronized Optional<SensorPacket> latest() {
        return Optional.ofNullable(packets.peekLast());
    }

    public synchronized int size() {
        return packets.size();
    }

    public synchronized long dropped() {
        return dropped;
    }

    public int capacity() {
        return capacity;
    }
}
