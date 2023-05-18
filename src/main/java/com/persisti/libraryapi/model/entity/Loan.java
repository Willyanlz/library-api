package com.persisti.libraryapi.model.entity;

import jakarta.persistence.*;
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

    @Column(name = "cliente")
    private String customer;

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
