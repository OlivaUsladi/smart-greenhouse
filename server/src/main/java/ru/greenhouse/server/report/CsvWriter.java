package ru.greenhouse.server.report;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import ru.greenhouse.server.processing.Reading;
import ru.greenhouse.server.processing.Sample;
import ru.greenhouse.server.processing.SensorType;

public final class CsvWriter {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private CsvWriter() {
    }

    // Разделитель ';' и метка BOM, чтобы файл правильно открывался в Excel
    public static void write(List<Sample> samples, Path file) throws IOException {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8))) {
            out.print('\uFEFF');
            StringBuilder head = new StringBuilder("Время;Пакетов");
            for (SensorType t : SensorType.values()) {
                head.append(';').append(t.title()).append(" (исх.)");
                head.append(';').append(t.title()).append(" (EMA)");
            }
            out.println(head.append(";Вода есть;Полив"));
            for (Sample s : samples) {
                StringBuilder row = new StringBuilder(TIME.format(s.start())).append(';').append(s.packets());
                for (SensorType t : SensorType.values()) {
                    Reading r = s.values().get(t);
                    row.append(';').append(r == null ? "" : format(r.raw()));
                    row.append(';').append(r == null ? "" : format(r.filtered()));
                }
                row.append(';').append(s.isGap() ? "" : s.waterOk() ? "1" : "0");
                row.append(';').append(s.isGap() ? "" : s.pumpOn() ? "1" : "0");
                out.println(row);
            }
        }
    }

    private static String format(double v) {
        return String.valueOf(v).replace('.', ',');
    }
}
