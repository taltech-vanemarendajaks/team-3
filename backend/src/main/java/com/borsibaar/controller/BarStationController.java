package com.borsibaar.controller;

import com.borsibaar.dto.BarStationRequestDto;
import com.borsibaar.dto.BarStationResponseDto;
import com.borsibaar.entity.User;
import com.borsibaar.repository.UserRepository;
import com.borsibaar.service.BarStationService;
import com.borsibaar.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/bar-stations")
@RequiredArgsConstructor
public class BarStationController {

    private final BarStationService barStationService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<BarStationResponseDto>> getAllStations(
            @CookieValue(name = "jwt", required = false) String token) {
        User user = authenticateUser(token);
        checkAdminRole(user);
        
        List<BarStationResponseDto> stations = barStationService.getAllStations(user.getOrganizationId());
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/user")
    public ResponseEntity<List<BarStationResponseDto>> getUserStations(
            @CookieValue(name = "jwt", required = false) String token) {
        User user = authenticateUser(token);
        
        List<BarStationResponseDto> stations = barStationService.getUserStations(user.getId(), user.getOrganizationId());
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarStationResponseDto> getStationById(
            @PathVariable Long id,
            @CookieValue(name = "jwt", required = false) String token) {
        User user = authenticateUser(token);
        
        BarStationResponseDto station = barStationService.getStationById(user.getOrganizationId(), id);
        return ResponseEntity.ok(station);
    }

    @PostMapping
    public ResponseEntity<BarStationResponseDto> createStation(
            @Valid @RequestBody BarStationRequestDto request,
            @CookieValue(name = "jwt", required = false) String token) {
        User user = authenticateUser(token);
        checkAdminRole(user);
        
        BarStationResponseDto station = barStationService.createStation(user.getOrganizationId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(station);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BarStationResponseDto> updateStation(
            @PathVariable Long id,
            @Valid @RequestBody BarStationRequestDto request,
            @CookieValue(name = "jwt", required = false) String token) {
        User user = authenticateUser(token);
        checkAdminRole(user);
        
        BarStationResponseDto station = barStationService.updateStation(user.getOrganizationId(), id, request);
        return ResponseEntity.ok(station);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(
            @PathVariable Long id,
            @CookieValue(name = "jwt", required = false) String token) {
        User user = authenticateUser(token);
        checkAdminRole(user);
        
        barStationService.deleteStation(user.getOrganizationId(), id);
        return ResponseEntity.noContent().build();
    }

    private User authenticateUser(String token) {
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        Claims claims = jwtService.parseToken(token);
        User user = userRepository.findByEmail(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (user.getOrganizationId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no organization");
        }
        return user;
    }

    private void checkAdminRole(User user) {
        if (user.getRole() == null || !"ADMIN".equals(user.getRole().getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin role required");
        }
    }
}

