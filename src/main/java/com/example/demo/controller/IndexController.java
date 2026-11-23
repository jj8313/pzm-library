package com.example.demo.controller;

import com.example.demo.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    private final BookService bookService;

    public IndexController(BookService bookService) {
        this.bookService = bookService;
    }

    // 메인 화면 (헤더의 loginMember는 GlobalAdvice가 넣어 줌)
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("newBooks", bookService.findNewBooks());   // 새로 들어온 책 10권
        return "index";
    }

}
