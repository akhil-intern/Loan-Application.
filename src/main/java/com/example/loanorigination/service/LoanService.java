package com.example.loanorigination.service;

import com.example.loanorigination.dto.CreateLoanRequest;
import com.example.loanorigination.dto.CreateLoanResponse;
import com.example.loanorigination.dto.LoanDetailResponse;
import com.example.loanorigination.dto.LoanSummaryResponse;
import com.example.loanorigination.entity.Loan;
import com.example.loanorigination.entity.LoanStatus;
import com.example.loanorigination.exception.ResourceNotFoundException;
import com.example.loanorigination.repository.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.locks.ReentrantLock;

@Service
public class LoanService {

    private static final String LOAN_ID_PREFIX = "LOAN";
    private static final int INITIAL_SEQUENCE = 1001;

    private final LoanRepository loanRepository;
    private final ReentrantLock loanIdGenerationLock = new ReentrantLock();

    public LoanService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Transactional
    public CreateLoanResponse createLoan(CreateLoanRequest request) {
        String loanId = generateNextLoanId();

        Loan loan = new Loan(
                loanId,
                request.customerName(),
                request.mobileNumber(),
                request.loanAmount(),
                request.loanType(),
                request.tenureMonths(),
                LoanStatus.PENDING,
                null
        );

        Loan savedLoan = loanRepository.save(loan);
        return new CreateLoanResponse(savedLoan.getLoanId(), savedLoan.getStatus());
    }

    @Transactional(readOnly = true)
    public LoanDetailResponse getLoanById(String loanId) {
        Loan loan = findLoanOrThrow(loanId);
        return toDetailResponse(loan);
    }

    @Transactional(readOnly = true)
    public Page<LoanSummaryResponse> getAllLoans(Pageable pageable) {
        return loanRepository.findAll(pageable).map(this::toSummaryResponse);
    }

    @Transactional
    public LoanDetailResponse approveLoan(String loanId) {
        Loan loan = findLoanOrThrow(loanId);

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException(
                    "Loan " + loanId + " cannot be approved because its current status is " + loan.getStatus()
            );
        }

        loan.setStatus(LoanStatus.APPROVED);
        Loan updatedLoan = loanRepository.save(loan);
        return toDetailResponse(updatedLoan);
    }

    @Transactional
    public LoanDetailResponse rejectLoan(String loanId, String remarks) {
        Loan loan = findLoanOrThrow(loanId);

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException(
                    "Loan " + loanId + " cannot be rejected because its current status is " + loan.getStatus()
            );
        }

        loan.setStatus(LoanStatus.REJECTED);
        loan.setRemarks(remarks);
        Loan updatedLoan = loanRepository.save(loan);
        return toDetailResponse(updatedLoan);
    }

    private String generateNextLoanId() {
        loanIdGenerationLock.lock();
        try {
            int nextSequence = loanRepository.findAllLoanIds().stream()
                    .map(this::extractNumericSuffix)
                    .max(Integer::compareTo)
                    .orElse(INITIAL_SEQUENCE - 1) + 1;
            return LOAN_ID_PREFIX + nextSequence;
        } finally {
            loanIdGenerationLock.unlock();
        }
    }

    private int extractNumericSuffix(String loanId) {
        if (loanId == null || !loanId.startsWith(LOAN_ID_PREFIX)) {
            return INITIAL_SEQUENCE - 1;
        }
        return Integer.parseInt(loanId.substring(LOAN_ID_PREFIX.length()));
    }

    private Loan findLoanOrThrow(String loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));
    }

    private LoanDetailResponse toDetailResponse(Loan loan) {
        return new LoanDetailResponse(
                loan.getLoanId(),
                loan.getCustomerName(),
                loan.getMobileNumber(),
                loan.getLoanAmount(),
                loan.getLoanType(),
                loan.getTenureMonths(),
                loan.getStatus(),
                loan.getRemarks()
        );
    }

    private LoanSummaryResponse toSummaryResponse(Loan loan) {
        return new LoanSummaryResponse(
                loan.getLoanId(),
                loan.getCustomerName(),
                loan.getLoanAmount(),
                loan.getLoanType(),
                loan.getStatus()
        );
    }
}
