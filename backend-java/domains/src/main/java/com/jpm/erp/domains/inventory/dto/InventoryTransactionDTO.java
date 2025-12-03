package com.jpm.erp.domains.inventory.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryTransactionDTO(
                UUID id,
                String date,
                String type,
                UUID partId,
                Integer quantity,
                BigDecimal pricePerUnit,
                String referenceId,
                UUID equipmentId,
                UUID supplierId,
                String notes,
                // Payment-related fields
                String paymentMethod, // Aligned with DB column payment_method
                String paymentStatus, // Derived: PAID/UNPAID based on paidDate
                String dueDate, // For credit purchases
                String paidDate // Date when payment was completed
) {
}
