package ru.greenhouse.server.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.greenhouse.server.processing.Reading;
import ru.greenhouse.server.processing.Sample;
import ru.greenhouse.server.processing.SensorType;
import ru.greenhouse.server.service.AuthService;
import ru.greenhouse.server.service.GreenhouseService;
import ru.greenhouse.server.service.LogEntry;
import ru.greenhouse.server.service.PacketReceiver;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final AuthService auth;
    private final GreenhouseService greenhouse;

    public ApiController(AuthService auth, GreenhouseService greenhouse) {
        this.auth = auth;
        this.greenhouse = greenhouse;
    }

    @PostMapping("/login")
    public Dto.LoginResponse login(@RequestBody Dto.LoginRequest request) {
        return new Dto.LoginResponse(auth.login(request.username(), request.password()));
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        auth.logout(AuthInterceptor.token(request));
    }

    @GetMapping("/current")
    public Dto.Current current() {
        Sample last = greenhouse.lastSample().orElse(null);
        List<Dto.SensorValue> sensors = new ArrayList<>();
        for (SensorType t : SensorType.values()) {
            Reading r = last == null ? null : last.values().get(t);
            sensors.add(new Dto.SensorValue(t.key(), t.title(), t.unit(),
                    r == null ? null : r.raw(), r == null ? null : r.filtered(),
                    r != null && t.inNorm(r.filtered()), t.normMin(), t.normMax()));
        }
        PacketReceiver rx = greenhouse.receiver();
        var packet = greenhouse.lastPacket();
        return new Dto.Current(last == null ? null : last.start(), greenhouse.isReceiving(), greenhouse.source(), sensors,
                packet.map(p -> p.pumpOn()).orElse(false), packet.map(p -> p.lampOn()).orElse(false),
                packet.map(p -> p.waterOk()).orElse(false), rx.accepted(), rx.rejected());
    }

    // minutes - сколько последних минут данных вернуть, 0 - все
    @GetMapping("/samples")
    public List<Dto.Point> samples(@RequestParam String sensor, @RequestParam(defaultValue = "0") int minutes) {
        SensorType type = SensorType.byKey(sensor);
        List<Sample> all = greenhouse.samples();
        if (all.isEmpty()) {
            return List.of();
        }
        LocalDateTime from = minutes > 0 ? all.get(all.size() - 1).start().minusMinutes(minutes) : LocalDateTime.MIN;
        List<Dto.Point> points = new ArrayList<>();
        for (Sample s : all) {
            if (s.start().isBefore(from)) {
                continue;
            }
            Reading r = s.values().get(type);
            points.add(new Dto.Point(s.start(), r == null ? null : r.raw(), r == null ? null : r.filtered()));
        }
        return points;
    }

    @GetMapping("/stats")
    public Dto.Stats stats() {
        List<Sample> all = greenhouse.samples();
        List<Dto.SensorStats> sensors = new ArrayList<>();
        for (SensorType t : SensorType.values()) {
            DoubleSummaryStatistics st = all.stream().map(s -> s.values().get(t)).filter(r -> r != null)
                    .mapToDouble(Reading::filtered).summaryStatistics();
            long inNorm = all.stream().map(s -> s.values().get(t)).filter(r -> r != null && t.inNorm(r.filtered())).count();
            boolean empty = st.getCount() == 0;
            sensors.add(new Dto.SensorStats(t.key(), t.title(), t.unit(),
                    empty ? null : st.getMin(), empty ? null : round(st.getAverage()), empty ? null : st.getMax(),
                    st.getCount(), empty ? null : round(inNorm * 100.0 / st.getCount())));
        }
        PacketReceiver rx = greenhouse.receiver();
        long gaps = all.stream().filter(Sample::isGap).count();
        long waterings = rx.log().stream().filter(e -> "pump".equals(e.kind()) && e.text().startsWith("Насос включён")).count();
        return new Dto.Stats(sensors, rx.accepted(), rx.rejected(), rx.events(), all.size(), gaps, waterings);
    }

    @GetMapping("/events")
    public List<LogEntry> events() {
        List<LogEntry> log = new ArrayList<>(greenhouse.receiver().log());
        Collections.reverse(log);
        return log;
    }

    private static double round(double v) {
        return Math.round(v * 10) / 10.0;
    }
}
