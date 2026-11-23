package com.example.demo.mapper;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {

    // ===== 글쓰기 =====
    void insert(Post post);
    void insertImage(@Param("postId") Long postId, @Param("filePath") String filePath);

    // ===== 목록 =====
    List<Post> findPage(@Param("offset") int offset,
                        @Param("type") String type,
                        @Param("keyword") String keyword);

    int countAll(@Param("type") String type,
                 @Param("keyword") String keyword);

    List<Post> findByMemberId(Long memberId);

    // ===== 상세 =====
    Post findById(Long id);
    List<String> findImages(Long postId);
    List<PostImage> findImageList(Long postId);                                         // ← 추가
    void deleteImage(@Param("imageId") Long imageId, @Param("postId") Long postId);     // ← 추가

    // ===== 조회수 =====
    int countView(@Param("postId") Long postId, @Param("viewer") String viewer);
    void insertView(@Param("postId") Long postId, @Param("viewer") String viewer);
    void increaseViewCount(Long id);

    // ===== 수정 / 삭제 =====
    void update(Post post);
    void delete(Long id);
}