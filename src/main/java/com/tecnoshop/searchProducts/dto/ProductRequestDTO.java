package com.tecnoshop.searchProducts.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {
    private String requestId;
    private String productId;
}
