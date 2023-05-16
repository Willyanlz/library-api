package com.persisti.libraryapi.model.repository;

import com.persisti.libraryapi.model.entity.Book;
import com.persisti.libraryapi.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    boolean existsByBookAndNotReturned(Book book);
}
