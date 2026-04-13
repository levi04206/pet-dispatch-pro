package com.wenxu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SitterApplyDTO {

    @NotBlank(message = "Real name cannot be blank")
    @Size(max = 50, message = "Real name cannot exceed 50 characters")
    private String realName;

    @NotBlank(message = "Phone cannot be blank")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "Phone format is invalid")
    private String phone;

    @NotBlank(message = "Id card cannot be blank")
    @Size(max = 20, message = "Id card cannot exceed 20 characters")
    private String idCard;

    @Size(max = 255, message = "Avatar URL cannot exceed 255 characters")
    private String avatar;
}
