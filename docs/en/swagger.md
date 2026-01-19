# 🚀 Swagger/OpenAPI Documentation Guide


## 📋 Table of Contents

  - [What is Swagger?](#what-is-swagger)
  - [🎯 What We Added](#what-we-added)
  - [📦 Step 1: Add Swagger Dependency](#step-1-add-swagger-dependency)
  - [⚙️ Step 2: Create Swagger Configuration](#step-2-create-swagger-configuration)
  - [🔐 Step 3: Configure Security](#step-3-configure-security)
    - [CORS Configuration for Swagger UI](#cors-configuration-for-swagger-ui)
  - [📝 Step 4: Configure Application Properties](#step-4-configure-application-properties)
- [SpringDoc OpenAPI configuration](#springdoc-openapi-configuration)
  - [🏷️ Step 5: Add Schema Annotations](#step-5-add-schema-annotations)
  - [🚫 Step 6: Hide Exception Handlers](#step-6-hide-exception-handlers)
  - [🚀 How to Use Swagger](#how-to-use-swagger)
    - [Starting the Application](#starting-the-application)
    - [Accessing Swagger UI](#accessing-swagger-ui)
    - [Using Swagger UI](#using-swagger-ui)
    - [Authenticating Requests](#authenticating-requests)
      - [Step 1: Get Your JWT Token](#step-1-get-your-jwt-token)
      - [Step 2: Add Token to Swagger UI](#step-2-add-token-to-swagger-ui)
      - [Step 3: Test Protected Endpoints](#step-3-test-protected-endpoints)
    - [Understanding JWT_SECRET](#understanding-jwt_secret)
  - [🔍 Understanding the Swagger UI](#understanding-the-swagger-ui)
    - [Main Sections](#main-sections)
    - [Endpoint Details](#endpoint-details)
    - [Response Codes](#response-codes)
  - [🛠️ Troubleshooting](#troubleshooting)
    - [Error 500 when accessing Swagger UI](#error-500-when-accessing-swagger-ui)
    - [CORS errors when executing requests](#cors-errors-when-executing-requests)
    - [Endpoints not showing up](#endpoints-not-showing-up)
    - [Authentication not working](#authentication-not-working)
  - [📚 Additional Resources](#additional-resources)
  - [🎓 For Junior Developers: Key Takeaways](#for-junior-developers-key-takeaways)
    - [Common Patterns](#common-patterns)
    - [Best Practices](#best-practices)
  - [🎉 You're All Set!](#youre-all-set)

---



## What is Swagger?

**Swagger** (now called **OpenAPI**) is a powerful tool that automatically generates interactive API documentation for your REST endpoints. Think of it as a **live, interactive manual** for your API that:

- 📚 **Documents all your endpoints** - Shows what endpoints exist, what parameters they need, and what they return
- 🧪 **Lets you test APIs directly** - You can make API calls right from the browser without using Postman or curl
- 🔍 **Shows request/response examples** - See exactly what data to send and what you'll get back
- 🎯 **Helps frontend developers** - They can see all available APIs and understand how to use them
- ✅ **Validates your API** - Helps catch issues early by showing the API structure

**In simple terms**: Swagger creates a beautiful web page where you can see and test all your API endpoints without writing any code!

---

## 🎯 What We Added

We integrated Swagger into the Borsibaar backend so you can:
- View all API endpoints in a nice UI
- Test endpoints directly from the browser
- See request/response schemas
- Understand authentication requirements

---

## 📦 Step 1: Add Swagger Dependency

We added the SpringDoc OpenAPI library to `pom.xml`. This is the library that generates Swagger documentation.

**File**: `backend/pom.xml`

```xml
<!-- SpringDoc OpenAPI (Swagger) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version>
</dependency>
```

**Why version 2.7.0?** 
- Spring Boot 3.5.5 requires SpringDoc 2.7.0 or higher
- Older versions (like 2.6.0) cause compatibility errors

---

## ⚙️ Step 2: Create Swagger Configuration

We created a configuration class that tells Swagger how to set up the API documentation.

**File**: `backend/src/main/java/com/borsibaar/config/OpenApiConfig.java`

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        // Configure API info (title, version, description)
        // Set up JWT authentication scheme
    }
    
    @Bean
    public GroupedOpenApi publicApi() {
        // Group all API endpoints together
        // Only scan /api/** and /auth/** paths
    }
}
```

**What this does:**
- Defines the API title, version, and description
- Sets up JWT Bearer token authentication for protected endpoints
- Groups all endpoints under "borsibaar-api"
- Only scans our API paths (not internal Spring endpoints)

---

## 🔐 Step 3: Configure Security

We updated the security configuration to allow access to Swagger UI without authentication.

**File**: `backend/src/main/java/com/borsibaar/config/SecurityConfig.java`

```java
.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**")
.permitAll()
```

**What this does:**
- Allows anyone to access Swagger UI pages (they're just documentation)
- Allows access to the OpenAPI JSON specification
- You still need authentication to actually call the API endpoints

### CORS Configuration for Swagger UI

Since Swagger UI runs on the same server (`http://localhost:8080`), we need to configure CORS to allow Swagger UI to make requests to the API endpoints.

**File**: `backend/src/main/resources/application.properties`

```properties
app.cors.allowed-origins=${APP_CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:8080}
```

**What this does:**
- Allows requests from `http://localhost:8080` (where Swagger UI is hosted)
- Also allows requests from `http://localhost:3000` (frontend)
- Spring automatically splits comma-separated origins into an array
- This enables Swagger UI to make API calls without CORS errors

**Important**: Without this configuration, you'll get CORS errors when trying to execute API requests from Swagger UI, even though Swagger UI and the API are on the same server!

---

## 📝 Step 4: Configure Application Properties

We added Swagger settings to `application.properties` to customize its behavior.

**File**: `backend/src/main/resources/application.properties`

```properties
# SpringDoc OpenAPI configuration
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
springdoc.default-consumes-media-type=application/json
springdoc.default-produces-media-type=application/json
springdoc.show-actuator=false
springdoc.writer-with-default-pretty-printer=true
springdoc.model-and-view-allowed=false
springdoc.use-management-port=false
springdoc.paths-to-match=/api/**,/auth/**
springdoc.remove-broken-reference-definitions=true
springdoc.disable-swagger-default-url=true
```

**Key settings explained:**
- `springdoc.api-docs.path` - Where the OpenAPI JSON is served
- `springdoc.swagger-ui.path` - Where the Swagger UI is accessible
- `springdoc.swagger-ui.tryItOutEnabled=true` - Enables the "Try it out" button
- `springdoc.paths-to-match` - Only document endpoints matching these patterns

---

## 🏷️ Step 5: Add Schema Annotations

We added `@Schema` annotations to internal record classes in controllers to improve documentation.

**File**: `backend/src/main/java/com/borsibaar/controller/AccountController.java`

```java
@Schema(description = "Current user information")
public record MeResponse(
    @Schema(description = "User email", example = "user@example.com")
    String email,
    // ... more fields
) {}
```

**What this does:**
- Adds descriptions to API models
- Provides example values
- Makes the documentation more readable and helpful

---

## 🚫 Step 6: Hide Exception Handlers

We added `@Hidden` annotations to exception handler methods so Swagger doesn't try to document them as API endpoints.

**File**: `backend/src/main/java/com/borsibaar/exception/ApiExceptionHandler.java`

```java
@Hidden
@ExceptionHandler(NotFoundException.class)
public ProblemDetail handleNotFound(...) {
    // ...
}
```

**Why?**
- Exception handlers are not API endpoints
- Swagger was trying to scan them and causing errors
- `@Hidden` tells Swagger to ignore these methods

---

## 🚀 How to Use Swagger

### Starting the Application

1. **Start the backend** (if using Docker):
   ```bash
   docker compose up backend
   ```

2. **Or start locally**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

### Accessing Swagger UI

Once the backend is running, open your browser and go to:

**http://localhost:8080/swagger-ui/index.html**

You should see a beautiful interface with all your API endpoints! 🎉

### Using Swagger UI

1. **Browse endpoints** - Expand any endpoint to see its details
2. **Authorize first** (for protected endpoints) - Click "Authorize" button and add your JWT token (see "Authenticating Requests" below)
3. **Try it out** - Click "Try it out" button on any endpoint
4. **Fill parameters** - Enter required parameters
5. **Execute** - Click "Execute" to make the API call
6. **See response** - View the response body, status code, and headers

**Important**: Most endpoints require authentication. Make sure to authorize before testing protected endpoints, otherwise you'll get CORS errors due to OAuth2 redirects.

### Authenticating Requests

For protected endpoints that require JWT authentication, you need to manually add the JWT token to Swagger UI:

#### Step 1: Get Your JWT Token

Since JWT tokens are stored in HTTP-only cookies, you need to extract them from your browser:

**Option A: Using Browser Developer Tools**
1. Open your browser's Developer Tools (F12 or Right-click → Inspect)
2. Go to the **Application** tab (Chrome) or **Storage** tab (Firefox)
3. Navigate to **Cookies** → `http://localhost:8080`
4. Find the cookie named `jwt`
5. Copy its **Value**

**Option B: Using Browser Console**
1. Open Developer Tools (F12)
2. Go to the **Console** tab
3. Run this command:
   ```javascript
   document.cookie.split('; ').find(row => row.startsWith('jwt='))?.split('=')[1]
   ```
4. Copy the token value that appears

**Option C: Login via Frontend First**
1. Open your frontend application (http://localhost:3000)
2. Login with Google OAuth
3. The JWT token will be set in cookies
4. Then extract it using Option A or B above

#### Step 2: Add Token to Swagger UI

1. In Swagger UI, click the **"Authorize"** button (🔒 icon) at the top right
2. In the "bearerAuth" section, enter your JWT token in the format: `Bearer <your-token>`
   - **Important**: Include the word "Bearer" followed by a space, then your token
   - Example: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
3. Click **"Authorize"**
4. Click **"Close"**

#### Step 3: Test Protected Endpoints

Now all your API requests will include the `Authorization: Bearer <token>` header, and protected endpoints will work!

**Note**: 
- The token is valid for 24 hours
- If you get 401 errors, your token may have expired - get a new one by logging in again
- Swagger UI doesn't automatically send cookies, which is why we need to manually add the token

---

### Understanding JWT_SECRET

**What is JWT_SECRET?**

`JWT_SECRET` is a secret key used by the backend to **sign** and **verify** JWT tokens. It's NOT used directly in Swagger UI for authorization.

**How it works:**

1. **Token Creation (Signing)**:
   - When a user logs in via Google OAuth, the backend receives the user's email
   - Backend generates a JWT token containing the email
   - Backend **signs** the token using `JWT_SECRET` (cryptographic signature)
   - This signature ensures the token is authentic and hasn't been tampered with

2. **Token Validation (Verification)**:
   - When a request comes with a JWT token, the backend extracts it
   - Backend **verifies** the signature using the same `JWT_SECRET`
   - If signature is invalid → token is rejected
   - If signature is valid → token is accepted

**Why it's important:**
- **Security**: Without the correct secret, tokens cannot be forged
- **Integrity**: If a token is modified, the signature won't match
- **Authentication**: Only your server can create valid tokens

**In Swagger UI context:**
- You don't use `JWT_SECRET` directly in Swagger UI
- You need a **ready-made JWT token** that was created using this secret
- The token is obtained after logging in through the frontend
- In Swagger UI, you simply use the already-created token

**Think of it like a seal on a document:**
- `JWT_SECRET` = the secret seal/stamp
- Creating a token = stamping the document
- Verifying a token = checking if the seal is authentic

**Summary:**
`JWT_SECRET` is the key for creating and verifying tokens on the server. It's never sent to clients and not used for authorization in Swagger UI. In Swagger UI, you use a ready-made JWT token that was created using this secret after login.

---

## 🔍 Understanding the Swagger UI

### Main Sections

1. **API Information** (top) - Shows API title, version, and description
2. **Endpoints List** (left sidebar) - All your API endpoints grouped by tags
3. **Endpoint Details** (main area) - Shows:
   - HTTP method (GET, POST, etc.)
   - Endpoint path
   - Parameters
   - Request body schema
   - Response schemas
   - Example values

### Endpoint Details

Each endpoint shows:
- **Parameters** - Query params, path variables, headers
- **Request Body** - JSON schema for POST/PUT requests
- **Responses** - Possible response codes and their schemas
- **Try it out** - Button to test the endpoint

### Response Codes

- **200 OK** - Success
- **201 Created** - Resource created
- **400 Bad Request** - Invalid input
- **401 Unauthorized** - Not authenticated
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error

---

## 🛠️ Troubleshooting

### Error 500 when accessing Swagger UI

**Problem**: Getting "Failed to load API definition" error

**Solutions**:
1. **Check SpringDoc version** - Must be 2.7.0+ for Spring Boot 3.5.5
2. **Check logs** - Look for errors in backend logs
3. **Verify security config** - Make sure Swagger paths are permitted
4. **Check exception handlers** - Ensure they have `@Hidden` annotation

### CORS errors when executing requests

**Problem**: Getting "Failed to fetch" or "CORS" errors when trying to execute API requests from Swagger UI

**Error message**: 
```
Failed to fetch.
Possible Reasons:
CORS
Network Failure
URL scheme must be "http" or "https" for CORS request.
```

**Common causes and solutions**:

1. **Missing Authentication (Most Common)**
   - **Symptom**: Error shows redirect to `https://accounts.google.com/o/oauth2/v2/auth`
   - **Cause**: You're trying to access a protected endpoint without authentication
   - **Solution**: 
     - Get your JWT token from browser cookies (see "Authenticating Requests" section above)
     - Click "Authorize" button in Swagger UI
     - Enter token as `Bearer <your-token>`
     - Then try the request again

2. **CORS Configuration Issue**
   - **Symptom**: Error mentions CORS but no OAuth redirect
   - **Solution**: 
     - Verify `app.cors.allowed-origins` includes `http://localhost:8080`
     ```properties
     app.cors.allowed-origins=${APP_CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:8080}
     ```
     - Restart backend (CORS config loads at startup)
     - Check logs for "CORS allowed origins" message

3. **Browser Cache**
   - Clear browser cache or try incognito mode
   - Hard refresh (Ctrl+Shift+R or Cmd+Shift+R)

**Why this happens**: 
- Protected endpoints require JWT authentication
- Without a token, Spring Security redirects to OAuth2 login
- Browsers block cross-origin redirects due to CORS policy
- Solution: Add JWT token via "Authorize" button before making requests
### Endpoints not showing up

**Problem**: Some endpoints are missing from Swagger UI

**Solutions**:
1. **Check path patterns** - Verify `springdoc.paths-to-match` includes your paths
2. **Check controller annotations** - Ensure `@RestController` is present
3. **Check security** - Endpoints might be blocked by security config

### Authentication not working

**Problem**: Can't test protected endpoints

**Solutions**:
1. **Get JWT token** - Extract from browser cookies or login response
2. **Use Authorize button** - Enter token as `Bearer <token>`
3. **Check token format** - Must include "Bearer " prefix

---

## 📚 Additional Resources

- **OpenAPI Specification**: https://swagger.io/specification/
- **SpringDoc Documentation**: https://springdoc.org/
- **Swagger UI Guide**: https://swagger.io/tools/swagger-ui/

---

## 🎓 For Junior Developers: Key Takeaways

1. **Swagger = Auto-generated API docs** - No need to write documentation manually
2. **It scans your code** - Automatically finds all `@RestController` classes
3. **It's interactive** - Test APIs without writing code
4. **It helps teams** - Frontend devs can see what APIs are available
5. **It's free** - Just add a dependency and configure it

### Common Patterns

- **`@Schema`** - Describes data models
- **`@Hidden`** - Hides endpoints/methods from Swagger
- **`@Operation`** - Adds descriptions to endpoints (optional)
- **`@ApiResponse`** - Documents response codes (optional)

### Best Practices

1. ✅ Add `@Schema` descriptions to DTOs
2. ✅ Use `@Hidden` on exception handlers
3. ✅ Keep Swagger version compatible with Spring Boot
4. ✅ Document authentication requirements
5. ✅ Provide example values in schemas

---

## 🎉 You're All Set!

Now you can:
- View all API endpoints at http://localhost:8080/swagger-ui/index.html
- Test endpoints directly from the browser
- Share API documentation with your team
- Understand API structure without reading code

Happy API documenting! 🚀
