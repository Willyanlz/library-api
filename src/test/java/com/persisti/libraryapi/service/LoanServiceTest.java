package com.persisti.libraryapi.service;

import com.persisti.libraryapi.api.dto.LoanFilterDTO;
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
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Test
    @DisplayName("Deve obter um emprestimo por id")
    public void getLoanDetailsTest(){
        //cenario
        Long id = 1l;

        Loan loan = createLoan();
        loan.setId(id);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

        //execucao
        Optional<Loan> result = service.getById(id);

        //verificacao
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um emprestimo por id")
    public void updateLoanTest(){
        //cenario
        Long id = 1l;
        Loan loan = createLoan();
        loan.setId(id);
        loan.setReturned(true);

        Mockito.when(repository.save(loan)).thenReturn(loan);

        //execucao
        Loan updatedLoan = service.update(loan);

        //verificacao
        assertThat(updatedLoan.getReturned()).isTrue();

        verify(repository).save(loan);
    }

    @Test
    @DisplayName("Deve filtrar emprestimos por propriedades")
    public void findLoanTest(){
        //cenario
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Teste").isbn("4321").build();

        Loan loan = createLoan();
        loan.setId(1l);
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> lista = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<Loan>(lista, pageRequest, lista.size());
        Mockito.when(repository.findByBookIsbnOrCustomer(
                        Mockito.anyString(),
                        Mockito.anyString(),
                        Mockito.any(PageRequest.class)))
                .thenReturn(page);

        //execucao
        Page<Loan> result = service.find(loanFilterDTO, pageRequest);

        //verificacao
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    public static Loan createLoan(){ Book book = Book.builder().id(1l).title("Teste").author("Teste").isbn("1234").build(); String customer = "Will"; return  Loan.builder().book(book).customer(customer).loanDate(LocalDate.now()).build();}
}
