package com.borsibaar.service;

import com.borsibaar.dto.*;
import com.borsibaar.entity.Category;
import com.borsibaar.entity.Inventory;
import com.borsibaar.entity.InventoryTransaction;
import com.borsibaar.entity.Product;
import com.borsibaar.exception.BadRequestException;
import com.borsibaar.exception.GoneException;
import com.borsibaar.exception.NotFoundException;
import com.borsibaar.repository.InventoryRepository;
import com.borsibaar.repository.InventoryTransactionRepository;
import com.borsibaar.repository.ProductRepository;
import com.borsibaar.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalesService {

        private final InventoryRepository inventoryRepository;
        private final InventoryTransactionRepository inventoryTransactionRepository;
        private final ProductRepository productRepository;

        @Transactional
        public SaleResponseDto processSale(SaleRequestDto request, UUID userId, Long organizationId) {
                // Generate unique sale reference ID
                String saleId = "SALE-" + System.currentTimeMillis();

                List<SaleItemResponseDto> saleItems = new ArrayList<>();
                BigDecimal totalAmount = BigDecimal.ZERO;

                // Process each item in the sale
                for (SaleItemRequestDto item : request.items()) {
                        SaleItemResponseDto saleItem = processSaleItem(item, userId, organizationId, saleId,
                                        request.barStationId());
                        saleItems.add(saleItem);
                        totalAmount = totalAmount.add(saleItem.totalPrice());
                }

                return new SaleResponseDto(
                                saleId,
                                saleItems,
                                totalAmount,
                                request.notes(),
                                OffsetDateTime.now());
        }

        private SaleItemResponseDto processSaleItem(SaleItemRequestDto item, UUID userId, Long organizationId,
                        String saleId, Long barStationId) {
                // Verify product exists and belongs to organization
                Product product = productRepository.findById(item.productId())
                                .orElseThrow(() -> NotFoundException.forEntity("Product", item.productId()));

                SecurityUtils.requireOrganization(product.getOrganizationId(), organizationId, "Product");

                if (!product.isActive()) {
                        throw GoneException.resourceDeleted("Product");
                }

                // Get inventory for this product
                /*
                 * Inventory inventory = inventoryRepository
                 * .findByOrganizationIdAndProductId(organizationId, item.productId())
                 * .orElseThrow(() -> new ResponseStatusException(
                 * HttpStatus.NOT_FOUND, "No inventory found for product: " +
                 * product.getName()));
                 */
                Inventory inventory = Optional.ofNullable(product.getInventory())
                                .orElseThrow(() -> NotFoundException.of("No inventory found for product", product.getName()));

                // Check stock availability
                BigDecimal oldQuantity = inventory.getQuantity();
                BigDecimal newQuantity = oldQuantity.subtract(item.quantity());

                if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                        throw BadRequestException.of(
                                        "Insufficient stock for " + product.getName() +
                                                        ". Available: " + oldQuantity + ", Requested: "
                                                        + item.quantity());
                }

                // Calculate pricing
                BigDecimal priceBeforeSale = Optional.ofNullable(inventory.getAdjustedPrice())
                                .orElse(product.getBasePrice());
                BigDecimal totalPrice = priceBeforeSale.multiply(item.quantity());

                BigDecimal priceAfterSale = priceBeforeSale;
                Category category = product.getCategory();
                if (category != null && category.isDynamicPricing()) {
                        priceAfterSale = priceBeforeSale.add(product.getOrganization().getPriceIncreaseStep());
                        if (product.getMaxPrice() != null && priceAfterSale.compareTo(product.getMaxPrice()) > 0) {
                                priceAfterSale = product.getMaxPrice();
                        }
                }


                // Update inventory
                inventory.setQuantity(newQuantity);
                inventory.setUpdatedAt(OffsetDateTime.now());
                inventory.setAdjustedPrice(priceAfterSale);

                inventory = inventoryRepository.save(inventory);

                // Create sale transaction
                createSaleTransaction(inventory, item.quantity(),
                                oldQuantity, newQuantity, priceBeforeSale, priceAfterSale,
                                saleId, userId, barStationId);

                return new SaleItemResponseDto(
                                item.productId(),
                                product.getName(),
                                item.quantity(),
                                priceBeforeSale,
                                totalPrice);
        }

        private void createSaleTransaction(Inventory inventory, BigDecimal quantity,
                        BigDecimal quantityBefore, BigDecimal quantityAfter,
                        BigDecimal priceBefore, BigDecimal priceAfter,
                        String saleId, UUID userId, Long barStationId) {
                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setInventory(inventory);
                transaction.setTransactionType("SALE");
                transaction.setQuantityChange(quantity.negate()); // Negative for sales
                transaction.setQuantityBefore(quantityBefore);
                transaction.setQuantityAfter(quantityAfter);
                transaction.setPriceBefore(priceBefore);
                transaction.setPriceAfter(priceAfter);
                transaction.setReferenceId(saleId);
                transaction.setNotes("POS Sale");
                transaction.setCreatedBy(userId);
                transaction.setBarStationId(barStationId);
                transaction.setCreatedAt(OffsetDateTime.now());
                inventoryTransactionRepository.save(transaction);
        }
}