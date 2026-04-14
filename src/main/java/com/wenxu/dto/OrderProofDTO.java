package com.wenxu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderProofDTO {

    @NotNull(message = "Order id is required")
    private Long orderId;

    @NotBlank(message = "Proof URL cannot be blank")
    @Size(max = 255, message = "Proof URL cannot exceed 255 characters")
    private String proofUrl;
}
