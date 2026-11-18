package ru.greenhouse.server.report;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import ru.greenhouse.server.processing.Reading;
import ru.greenhouse.server.processing.Sample;
import ru.greenhouse.server.processing.SensorType;

public final class ChartWriter {

    private ChartWriter() {
    }

    public static List<String> writeAll(List<Sample> samples, Path dir) throws IOException {
        List<String> files = new ArrayList<>();
        for (SensorType type : SensorType.values()) {
            String name = type.name().toLowerCase() + ".png";
            write(samples, type, dir.resolve(name));
            files.add(name);
        }
        return files;
    }

    // Пропуск данных записывается как NaN, поэтому линия графика в этом месте прерывается
    public static void write(List<Sample> samples, SensorType type, Path file) throws IOException {
        List<Date> time = new ArrayList<>();
        List<Double> raw = new ArrayList<>();
        List<Double> ema = new ArrayList<>();
        for (Sample s : samples) {
            Reading r = s.values().get(type);
            time.add(Date.from(s.start().atZone(ZoneId.systemDefault()).toInstant()));
            raw.add(r == null ? Double.NaN : r.raw());
            ema.add(r == null ? Double.NaN : r.filtered());
        }
        XYChart chart = new XYChartBuilder().width(1000).height(500)
                .title(type.title()).xAxisTitle("Время").yAxisTitle(type.unit()).build();
        chart.getStyler().setDatePattern("HH:mm");
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setLegendPosition(org.knowm.xchart.style.Styler.LegendPosition.InsideNE);
        if (!time.isEmpty()) {
            XYSeries rawSeries = chart.addSeries("Исходные значения", time, raw);
            rawSeries.setMarker(SeriesMarkers.NONE);
            rawSeries.setLineColor(new Color(150, 150, 150));
            rawSeries.setLineStyle(new BasicStroke(1f));
            XYSeries emaSeries = chart.addSeries("Сглаженные (EMA)", time, ema);
            emaSeries.setMarker(SeriesMarkers.NONE);
            emaSeries.setLineColor(new Color(46, 125, 50));
            emaSeries.setLineStyle(new BasicStroke(3f));
        }
        BitmapEncoder.saveBitmap(chart, file.toString(), BitmapEncoder.BitmapFormat.PNG);
    }
}
