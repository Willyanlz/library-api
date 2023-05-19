package com.persisti.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persisti.libraryapi.api.dto.BookDTO;
import com.persisti.libraryapi.exception.BusinessException;
import com.persisti.libraryapi.model.entity.Book;
import com.persisti.libraryapi.service.BookService;
import com.persisti.libraryapi.service.LoanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @MockBean
    LoanService loanService;

    @Test
    @DisplayName("Deve criar um livro")
    public void createBookTest() throws Exception {
        //cenario
        BookDTO dto = createNewBook();
        Book savedBook = Book.builder().id(10l).author("Will").title("Criando uma API Rest com testes").isbn("1234").build();

        //execucao
        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);
        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //verificacao
        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(10l))
                .andExpect(jsonPath("title").value(dto.getTitle()))
                .andExpect(jsonPath("author").value(dto.getAuthor()))
                .andExpect(jsonPath("isbn").value(dto.getIsbn()))
                ;
    }


    @Test
    @DisplayName("Deve emitir erro caso dados insuficientes para criar livros")
    public void createInvalidBookTest() throws Exception{
        //cenario
        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        //execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //verificacao
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Deve emitir erro ao tentar criar livro com isbn repetido")
    public void createBookWithDuplicatedIsbn() throws Exception{

        //cenario
        BookDTO dto = createNewBook();
        String json = new ObjectMapper().writeValueAsString(dto);

        String msgError = "Isbn ja cadastrado!";
        BDDMockito.given(service.save(Mockito.any(Book.class)))
                .willThrow(new BusinessException(msgError));

        //execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //verificação
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value(msgError));
    }

    @Test
    @DisplayName("Deve obter o detalhes do lviro")
    public void getBookDetailsTest() throws Exception{
        //cenario
        Long id = 1l;
        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .author(createNewBook().getAuthor())
                .isbn(createNewBook().getIsbn())
                .build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        //verificacao
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(jsonPath("isbn").value(createNewBook().getIsbn()));
    }

    @Test
    @DisplayName("Deve emitir erro caso livro nao seja encontrado")
    public void bookNotFoundTest() throws Exception{
        //cenario
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        //verificacao
        mvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception {
        //cenario

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(1l).build()));

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        //verificacao
        mvc.perform(request)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve emitir erro quando nao encontrar livro para deletar")
    public void deleteInexistentBookTest() throws Exception {
        //cenario
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        //verificacao
        mvc.perform(request)
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception {
        //cenario
        Long id = 1l;
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        Book updatingBook = Book.builder().id(id).author("Update").title("Update").isbn("1000").build();

        BDDMockito.given(service.getById(id))
                .willReturn(Optional.of(updatingBook));

        Book updatedBook = Book.builder().id(id).author("Will").title("Criando uma API Rest com testes").isbn("1000").build();

        BDDMockito.given(service.update(updatingBook)).willReturn(updatedBook);

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .content(json)
                .accept(MediaType.APPLICATION_JSON);

    }


    @Test
    @DisplayName("Deve emitir erro ao tentar atualizar um livro inexistente")
    public void updateInexistentBookTest() throws Exception {
        //cenario
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        BDDMockito.given(service.getById(Mockito.anyLong()))
                .willReturn(Optional.empty());

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON);
    }

    @Test
    @DisplayName("Deve filtrar livros")
    public void findBooksTest() throws Exception{
        //cenario
        Long id = 1l;

        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .author(createNewBook().getAuthor())
                .isbn(createNewBook().getIsbn())
                .build();

        //execucao
        BDDMockito.given(service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book> (Arrays.asList(book), PageRequest.of(0,100), 1));

        String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        //verificacao
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

    private static BookDTO createNewBook() {
        return BookDTO.builder().author("Will").title("Criando uma API Rest com testes").isbn("1234").build();
    }
}
