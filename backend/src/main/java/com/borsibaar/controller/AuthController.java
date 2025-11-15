package com.borsibaar.controller;

import com.borsibaar.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login/success")
    public void success(HttpServletResponse response, OAuth2AuthenticationToken auth) throws IOException {
        var result = authService.processOAuthLogin(auth);

        Cookie cookie = new Cookie("jwt", result.dto().token());
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // TODO: Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 day
        response.addCookie(cookie);

        String redirect = result.needsOnboarding() ? "/onboarding" : "/dashboard";
        response.sendRedirect(frontendUrl + redirect);
    }
}
