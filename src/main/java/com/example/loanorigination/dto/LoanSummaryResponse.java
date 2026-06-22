package com.example.loanorigination.dto;

import com.example.loanorigination.entity.LoanStatus;

import java.math.BigDecimal;

public record LoanSummaryResponse(
        String loanId,
        String customerName,
        BigDecimal loanAmount,
        String loanType,
        LoanStatus status
) {
}
