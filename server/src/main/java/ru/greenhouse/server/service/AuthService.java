package ru.greenhouse.server.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import ru.greenhouse.server.config.GreenhouseProperties;

@Service
public class AuthService {

    private static final Duration TOKEN_LIFETIME = Duration.ofHours(24);

    private final String user;
    private final byte[] password;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Instant> tokens = new ConcurrentHashMap<>();

    public AuthService(GreenhouseProperties props) {
        if (props.password() == null || props.password().isBlank()) {
            throw new IllegalStateException("Задай пароль GH_PASSWORD в файле server/.env (образец - .env.example)");
        }
        this.user = props.user();
        this.password = props.password().getBytes(StandardCharsets.UTF_8);
    }

    public String login(String username, String pass) {
        boolean userOk = user.equals(username);
        boolean passOk = pass != null && MessageDigest.isEqual(password, pass.getBytes(StandardCharsets.UTF_8));
        if (!userOk || !passOk) {
            throw new SecurityException("Неверный логин или пароль");
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.put(token, Instant.now().plus(TOKEN_LIFETIME));
        return token;
    }

    public boolean isValid(String token) {
        if (token == null) {
            return false;
        }
        Instant expires = tokens.get(token);
        if (expires == null || expires.isBefore(Instant.now())) {
            tokens.remove(token);
            return false;
        }
        return true;
    }

    public void logout(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }
}
