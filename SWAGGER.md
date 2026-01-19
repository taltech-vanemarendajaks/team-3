# 🚀 Swagger/OpenAPI Documentation Guide

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
2. **Try it out** - Click "Try it out" button on any endpoint
3. **Fill parameters** - Enter required parameters
4. **Execute** - Click "Execute" to make the API call
5. **See response** - View the response body, status code, and headers

### Authenticating Requests

For protected endpoints that require JWT authentication:

1. Click the **"Authorize"** button at the top right
2. Enter your JWT token in the format: `Bearer <your-token>`
3. Click "Authorize"
4. Now all requests will include the authentication header

**Note**: In our setup, JWT tokens are stored in cookies. Swagger UI doesn't automatically send cookies, so you may need to:
- Use browser developer tools to get your JWT token from cookies
- Or test endpoints that don't require authentication first

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
