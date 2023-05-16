package com.persisti.libraryapi.service;

import com.persisti.libraryapi.exception.BusinessException;
import com.persisti.libraryapi.model.entity.Book;
import com.persisti.libraryapi.model.entity.Loan;
import com.persisti.libraryapi.model.repository.LoanRepository;
import com.persisti.libraryapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    LoanService service;

    @MockBean
    LoanRepository repository;

    @BeforeEach
    public void setUp(){
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um emprestimo")
    public void saveLoanTest(){
        //cenario
        Book book = Book.builder().id(1l).title("Teste").author("Teste").isbn("1234").build();
        String customer = "Will";
        Loan savingLoan = Loan.builder().book(book).customer(customer).loanDate(LocalDate.now()).build();

        Loan savedLoan = Loan.builder().id(1l).book(book).loanDate(LocalDate.now()).customer(customer).build();

        Mockito.when(repository.save(savingLoan)).thenReturn(savedLoan);

        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(false);

        //execucao
        Loan loan = service.save(savingLoan);

        //verificacao
        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }

    @Test
    @DisplayName("Deve emitir erro ao tentar salvar livro ja emprestado")
    public void loanedSaveTest(){
        //cenario
        Book book = Book.builder().id(1l).title("Teste").author("Teste").isbn("1234").build();
        String customer = "Will";
        Loan savingLoan = Loan.builder().book(book).customer(customer).loanDate(LocalDate.now()).build();

        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        //execucao
        Throwable exception = catchThrowable(() -> service.save(savingLoan));

        //verificacao
        assertThat(exception).isInstanceOf(BusinessException.class)
                .hasMessage(("Book already loaned"));

        Mockito.verify(repository, Mockito.never()).save(savingLoan);
    }
}
