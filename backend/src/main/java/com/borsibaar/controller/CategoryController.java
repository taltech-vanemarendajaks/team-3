package com.borsibaar.controller;

import com.borsibaar.dto.CategoryRequestDto;
import com.borsibaar.dto.CategoryResponseDto;
import com.borsibaar.entity.User;
import com.borsibaar.repository.UserRepository;
import com.borsibaar.service.CategoryService;
import com.borsibaar.service.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDto createCategory(@RequestBody CategoryRequestDto request,
            @CookieValue(name = "jwt", required = false) String token) {
        User user = authenticateUser(token);
        return categoryService.create(request, user.getOrganizationId());
    }

    @GetMapping
    public List<CategoryResponseDto> getAll(@CookieValue(name = "jwt", required = false) String token) {
        User user = authenticateUser(token);
        return categoryService.getAllByOrg(user.getOrganizationId());
    }

    @GetMapping("/{id}")
    public CategoryResponseDto getById(@PathVariable Long id,
            @CookieValue(name = "jwt", required = false) String token) {
        User user = authenticateUser(token);
        return categoryService.getByIdAndOrg(id, user.getOrganizationId());
    }

    @DeleteMapping({ "/{id}" })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @CookieValue(name = "jwt", required = false) String token) {
        User user = authenticateUser(token);
        categoryService.deleteReturningDto(id, user.getOrganizationId());
    }

    private User authenticateUser(String token) {
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        Claims claims = jwtService.parseToken(token);
        User user = userRepository.findByEmail(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (user.getOrganizationId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no organization");
        }
        return user;
    }
}
