package com.example.loanorigination.controller;

import com.example.loanorigination.dto.CreateLoanRequest;
import com.example.loanorigination.dto.CreateLoanResponse;
import com.example.loanorigination.dto.LoanDetailResponse;
import com.example.loanorigination.dto.LoanSummaryResponse;
import com.example.loanorigination.dto.RejectLoanRequest;
import com.example.loanorigination.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public ResponseEntity<CreateLoanResponse> createLoan(@Valid @RequestBody CreateLoanRequest request) {
        CreateLoanResponse response = loanService.createLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<LoanDetailResponse> getLoanById(@PathVariable String loanId) {
        LoanDetailResponse response = loanService.getLoanById(loanId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<LoanSummaryResponse>> getAllLoans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "loanId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LoanSummaryResponse> response = loanService.getAllLoans(pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{loanId}/approve")
    public ResponseEntity<LoanDetailResponse> approveLoan(@PathVariable String loanId) {
        LoanDetailResponse response = loanService.approveLoan(loanId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{loanId}/reject")
    public ResponseEntity<LoanDetailResponse> rejectLoan(
            @PathVariable String loanId,
            @Valid @RequestBody RejectLoanRequest request) {
        LoanDetailResponse response = loanService.rejectLoan(loanId, request.remarks());
        return ResponseEntity.ok(response);
    }
}
