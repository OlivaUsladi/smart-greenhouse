package ru.greenhouse.server.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.greenhouse.server.service.AuthService;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService auth;

    public AuthInterceptor(AuthService auth) {
        this.auth = auth;
    }

    static String token(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (auth.isValid(token(request))) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}
