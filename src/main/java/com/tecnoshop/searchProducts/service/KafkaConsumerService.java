package com.tecnoshop.searchProducts.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnoshop.searchProducts.model.Product;
import com.tecnoshop.searchProducts.dto.ProductRequestDTO;
import com.tecnoshop.searchProducts.dto.ProductResponseDTO;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class KafkaConsumerService {

    private static final String REQUEST_TOPIC = "product.get.request";
    private static final String RESPONSE_TOPIC = "product.get.response";

    @Autowired
    private ProductService productService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = REQUEST_TOPIC, groupId = "search-product-group")
    public void listenProductRequest(ConsumerRecord<String, String> record) {
        System.out.println("🟢 Mensaje recibido desde Kafka (" + REQUEST_TOPIC + "): " + record.value());

        try {
            ProductRequestDTO request = objectMapper.readValue(record.value(), ProductRequestDTO.class);
            String idStr = request.getProductId();
            String requestId = request.getRequestId();

            if (idStr == null || idStr.isBlank() || requestId == null || requestId.isBlank()) {
                logAndRespond("❌ Datos inválidos en el mensaje", "Datos inválidos", requestId);
                return;
            }

            Long id;
            try {
                id = Long.parseLong(idStr);
            } catch (NumberFormatException e) {
                logAndRespond("❌ ID no es un número válido: " + idStr, "ID inválido", requestId);
                return;
            }

            System.out.println("🔍 ID del producto solicitado: " + id);

            Optional<Product> productOpt = productService.getById(id);

            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                System.out.printf("✅ Producto encontrado: ID=%d, Nombre=%s, Precio=%.2f\n",
                        product.getId(), product.getName(), product.getPrice());

                ProductResponseDTO responseDTO = new ProductResponseDTO();
                responseDTO.setRequestId(requestId);
                responseDTO.setId(product.getId());
                responseDTO.setName(product.getName());
                responseDTO.setDescription(product.getDescription());
                responseDTO.setPrice(product.getPrice());
                responseDTO.setStock(product.getStock());
                responseDTO.setSku(product.getSku());
                responseDTO.setIsPublished(product.getIsPublished());
                responseDTO.setCreatedAt(product.getCreatedAt() != null ? product.getCreatedAt().toString() : null);
                responseDTO.setUpdatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : null);
                responseDTO.setDeletedAt(product.getDeletedAt());

                String responseJson = objectMapper.writeValueAsString(responseDTO);
                kafkaTemplate.send(RESPONSE_TOPIC, requestId, responseJson); // ✅ Key = requestId
                System.out.println("📤 Respuesta enviada con requestId=" + requestId + ": " + responseJson);

            } else {
                logAndRespond("⚠️ Producto no encontrado con ID: " + id, "Producto no encontrado", requestId);
            }

        } catch (Exception e) {
            System.out.println("❌ Excepción en KafkaConsumerService: " + e.getClass().getSimpleName()
                    + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void logAndRespond(String logMessage, String errorMsg, String requestId) {
        System.out.println(logMessage);
        try {
            ProductResponseDTO errorResponse = new ProductResponseDTO();
            errorResponse.setRequestId(requestId);
            errorResponse.setName("error");
            errorResponse.setDescription(errorMsg);
            String errorJson = objectMapper.writeValueAsString(errorResponse);
            kafkaTemplate.send(RESPONSE_TOPIC, requestId, errorJson); // ✅ Key = requestId
        } catch (Exception ex) {
            System.out.println("❌ Error serializando respuesta de error: " + ex.getMessage());
        }
    }
}
