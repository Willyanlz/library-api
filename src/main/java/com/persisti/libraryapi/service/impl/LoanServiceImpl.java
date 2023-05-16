package com.persisti.libraryapi.service.impl;

import com.persisti.libraryapi.exception.BusinessException;
import com.persisti.libraryapi.model.entity.Loan;
import com.persisti.libraryapi.model.repository.LoanRepository;
import com.persisti.libraryapi.service.LoanService;

public class LoanServiceImpl implements LoanService {

    private LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        if(repository.existsByBookAndNotReturned(loan.getBook())){
            throw new BusinessException("Book already loaned");
        }
        return repository.save(loan);
    }
}
