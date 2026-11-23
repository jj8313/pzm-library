package com.example.demo.service;

import com.example.demo.entity.Member;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostImage;
import com.example.demo.mapper.BoardMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class BoardService {

    private static final String UPLOAD_DIR = "C:/pzm-upload/";

    private final BoardMapper boardMapper;

    public BoardService(BoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    // ===== 글쓰기 =====

    // 글 저장 + 이미지 여러 장
    @Transactional(rollbackFor = Exception.class)
    public void write(Post post, List<MultipartFile> images) throws IOException {
        boardMapper.insert(post);   // 저장 후 post.id 생김

        if (images == null) return;

        new File(UPLOAD_DIR).mkdirs();

        for (MultipartFile image : images) {
            if (image.isEmpty()) continue;   // 빈 칸은 건너뜀

            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            image.transferTo(new File(UPLOAD_DIR + fileName));
            boardMapper.insertImage(post.getId(), fileName);
        }
    }

    // ===== 목록 =====

    // 한 페이지 글 목록 (+ 검색)
    public List<Post> findPage(int page, String type, String keyword) {
        int offset = (page - 1) * 10;
        return boardMapper.findPage(offset, type, keyword);
    }

    // 전체 페이지 수 (+ 검색)
    public int getTotalPages(String type, String keyword) {
        int total = boardMapper.countAll(type, keyword);
        return (total + 9) / 10;
    }

    // 내가 쓴 글
    public List<Post> findMyPost(Long memberId) {
        return boardMapper.findByMemberId(memberId);
    }

    // ===== 상세 =====

    // 글 하나 (조회수는 안 건드림)
    public Post findById(Long id) {
        return boardMapper.findById(id);
    }

    // 이 글의 이미지들 (상세 화면용: 파일 이름만)
    public List<String> findImages(Long postId) {
        return boardMapper.findImages(postId);
    }

    // ← 추가: 수정 화면용 (번호 + 파일 이름)
    public List<PostImage> findImageList(Long postId) {
        return boardMapper.findImageList(postId);
    }

    // ===== 조회수 =====

    // 오늘 처음 보는 사람이면 +1
    public void increaseViewCount(Long postId, String viewer) {
        int count = boardMapper.countView(postId, viewer);
        if (count == 0) {
            boardMapper.insertView(postId, viewer);
            boardMapper.increaseViewCount(postId);
        }
    }

    // ===== 수정 / 삭제 =====

    // ← 수정: 글 수정 + 체크한 이미지 삭제 (본인 글일 때만)
    // 글 수정 + 체크한 이미지 삭제 (본인 글일 때만)
    @Transactional
    public void update(Post form, Long loginMemberId, List<Long> deleteImageIds) {
        Post post = boardMapper.findById(form.getId());

        if (post == null || !post.getMemberId().equals(loginMemberId)) {
            throw new IllegalStateException("본인 글만 수정할 수 있습니다.");
        }

        // ① 글 내용 수정
        boardMapper.update(form);

        // ② 체크한 이미지 지우기: DB 먼저, 파일은 나중에
        if (deleteImageIds != null) {
            // 이 글의 이미지 목록 (번호 + 파일 이름)
            List<PostImage> images = boardMapper.findImageList(form.getId());

            for (PostImage image : images) {
                if (deleteImageIds.contains(image.getId())) {               // 체크한 이미지면
                    boardMapper.deleteImage(image.getId(), form.getId());   // DB 기록 삭제
                    new File(UPLOAD_DIR + image.getFilePath()).delete();     // 실제 파일 삭제
                }
            }
        }
    }

    // 글 삭제 (본인 또는 관리자)
    public void delete(Long id, Member loginMember) {
        Post post = boardMapper.findById(id);
        if (post == null) {
            throw new IllegalStateException("없는 글입니다.");
        }

        boolean isMine = post.getMemberId().equals(loginMember.getId());   // 내 글인가
        boolean isAdmin = loginMember.isAdmin();           // 관리자인가

        if (!isMine && !isAdmin) {
            throw new IllegalStateException("본인 글만 삭제할 수 있습니다.");
        }

        boardMapper.delete(id);
    }
}