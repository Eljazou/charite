package com.example.charite.dto;

import com.example.charite.enums.CharityActionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor
public class CharityActionCreateRequest {
    private String title;
    private String description;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal goalAmount;
    private CharityActionStatus status;
}