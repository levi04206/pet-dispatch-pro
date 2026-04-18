package com.wenxu.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PetInfoAddDTO {

    @NotBlank(message = "Pet name cannot be blank")
    @Size(max = 50, message = "Pet name cannot exceed 50 characters")
    private String petName;

    @NotNull(message = "Pet type is required")
    @Min(value = 1, message = "Pet type must be 1, 2, or 3")
    @Max(value = 3, message = "Pet type must be 1, 2, or 3")
    private Integer petType;

    @Size(max = 50, message = "Breed cannot exceed 50 characters")
    private String breed;

    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    @DecimalMax(value = "999.99", message = "Weight cannot exceed 999.99")
    private Double weight;

    @Min(value = 0, message = "Neutered flag must be 0 or 1")
    @Max(value = 1, message = "Neutered flag must be 0 or 1")
    private Integer isNeutered;

    @Min(value = 0, message = "Aggressive tag must be 0, 1, or 2")
    @Max(value = 2, message = "Aggressive tag must be 0, 1, or 2")
    private Integer aggressiveTag;
}
