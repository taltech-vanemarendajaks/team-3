# Technical Debt Analysis and Improvement Recommendations

## 📋 Table of Contents

  - [Overview](#overview)
  - [🔴 Critical Issues](#critical-issues)
  - [🟠 Important Issues](#important-issues)
  - [🟡 Structure and Architecture Issues](#structure-and-architecture-issues)
  - [🟢 Code Quality Improvements](#code-quality-improvements)
  - [📋 Priority Summary](#priority-summary)
  - [🎯 Implementation Recommendations](#implementation-recommendations)

---

## Overview

This document contains an analysis of the Borsibaar project codebase regarding technical debt, structural problems, deviations from best practices, and opportunities for improvement.

---

## 🔴 Critical Issues

### 1. TypeScript Type Checking Disabled in Production Build

**File:** `frontend/next.config.ts`

```typescript
typescript: {
    ignoreBuildErrors: true,
}
```

**Problem:** TypeScript errors are ignored during build, which can lead to runtime errors in production.

**Recommendation:** Remove `ignoreBuildErrors: true` and fix all type errors. Use strict TypeScript mode.

**Priority:** CRITICAL

---

### 2. Using System.out.println Instead of Logging

**Files:**
- `backend/src/main/java/com/borsibaar/controller/InventoryController.java` (lines 47-49)
- `backend/src/main/java/com/borsibaar/controller/OrganizationController.java` (line 38)
- `backend/src/main/java/com/borsibaar/jobs/PriceCorrectionJob.java` (lines 32, 36, 88)

**Problem:** Using `System.out.println` for logging:
- Does not allow controlling log level
- Not structured
- Cannot filter in production
- Difficult to track in distributed systems

**Recommendation:** Replace with SLF4J/Logback using Lombok's `@Slf4j`:
```java
@Slf4j
public class InventoryController {
    log.debug("Received request: {}", request);
    log.info("Processing stock addition for product: {}", request.productId());
}
```

**Priority:** HIGH

---

### 3. Duplication of organizationId in Inventory

**File:** `backend/src/main/java/com/borsibaar/entity/Inventory.java`

**Problem:** 
- `Inventory` stores `organizationId`, although it already exists in `Product`
- `@Deprecated` annotation and TODO, but the field is still used
- Risk of data desynchronization
- Complicates maintenance

**Recommendation:** 
1. Remove `organizationId` field from `Inventory`
2. Use `inventory.getProduct().getOrganizationId()` for access
3. Update all repository queries
4. Create Liquibase migration to remove the column

**Priority:** HIGH

---

## 🟠 Important Issues

### 4. Missing Business Rule Validation in DTOs

**Files:** Various DTOs

**Problem:**
- `ProductRequestDto` has basic validation, but no check for `minPrice <= basePrice <= maxPrice`
- No validation for negative quantities in some DTOs
- No check for reasonable limits (e.g., price cannot be 0.0001)

**Recommendation:**
```java
public record ProductRequestDto(
    @NotBlank String name,
    @NotNull @DecimalMin("0.01") BigDecimal currentPrice,
    @NotNull @DecimalMin("0.01") BigDecimal minPrice,
    @NotNull @DecimalMin("0.01") BigDecimal maxPrice,
    @NotNull Long categoryId
) {
    @AssertTrue(message = "minPrice must be <= currentPrice <= maxPrice")
    public boolean isValidPriceRange() {
        return minPrice.compareTo(currentPrice) <= 0 
            && currentPrice.compareTo(maxPrice) <= 0;
    }
}
```

**Priority:** MEDIUM

---

### 5. N+1 Problem in Queries

**File:** `backend/src/main/java/com/borsibaar/service/InventoryService.java` (lines 54-84)

**Problem:**
```java
inventories.stream()
    .map(inv -> {
        Product product = productRepository.findById(inv.getProductId())
            .orElse(null); // N+1 query!
```

**Recommendation:**
- Use `@EntityGraph` or JOIN FETCH in repository
- Or load all products with one query and create a Map

```java
@Query("SELECT i FROM Inventory i JOIN FETCH i.product WHERE i.organizationId = :orgId")
List<Inventory> findByOrganizationIdWithProduct(@Param("orgId") Long orgId);
```

**Priority:** MEDIUM

---

### 6. Monolithic Inventory Component (1196 lines)

**File:** `frontend/app/(protected)/(sidebar)/inventory/page.tsx`

**Problem:**
- One file contains all inventory management logic
- Mixed: data fetching, business logic, UI components, forms
- Difficult to test and maintain
- Many `@ts-expect-error` comments

**Recommendation:** Split into:
- `hooks/useInventory.ts` - data handling logic
- `components/inventory/InventoryTable.tsx` - table
- `components/inventory/ProductForm.tsx` - product creation form
- `components/inventory/StockModal.tsx` - modal windows
- `types/inventory.ts` - types (or use shared types)

**Priority:** MEDIUM

---

### 7. Missing Typing Between Frontend and Backend

**Problem:**
- TypeScript types are written manually and can get out of sync
- Many `@ts-expect-error` comments (15+ places)
- No single source of truth for types

**Recommendation:**
1. Generate TypeScript types from OpenAPI/Swagger schema
2. Use `springdoc-openapi` to generate schema
3. Use `openapi-typescript` to generate types
4. Or create a shared package with types

**Priority:** MEDIUM

---

### 8. Inconsistent Error Handling

**Problem:**
- Some controllers use `ResponseStatusException`
- Others use custom exceptions
- Frontend handles errors differently in different places
- No centralized error boundary in React

**Recommendation:**
- Backend: use only custom exceptions through `ApiExceptionHandler`
- Frontend: create `ErrorBoundary` component and unified `useErrorHandler` hook
- Use toast notifications for errors

**Priority:** MEDIUM

---

### 9. Manual Organization Check in Each Method

**Problem:** 
- Each service method manually checks `organizationId`
- Code duplication
- Easy to forget to add check

**Recommendation:**
Create AOP aspect or use Spring Security method-level security:
```java
@PreAuthorize("hasPermission(#productId, 'Product', 'READ')")
public ProductResponseDto getById(Long productId) { ... }
```

Or create base service with check:
```java
protected void validateOrganizationAccess(Long organizationId, Long resourceOrgId) {
    if (!organizationId.equals(resourceOrgId)) {
        throw new ForbiddenException("Access denied");
    }
}
```

**Priority:** MEDIUM

---

### 10. Hardcoded Strings for Transaction Types

**Files:** Various services

**Problem:**
```java
transaction.setTransactionType("SALE");
transaction.setTransactionType("PURCHASE");
transaction.setTransactionType("ADJUSTMENT");
```

**Recommendation:** Create enum:
```java
public enum TransactionType {
    SALE, PURCHASE, ADJUSTMENT, RETURN, 
    TRANSFER_IN, TRANSFER_OUT, INITIAL
}
```

**Priority:** LOW

---

## 🟡 Structure and Architecture Issues

### 11. Mixed Responsibilities in SalesService

**File:** `backend/src/main/java/com/borsibaar/service/SalesService.java`

**Problem:**
- `SalesService` contains dynamic pricing logic
- Pricing logic scattered between `SalesService` and `PriceCorrectionJob`
- No single place for pricing rules

**Recommendation:** Create `PricingService`:
```java
@Service
public class PricingService {
    public BigDecimal calculatePriceAfterSale(
        Product product, 
        BigDecimal currentPrice, 
        Organization org
    ) {
        if (!product.getCategory().isDynamicPricing()) {
            return currentPrice;
        }
        BigDecimal newPrice = currentPrice.add(org.getPriceIncreaseStep());
        return newPrice.min(product.getMaxPrice());
    }
}
```

**Priority:** MEDIUM

---

### 12. Inefficient Revenue Calculation

**File:** `backend/src/main/java/com/borsibaar/service/InventoryService.java` (lines 458-479)

**Problem:**
```java
private BigDecimal calculateTotalRevenue(List<InventoryTransaction> transactions) {
    return transactions.stream()
        .map(transaction -> {
            return inventoryRepository.findById(transaction.getInventoryId())
                .flatMap(inventory -> productRepository.findById(inventory.getProductId()))
                .map(product -> {
                    // N+1 queries!
```

**Recommendation:**
- Use JOIN in repository query
- Or use `priceBefore` from transaction (already in data)

**Priority:** MEDIUM

---

### 13. Commented Code in Production

**File:** `backend/src/main/java/com/borsibaar/service/SalesService.java` (lines 74-80)

**Problem:** Commented code should be removed or explained

**Recommendation:** Remove or move to git history

**Priority:** LOW

---

### 14. Using String Keys for Grouping

**File:** `backend/src/main/java/com/borsibaar/service/InventoryService.java` (lines 305-312)

**Problem:**
```java
return userId.toString() + "|" + (stationId != null ? stationId.toString() : "null");
// Then parsing back
String[] parts = entry.getKey().split("\|");
```

**Recommendation:** Use composite key:
```java
record UserStationKey(UUID userId, Long stationId) {}

Map<UserStationKey, List<InventoryTransaction>> transactionsByKey = 
    transactions.stream()
        .collect(Collectors.groupingBy(t -> 
            new UserStationKey(t.getCreatedBy(), t.getBarStationId())
        ));
```

**Priority:** LOW

---

### 15. Missing Validation in Frontend

**Problem:**
- Forms are not validated on client before submission
- No check for negative prices, empty fields, etc.
- Errors shown only after server response

**Recommendation:**
- Use `react-hook-form` with `zod` for validation
- Or use `yup` for validation schemas
- Show errors in real-time

**Priority:** MEDIUM

---

### 16. No Race Condition Handling

**Problem:**
- Race conditions possible with concurrent sales of same product
- No optimistic locking
- Can lead to negative stock

**Recommendation:**
Add `@Version` to `Inventory`:
```java
@Version
private Long version;
```

And handle `OptimisticLockingFailureException` in service.

**Priority:** HIGH

---

### 17. Public Access to organizationId in Controller

**File:** `backend/src/main/java/com/borsibaar/controller/InventoryController.java` (lines 24-34)

**Problem:**
```java
@RequestParam(required = false) Long organizationId
// If organizationId is provided, use it (for public access)
```

**Recommendation:**
- If for public access, create separate endpoint `/api/public/inventory`
- Or use separate controller with different authorization
- Document security of this approach

**Priority:** MEDIUM

---

### 18. Missing Structured Logging

**Problem:**
- No structured logs (JSON format)
- No correlation IDs for request tracing
- Difficult to track business events

**Recommendation:**
- Use structured logging (e.g., Logstash JSON encoder)
- Add MDC for correlation ID
- Log business events separately from technical

**Priority:** MEDIUM

---

### 19. Suboptimal Queries in Statistics

**File:** `backend/src/main/java/com/borsibaar/service/InventoryService.java`

**Problem:**
- `getUserSalesStats` and `getStationSalesStats` make many queries
- Can be optimized with one query with JOIN

**Recommendation:**
Create native queries or use `@EntityGraph`:
```java
@Query("SELECT t FROM InventoryTransaction t " +
       "JOIN FETCH t.inventory i " +
       "JOIN FETCH i.product p " +
       "WHERE t.transactionType = 'SALE' AND p.organizationId = :orgId")
List<InventoryTransaction> findSalesWithDetails(@Param("orgId") Long orgId);
```

**Priority:** LOW

---

### 20. Missing Caching

**Problem:**
- Categories loaded every time
- Organizations loaded repeatedly
- No caching for frequently requested data

**Recommendation:**
Add Spring Cache:
```java
@Cacheable("categories")
public List<CategoryResponseDto> getAllByOrg(Long organizationId) { ... }
```

**Priority:** LOW

---

## 🟢 Code Quality Improvements

### 21. Inconsistent Optional Usage

**Problem:**
- Some places use `Optional.ofNullable().orElse()`
- Others use direct null checks
- No unified style

**Recommendation:** Unify approach to working with nullable values

**Priority:** LOW

---

### 22. Magic Numbers and Strings

**Problem:**
- `"SALE-" + System.currentTimeMillis()` - ID format
- `"REDUCE-" + System.currentTimeMillis()` - reference ID format
- No constants for these formats

**Recommendation:**
```java
public class TransactionConstants {
    public static final String SALE_ID_PREFIX = "SALE-";
    public static final String REDUCE_ID_PREFIX = "REDUCE-";
}
```

**Priority:** LOW

---

### 23. Missing API Documentation

**Problem:**
- No Swagger/OpenAPI documentation
- Difficult to understand API without reading code
- No request/response examples

**Recommendation:**
Add `springdoc-openapi`:
```java
@Operation(summary = "Add stock to inventory")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Stock added"),
    @ApiResponse(responseCode = "404", description = "Product not found")
})
```

**Priority:** MEDIUM

---

### 24. Tests Cover Only Happy Path

**Problem:**
- Few negative tests
- No tests for concurrent access
- No tests for edge cases

**Recommendation:**
Add tests for:
- Negative scenarios (insufficient stock, wrong organization)
- Concurrent updates
- Edge cases (null values, boundary values)

**Priority:** MEDIUM

---

### 25. Missing Metrics and Monitoring

**Problem:**
- No performance metrics
- No request tracing
- Difficult to track issues in production

**Recommendation:**
- Add Micrometer for metrics
- Integrate with Prometheus/Grafana
- Add health checks

**Priority:** MEDIUM

---

## 📋 Priority Summary

### Critical (fix immediately):
1. ✅ TypeScript checking disabled in build
2. ✅ Using System.out.println
3. ✅ Duplication of organizationId in Inventory

### High (fix soon):
4. ✅ Missing race condition handling
5. ✅ Missing business rule validation
6. ✅ N+1 problems in queries
7. ✅ Monolithic Inventory component

### Medium (plan for next sprint):
8. ✅ Missing Frontend-Backend typing
9. ✅ Inconsistent error handling
10. ✅ Manual organization check
11. ✅ Mixed responsibilities in services
12. ✅ Missing validation in Frontend
13. ✅ Public access to organizationId
14. ✅ Missing structured logging
15. ✅ Missing API documentation
16. ✅ Improve test coverage
17. ✅ Missing metrics

### Low (can postpone):
18. ✅ Hardcoded strings for transaction types
19. ✅ Commented code
20. ✅ Using string keys
21. ✅ Inconsistent Optional usage
22. ✅ Magic numbers
23. ✅ Suboptimal queries in statistics
24. ✅ Missing caching

---

## 🎯 Implementation Recommendations

1. **Start with critical issues** - they can lead to production issues
2. **Refactor gradually** - don't rewrite everything at once
3. **Add tests before refactoring** - ensure safety of changes
4. **Document changes** - update architectural documents
5. **Code review** - all changes should go through review

---

*Document created based on codebase analysis from 2025-01-13*
