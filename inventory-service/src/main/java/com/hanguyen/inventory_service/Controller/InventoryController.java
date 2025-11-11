package com.hanguyen.inventory_service.Controller;

import com.hanguyen.inventory_service.dto.ApiResponse;
import com.hanguyen.inventory_service.dto.reponse.InventoryResponse;
import com.hanguyen.inventory_service.dto.request.InventoryRequest;
import com.hanguyen.inventory_service.service.InventoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
@RequestMapping("/inventory")
public class InventoryController {

    InventoryService inventoryService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<InventoryResponse> setStock(@RequestBody InventoryRequest request) {
        int newStock = inventoryService.setStock(request);

        return ApiResponse.<InventoryResponse>builder()
                .result(InventoryResponse.builder()
                        .BookId(request.getBookId())
                        .stock(newStock)
                        .build())
                .build();
    }

    @GetMapping("/{bookId}")
    public ApiResponse<InventoryResponse> getStock(@PathVariable String bookId) {
        int stock = inventoryService.getStock(bookId);
        return ApiResponse.<InventoryResponse>builder()
                .result(InventoryResponse.builder()
                        .BookId(bookId)
                        .stock(stock)
                        .build())
                .build();
    }
}