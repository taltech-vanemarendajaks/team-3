package com.borsibaar.controller;

import com.borsibaar.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import com.borsibaar.repository.UserRepository;
import com.borsibaar.repository.RoleRepository;

import com.borsibaar.entity.User;
import com.borsibaar.entity.Role;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Duration;

@RestController
@RequestMapping("/api/test")
@Profile("test") // or @ConditionalOnProperty
public class TestAuthController {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository; // optional
  private final JwtService jwtService;

  public TestAuthController(UserRepository userRepository,
                            RoleRepository roleRepository,
                            JwtService jwtService) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.jwtService = jwtService;
  }

  public record TestLoginRequest(String email) {}

  @PostMapping("/login")
  public ResponseEntity<Void> login(@RequestBody TestLoginRequest req,
                                    HttpServletResponse response) {

    String email = req.email().toLowerCase().trim();

    User user = userRepository.findByEmail(email)
      .orElseGet(() -> {
        User u = new User();
        u.setEmail(email);
        u.setName("Test User");
        u.setOrganizationId(2L);

        // Optional: assign default role
        Role roleUser = roleRepository.findByName("USER")
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Missing role USER"));
        u.setRole(roleUser);

        return userRepository.save(u);
      });

    String jwt = jwtService.generateToken(user.getEmail()); // or generateToken(user.getEmail())

    ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
      .httpOnly(true)
      .sameSite("Lax")
      .path("/")
      // .secure(true) // when on https
      .maxAge(Duration.ofHours(1))
      .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    return ResponseEntity.ok().build();
  }
}