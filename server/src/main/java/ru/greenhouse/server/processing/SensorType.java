package ru.greenhouse.server.processing;

public enum SensorType {
    SOIL("soil", "Влажность почвы", "%", 40, 80),
    LIGHT("light", "Освещённость", "%", 30, 100),
    AIR_TEMP("air_temp", "Температура воздуха", "°C", 18, 26),
    AIR_HUM("air_hum", "Влажность воздуха", "%", 50, 80);

    private final String key;
    private final String title;
    private final String unit;
    private final double normMin;
    private final double normMax;

    SensorType(String key, String title, String unit, double normMin, double normMax) {
        this.key = key;
        this.title = title;
        this.unit = unit;
        this.normMin = normMin;
        this.normMax = normMax;
    }

    public static SensorType byKey(String key) {
        for (SensorType t : values()) {
            if (t.key.equals(key)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Неизвестный датчик: " + key);
    }

    public boolean inNorm(double value) {
        return value >= normMin && value <= normMax;
    }

    public String key() {
        return key;
    }

    public String title() {
        return title;
    }

    public String unit() {
        return unit;
    }

    public double normMin() {
        return normMin;
    }

    public double normMax() {
        return normMax;
    }
}
