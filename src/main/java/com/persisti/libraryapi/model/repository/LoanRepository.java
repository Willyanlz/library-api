//package com.persisti.libraryapi.model.repository;
//
//import com.persisti.libraryapi.model.entity.Book;
//import com.persisti.libraryapi.model.entity.Loan;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//public interface LoanRepository extends JpaRepository<Loan, Long> {
//
//    @Query(value = "select case when ( count(l.id) > 0 ) then true else false end " +
//            "from Loan l where l.book = :book and l.returned is null or l.returned is not true")
//    boolean existsByBookAndNotReturned(@Param("book") Book book);
//}

package com.persisti.libraryapi.model.repository;

        import com.persisti.libraryapi.model.entity.Book;
        import com.persisti.libraryapi.model.entity.Loan;
        import org.springframework.data.domain.Page;
        import org.springframework.data.domain.Pageable;
        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.data.jpa.repository.Query;
        import org.springframework.data.repository.query.Param;

        import java.time.LocalDate;
        import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query(value = "SELECT CASE WHEN COUNT(l.id) > 0 THEN true ELSE false END " +
            "FROM Loan l WHERE l.book = :book AND (l.returned IS NULL OR l.returned <> true)")
    boolean existsByBookAndNotReturned(@Param("book") Book book);

    @Query("SELECT l FROM Loan AS l JOIN l.book AS b WHERE b.isbn = :isbn OR l.customer = :customer")
    Page<Loan> findByBookIsbnOrCustomer(
            @Param("isbn") String isbn,
            @Param("customer") String customer,
            Pageable pageRequest
    );

    Page<Loan> findByBook(Book book, Pageable pageable);

    @Query("SELECT l FROM Loan l WHERE l.loanDate <= :threeDaysAgo AND (l.returned IS NULL OR l.returned <> true)")
    List<Loan> findByLoanDateLessThanAndNotReturned(@Param("threeDaysAgo") LocalDate threeDaysAgo);

}


