package com.example.demo.controller.admin;

import com.example.demo.config.I18n;
import com.example.demo.entity.Book;
import com.example.demo.entity.Member;
import com.example.demo.service.BookService;
import com.example.demo.service.KakaoBookService;
import com.example.demo.service.LoanService;
import com.example.demo.service.LostBookService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/books")
public class AdminBookController {

    private final KakaoBookService kakaoBookService;
    private final BookService bookService;
    private final LoanService loanService;
    private final LostBookService lostBookService;
    private final I18n i18n;   // 결과 메시지를 화면 언어로

    public AdminBookController(KakaoBookService kakaoBookService,
                               BookService bookService,
                               LoanService loanService,
                               LostBookService lostBookService,
                               I18n i18n) {
        this.kakaoBookService = kakaoBookService;
        this.bookService = bookService;
        this.loanService = loanService;
        this.lostBookService = lostBookService;
        this.i18n = i18n;
    }

    // 카카오 검색 + 등록 화면:  GET /admin/books?query=자바&page=2
    @GetMapping
    public String searchForm(@RequestParam(defaultValue = "") String query,
                             @RequestParam(defaultValue = "1") int page,
                             HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";
        if (!loginMember.isAdmin()) return "redirect:/";

        model.addAttribute("query", query);
        model.addAttribute("page", page);
        model.addAttribute("books", kakaoBookService.search(query, page));
        return "admin/bookSearch";
    }

    // [등록] 버튼
    @PostMapping("/register")
    public String register(Book book,
                           @RequestParam int quantity,
                           @RequestParam(defaultValue = "") String query,
                           @RequestParam(defaultValue = "1") int page,
                           HttpSession session,
                           RedirectAttributes ra) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";
        if (!loginMember.isAdmin()) return "redirect:/";

        bookService.register(book, quantity);

        ra.addAttribute("query", query);
        ra.addAttribute("page", page);
        // 「자바의 정석」 2권 등록 완료! ({0}=제목, {1}=권수)
        ra.addFlashAttribute("message", i18n.get("admin.msg.registered", book.getTitle(), quantity));
        return "redirect:/admin/books";
    }

    // 등록된 도서 (검색 + 페이징)
    @GetMapping("/list")
    public String bookList(@RequestParam(defaultValue = "") String keyword,
                           @RequestParam(defaultValue = "1") int page,
                           HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";
        if (!loginMember.isAdmin()) return "redirect:/";

        int total = bookService.countAll(keyword);

        model.addAttribute("keyword", keyword);
        model.addAttribute("books", bookService.findAll(keyword, page));
        model.addAttribute("total", total);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", (total + 9) / 10);
        model.addAttribute("lostCount", lostBookService.countAll());     // 분실 몇 권 (링크용)
        return "admin/bookList";
    }

    // 분실 도서 목록: GET /admin/books/lost
    @GetMapping("/lost")
    public String lostList(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";
        if (!loginMember.isAdmin()) return "redirect:/";

        model.addAttribute("lostBooks", lostBookService.findAll());
        return "admin/lostList";
    }

    // 책별 대출 이력 + 실물 목록: GET /admin/books/8/loans
    @GetMapping("/{id}/loans")
    public String bookLoans(@PathVariable Long id, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";
        if (!loginMember.isAdmin()) return "redirect:/";

        model.addAttribute("book", bookService.findById(id));
        model.addAttribute("copies", lostBookService.findCopies(id));   // 실물 책 목록
        model.addAttribute("loans", loanService.findByBookId(id));      // 대출 이력
        return "admin/bookLoans";
    }

    // 분실 처리: POST /admin/books/8/copies/21/lost  (사유는 입력 창에서 받음)
    @PostMapping("/{bookId}/copies/{copyId}/lost")
    public String markLost(@PathVariable Long bookId,
                           @PathVariable Long copyId,
                           @RequestParam String reason,
                           HttpSession session, RedirectAttributes ra) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";
        if (!loginMember.isAdmin()) return "redirect:/";

        String result = lostBookService.markLost(copyId, reason);

        if (result.equals("OK")) {
            ra.addFlashAttribute("message", i18n.get("admin.msg.lostDone"));
        } else if (result.equals("RESERVED")) {
            ra.addFlashAttribute("message", i18n.get("admin.msg.lostReserved"));
        } else if (result.equals("ALREADY")) {
            ra.addFlashAttribute("message", i18n.get("admin.msg.lostAlready"));
        } else {
            ra.addFlashAttribute("message", i18n.get("admin.msg.lostNone"));
        }
        return "redirect:/admin/books/" + bookId + "/loans";
    }
}