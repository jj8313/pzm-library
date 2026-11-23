package com.example.demo.mapper;

import com.example.demo.entity.Book;
import com.example.demo.entity.BookCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookMapper {
    // ① ISBN으로 책 찾기 (없으면 null)
    Book findByIsbn(String isbn);

    // ② 책 정보 저장 (저장 후 book.id 채워짐)
    void insert(Book book);

    // ③ 이 책 실물이 몇 권 있나
    int countCopies(Long bookId);

    // ④ 실물 한 권 저장
    void insertCopy(@Param("bookId") Long bookId,
                    @Param("barcode") String barcode);

    // 페이징 처리
    List<Book> findAll(@Param("keyword") String keyword,
                       @Param("offset") int offset);
    int countAll(String keyword);

    Book findById(Long id);


    int countAvailable(Long bookId);

    List<Book> findNewBooks();

    List<BookCopy> findCopiesByBookId(Long bookId);
    BookCopy findCopyById(Long id);
    void updateCopyLost(Long id);
}
