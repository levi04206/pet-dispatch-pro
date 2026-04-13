package com.wenxu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderCreateDTO {

    @NotNull(message = "Pet id is required")
    private Long petId;

    @NotNull(message = "Reserve time is required")
    @Future(message = "Reserve time must be in the future")
    private LocalDateTime reserveTime;

    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.00", message = "Distance cannot be negative")
    private BigDecimal distance;
}
