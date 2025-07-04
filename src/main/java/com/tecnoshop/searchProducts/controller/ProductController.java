package com.tecnoshop.searchProducts.controller;

import com.tecnoshop.searchProducts.model.Product;
import com.tecnoshop.searchProducts.service.ProductService;
import com.tecnoshop.searchProducts.dto.ErrorMessage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Product product = service.getById(id).orElse(null);

        if (product == null) {
            return ResponseEntity.status(404)
                    .body(new ErrorMessage("Producto no encontrado"));
        }

        return ResponseEntity.ok(product);
    }

}
