package com.example.demo.service;

import com.example.demo.entity.Book;
import com.example.demo.mapper.BookMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {
    private final BookMapper bookMapper;

    public BookService(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }


    // 메인 화면: 새로 들어온 책 10권
    public List<Book> findNewBooks() {
        return bookMapper.findNewBooks();
    }
    // 책 등록: 책 정보 + 몇 권
    @Transactional
    public void register(Book form, int quantity) {

        // ① 이 ISBN 책이 이미 있나?
        Book book = bookMapper.findByIsbn(form.getIsbn());

        if (book == null) {
            // 없으면 → 새로 저장 (저장하면서 form.id 채워짐)
            bookMapper.insert(form);
            book = form;
        }
        // 있으면 → 위에서 찾은 book 그대로 씀

        // ② 지금 실물이 몇 권 있나?
        int start = bookMapper.countCopies(book.getId());

        // ③ 권수만큼 실물 저장
        for (int i = 1; i <= quantity; i++) {
            String barcode = "B" + book.getId() + "-" + (start + i);
            bookMapper.insertCopy(book.getId(), barcode);
        }
    }

    // 책 목록 (page: 몇 페이지)
    public List<Book> findAll(String keyword, int page) {
        int offset = (page - 1) * 10;   // 누른 번호 → 건너뛸 개수
        List<Book> books = bookMapper.findAll(keyword, offset);

        for (Book book : books) {
            book.setTotalCount(bookMapper.countCopies(book.getId()));
            book.setAvailableCount(bookMapper.countAvailable(book.getId()));
        }
        return books;
    }

    // 검색 결과 전체 개수
    public int countAll(String keyword) {
        return bookMapper.countAll(keyword);
    }

    // 책 1권 (상세 화면용)
    public Book findById(Long id) {
        return bookMapper.findById(id);
    }

    // 전체 권수
    public int countCopies(Long bookId) {
        return bookMapper.countCopies(bookId);
    }

    // 대출 가능 권수
    public int countAvailable(Long bookId) {
        return bookMapper.countAvailable(bookId);
    }


}
