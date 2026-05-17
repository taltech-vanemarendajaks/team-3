# Quick Testing Instructions: BarStationRequestDto Validation

## 1. Run Unit Tests (Fastest Method)

The unit tests don't require authentication and test all validation scenarios:

```bash
# Run all validation tests
docker compose exec backend ./mvnw test -Dtest=BarStationControllerTest

# Or run specific validation test
docker compose exec backend ./mvnw test -Dtest=BarStationControllerTest#testCreateStation_WithEmptyName_ReturnsBadRequest
```

**Expected:** All tests should pass, verifying that validation returns HTTP 400 Bad Request with proper error messages.

---

## 2. Manual API Testing with curl

### Prerequisites
- Backend running on `http://localhost:8080`
- Valid JWT token (obtain via login endpoint)

### Test Cases

#### Test 1: Empty Name (Should return 400)
```bash
curl -X POST http://localhost:8080/api/bar-stations \
  -H "Content-Type: application/json" \
  -H "Cookie: jwt=YOUR_JWT_TOKEN" \
  -d '{
    "name": "",
    "description": "Test description",
    "isActive": true
  }'
```

**Expected Response:**
```json
{
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

#### Test 2: Null Name (Should return 400)
```bash
curl -X POST http://localhost:8080/api/bar-stations \
  -H "Content-Type: application/json" \
  -H "Cookie: jwt=YOUR_JWT_TOKEN" \
  -d '{
    "name": null,
    "description": "Test description",
    "isActive": true
  }'
```

#### Test 3: Name Exceeds Max Length (121 chars - Should return 400)
```bash
curl -X POST http://localhost:8080/api/bar-stations \
  -H "Content-Type: application/json" \
  -H "Cookie: jwt=YOUR_JWT_TOKEN" \
  -d "{
    \"name\": \"$(python3 -c 'print(\"a\" * 121)')\",
    \"description\": \"Test\",
    \"isActive\": true
  }"
```

**Expected Response:**
```json
{
  "title": "Validation failed",
  "status": 400,
  "detail": "name: Station name must not exceed 120 characters",
  "errors": {
    "name": "Station name must not exceed 120 characters"
  }
}
```

#### Test 4: Description Exceeds Max Length (501 chars - Should return 400)
```bash
curl -X POST http://localhost:8080/api/bar-stations \
  -H "Content-Type: application/json" \
  -H "Cookie: jwt=YOUR_JWT_TOKEN" \
  -d "{
    \"name\": \"Valid Station\",
    \"description\": \"$(python3 -c 'print(\"a\" * 501)')\",
    \"isActive\": true
  }"
```

**Expected Response:**
```json
{
  "title": "Validation failed",
  "status": 400,
  "detail": "description: Description must not exceed 500 characters",
  "errors": {
    "description": "Description must not exceed 500 characters"
  }
}
```

#### Test 5: Valid Data (Should return 201 Created)
```bash
curl -X POST http://localhost:8080/api/bar-stations \
  -H "Content-Type: application/json" \
  -H "Cookie: jwt=YOUR_JWT_TOKEN" \
  -d '{
    "name": "Test Station",
    "description": "Valid description",
    "isActive": true
  }'
```

**Expected Response:** HTTP 201 Created with station data

#### Test 6: Update Endpoint Validation
```bash
# Test update with empty name
curl -X PUT http://localhost:8080/api/bar-stations/1 \
  -H "Content-Type: application/json" \
  -H "Cookie: jwt=YOUR_JWT_TOKEN" \
  -d '{
    "name": "",
    "description": "Updated description",
    "isActive": true
  }'
```

**Expected:** HTTP 400 Bad Request with validation error

---

## 3. Quick Test Script

Create `test-validation.sh`:

```bash
#!/bin/bash
BASE_URL="http://localhost:8080/api/bar-stations"
JWT_TOKEN="YOUR_JWT_TOKEN_HERE"  # Replace with your actual JWT token

echo "=== Test 1: Empty name ==="
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "Cookie: jwt=$JWT_TOKEN" \
  -d '{"name": "", "description": "Test"}' \
  -w "\nHTTP Status: %{http_code}\n\n"

echo "=== Test 2: Name too long (121 chars) ==="
LONG_NAME=$(python3 -c 'print("a" * 121)')
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "Cookie: jwt=$JWT_TOKEN" \
  -d "{\"name\": \"$LONG_NAME\", \"description\": \"Test\"}" \
  -w "\nHTTP Status: %{http_code}\n\n"

echo "=== Test 3: Description too long (501 chars) ==="
LONG_DESC=$(python3 -c 'print("a" * 501)')
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "Cookie: jwt=$JWT_TOKEN" \
  -d "{\"name\": \"Valid Station\", \"description\": \"$LONG_DESC\"}" \
  -w "\nHTTP Status: %{http_code}\n\n"

echo "=== Test 4: Valid data ==="
curl -X POST "$BASE_URL" \
  -H "Content-Type: application/json" \
  -H "Cookie: jwt=$JWT_TOKEN" \
  -d '{"name": "Test Station", "description": "Valid description", "isActive": true}' \
  -w "\nHTTP Status: %{http_code}\n\n"
```

Make it executable and run:
```bash
chmod +x test-validation.sh
./test-validation.sh
```

---

## 4. Using Swagger UI (If Available)

1. Open http://localhost:8080/swagger-ui/index.html
2. Navigate to `POST /api/bar-stations`
3. Click "Try it out"
4. Test with invalid data:
   - Empty name: `{"name": "", "description": "Test"}`
   - Long name: `{"name": "a...a" (121 chars), "description": "Test"}`
   - Long description: `{"name": "Valid", "description": "a...a" (501 chars)}`
5. Check response - should be HTTP 400 with validation errors

---

## Expected Results Summary

| Test Case | Expected Status | Expected Error Field |
|-----------|----------------|---------------------|
| Empty name | 400 Bad Request | `errors.name: "Station name is required"` |
| Null name | 400 Bad Request | `errors.name: "Station name is required"` |
| Name > 120 chars | 400 Bad Request | `errors.name: "Station name must not exceed 120 characters"` |
| Description > 500 chars | 400 Bad Request | `errors.description: "Description must not exceed 500 characters"` |
| Valid data | 201 Created | No errors |

---

## Quick Checklist

- [ ] Unit tests pass
- [ ] Empty name returns 400
- [ ] Null name returns 400
- [ ] Name exceeding 120 chars returns 400
- [ ] Description exceeding 500 chars returns 400
- [ ] Valid data creates station successfully
- [ ] Update endpoint also validates input
- [ ] Error response format matches expected structure

---

## Notes

- **Authentication Required:** All API endpoints require a valid JWT token in cookies
- **Validation happens before service layer:** Invalid requests never reach the service, preventing unnecessary processing
- **Error format:** Uses RFC 7807 ProblemDetail format with field-level error messages
- **Unit tests are fastest:** No authentication needed, tests all scenarios automatically
