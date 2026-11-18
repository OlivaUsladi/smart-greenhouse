package ru.greenhouse.server.processing;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import ru.greenhouse.server.device.SensorPacket;

// Дискретизация: пакеты из буфера группируются в окна одинаковой длины,
// каждое окно даёт один отсчёт (среднее значение), который затем сглаживается EMA.
public class Sampler {

    private final SensorBuffer buffer;
    private final Duration window;
    private final Calibration calibration;
    private final Consumer<Sample> output;
    private final Map<SensorType, EmaFilter> filters = new EnumMap<>(SensorType.class);
    private LocalDateTime windowStart;

    public Sampler(SensorBuffer buffer, Duration window, int emaN, Calibration calibration, Consumer<Sample> output) {
        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("Окно дискретизации должно быть больше нуля");
        }
        this.buffer = buffer;
        this.window = window;
        this.calibration = calibration;
        this.output = output;
        for (SensorType type : SensorType.values()) {
            filters.put(type, new EmaFilter(emaN));
        }
    }

    public void advanceTo(LocalDateTime time) {
        if (windowStart == null) {
            windowStart = align(time);
        }
        while (!time.isBefore(windowStart.plus(window))) {
            closeWindow();
        }
    }

    public void flush() {
        if (windowStart != null && !buffer.between(windowStart, windowStart.plus(window)).isEmpty()) {
            closeWindow();
        }
    }

    private void closeWindow() {
        LocalDateTime end = windowStart.plus(window);
        List<SensorPacket> packets = buffer.between(windowStart, end);
        if (packets.isEmpty()) {
            filters.values().forEach(EmaFilter::reset);
            output.accept(Sample.gap(windowStart));
        } else {
            output.accept(process(windowStart, packets));
        }
        windowStart = end;
    }

    private Sample process(LocalDateTime start, List<SensorPacket> packets) {
        Map<SensorType, Double> averages = new EnumMap<>(SensorType.class);
        averages.put(SensorType.SOIL, average(packets,
                p -> SensorMath.soilPercent(p.soilRaw(), calibration.soilDryRaw(), calibration.soilWetRaw())));
        averages.put(SensorType.LIGHT, average(packets,
                p -> SensorMath.lightPercent(p.lightRaw(), calibration.lightInverted())));
        averages.put(SensorType.AIR_TEMP, average(packets, SensorPacket::temperature));
        averages.put(SensorType.AIR_HUM, average(packets, SensorPacket::humidity));

        Map<SensorType, Reading> values = new EnumMap<>(SensorType.class);
        averages.forEach((type, value) -> {
            if (value != null) {
                double filtered = filters.get(type).apply(value);
                values.put(type, new Reading(SensorMath.round(value), SensorMath.round(filtered)));
            }
        });
        SensorPacket last = packets.get(packets.size() - 1);
        boolean pumpOn = packets.stream().anyMatch(SensorPacket::pumpOn);
        return new Sample(start, packets.size(), values, last.waterOk(), pumpOn, last.lampOn());
    }

    private LocalDateTime align(LocalDateTime time) {
        long seconds = window.getSeconds();
        LocalDateTime t = time.withNano(0);
        return t.minusSeconds(t.toLocalTime().toSecondOfDay() % seconds);
    }

    private static Double average(List<SensorPacket> packets, Function<SensorPacket, Double> value) {
        return packets.stream().map(value).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).average().stream().boxed().findFirst().orElse(null);
    }

    public record Calibration(int soilDryRaw, int soilWetRaw, boolean lightInverted) {
    }
}
