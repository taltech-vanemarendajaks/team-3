package com.borsibaar.backend.service;

import com.borsibaar.backend.repository.CategoryRepository;
import com.borsibaar.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
}
