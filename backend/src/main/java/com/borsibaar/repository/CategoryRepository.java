package com.borsibaar.repository;

import com.borsibaar.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByIdAndOrganizationId(Long id, Long organizationId);

    Iterable<Category> findAllByOrganizationId(Long organizationId);

    boolean existsByOrganizationIdAndNameIgnoreCase(Long organizationId, String name);
}
