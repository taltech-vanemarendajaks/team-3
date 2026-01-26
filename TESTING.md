# Testing Strategy

## Task: Fix TypeScript Configuration and Add Inventory Types

### Branch
`task/Fix-TypeScript-Configuration-and-Add-Inventory-Types`

### Description
This task fixes TypeScript configuration to enable proper type checking during build and adds missing type definitions for inventory data to remove all `@ts-expect-error` comments.

### Changes Made
1. **Fixed `next.config.ts`:**
   - Removed `typescript.ignoreBuildErrors: true` option
   - Build will now fail on TypeScript errors

2. **Added types to `inventory/page.tsx`:**
   - Added `InventoryResponseDto` interface (matching backend DTO)
   - Added `CategoryResponseDto` interface
   - Updated state variable types:
     - `inventory`: `useState<InventoryResponseDto[]>([])`
     - `selectedProduct`: `useState<InventoryResponseDto | null>(null)`
     - `categories`: `useState<CategoryResponseDto[]>([])`

3. **Removed all `@ts-expect-error` comments (11 locations):**
   - Lines 222, 249, 280 - removed comments in `handleAddStock`, `handleRemoveStock`, `handleAdjustStock`
   - Lines 368, 374, 380, 386, 393 - added parameter types to `open*Modal` functions
   - Line 401 - removed comment in `filteredInventory`
   - Line 406 - added parameter type to `getStockStatus`
   - Line 519 - removed comment in `map` function

4. **Added null checks:**
   - Added `if (!selectedProduct)` checks in `handleRemoveStock` and `handleAdjustStock` functions

### Files Modified
- `frontend/next.config.ts`
- `frontend/app/(protected)/(sidebar)/inventory/page.tsx`

---

## 1. Type Checking Verification

**Build Test:**
```bash
cd frontend
npm run build
```
- ✅ Should complete without TypeScript errors
- ✅ Should fail if there are any type mismatches
- ❌ If `ignoreBuildErrors: true` is still present, errors will be ignored

**Type Check Only:**
```bash
cd frontend
npx tsc --noEmit
```
- ✅ Should show no type errors
- ✅ Useful for quick checks without full build

## 2. Development Server Testing

**Start Dev Server:**
```bash
cd frontend
npm run dev
```
- ✅ Should start without TypeScript errors in console
- ✅ Check browser console for runtime errors

## 3. Functional Testing - Inventory Page

**Test Inventory Page (`/inventory`):**
1. **Page Load:**
   - ✅ Page loads without errors
   - ✅ Inventory list displays correctly
   - ✅ All product data shows (name, price, quantity, etc.)

2. **Product Actions:**
   - ✅ Click "Add Stock" button - modal opens, no console errors
   - ✅ Click "Remove Stock" button - modal opens, no console errors
   - ✅ Click "Adjust Stock" button - modal opens, no console errors
   - ✅ Click "View History" button - modal opens, history loads
   - ✅ Click "Delete Product" button - modal opens, no console errors

3. **Form Operations:**
   - ✅ Add stock with valid quantity - should work
   - ✅ Remove stock with valid quantity - should work
   - ✅ Adjust stock to new quantity - should work
   - ✅ Create new product - should work
   - ✅ Create new category - should work

4. **Search Functionality:**
   - ✅ Type in search box - filtered results show correctly
   - ✅ No type errors when filtering

5. **Edge Cases:**
   - ✅ Try to add/remove stock when no product selected - should show alert
   - ✅ Try operations with empty/null data - should handle gracefully
   - ✅ Check with products that have null/undefined fields

## 4. Type Safety Testing

**Test Type Errors (should fail):**
1. Temporarily break a type to verify build fails:
   ```typescript
   // In inventory/page.tsx, temporarily add:
   const test: InventoryResponseDto = { wrongField: 123 };
   ```
   - ✅ Build should fail with type error
   - ✅ Remove test code after verification

## 5. Browser Console Testing

**Check for Runtime Errors:**
1. Open browser DevTools (F12)
2. Go to Console tab
3. Navigate to `/inventory` page
4. ✅ Should see no TypeScript-related errors
5. ✅ Should see no "Cannot read property of undefined" errors
6. ✅ All API calls should work correctly

## 6. Integration Testing

**Test with Real Backend:**
1. Start backend: `docker compose up`
2. Start frontend: `cd frontend && npm run dev`
3. Login and navigate to inventory
4. ✅ All CRUD operations work
5. ✅ Data displays correctly from API
6. ✅ No type mismatches between frontend and backend

## 7. Linting Check

```bash
cd frontend
npm run lint
```
- ✅ Should pass without errors
- ✅ No unused variables or imports

## 8. Visual Regression (Optional)

- ✅ UI looks the same as before
- ✅ All modals open correctly
- ✅ Tables display data properly
- ✅ No layout issues

## Quick Test Checklist

```bash
# 1. Type check
cd frontend && npx tsc --noEmit

# 2. Build test
cd frontend && npm run build

# 3. Dev server
cd frontend && npm run dev
# Then manually test inventory page in browser

# 4. Lint
cd frontend && npm run lint
```

## Expected Results

**Before Fix:**
- ❌ Build succeeds even with type errors
- ❌ `@ts-expect-error` comments suppress warnings
- ❌ Runtime errors possible due to missing types

**After Fix:**
- ✅ Build fails if there are type errors
- ✅ All types are properly defined
- ✅ No `@ts-expect-error` comments needed
- ✅ Better IDE autocomplete and IntelliSense
- ✅ Catch type errors at compile time, not runtime

---

## Task: Add Sorting to Inventory Page Product List

### Branch
`task/Add-Sorting-to-Inventory-Page-Product-List`

### Description
This task adds column-based sorting functionality to the inventory page product list, allowing users to sort products by name, price, quantity, and other columns to improve usability and make it easier to find and manage products.

### Changes Made
1. **Added sorting state management:**
   - Added `sortColumn` state (tracks which column is being sorted)
   - Added `sortDirection` state ('asc' | 'desc')
   - Default sort by product name ascending

2. **Implemented sorting logic:**
   - Created `handleSort` function to toggle sort direction or set new column
   - Created `sortedInventory` that sorts filtered results
   - Handles different data types: strings (productName), numbers (prices, quantity), dates (updatedAt)

3. **Made table headers clickable:**
   - Added click handlers to sortable column headers
   - Added hover effects to indicate clickability
   - Added cursor and select-none classes

4. **Added visual indicators:**
   - Imported `ArrowUp` and `ArrowDown` icons from lucide-react
   - Display sort direction icons next to active column header

5. **Sortable columns:**
   - Product Name (alphabetical)
   - Current Price (numerical)
   - Min Price (numerical)
   - Max Price (numerical)
   - Quantity (numerical)
   - Last Updated (date/time)

### Files Modified
- `frontend/app/(protected)/(sidebar)/inventory/page.tsx`

---

## Testing Strategy for Sorting Feature

### 1. Build Verification

**Build Test:**
```bash
cd frontend
npm run build
```
- ✅ Should complete without errors
- ✅ No TypeScript errors related to sorting functionality

### 2. Visual Testing - Table Headers

**Test Clickable Headers:**
1. Navigate to `/inventory` page
2. **Hover over column headers:**
   - ✅ Product, Current Price, Min Price, Max Price, Quantity, Last Updated headers show hover effect (background changes)
   - ✅ Cursor changes to pointer when hovering over sortable headers
   - ✅ Status and Actions columns should NOT show hover effect (not sortable)

3. **Check visual indicators:**
   - ✅ Product Name column should show ArrowUp icon (default sort)
   - ✅ Other sortable columns should not show icons initially
   - ✅ Icons appear/disappear correctly when clicking different columns

### 3. Functional Testing - Sorting Behavior

**Test Each Sortable Column:**

1. **Product Name (Alphabetical):**
   - ✅ Click "Product" header - items sort A-Z
   - ✅ Click again - items sort Z-A
   - ✅ Arrow icon changes direction (↑ to ↓)
   - ✅ Sorting is case-insensitive

2. **Current Price (Numerical):**
   - ✅ Click "Current Price" header - items sort by price (low to high)
   - ✅ Click again - items sort (high to low)
   - ✅ Arrow icon appears and changes direction
   - ✅ Products with same price maintain relative order

3. **Min Price (Numerical):**
   - ✅ Click "Min Price" header - items sort by min price
   - ✅ Click again - reverses order
   - ✅ Handles missing/null values correctly (shows as 0 or --)

4. **Max Price (Numerical):**
   - ✅ Click "Max Price" header - items sort by max price
   - ✅ Click again - reverses order
   - ✅ Handles missing/null values correctly

5. **Quantity (Numerical):**
   - ✅ Click "Quantity" header - items sort by quantity (low to high)
   - ✅ Click again - items sort (high to low)
   - ✅ Zero quantities appear at top or bottom depending on direction

6. **Last Updated (Date/Time):**
   - ✅ Click "Last Updated" header - items sort by date (oldest first)
   - ✅ Click again - items sort (newest first)
   - ✅ Date parsing works correctly

### 4. Sorting with Search/Filter

**Test Sorting + Search Integration:**
1. Type in search box to filter products
2. ✅ Filtered results are still sortable
3. ✅ Sorting applies to filtered results only
4. ✅ Clear search - sorting persists on full inventory
5. ✅ Change sort while search is active - works correctly

### 5. Edge Cases

**Test Edge Cases:**
1. **Empty inventory:**
   - ✅ Page loads without errors when no products
   - ✅ Headers still clickable (no crash)
   - ✅ Empty state message displays correctly

2. **Single product:**
   - ✅ Sorting works with one item (no errors)
   - ✅ Visual indicators still appear

3. **Products with missing data:**
   - ✅ Products with null/undefined prices handled gracefully
   - ✅ Products with zero quantity sort correctly
   - ✅ Products with missing dates handled correctly

4. **Rapid clicking:**
   - ✅ Rapidly clicking headers doesn't cause errors
   - ✅ Sort state updates correctly
   - ✅ UI remains responsive

### 6. Default Sort Behavior

**Test Default Sorting:**
1. ✅ Page loads with default sort (Product Name, ascending)
2. ✅ ArrowUp icon visible on Product Name column
3. ✅ Products displayed in alphabetical order by default
4. ✅ Default sort persists after page refresh (if state is maintained)

### 7. Visual Indicators

**Test Visual Feedback:**
1. ✅ Active sort column shows appropriate arrow icon
2. ✅ ArrowUp (↑) shows for ascending sort
3. ✅ ArrowDown (↓) shows for descending sort
4. ✅ Only one column shows sort icon at a time
5. ✅ Icons are properly aligned with column headers
6. ✅ Hover effect works on all sortable columns

### 8. Browser Console Testing

**Check for Errors:**
1. Open browser DevTools (F12)
2. Go to Console tab
3. Navigate to `/inventory` page
4. Click various column headers
5. ✅ No JavaScript errors in console
6. ✅ No React warnings
7. ✅ No TypeScript-related errors

### 9. Responsive Design Testing

**Test on Different Screen Sizes:**
1. ✅ Sorting works on desktop (full width)
2. ✅ Sorting works on tablet (medium width)
3. ✅ Sorting works on mobile (narrow width)
4. ✅ Headers remain clickable on touch devices
5. ✅ Icons visible and properly sized on all screens

### 10. Performance Testing

**Test Performance:**
1. ✅ Sorting is instant with small inventory (< 50 items)
2. ✅ Sorting is fast with medium inventory (50-200 items)
3. ✅ Sorting works without lag with large inventory (200+ items)
4. ✅ No unnecessary re-renders when clicking headers

### Quick Test Checklist

```bash
# 1. Build test
cd frontend && npm run build

# 2. Dev server
cd frontend && npm run dev
# Then manually test sorting in browser:
# - Click each sortable column header
# - Verify sort direction toggles
# - Check visual indicators
# - Test with search/filter
```

### Manual Testing Steps

1. **Start the application:**
   ```bash
   cd frontend && npm run dev
   ```

2. **Navigate to inventory page:**
   - Login if needed
   - Go to `/inventory`

3. **Test each sortable column:**
   - Click "Product" header → verify A-Z sort
   - Click again → verify Z-A sort
   - Click "Current Price" → verify price sort
   - Click "Quantity" → verify quantity sort
   - Click "Last Updated" → verify date sort

4. **Test with search:**
   - Type in search box
   - Click sortable header
   - Verify filtered results are sorted

5. **Verify visual indicators:**
   - Check arrow icons appear/disappear correctly
   - Verify hover effects work

### Expected Results

**Before Implementation:**
- ❌ No sorting functionality
- ❌ Products displayed in fixed order
- ❌ Difficult to find specific products in large inventory

**After Implementation:**
- ✅ All sortable columns work correctly
- ✅ Visual indicators show active sort
- ✅ Sorting works with search/filter
- ✅ Default sort applied on page load
- ✅ Smooth user experience with hover effects
- ✅ No performance issues

---

## Task: Add Validation to BarStationRequestDto

### Branch
`task/Add-Validation-to-BarStationRequestDto`

### Description
This task adds validation annotations to `BarStationRequestDto` to ensure data quality at the API boundary and provide clear error messages for invalid requests.

### Changes Made
1. **Added validation annotations:**
   - `name`: `@NotBlank` and `@Size(max = 120)`
   - `description`: `@Size(max = 500)` (optional field)

### Files Modified
- `backend/src/main/java/com/borsibaar/dto/BarStationRequestDto.java`

---

## Testing Strategy for BarStationRequestDto Validation

### 1. Build Verification

**Build Test:**
```bash
cd backend
docker compose exec backend ./mvnw clean compile
```
- ✅ Should compile without errors
- ✅ No import errors for validation annotations

### 2. Unit Tests - Controller Level

**Test Validation in BarStationControllerTest:**

1. **Test empty/null name:**
   ```java
   BarStationRequestDto request = new BarStationRequestDto(null, "Desc", true, null);
   // Should return 400 Bad Request
   ```

2. **Test blank name:**
   ```java
   BarStationRequestDto request = new BarStationRequestDto("", "Desc", true, null);
   // Should return 400 Bad Request
   ```

3. **Test name exceeding max length:**
   ```java
   String longName = "a".repeat(121); // 121 characters
   BarStationRequestDto request = new BarStationRequestDto(longName, "Desc", true, null);
   // Should return 400 Bad Request
   ```

4. **Test description exceeding max length:**
   ```java
   String longDesc = "a".repeat(501); // 501 characters
   BarStationRequestDto request = new BarStationRequestDto("Station", longDesc, true, null);
   // Should return 400 Bad Request
   ```

5. **Test valid request:**
   ```java
   BarStationRequestDto request = new BarStationRequestDto("Valid Station", "Description", true, null);
   // Should return 201 Created or 200 OK
   ```

### 3. Manual API Testing

**Test via Swagger UI or curl:**

1. **Test POST /api/bar-stations with empty name:**
   ```bash
   curl -X POST http://localhost:8080/api/bar-stations \
     -H "Content-Type: application/json" \
     -H "Cookie: jwt=YOUR_TOKEN" \
     -d '{"name": "", "description": "Test", "isActive": true}'
   ```
   - ✅ Should return 400 Bad Request
   - ✅ Error message should mention "Station name is required"

2. **Test POST /api/bar-stations with null name:**
   ```bash
   curl -X POST http://localhost:8080/api/bar-stations \
     -H "Content-Type: application/json" \
     -H "Cookie: jwt=YOUR_TOKEN" \
     -d '{"description": "Test", "isActive": true}'
   ```
   - ✅ Should return 400 Bad Request

3. **Test POST /api/bar-stations with long name:**
   ```bash
   curl -X POST http://localhost:8080/api/bar-stations \
     -H "Content-Type: application/json" \
     -H "Cookie: jwt=YOUR_TOKEN" \
     -d '{"name": "VERY_LONG_NAME_...", "description": "Test", "isActive": true}'
   ```
   - ✅ Should return 400 Bad Request if name > 120 chars
   - ✅ Error message should mention max length

4. **Test POST /api/bar-stations with long description:**
   ```bash
   curl -X POST http://localhost:8080/api/bar-stations \
     -H "Content-Type: application/json" \
     -H "Cookie: jwt=YOUR_TOKEN" \
     -d '{"name": "Station", "description": "VERY_LONG_DESC...", "isActive": true}'
   ```
   - ✅ Should return 400 Bad Request if description > 500 chars

5. **Test POST /api/bar-stations with valid data:**
   ```bash
   curl -X POST http://localhost:8080/api/bar-stations \
     -H "Content-Type: application/json" \
     -H "Cookie: jwt=YOUR_TOKEN" \
     -d '{"name": "Valid Station", "description": "Valid description", "isActive": true}'
   ```
   - ✅ Should return 201 Created
   - ✅ Station should be created successfully

6. **Test PUT /api/bar-stations/{id} with invalid data:**
   - ✅ Same validation rules apply for update endpoint
   - ✅ Test all invalid scenarios for update

### 4. Error Response Format Testing

**Verify error response structure:**
1. Make invalid request
2. ✅ Response status: 400 Bad Request
3. ✅ Response body contains:
   ```json
   {
     "type": "about:blank",
     "title": "Validation failed",
     "status": 400,
     "detail": "name: Station name is required",
     "errors": {
       "name": "Station name is required"
     },
     "timestamp": "...",
     "path": "/api/bar-stations"
   }
   ```

### 5. Edge Cases

**Test edge cases:**
1. **Name exactly at max length (120 chars):**
   - ✅ Should be accepted

2. **Name one character over max (121 chars):**
   - ✅ Should be rejected

3. **Description exactly at max length (500 chars):**
   - ✅ Should be accepted

4. **Description one character over max (501 chars):**
   - ✅ Should be rejected

5. **Null description:**
   - ✅ Should be accepted (optional field)

6. **Empty description:**
   - ✅ Should be accepted (optional field)

7. **Whitespace-only name:**
   - ✅ Should be rejected (due to @NotBlank)

### 6. Integration with Existing Functionality

**Test that validation doesn't break existing functionality:**
1. ✅ Existing valid requests still work
2. ✅ Service layer duplicate name check still works
3. ✅ User assignment still works
4. ✅ All existing tests still pass

### 7. Quick Test Checklist

```bash
# 1. Compile
cd backend
docker compose exec backend ./mvnw clean compile

# 2. Run existing tests
docker compose exec backend ./mvnw test

# 3. Test via Swagger UI
# Open http://localhost:8080/swagger-ui/index.html
# Test POST /api/bar-stations with invalid data
```

### Expected Results

**Before Implementation:**
- ❌ Empty/null names accepted (causes errors later)
- ❌ No length validation
- ❌ Inconsistent with other DTOs

**After Implementation:**
- ✅ Empty/null names rejected with clear error message
- ✅ Length validation enforced
- ✅ Consistent with other DTOs (ProductRequestDto, etc.)
- ✅ Clear error messages for clients
- ✅ Invalid data caught at API boundary

### Unit Tests - Are They Necessary?

**Yes, unit tests are necessary and recommended.**

**Why Unit Tests Are Important:**
1. **Validation happens at controller level** - Tests verify `@Valid` annotation works correctly
2. **Error response format** - Tests ensure error messages are properly formatted
3. **Prevent regressions** - Tests catch if validation is accidentally removed or changed
4. **Documentation** - Tests serve as examples of expected behavior

**Recommended Unit Tests:**

Add to `BarStationControllerTest.java`:

1. **Test empty/null name:**
   ```java
   @Test
   void testCreateStation_WithEmptyName_ReturnsBadRequest() throws Exception {
       BarStationRequestDto request = new BarStationRequestDto("", "Desc", true, null);
       
       mockMvc.perform(post("/api/bar-stations")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.title").value("Validation failed"))
               .andExpect(jsonPath("$.errors.name").exists());
   }
   ```

2. **Test null name:**
   ```java
   @Test
   void testCreateStation_WithNullName_ReturnsBadRequest() throws Exception {
       BarStationRequestDto request = new BarStationRequestDto(null, "Desc", true, null);
       
       mockMvc.perform(post("/api/bar-stations")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest());
   }
   ```

3. **Test name exceeding max length:**
   ```java
   @Test
   void testCreateStation_WithNameExceedingMaxLength_ReturnsBadRequest() throws Exception {
       String longName = "a".repeat(121); // 121 characters
       BarStationRequestDto request = new BarStationRequestDto(longName, "Desc", true, null);
       
       mockMvc.perform(post("/api/bar-stations")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errors.name").exists());
   }
   ```

4. **Test description exceeding max length:**
   ```java
   @Test
   void testCreateStation_WithDescriptionExceedingMaxLength_ReturnsBadRequest() throws Exception {
       String longDesc = "a".repeat(501); // 501 characters
       BarStationRequestDto request = new BarStationRequestDto("Station", longDesc, true, null);
       
       mockMvc.perform(post("/api/bar-stations")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errors.description").exists());
   }
   ```

5. **Test valid request still works:**
   ```java
   @Test
   void testCreateStation_WithValidData_ReturnsCreated() throws Exception {
       BarStationRequestDto request = new BarStationRequestDto("Valid Station", "Description", true, null);
       // ... existing test setup ...
       // Should return 201 Created (existing test should still pass)
   }
   ```

**Test Coverage:**
- ✅ Empty/null name validation
- ✅ Name length validation
- ✅ Description length validation
- ✅ Valid requests still work
- ✅ Error response format is correct
