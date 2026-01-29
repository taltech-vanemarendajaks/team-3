package com.borsibaar.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/test")
@Profile("test") // Only active when spring.profiles.active=test
public class TestAuthController {

    private final JwtEncoder jwtEncoder;

    public TestAuthController(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping("/login")
    public TestLoginResponse login(
            @RequestBody TestLoginRequest request,
            HttpServletResponse response
    ) {
        // Generate a JWT for the test user
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("test-issuer")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(request.email())
                .claim("email", request.email())
                .claim("name", "Test User")
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        // Set it as an httpOnly cookie (matching your app's real auth flow)
        Cookie cookie = new Cookie("ACCESS_TOKEN", token); // adjust name if needed
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // set true if using HTTPS in tests
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        response.addCookie(cookie);

        return new TestLoginResponse(token);
    }

    public record TestLoginRequest(String email) {}
    public record TestLoginResponse(String token) {}
}