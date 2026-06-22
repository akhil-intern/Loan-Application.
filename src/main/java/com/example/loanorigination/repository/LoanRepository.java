package com.example.loanorigination.repository;

import com.example.loanorigination.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, String> {

    @Query("SELECT l.loanId FROM Loan l WHERE l.loanId LIKE 'LOAN%'")
    List<String> findAllLoanIds();
}
