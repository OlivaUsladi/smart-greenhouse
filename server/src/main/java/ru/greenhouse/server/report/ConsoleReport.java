package ru.greenhouse.server.report;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDateTime;
import ru.greenhouse.server.config.GreenhouseProperties;
import ru.greenhouse.server.processing.Reading;
import ru.greenhouse.server.processing.Sample;
import ru.greenhouse.server.processing.SensorBuffer;
import ru.greenhouse.server.processing.SensorType;
import ru.greenhouse.server.service.PacketReceiver;

public class ConsoleReport {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    public void header(GreenhouseProperties p) {
        System.out.println("Умная теплица: приём и предварительная обработка данных");
        System.out.printf("Источник: %s (ускорение %.0f) | окно %d с | буфер %d пакетов | EMA N = %d%n",
                p.sourceFile(), p.speed(), p.windowSec(), p.bufferSize(), p.emaN());
        System.out.println("Время     Пакетов  Почва, % (исх./EMA)  Свет, %       Темп., °C     Вл. возд., %");
    }

    public void sample(Sample s) {
        if (s.isGap()) {
            System.out.printf("%s  -        нет данных (пропуск)%n", TIME.format(s.start()));
            return;
        }
        System.out.printf("%s  %-7d  %-19s  %-12s  %-12s  %s%s%n",
                TIME.format(s.start()), s.packets(),
                pair(s, SensorType.SOIL), pair(s, SensorType.LIGHT),
                pair(s, SensorType.AIR_TEMP), pair(s, SensorType.AIR_HUM),
                s.pumpOn() ? "  [полив]" : "");
    }

    public void event(LocalDateTime at, String text) {
        System.out.printf("%s  событие: %s%n", TIME.format(at), text);
    }

    public void message(String message) {
        System.out.println("          сообщение платы: " + message);
    }

    public void rejected(String line, String reason) {
        System.out.println("          ОТБРОШЕНО (" + reason + "): " + line);
    }

    public void summary(PacketReceiver r, SensorBuffer buffer, List<Sample> samples, Path dir, List<String> charts) {
        long gaps = samples.stream().filter(Sample::isGap).count();
        System.out.println();
        System.out.println("Итог:");
        System.out.println("  принято пакетов измерений: " + r.accepted());
        System.out.println("  принято событий: " + r.events() + ", служебных сообщений: " + r.messages());
        System.out.println("  отброшено строк: " + r.rejected());
        System.out.println("  вытеснено из буфера: " + buffer.dropped() + " (ёмкость " + buffer.capacity() + ")");
        System.out.println("  получено отсчётов: " + samples.size() + ", из них пропусков: " + gaps);
        System.out.println("  таблица: " + dir.resolve("samples.csv"));
        charts.forEach(c -> System.out.println("  график: " + dir.resolve(c)));
    }

    private static String pair(Sample s, SensorType type) {
        Reading r = s.values().get(type);
        return r == null ? "нет" : String.format("%.1f / %.1f", r.raw(), r.filtered());
    }
}
