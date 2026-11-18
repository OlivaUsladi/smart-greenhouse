package ru.greenhouse.server.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import ru.greenhouse.server.device.SensorPacket;

class SamplerTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 18, 8, 0, 0);

    private final List<Sample> samples = new ArrayList<>();
    private final SensorBuffer buffer = new SensorBuffer(60);
    private final Sampler sampler = new Sampler(buffer, Duration.ofSeconds(10), 5,
            new Sampler.Calibration(520, 260, true), samples::add);

    private void receive(int second, int soilRaw, double temp) {
        LocalDateTime at = T0.plusSeconds(second);
        sampler.advanceTo(at);
        buffer.add(new SensorPacket(second, soilRaw, 512, temp, 60.0, false, true, true, 0, true, at));
    }

    @Test
    void packetsOfOneWindowAreAveraged() {
        receive(0, 390, 22.0);
        receive(2, 390, 22.2);
        receive(4, 390, 22.4);
        receive(6, 390, 22.6);
        receive(8, 390, 22.8);
        receive(10, 260, 30.0);
        assertEquals(1, samples.size());
        Sample s = samples.get(0);
        assertEquals(5, s.packets());
        assertEquals(50.0, s.values().get(SensorType.SOIL).raw());
        assertEquals(22.4, s.values().get(SensorType.AIR_TEMP).raw());
    }

    @Test
    void emaIsAppliedBetweenWindows() {
        receive(0, 390, 20.0);
        receive(10, 390, 23.0);
        receive(20, 390, 23.0);
        assertEquals(20.0, samples.get(0).values().get(SensorType.AIR_TEMP).filtered());
        assertEquals(21.0, samples.get(1).values().get(SensorType.AIR_TEMP).filtered());
    }

    @Test
    void emptyWindowIsGapAndResetsEma() {
        receive(0, 390, 20.0);
        receive(30, 390, 26.0);
        sampler.flush();
        assertEquals(4, samples.size());
        assertFalse(samples.get(0).isGap());
        assertTrue(samples.get(1).isGap());
        assertTrue(samples.get(2).isGap());
        assertEquals(26.0, samples.get(3).values().get(SensorType.AIR_TEMP).filtered());
    }
}
