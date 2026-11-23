package com.example.demo.controller.board;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Member;
import com.example.demo.entity.Post;
import com.example.demo.service.BoardService;
import com.example.demo.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    public BoardController(BoardService boardService, CommentService commentService) {
        this.boardService = boardService;
        this.commentService = commentService;
    }

    // ===================== 게시글 =====================

    // 목록 (페이징 + 검색)
    @GetMapping("/list")
    public String boardlist(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "all") String type,
                            @RequestParam(defaultValue = "") String keyword,
                            Model model) {

        model.addAttribute("posts", boardService.findPage(page, type, keyword));
        model.addAttribute("page", page);
        model.addAttribute("totalPages", boardService.getTotalPages(type, keyword));
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        return "board/boardlist";
    }

    // 내가 쓴 글
    @GetMapping("/my")
    public String myPosts(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }

        model.addAttribute("posts", boardService.findMyPost(loginMember.getId()));
        return "board/boardlist";
    }

    // 글쓰기 버튼 누를 때 로그인 확인 (JS가 호출)
    @GetMapping("/check-login")
    @ResponseBody
    public String checkLogin(HttpSession session) {
        if (session.getAttribute("loginMember") == null) {
            return "NO";
        }
        return "OK";
    }

    // 글쓰기 화면
    @GetMapping("/write")
    public String boardwrite(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }
        return "board/boardwrite";
    }

    // 글 저장
    @PostMapping("/write")
    public String write(Post post,
                        @RequestParam(value = "images", required = false) List<MultipartFile> images,
                        @RequestParam(defaultValue = "false") boolean notice,   // 공지 체크 여부
                        HttpSession session) throws IOException {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }

        post.setMemberId(loginMember.getId());

        // 관리자가 체크했을 때만 공지, 나머지는 일반 글
        if (notice && loginMember.isAdmin()) {
            post.setBoard("NOTICE");
        } else {
            post.setBoard("FREE");
        }

        boardService.write(post, images);
        return "redirect:/board/list";
    }

    // 상세 (조회수 + 이미지 + 댓글)
    @GetMapping("/{id}")
    public String boarddetail(@PathVariable Long id,
                              HttpServletRequest request,
                              HttpSession session, Model model) {

        Member loginMember = (Member) session.getAttribute("loginMember");

        // 누가 보는지 (회원번호 또는 IP)
        String viewer;
        if (loginMember != null) {
            viewer = "member:" + loginMember.getId();
        } else {
            viewer = "ip:" + request.getRemoteAddr();
        }

        // 오늘 처음 보는 사람이면 조회수 +1
        boardService.increaseViewCount(id, viewer);

        Post post = boardService.findById(id);
        if (post == null) {
            return "redirect:/board/list";
        }

        model.addAttribute("post", post);
        model.addAttribute("images", boardService.findImages(id));
        model.addAttribute("comments", commentService.findComments(id));
        return "board/boarddetail";
    }

    // 수정 화면
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }

        Post post = boardService.findById(id);
        if (post == null) {
            return "redirect:/board/list";
        }

        // 내 글이 아니면 상세로 돌려보냄
        if (!post.getMemberId().equals(loginMember.getId())) {
            return "redirect:/board/" + id;
        }

        model.addAttribute("post", post);
        model.addAttribute("images", boardService.findImageList(id));   // 번호 + 파일 이름 (체크박스용)
        return "board/boardedit";
    }

    // 수정 저장
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Post form,
                       @RequestParam(required = false) List<Long> deleteImageIds,   // 체크한 이미지 번호들
                       HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }

        form.setId(id);

        try {
            boardService.update(form, loginMember.getId(), deleteImageIds);
        } catch (IllegalStateException e) {
            return "redirect:/board/" + id;
        }

        return "redirect:/board/" + id;   // 수정 결과를 바로 볼 수 있게 상세로
    }

    // 삭제
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }

        try {
            boardService.delete(id, loginMember);          // ← .getId() 지우기
        } catch (IllegalStateException e) {
            return "redirect:/board/" + id;
        }

        return "redirect:/board/list";
    }

    // ===================== 댓글 =====================

    // 댓글 등록: /board/3/comment
    @PostMapping("/{id}/comment")
    public String writeComment(@PathVariable Long id,
                               @RequestParam String content,
                               HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }

        Comment comment = new Comment();
        comment.setPostId(id);
        comment.setMemberId(loginMember.getId());
        comment.setContent(content);

        try {
            commentService.writeComment(comment);
        } catch (IllegalArgumentException e) {
            // 빈 댓글은 저장 안 함
        }

        return "redirect:/board/" + id;
    }

    // 댓글 수정: /board/comment/edit/5
    @PostMapping("/comment/edit/{id}")
    public String updateComment(@PathVariable Long id,
                                @RequestParam String content,
                                HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }

        try {
            Long postId = commentService.updateComment(id, content, loginMember.getId());
            return "redirect:/board/" + postId;
        } catch (IllegalStateException e) {
            return "redirect:/board/list";
        }
    }

    // 댓글 삭제: /board/comment/delete/5 (본인 또는 관리자)
    @PostMapping("/comment/delete/{id}")
    public String deleteComment(@PathVariable Long id, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }

        try {
            Long postId = commentService.deleteComment(id, loginMember);   // ← .getId() 지우기
            return "redirect:/board/" + postId;
        } catch (IllegalStateException e) {
            return "redirect:/board/list";
        }
    }
    // 답글 등록: /board/comment/21/reply
    @PostMapping("/comment/{parentId}/reply")
    public String writeReply(@PathVariable Long parentId,       // 어느 댓글에 다는 답글인지
                             @RequestParam Long postId,         // 끝나고 돌아갈 글 번호
                             @RequestParam String content,
                             HttpSession session) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }

        Comment reply = new Comment();
        reply.setParentId(parentId);
        reply.setMemberId(loginMember.getId());
        reply.setContent(content);

        try {
            commentService.writeReply(reply);
        } catch (IllegalArgumentException e) {
            // 빈 답글, 답글의 답글 등은 저장 안 함
        }

        return "redirect:/board/" + postId;
    }
}