package com.borsibaar.controller;

import com.borsibaar.dto.BarStationRequestDto;
import com.borsibaar.dto.BarStationResponseDto;
import com.borsibaar.entity.User;
import com.borsibaar.service.BarStationService;
import com.borsibaar.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bar-stations")
@RequiredArgsConstructor
public class BarStationController {

    private final BarStationService barStationService;

    @GetMapping
    public ResponseEntity<List<BarStationResponseDto>> getAllStations() {
        User admin = SecurityUtils.getCurrentAdmin();
        List<BarStationResponseDto> stations = barStationService.getAllStations(admin.getOrganizationId());
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/user")
    public ResponseEntity<List<BarStationResponseDto>> getUserStations() {
        User user = SecurityUtils.getCurrentUser();
        List<BarStationResponseDto> stations = barStationService.getUserStations(user.getId(), user.getOrganizationId());
        return ResponseEntity.ok(stations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarStationResponseDto> getStationById(@PathVariable Long id) {
        Long organizationId = SecurityUtils.getCurrentOrganizationId();
        BarStationResponseDto station = barStationService.getStationById(organizationId, id);
        return ResponseEntity.ok(station);
    }

    @PostMapping
    public ResponseEntity<BarStationResponseDto> createStation(@Valid @RequestBody BarStationRequestDto request) {
        User admin = SecurityUtils.getCurrentAdmin();
        BarStationResponseDto station = barStationService.createStation(admin.getOrganizationId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(station);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BarStationResponseDto> updateStation(
            @PathVariable Long id,
            @Valid @RequestBody BarStationRequestDto request) {
        User admin = SecurityUtils.getCurrentAdmin();
        BarStationResponseDto station = barStationService.updateStation(admin.getOrganizationId(), id, request);
        return ResponseEntity.ok(station);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Long id) {
        User admin = SecurityUtils.getCurrentAdmin();
        barStationService.deleteStation(admin.getOrganizationId(), id);
        return ResponseEntity.noContent().build();
    }
}

