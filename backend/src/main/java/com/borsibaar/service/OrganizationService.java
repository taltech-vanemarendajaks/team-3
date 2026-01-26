package com.borsibaar.service;

import com.borsibaar.dto.OrganizationRequestDto;
import com.borsibaar.dto.OrganizationResponseDto;
import com.borsibaar.entity.Organization;
import com.borsibaar.exception.NotFoundException;
import com.borsibaar.mapper.OrganizationMapper;
import com.borsibaar.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    public OrganizationService(OrganizationRepository organizationRepository, OrganizationMapper organizationMapper) {
        this.organizationRepository = organizationRepository;
        this.organizationMapper = organizationMapper;
    }

    @Transactional
    public OrganizationResponseDto create(OrganizationRequestDto request) {
        Organization organization = organizationMapper.toEntity(request);
        organization.setCreatedAt(OffsetDateTime.now());
        organization.setUpdatedAt(organization.getCreatedAt());
        Organization saved = organizationRepository.save(organization);
        return organizationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrganizationResponseDto getById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> NotFoundException.forEntity("Organization", id));
        return organizationMapper.toResponse(organization);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponseDto> getAll() {
        return organizationRepository.findAll()
                .stream()
                .map(organizationMapper::toResponse)
                .toList();
    }

    @Transactional
    public OrganizationResponseDto update(Long id, OrganizationRequestDto request) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> NotFoundException.forEntity("Organization", id));
        organizationMapper.updateEntity(organization, request);
        organization.setUpdatedAt(OffsetDateTime.now());
        Organization saved = organizationRepository.save(organization);
        return organizationMapper.toResponse(saved);
    }
}
