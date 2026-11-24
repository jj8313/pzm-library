package com.example.demo.controller.book;

import com.example.demo.config.I18n;
import com.example.demo.entity.Book;
import com.example.demo.entity.Member;
import com.example.demo.service.BookService;
import com.example.demo.service.LoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookController {

    private final BookService bookService;
    private final LoanService loanService;
    private final I18n i18n;   // 결과 메시지를 화면 언어로

    public BookController(BookService bookService, LoanService loanService, I18n i18n) {
        this.bookService = bookService;
        this.loanService = loanService;
        this.i18n = i18n;
    }

    // 회원용 도서 목록 + 검색 + 페이징 (로그인 안 해도 볼 수 있음)
    @GetMapping("/books")
    public String bookList(@RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "1") int page,
                           Model model) {

        int total = bookService.countAll(keyword);   // 전체 개수
        int totalPages = (total + 9) / 10;           // 페이지 수

        model.addAttribute("keyword", keyword);
        model.addAttribute("books", bookService.findAll(keyword, page));
        model.addAttribute("total", total);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        return "book/bookSearchResult";
    }

    // 도서 상세
    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id);

        // 없는 번호로 들어오면 목록으로
        if (book == null) return "redirect:/books";

        model.addAttribute("book", book);
        model.addAttribute("totalCount", bookService.countCopies(id));
        model.addAttribute("availableCount", bookService.countAvailable(id));
        return "book/bookDetail";
    }

    // 대출 신청:  POST /books/3/loan
    @PostMapping("/books/{id}/loan")
    public String requestLoan(@PathVariable Long id,
                              HttpSession session, RedirectAttributes ra) {
        // 로그인 안 했으면 로그인 화면으로
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";

        // 신청 결과: "OK" / "SUSPENDED" / "OVERDUE" / "LIMIT" / "DUPLICATE" / "NO_BOOK"
        String result = loanService.requestLoan(loginMember.getId(), id);

        // 결과별로 다른 메시지 (화면 언어로)
        if (result.equals("OK")) {
            ra.addFlashAttribute("message", i18n.get("loan.msg.requested"));
        } else if (result.equals("SUSPENDED")) {
            ra.addFlashAttribute("message", i18n.get("loan.msg.suspended"));
        } else if (result.equals("OVERDUE")) {
            ra.addFlashAttribute("message", i18n.get("loan.msg.hasOverdue"));
        } else if (result.equals("LIMIT")) {
            ra.addFlashAttribute("message", i18n.get("loan.msg.limit"));
        } else if (result.equals("DUPLICATE")) {
            ra.addFlashAttribute("message", i18n.get("loan.msg.duplicate"));
        } else {
            ra.addFlashAttribute("message", i18n.get("loan.msg.noBook"));
        }

        return "redirect:/books/" + id;   // 다시 그 책 상세로
    }
}