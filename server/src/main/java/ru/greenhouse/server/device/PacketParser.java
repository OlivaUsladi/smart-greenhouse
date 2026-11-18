package ru.greenhouse.server.device;

import java.time.LocalDateTime;
import java.util.Optional;

// Пакет измерений: $GH,номер,почва,свет,температура,влажность,насос,лента,вода,режим_света,часы*XX
// Событие:         $EV,PUMP|LAMP,1|0,A|M,секунды*XX
// XX - исключающее ИЛИ (XOR) всех символов между '$' и '*' в шестнадцатеричном виде.
public final class PacketParser {

    private PacketParser() {
    }

    public static int checksum(String body) {
        int cs = 0;
        for (int i = 0; i < body.length(); i++) {
            cs ^= body.charAt(i);
        }
        return cs & 0xFF;
    }

    public static String frame(String body) {
        return "$" + body + "*" + String.format("%02X", checksum(body));
    }

    public static Optional<String> unwrap(String line) {
        if (line == null) {
            return Optional.empty();
        }
        String s = line.trim();
        int star = s.lastIndexOf('*');
        if (!s.startsWith("$") || star < 0 || star + 3 != s.length()) {
            return Optional.empty();
        }
        String body = s.substring(1, star);
        try {
            int expected = Integer.parseInt(s.substring(star + 1), 16);
            return checksum(body) == expected ? Optional.of(body) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static SensorPacket parseSensor(String body, LocalDateTime at) {
        String[] f = body.split(",", -1);
        if (f.length != 11 || !"GH".equals(f[0])) {
            throw new IllegalArgumentException("неверное число полей");
        }
        return new SensorPacket(
                Long.parseLong(f[1]),
                Integer.parseInt(f[2]),
                Integer.parseInt(f[3]),
                parseNullable(f[4]),
                parseNullable(f[5]),
                "1".equals(f[6]),
                "1".equals(f[7]),
                "1".equals(f[8]),
                Integer.parseInt(f[9]),
                "1".equals(f[10]),
                at);
    }

    public static DeviceEvent parseEvent(String body, LocalDateTime at) {
        String[] f = body.split(",", -1);
        if (f.length != 5 || !"EV".equals(f[0])) {
            throw new IllegalArgumentException("неверное число полей");
        }
        return new DeviceEvent(f[1].toLowerCase(), "1".equals(f[2]), "M".equals(f[3]), Integer.parseInt(f[4]), at);
    }

    public static void dispatch(String line, LocalDateTime at, DeviceListener listener) {
        if (line == null || line.isBlank()) {
            return;
        }
        String s = line.trim();
        if (s.startsWith("#")) {
            listener.onMessage(s, at);
            return;
        }
        Optional<String> body = unwrap(s);
        if (body.isEmpty()) {
            listener.onRejected(s, "неверный формат или контрольная сумма", at);
            return;
        }
        try {
            if (body.get().startsWith("GH,")) {
                listener.onPacket(parseSensor(body.get(), at));
            } else if (body.get().startsWith("EV,")) {
                listener.onEvent(parseEvent(body.get(), at));
            } else {
                listener.onRejected(s, "неизвестный тип пакета", at);
            }
        } catch (RuntimeException e) {
            listener.onRejected(s, "ошибка разбора полей: " + e.getMessage(), at);
        }
    }

    private static Double parseNullable(String s) {
        return "NA".equals(s) || s.isEmpty() ? null : Double.parseDouble(s);
    }
}
