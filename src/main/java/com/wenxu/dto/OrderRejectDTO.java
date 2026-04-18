package com.wenxu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderRejectDTO {

    @NotNull(message = "Order id is required")
    private Long orderId;

    @NotBlank(message = "Reject reason cannot be blank")
    @Size(max = 100, message = "Reject reason cannot exceed 100 characters")
    private String rejectReason;
}
