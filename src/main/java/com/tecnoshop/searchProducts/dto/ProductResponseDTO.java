package com.tecnoshop.searchProducts.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private String requestId;
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
    private String sku;
    private Boolean isPublished;
    private String createdAt;
    private String updatedAt;
    private Object deletedAt;
}
