package com.borsibaar.controller;

import com.borsibaar.entity.User;
import com.borsibaar.repository.UserRepository;
import com.borsibaar.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final UserRepository userRepository;
    private final JwtService jwt;

    public AccountController(UserRepository userRepository, JwtService jwt) {
        this.userRepository = userRepository;
        this.jwt = jwt;
    }

    public record MeResponse(String email, String name, String role, Long organizationId, boolean needsOnboarding) {
    }

    public record onboardingRequest(Long organizationId, boolean acceptTerms) {
    }

    @GetMapping
    public ResponseEntity<MeResponse> me(@CookieValue(name = "jwt", required = false) String token) {
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Claims claims = jwt.parseToken(token);
        User user = userRepository.findByEmail(claims.getSubject()).orElse(null);
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return ResponseEntity.ok(new MeResponse(
                user.getEmail(),
                user.getName(),
                user.getRole() != null ? user.getRole().getName() : null,
                user.getOrganizationId(),
                user.getOrganizationId() == null));
    }

    @PostMapping("/onboarding")
    @Transactional
    public ResponseEntity<Void> finish(@CookieValue(name = "jwt", required = false) String token,
            @RequestBody onboardingRequest req) {
        if (token == null)
            return ResponseEntity.status(401).build();
        if (req.organizationId() == null || !req.acceptTerms())
            return ResponseEntity.badRequest().build();

        Claims claims = jwt.parseToken(token);
        User user = userRepository.findByEmail(claims.getSubject()).orElse(null);
        if (user == null)
            return ResponseEntity.status(401).build();

        // Set org only (idempotent: do nothing if already set)
        if (user.getOrganizationId() == null) {
            user.setOrganizationId(req.organizationId());
            userRepository.save(user);
        }

        // If later you add orgId to JWT, re-issue token here.
        return ResponseEntity.noContent().build();
    }

}
