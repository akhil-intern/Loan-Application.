package com.example.loanorigination.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateLoanRequest(
        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be exactly 10 digits")
        String mobileNumber,

        @NotNull(message = "Loan amount is required")
        @Positive(message = "Loan amount must be positive")
        BigDecimal loanAmount,

        @NotBlank(message = "Loan type is required")
        String loanType,

        @NotNull(message = "Tenure months is required")
        @Positive(message = "Tenure months must be positive")
        Integer tenureMonths
) {
}
