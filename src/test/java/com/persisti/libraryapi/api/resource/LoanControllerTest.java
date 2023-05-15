package com.persisti.libraryapi.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.persisti.libraryapi.api.dto.LoanDTO;
import com.persisti.libraryapi.exception.BusinessException;
import com.persisti.libraryapi.service.LoanService;
import com.persisti.libraryapi.model.entity.Book;
import com.persisti.libraryapi.model.entity.Loan;
import com.persisti.libraryapi.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
public class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @Test
    @DisplayName("Deve realizar um emprestimo")
    public void createLoanTest() throws Exception {
        //cenario
        LoanDTO dto = LoanDTO.builder().isbn("1234").customer("Teste").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1l).isbn("1234").build();
        BDDMockito.given(bookService.getBookByIsbn("1234")).willReturn(Optional.of(book));

        Loan loan = Loan.builder().id(1l).customer("Teste").book(book).loanDate(LocalDate.now()).build();
        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //verificacao
        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("Deve emitir erro ao tentar salvar livro com isbn inexixtente")
    public void invalidIsbnCreateLoanTest() throws Exception{
        //cenario
        LoanDTO dto = LoanDTO.builder().isbn("1234").customer("Teste").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("1234")).willReturn(Optional.empty());

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //verificacao
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book not found for passed isbn"));
    }

    @Test
    @DisplayName("Deve emitir erro ao tentar emprestar livro ja emprestado")
    public void loanedBookErrorOnCreateLoanTest() throws Exception{
        //cenario
        LoanDTO dto = LoanDTO.builder().isbn("1234").customer("Teste").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Book book = Book.builder().id(1l).isbn("1234").build();
        BDDMockito.given(bookService.getBookByIsbn("1234")).willReturn(Optional.of(book));

        BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
                .willThrow(new BusinessException("Book already loaned"));

        //execucao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        //verificacao
        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book already loaned"));
    }
}
