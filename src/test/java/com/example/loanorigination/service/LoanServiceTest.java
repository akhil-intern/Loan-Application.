package com.example.loanorigination.service;

import com.example.loanorigination.dto.CreateLoanRequest;
import com.example.loanorigination.dto.CreateLoanResponse;
import com.example.loanorigination.dto.LoanDetailResponse;
import com.example.loanorigination.entity.Loan;
import com.example.loanorigination.entity.LoanStatus;
import com.example.loanorigination.exception.ResourceNotFoundException;
import com.example.loanorigination.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanService loanService;

    private CreateLoanRequest createLoanRequest;
    private Loan pendingLoan;

    @BeforeEach
    void setUp() {
        createLoanRequest = new CreateLoanRequest(
                "John Doe",
                "9876543210",
                new BigDecimal("500000.00"),
                "HOME",
                120
        );

        pendingLoan = new Loan(
                "LOAN1001",
                "John Doe",
                "9876543210",
                new BigDecimal("500000.00"),
                "HOME",
                120,
                LoanStatus.PENDING,
                null
        );
    }

    @Test
    @DisplayName("createLoan should generate first loan ID when no existing loans")
    void createLoan_shouldGenerateFirstLoanIdWhenNoExistingLoans() {
        when(loanRepository.findAllLoanIds()).thenReturn(java.util.List.of());
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateLoanResponse response = loanService.createLoan(createLoanRequest);

        assertThat(response.loanId()).isEqualTo("LOAN1001");
        assertThat(response.status()).isEqualTo(LoanStatus.PENDING);

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).save(loanCaptor.capture());

        Loan savedLoan = loanCaptor.getValue();
        assertThat(savedLoan.getLoanId()).isEqualTo("LOAN1001");
        assertThat(savedLoan.getCustomerName()).isEqualTo("John Doe");
        assertThat(savedLoan.getMobileNumber()).isEqualTo("9876543210");
        assertThat(savedLoan.getLoanAmount()).isEqualByComparingTo("500000.00");
        assertThat(savedLoan.getLoanType()).isEqualTo("HOME");
        assertThat(savedLoan.getTenureMonths()).isEqualTo(120);
        assertThat(savedLoan.getStatus()).isEqualTo(LoanStatus.PENDING);
        assertThat(savedLoan.getRemarks()).isNull();
    }

    @Test
    @DisplayName("createLoan should increment loan ID based on maximum existing suffix")
    void createLoan_shouldIncrementLoanIdBasedOnMaximumExistingSuffix() {
        when(loanRepository.findAllLoanIds()).thenReturn(java.util.List.of("LOAN1042"));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateLoanResponse response = loanService.createLoan(createLoanRequest);

        assertThat(response.loanId()).isEqualTo("LOAN1043");
        assertThat(response.status()).isEqualTo(LoanStatus.PENDING);
    }

    @Test
    @DisplayName("approveLoan should change status from PENDING to APPROVED")
    void approveLoan_shouldChangeStatusFromPendingToApproved() {
        when(loanRepository.findById("LOAN1001")).thenReturn(Optional.of(pendingLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanDetailResponse response = loanService.approveLoan("LOAN1001");

        assertThat(response.loanId()).isEqualTo("LOAN1001");
        assertThat(response.status()).isEqualTo(LoanStatus.APPROVED);

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).save(loanCaptor.capture());
        assertThat(loanCaptor.getValue().getStatus()).isEqualTo(LoanStatus.APPROVED);
    }

    @Test
    @DisplayName("approveLoan should throw IllegalStateException when loan is not PENDING")
    void approveLoan_shouldThrowIllegalStateExceptionWhenLoanIsNotPending() {
        pendingLoan.setStatus(LoanStatus.APPROVED);
        when(loanRepository.findById("LOAN1001")).thenReturn(Optional.of(pendingLoan));

        assertThatThrownBy(() -> loanService.approveLoan("LOAN1001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be approved");

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("approveLoan should throw ResourceNotFoundException when loan does not exist")
    void approveLoan_shouldThrowResourceNotFoundExceptionWhenLoanDoesNotExist() {
        when(loanRepository.findById("LOAN9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.approveLoan("LOAN9999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Loan not found with id: LOAN9999");

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("rejectLoan should change status from PENDING to REJECTED and save remarks")
    void rejectLoan_shouldChangeStatusFromPendingToRejectedAndSaveRemarks() {
        when(loanRepository.findById("LOAN1001")).thenReturn(Optional.of(pendingLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanDetailResponse response = loanService.rejectLoan("LOAN1001", "Insufficient credit score");

        assertThat(response.loanId()).isEqualTo("LOAN1001");
        assertThat(response.status()).isEqualTo(LoanStatus.REJECTED);
        assertThat(response.remarks()).isEqualTo("Insufficient credit score");

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).save(loanCaptor.capture());

        Loan savedLoan = loanCaptor.getValue();
        assertThat(savedLoan.getStatus()).isEqualTo(LoanStatus.REJECTED);
        assertThat(savedLoan.getRemarks()).isEqualTo("Insufficient credit score");
    }

    @Test
    @DisplayName("rejectLoan should throw IllegalStateException when loan is not PENDING")
    void rejectLoan_shouldThrowIllegalStateExceptionWhenLoanIsNotPending() {
        pendingLoan.setStatus(LoanStatus.REJECTED);
        when(loanRepository.findById("LOAN1001")).thenReturn(Optional.of(pendingLoan));

        assertThatThrownBy(() -> loanService.rejectLoan("LOAN1001", "Already rejected"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be rejected");

        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    @DisplayName("rejectLoan should throw ResourceNotFoundException when loan does not exist")
    void rejectLoan_shouldThrowResourceNotFoundExceptionWhenLoanDoesNotExist() {
        when(loanRepository.findById("LOAN9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.rejectLoan("LOAN9999", "Invalid loan"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Loan not found with id: LOAN9999");

        verify(loanRepository, never()).save(any(Loan.class));
    }
}
