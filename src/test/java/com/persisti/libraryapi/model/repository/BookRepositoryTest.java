package com.persisti.libraryapi.model.repository;

import com.persisti.libraryapi.model.entity.Book;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar verdadeiro se existir livor com isbn informado")
    public void returnTrueWhenIsbnExists(){
        //cenario
        String isbn = "1234";
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        //execucao
        boolean exists = repository.existsByIsbn(isbn);

        //verificacao
        assertThat(exists).isTrue();
    }

    public static Book createNewBook(String isbn) {
        return Book.builder().title("Teste").author("Teste").isbn("1234").build();
    }

    @Test
    @DisplayName("Deve retornar falso se não existir livro com isbn informado")
    public void returnFalseWhenIsbnNotExists(){
        //cenario
        String isbn = "1234";

        //execucao
        boolean exists = repository.existsByIsbn(isbn);

        //verificacao
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter livro por id")
    public void findByIdTest(){
        //cenario
        Book book = createNewBook("123");
        entityManager.persist(book);

        //execucao
        Optional<Book> foundBook = repository.findById(book.getId());

        //verificacao
        assertThat(foundBook.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Deve salvar livro")
    public void saveBookTest(){
        //cenario
        Book book = createNewBook("123");

        //execucao
        book.setTitle("Update");
        Book savedBook = repository.save(book);

        //verificacao
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo("Update");
    }

    @Test
    @DisplayName("Deve emitir erro ao tentar salvar livro inexixtente")
    public void saveInexistentBookTest(){
        //cenario
        Book book = createNewBook("123");
        book.setId(Mockito.anyLong());

        //execucao
        Optional<Book> notFoundBook = repository.findById(book.getId());

        //verificacao
        Assertions.assertThat(notFoundBook.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Deve deletar livro por id")
    public void deleteByIdTest(){
        //cenario
        Book book = createNewBook("123");
        entityManager.persist(book);

        //execucao
//        repository.delete(book);
//        Optional<Book> deletedBook = repository.findById(book.getId());

        Book foundBook = entityManager.find(Book.class, book.getId());
        repository.delete(foundBook);

        //verificacao
//        assertThat(deletedBook.isPresent()).isFalse();
        Book deletedBook = entityManager.find(Book.class, book.getId());
        assertThat(deletedBook).isNull();
    }

    @Test
    @DisplayName("Deve emitir erro ao tentar deletar livro inexistente")
    public void deleteInexistentByIdTest(){
        //cenario
        Book book = createNewBook("123");
        book.setId(Mockito.anyLong());

        //execucao
        repository.delete(book);
        Optional<Book> deletedBook = repository.findById(book.getId());

        //verificacao
        assertThat(deletedBook.isEmpty()).isTrue();
    }

}
