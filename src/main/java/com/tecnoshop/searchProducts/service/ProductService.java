package com.tecnoshop.searchProducts.service;

import com.tecnoshop.searchProducts.model.Product;
import com.tecnoshop.searchProducts.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public Optional<Product> getById(Long id) {
        return repo.findById(id).filter(p -> p.getDeletedAt() == null);
    }
}
