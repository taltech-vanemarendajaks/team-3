package com.borsibaar.controller;

import com.borsibaar.dto.OrganizationRequestDto;
import com.borsibaar.dto.OrganizationResponseDto;
import com.borsibaar.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationResponseDto create(@RequestBody @Valid OrganizationRequestDto request) {
        return organizationService.create(request);
    }

    @GetMapping("/{id}")
    public OrganizationResponseDto get(@PathVariable Long id) {
        return organizationService.getById(id);
    }

    @GetMapping
    public List<OrganizationResponseDto> getAll() {
        return organizationService.getAll();
    }

    @PutMapping("/{id}")
    public OrganizationResponseDto update(@PathVariable Long id, @RequestBody @Valid OrganizationRequestDto request) {
        log.debug("Updating organization {} with request: {}", id, request);
        return organizationService.update(id, request);
    }

}
