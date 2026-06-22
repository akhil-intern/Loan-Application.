package com.example.loanorigination.config;

import com.example.loanorigination.entity.Loan;
import com.example.loanorigination.entity.LoanStatus;
import com.example.loanorigination.repository.LoanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

@Configuration
@Profile("dev")
public class DataLoader {

    @Bean
    CommandLineRunner seedLoans(LoanRepository loanRepository) {
        return args -> {
            if (loanRepository.count() > 0) {
                return;
            }

            loanRepository.save(new Loan(
                    "LOAN1001",
                    "John Doe",
                    "9876543210",
                    new BigDecimal("500000"),
                    "PERSONAL",
                    36,
                    LoanStatus.PENDING,
                    null
            ));
            loanRepository.save(new Loan(
                    "LOAN1002",
                    "Jane Smith",
                    "9123456780",
                    new BigDecimal("800000"),
                    "HOME",
                    240,
                    LoanStatus.APPROVED,
                    null
            ));
            loanRepository.save(new Loan(
                    "LOAN1003",
                    "David Wilson",
                    "9988776655",
                    new BigDecimal("300000"),
                    "VEHICLE",
                    48,
                    LoanStatus.REJECTED,
                    "Insufficient credit score"
            ));
        };
    }
}
