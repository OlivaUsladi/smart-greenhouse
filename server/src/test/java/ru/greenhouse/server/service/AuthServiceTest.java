package ru.greenhouse.server.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import ru.greenhouse.server.config.GreenhouseProperties;

class AuthServiceTest {

    private static GreenhouseProperties props(String password) {
        return new GreenhouseProperties(Path.of("test-data/packets.log"), 0, 10, 60, 5, 520, 260, true,
                Path.of("results"), "admin", password);
    }

    private final AuthService auth = new AuthService(props("secret"));

    @Test
    void correctPasswordGivesValidToken() {
        String token = auth.login("admin", "secret");
        assertTrue(auth.isValid(token));
        auth.logout(token);
        assertFalse(auth.isValid(token));
    }

    @Test
    void wrongPasswordIsRejected() {
        assertThrows(SecurityException.class, () -> auth.login("admin", "wrong"));
        assertThrows(SecurityException.class, () -> auth.login("user", "secret"));
        assertFalse(auth.isValid("random-token"));
    }

    @Test
    void emptyPasswordStopsServer() {
        assertThrows(IllegalStateException.class, () -> new AuthService(props("")));
    }
}
