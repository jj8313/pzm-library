package com.example.demo.mapper;

import com.example.demo.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MemberMapper {
    void insert(Member member);
    Member findById(Long id);
    Member findByEmail(String email);
    List<Member> findAll();
    void update(Member member);
    //void delete(Long id);
    Member findByNickname(String nickname);
    // 회원탈퇴 (개인정보만 바꿈)
    void withdraw(Long id);

    // 언어 저장: "ko" / "ja"
    void updateLang(@Param("id") Long id, @Param("lang") String lang);

    List<Member> findPage(@Param("keyword") String keyword, @Param("offset") int offset);
    int countAll(@Param("keyword") String keyword);
}
