package ru.greenhouse.server.processing;

public final class SensorMath {

    private static final double ADC_MAX = 1023;

    private SensorMath() {
    }

    public static double soilPercent(double raw, int dryRaw, int wetRaw) {
        if (dryRaw == wetRaw) {
            return 0;
        }
        double pct = (dryRaw - raw) * 100.0 / (dryRaw - wetRaw);
        return clamp(pct);
    }

    public static double lightPercent(double raw, boolean inverted) {
        double pct = clamp(raw * 100.0 / ADC_MAX);
        return inverted ? 100 - pct : pct;
    }

    public static double round(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private static double clamp(double pct) {
        return Math.max(0, Math.min(100, pct));
    }
}
