package ru.greenhouse.server.service;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ru.greenhouse.server.config.GreenhouseProperties;
import ru.greenhouse.server.device.DeviceLink;
import ru.greenhouse.server.device.FileDeviceLink;
import ru.greenhouse.server.device.SensorPacket;
import ru.greenhouse.server.processing.Sample;
import ru.greenhouse.server.processing.Sampler;
import ru.greenhouse.server.processing.SensorBuffer;
import ru.greenhouse.server.report.ChartWriter;
import ru.greenhouse.server.report.ConsoleReport;
import ru.greenhouse.server.report.CsvWriter;

@Service
public class GreenhouseService {

    private static final Logger log = LoggerFactory.getLogger(GreenhouseService.class);

    private final GreenhouseProperties props;
    private final SensorBuffer buffer;
    private final PacketReceiver receiver;
    private final Sampler sampler;
    private final ConsoleReport console = new ConsoleReport();
    private final List<Sample> samples = new CopyOnWriteArrayList<>();
    private final DeviceLink link;
    private volatile boolean receiving;

    public GreenhouseService(GreenhouseProperties props) {
        this.props = props;
        this.buffer = new SensorBuffer(props.bufferSize());
        this.sampler = new Sampler(buffer, Duration.ofSeconds(props.windowSec()), props.emaN(),
                new Sampler.Calibration(props.soilDryRaw(), props.soilWetRaw(), props.lightInverted()), this::onSample);
        this.receiver = new PacketReceiver(buffer, sampler, console);
        this.link = new FileDeviceLink(props.sourceFile(), props.speed());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        Thread thread = new Thread(this::receive, "receiver");
        thread.setDaemon(true);
        thread.start();
    }

    private void receive() {
        if (!Files.exists(props.sourceFile())) {
            log.error("Файл с данными не найден: {}", props.sourceFile().toAbsolutePath());
            return;
        }
        receiving = true;
        console.header(props);
        try {
            link.start(receiver);
            sampler.flush();
            Files.createDirectories(props.outputDir());
            CsvWriter.write(samples, props.outputDir().resolve("samples.csv"));
            List<String> charts = ChartWriter.writeAll(samples, props.outputDir());
            console.summary(receiver, buffer, samples, props.outputDir(), charts);
        } catch (IOException e) {
            log.error("Ошибка чтения или записи файла: {}", e.getMessage());
        } finally {
            receiving = false;
        }
    }

    private void onSample(Sample sample) {
        samples.add(sample);
        console.sample(sample);
    }

    public List<Sample> samples() {
        return samples;
    }

    public Optional<Sample> lastSample() {
        for (int i = samples.size() - 1; i >= 0; i--) {
            if (!samples.get(i).isGap()) {
                return Optional.of(samples.get(i));
            }
        }
        return Optional.empty();
    }

    public Optional<SensorPacket> lastPacket() {
        return buffer.latest();
    }

    public boolean isReceiving() {
        return receiving;
    }

    public PacketReceiver receiver() {
        return receiver;
    }

    public String source() {
        return "журнал " + props.sourceFile();
    }
}
