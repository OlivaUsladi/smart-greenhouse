package ru.greenhouse.server.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PacketParserTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 18, 8, 0);

    @Test
    void checksumOfExamplePacket() {
        assertEquals(0x08, PacketParser.checksum("GH,124,612,388,23.6,64.2,0,1,1,0,1"));
        assertEquals("$EV,PUMP,1,M,3*44", PacketParser.frame("EV,PUMP,1,M,3"));
    }

    @Test
    void validPacketIsParsed() {
        String body = PacketParser.unwrap("$GH,124,612,388,23.6,64.2,0,1,1,0,1*08").orElseThrow();
        SensorPacket p = PacketParser.parseSensor(body, NOW);
        assertEquals(124, p.seq());
        assertEquals(612, p.soilRaw());
        assertEquals(388, p.lightRaw());
        assertEquals(23.6, p.temperature());
        assertTrue(p.lampOn());
        assertFalse(p.pumpOn());
    }

    @Test
    void damagedPacketIsRejected() {
        assertTrue(PacketParser.unwrap("$GH,124,613,388,23.6,64.2,0,1,1,0,1*08").isEmpty());
        assertTrue(PacketParser.unwrap("$GH,124,612,38").isEmpty());
        assertTrue(PacketParser.unwrap("GH,124,612,388,23.6,64.2,0,1,1,0,1*08").isEmpty());
    }

    @Test
    void missingDhtValueBecomesNull() {
        SensorPacket p = PacketParser.parseSensor("GH,1,500,300,NA,NA,0,0,1,0,0", NOW);
        assertNull(p.temperature());
        assertNull(p.humidity());
    }

    @Test
    void eventIsParsed() {
        DeviceEvent e = PacketParser.parseEvent("EV,PUMP,1,M,3", NOW);
        assertEquals("pump", e.device());
        assertTrue(e.on());
        assertTrue(e.manual());
        assertEquals(3, e.seconds());
    }

    @Test
    void dispatchSendsEachLineToItsHandler() {
        List<String> calls = new ArrayList<>();
        DeviceListener listener = new DeviceListener() {
            public void onPacket(SensorPacket packet) { calls.add("packet"); }
            public void onEvent(DeviceEvent event) { calls.add("event"); }
            public void onMessage(String message, LocalDateTime at) { calls.add("message"); }
            public void onRejected(String line, String reason, LocalDateTime at) { calls.add("rejected"); }
        };
        PacketParser.dispatch("$GH,124,612,388,23.6,64.2,0,1,1,0,1*08", NOW, listener);
        PacketParser.dispatch("$EV,PUMP,1,M,3*44", NOW, listener);
        PacketParser.dispatch("#READY,GH", NOW, listener);
        PacketParser.dispatch("$GH,124,612,388,23.6,64.2,0,1,1,0,1*09", NOW, listener);
        assertEquals(List.of("packet", "event", "message", "rejected"), calls);
    }
}
