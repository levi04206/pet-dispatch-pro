package com.wenxu.vo;

import lombok.Data;

@Data
public class PetInfoVO {

    private Long id;
    private String petName;
    private Integer petType;
    private String breed;
    private Double weight;
}
