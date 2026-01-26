package com.borsibaar.controller;

import com.borsibaar.dto.*;
import com.borsibaar.entity.User;
import com.borsibaar.service.InventoryService;
import com.borsibaar.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<InventoryResponseDto> getOrganizationInventory(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long organizationId) {
        // If organizationId is provided, use it (for public access)
        // Otherwise, get from authenticated user
        Long orgId = organizationId != null ? organizationId : SecurityUtils.getCurrentOrganizationId();
        return inventoryService.getByOrganization(orgId, categoryId);
    }

    @GetMapping("/product/{productId}")
    public InventoryResponseDto getProductInventory(@PathVariable Long productId) {
        return inventoryService.getByProductAndOrganization(productId, SecurityUtils.getCurrentOrganizationId());
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponseDto addStock(@RequestBody @Valid AddStockRequestDto request) {
        User user = SecurityUtils.getCurrentUser();
        log.debug("Adding stock - productId: {}, quantity: {}", request.productId(), request.quantity());
        return inventoryService.addStock(request, user.getId(), user.getOrganizationId());
    }

    @PostMapping("/remove")
    public InventoryResponseDto removeStock(@RequestBody @Valid RemoveStockRequestDto request) {
        User user = SecurityUtils.getCurrentUser();
        return inventoryService.removeStock(request, user.getId(), user.getOrganizationId());
    }

    @PostMapping("/adjust")
    public InventoryResponseDto adjustStock(@RequestBody @Valid AdjustStockRequestDto request) {
        User user = SecurityUtils.getCurrentUser();
        return inventoryService.adjustStock(request, user.getId(), user.getOrganizationId());
    }

    @GetMapping("/product/{productId}/history")
    public List<InventoryTransactionResponseDto> getTransactionHistory(@PathVariable Long productId) {
        return inventoryService.getTransactionHistory(productId, SecurityUtils.getCurrentOrganizationId());
    }

    @GetMapping("/sales-stats")
    public List<UserSalesStatsResponseDto> getUserSalesStats() {
        return inventoryService.getUserSalesStats(SecurityUtils.getCurrentOrganizationId());
    }

    @GetMapping("/station-sales-stats")
    public List<StationSalesStatsResponseDto> getStationSalesStats() {
        return inventoryService.getStationSalesStats(SecurityUtils.getCurrentOrganizationId());
    }
}
