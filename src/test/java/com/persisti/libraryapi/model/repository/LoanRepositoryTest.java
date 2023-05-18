package com.persisti.libraryapi.model.repository;

import com.persisti.libraryapi.model.entity.Book;
import com.persisti.libraryapi.model.entity.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static com.persisti.libraryapi.model.repository.BookRepositoryTest.createNewBook;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve verificar se existe emprestimo nao devolvido")
    public void existsByBookAndNotReturnedTest(){
        //cenario
        Loan loan = createAndPersistLoan();
        Book book = loan.getBook();

        //execucao
        boolean exists = repository.existsByBookAndNotReturned(book);

        //verificacao
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve buscar um emprestimo por isbn do livro ou customer")
    public void findByBookIsbnOrCustomerTest(){
        //cenario
        Loan loan = createAndPersistLoan();

        //execucao
        Page<Loan> result = repository.findByBookIsbnOrCustomer("123", "Teste", PageRequest.of(0,10));

        //verificacao
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);

        System.out.println(result.getContent());
        System.out.println(loan);
    }

    public Loan createAndPersistLoan(){
        Book book = createNewBook("123");
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Teste").loanDate(LocalDate.now()).build();
        entityManager.persist(loan);

        return loan;
    }
}
