package com.persisti.libraryapi.model.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente", length = 100)
    private String customer;

    @Column(name = "customer_email")
    private String customerEmail;

    @JoinColumn(name = "id_book")
    @ManyToOne
    private Book book;

    @Column(name = "data_emprestimo")
    private LocalDate loanDate;

    @Column(name = "devolvido")
    private boolean returned;

    public boolean getReturned() {
        return returned;
    }
}
