package com.persisti.libraryapi.service;

import com.persisti.libraryapi.model.entity.Loan;

public interface LoanService {
    Loan save(Loan loan);
}
