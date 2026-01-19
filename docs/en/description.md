# Borsibaar Application Business Logic


## 📋 Table of Contents

  - [General Description](#general-description)
  - [Core Domain Entities](#core-domain-entities)
    - [1. Organization](#1-organization)
    - [2. User](#2-user)
    - [3. Category](#3-category)
    - [4. Product](#4-product)
    - [5. Inventory](#5-inventory)
    - [6. InventoryTransaction](#6-inventorytransaction)
    - [7. BarStation](#7-barstation)
  - [Core Business Processes](#core-business-processes)
    - [1. Authentication and Authorization](#1-authentication-and-authorization)
    - [2. Product Management](#2-product-management)
    - [3. Inventory Management](#3-inventory-management)
    - [4. Dynamic Pricing](#4-dynamic-pricing)
    - [5. Sales (POS)](#5-sales-pos)
    - [6. Bar Station Management](#6-bar-station-management)
    - [7. Analytics and Reporting](#7-analytics-and-reporting)
    - [8. Transaction History](#8-transaction-history)
  - [Business Rules and Constraints](#business-rules-and-constraints)
    - [1. Multi-tenancy](#1-multi-tenancy)
    - [2. Stock](#2-stock)
    - [3. Pricing](#3-pricing)
    - [4. Products](#4-products)
    - [5. Users](#5-users)
    - [6. Transactions](#6-transactions)
  - [User Scenarios](#user-scenarios)
    - [1. Administrator](#1-administrator)
    - [2. User (Bartender)](#2-user-bartender)
    - [3. Inventory Manager](#3-inventory-manager)
  - [Technical Features](#technical-features)

---



## General Description
Warehouse/inventory management system for bars/restaurants with multi-tenant architecture. Each organization manages its own products, inventory, categories, users, and bar stations.

---

## Core Domain Entities

### 1. Organization
- Root entity of multi-tenancy
- Dynamic pricing parameters: `priceIncreaseStep`, `priceDecreaseStep`
- Settings apply to all organization products

### 2. User
- Authentication via OAuth2 (Google)
- Roles: `USER`, `ADMIN`
- Association with one organization (`organizationId`)
- Many-to-many relationship with `BarStation` (station access)

### 3. Category
- Product grouping
- `dynamicPricing` flag (enables dynamic pricing)

### 4. Product
- Product catalog
- Prices: `basePrice`, `minPrice`, `maxPrice`
- `isActive` flag (soft delete)
- Relationship with category and organization

### 5. Inventory
- Product-based stock levels
- `quantity` (non-negative value)
- `adjustedPrice` (current price with dynamics calculation)

### 6. InventoryTransaction
- Audit trail for inventory and price changes
- Types: `SALE`, `PURCHASE`, `ADJUSTMENT`, `RETURN`, `TRANSFER_IN`, `TRANSFER_OUT`, `INITIAL`
- Stores: `quantityBefore`, `quantityAfter`, `priceBefore`, `priceAfter`, `createdBy`, `barStationId`, `referenceId`

### 7. BarStation
- Point of sale (POS) locations
- Many-to-many relationship with users
- Used for tracking sales by station

---

## Core Business Processes

### 1. Authentication and Authorization
- OAuth2 via Google
- JWT tokens in HTTP-only cookies
- Organization check on every request
- Onboarding: organization's first user gets `ADMIN` role

### 2. Product Management
- **Creation**: automatically creates inventory record with `quantity = 0` and `INITIAL` transaction
- **Deletion**: soft delete (`isActive = false`)
- **Validation**: name uniqueness within organization (case-insensitive)

### 3. Inventory Management

**Adding Stock (`PURCHASE`)**:
- Increases `quantity`
- Creates `PURCHASE` type transaction
- Price does not change

**Removing Stock (`ADJUSTMENT`)**:
- Decreases `quantity` (non-negative check)
- Creates `ADJUSTMENT` type transaction with negative `quantityChange`
- Can specify `referenceId` and `notes`

**Adjusting Stock (`ADJUSTMENT`)**:
- Sets new `quantity` value
- Creates transaction with difference

### 4. Dynamic Pricing

**On Sale (`SalesService.processSaleItem`)**:
- If category has `dynamicPricing = true`:
  - Price increases by `organization.priceIncreaseStep`
  - Limit: not above `product.maxPrice`
  - New price saved to `inventory.adjustedPrice`
- If `dynamicPricing = false`: price does not change

**Automatic Price Reduction (`PriceCorrectionJob`)**:
- Runs every minute (`@Scheduled(cron = "0 * * * * *")`)
- Finds products without sales in the last minute
- Decreases price by `organization.priceDecreaseStep`
- Limit: not below `product.minPrice`
- Creates `ADJUSTMENT` type transaction with price change (quantity unchanged)

### 5. Sales (POS)

**Sales Process (`SalesService.processSale`)**:
1. Generate unique `saleId` (format: `SALE-{timestamp}`)
2. For each product:
   - Stock availability check
   - Product activity check
   - Organization ownership check
   - Price calculation (with dynamic pricing)
   - Stock reduction
   - Price update (if dynamic pricing enabled)
   - Create `SALE` transaction with negative `quantityChange`
   - Associate with `barStationId` and `userId`
3. Returns: list of sold products, total amount, `saleId`

**Validations**:
- Insufficient stock → error
- Product inactive → error
- Product not owned by organization → 403 error

### 6. Bar Station Management

**Creation/Update**:
- Only for `ADMIN`
- User assignment (many-to-many)
- Name uniqueness validation within organization

**Access**:
- `ADMIN`: sees all organization stations
- `USER`: sees only assigned stations
- If `USER` has one station → automatic redirect to POS

### 7. Analytics and Reporting

**User-based Statistics (`getUserSalesStats`)**:
- Grouping by `userId` + `barStationId`
- Unique sales count (`referenceId`)
- Revenue calculation (using `basePrice`)

**Station-based Statistics (`getStationSalesStats`)**:
- Grouping by `barStationId`
- Unique sales count
- Total revenue calculation

**Dashboard Display**:
- Station leaderboard
- User performance table
- Organization settings (only for `ADMIN`)

### 8. Transaction History

**Viewing History (`getTransactionHistory`)**:
- All product transactions
- Sorted by date (newest first)
- Displays: type, quantity change, prices before/after, user, date, `referenceId`, `notes`

---

## Business Rules and Constraints

### 1. Multi-tenancy
- All operations isolated by `organizationId`
- Ownership check before each operation

### 2. Stock
- Cannot be negative (CHECK constraint + code validation)
- Stock sufficiency checked on sale

### 3. Pricing
- Dynamic pricing works only for categories with `dynamicPricing = true`
- Prices limited to `minPrice` and `maxPrice` range
- Automatic reduction only for products without sales

### 4. Products
- Name uniqueness within organization (case-insensitive)
- Soft delete (`isActive = false`)
- Inventory record with zero stock created automatically on creation

### 5. Users
- One user = one organization
- Organization's first user automatically becomes `ADMIN`
- Users can be assigned to multiple bar stations

### 6. Transactions
- Immutable
- Always store state before and after change
- Relationship with user (`createdBy`) and station (`barStationId`)

---

## User Scenarios

### 1. Administrator
- Organization management (pricing step configuration)
- Category and product management
- Bar station and user management
- Sales analytics viewing

### 2. User (Bartender)
- Access to assigned bar stations
- Product sales via POS
- Stock and price viewing

### 3. Inventory Manager
- Stock management (addition, removal, adjustment)
- Transaction history viewing
- New product and category creation

---

## Technical Features

- **Transactional**: all inventory operations execute under `@Transactional`
- **Audit**: all changes recorded in `InventoryTransaction`
- **Performance**: grouping queries for statistics, eager loading for related entities
- **Security**: access control checks at service and controller level

---

This description reflects the current application business logic based on codebase analysis.
