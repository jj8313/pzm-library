package com.example.demo.controller.api;

import com.example.demo.entity.Member;
import com.example.demo.entity.Post;
import com.example.demo.service.BoardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

// 게시판 데이터 API
@RestController
@RequestMapping("/api/board")
public class BoardApiController {

    private final BoardService boardService;

    public BoardApiController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 글 목록: GET /api/board?page=1&type=all&keyword=
    @GetMapping
    public List<Post> list(@RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "all") String type,
                           @RequestParam(defaultValue = "") String keyword) {
        return boardService.findPage(page, type, keyword);
    }

    // 글 1개: GET /api/board/5
    @GetMapping("/{id}")
    public ResponseEntity<Post> detail(@PathVariable Long id) {
        Post post = boardService.findById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();   // 없는 글 → 404
        }
        return ResponseEntity.ok(post);                 // 있으면 → 200 + 글 데이터
    }

    // 글쓰기: POST /api/board
    // 보내는 JSON 예: {"title": "제목", "content": "내용"}
    @PostMapping
    public ResponseEntity<Void> write(@RequestBody Post post,
                                      HttpSession session) throws IOException {
        // ① 로그인 확인
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();   // 401
        }

        // ② 제목·내용이 비었으면 막기
        if (post.getTitle() == null || post.getTitle().isBlank()
                || post.getContent() == null || post.getContent().isBlank()) {
            return ResponseEntity.badRequest().build();                       // 400
        }

        // ③ 작성자는 JSON이 아니라 세션에서! (남의 이름으로 못 쓰게)
        post.setMemberId(loginMember.getId());
        post.setBoard("FREE");                       // API로는 일반 글만

        // ④ 저장 (API는 이미지 없이 글만)
        boardService.write(post, null);
        return ResponseEntity.status(HttpStatus.CREATED).build();             // 201
    }

    // 글 수정: PUT /api/board/5
    // 보내는 JSON 예: {"title": "고친 제목", "content": "고친 내용"}
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @RequestBody Post form,
                                       HttpSession session) {
        // ① 로그인 확인
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();   // 401
        }

        // ② 글이 있는지 확인
        Post post = boardService.findById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();                         // 404
        }

        // ③ 내 글인지 확인
        if (!post.getMemberId().equals(loginMember.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();       // 403
        }

        // ④ 수정 (API는 이미지 삭제 없이 글만)
        form.setId(id);
        boardService.update(form, loginMember.getId(), null);
        return ResponseEntity.ok().build();                                   // 200
    }
    // 글 삭제: DELETE /api/board/5
    // 글 삭제: DELETE /api/board/5 (본인 또는 관리자)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       HttpSession session) {
        // ① 로그인 확인
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();   // 401
        }

        // ② 글이 있는지 확인
        Post post = boardService.findById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();                         // 404
        }

        // ③ 내 글이거나 관리자인지 확인
        boolean isMine = post.getMemberId().equals(loginMember.getId());
        boolean isAdmin = loginMember.isAdmin();
        if (!isMine && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();       // 403
        }

        // ④ 삭제
        boardService.delete(id, loginMember);
        return ResponseEntity.noContent().build();                            // 204
    }
}