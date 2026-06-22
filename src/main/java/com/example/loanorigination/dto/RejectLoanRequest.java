package com.example.loanorigination.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectLoanRequest(
        @NotBlank(message = "Remarks are required")
        String remarks
) {
}
