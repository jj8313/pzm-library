package com.example.demo.controller.api;

import com.example.demo.entity.Book;
import com.example.demo.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 책 데이터 API (화면 대신 JSON을 돌려줌)
@RestController
@RequestMapping("/api/books")
public class BookApiController {

    private final BookService bookService;

    public BookApiController(BookService bookService) {
        this.bookService = bookService;
    }

    // 책 목록: GET /api/books?keyword=자바&page=1
    @GetMapping
    public List<Book> list(@RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "1") int page) {
        return bookService.findAll(keyword, page);
    }

    // 책 1권: GET /api/books/3
    @GetMapping("/{id}")
    public ResponseEntity<Book> detail(@PathVariable Long id) {
        Book book = bookService.findById(id);
        if (book == null) {
            return ResponseEntity.notFound().build();   // 없는 책 → 404
        }
        return ResponseEntity.ok(book);                 // 있으면 → 200 + 책 데이터
    }
}
