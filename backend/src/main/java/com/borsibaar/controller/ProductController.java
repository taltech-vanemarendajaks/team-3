package com.borsibaar.controller;

import com.borsibaar.dto.ProductRequestDto;
import com.borsibaar.dto.ProductResponseDto;
import com.borsibaar.entity.User;
import com.borsibaar.repository.UserRepository;
import com.borsibaar.service.JwtService;
import com.borsibaar.service.ProductService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public ProductController(ProductService productService, JwtService jwtService, UserRepository userRepository) {
        this.productService = productService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDto create(@RequestBody @Valid ProductRequestDto request,
            @CookieValue(name = "jwt", required = false) String token) {
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        Claims claims = jwtService.parseToken(token);
        User user = userRepository.findByEmail(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (user.getOrganizationId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no organization");
        }

        return productService.create(request, user.getOrganizationId());

    }

    @GetMapping("/{id}")
    public ProductResponseDto get(@PathVariable Long id) {
        return productService.getById(id);
    }

    @DeleteMapping("/{id}")
    public ProductResponseDto delete(@PathVariable Long id) {
        return productService.getById(id);
    }

}
