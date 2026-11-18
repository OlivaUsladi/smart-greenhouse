package ru.greenhouse.server.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import ru.greenhouse.server.device.SensorPacket;

class ProcessingTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 18, 8, 0);

    @Test
    void emaFollowsFormula() {
        EmaFilter ema = new EmaFilter(5);
        assertEquals(1.0 / 3, ema.alpha(), 1e-9);
        assertEquals(60, ema.apply(60), 1e-9);
        assertEquals(50, ema.apply(30), 1e-9);
        assertEquals(40, ema.apply(20), 1e-9);
        ema.reset();
        assertEquals(10, ema.apply(10), 1e-9);
    }

    @Test
    void bufferDropsOldestWhenFull() {
        SensorBuffer buffer = new SensorBuffer(3);
        for (int i = 1; i <= 5; i++) {
            buffer.add(packet(i, T0.plusSeconds(2L * i)));
        }
        assertEquals(3, buffer.size());
        assertEquals(2, buffer.dropped());
        assertEquals(5, buffer.latest().orElseThrow().seq());
        assertEquals(3, buffer.between(T0, T0.plusMinutes(1)).get(0).seq());
    }

    @Test
    void soilPercentUsesCalibration() {
        assertEquals(0, SensorMath.soilPercent(520, 520, 260), 1e-9);
        assertEquals(100, SensorMath.soilPercent(260, 520, 260), 1e-9);
        assertEquals(50, SensorMath.soilPercent(390, 520, 260), 1e-9);
        assertEquals(100, SensorMath.soilPercent(200, 520, 260), 1e-9);
    }

    @Test
    void lightPercentIsInverted() {
        assertEquals(100, SensorMath.lightPercent(0, true), 1e-9);
        assertEquals(0, SensorMath.lightPercent(1023, true), 1e-9);
        assertEquals(100, SensorMath.lightPercent(1023, false), 1e-9);
    }

    static SensorPacket packet(long seq, LocalDateTime at) {
        return new SensorPacket(seq, 390, 512, 22.0, 60.0, false, true, true, 0, true, at);
    }
}
