package com.example.loanorigination.dto;

import com.example.loanorigination.entity.LoanStatus;

import java.math.BigDecimal;

public record LoanDetailResponse(
        String loanId,
        String customerName,
        String mobileNumber,
        BigDecimal loanAmount,
        String loanType,
        Integer tenureMonths,
        LoanStatus status,
        String remarks
) {
}
