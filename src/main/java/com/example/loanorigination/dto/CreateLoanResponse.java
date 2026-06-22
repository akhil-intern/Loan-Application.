package com.example.loanorigination.dto;

import com.example.loanorigination.entity.LoanStatus;

public record CreateLoanResponse(
        String loanId,
        LoanStatus status
) {
}
