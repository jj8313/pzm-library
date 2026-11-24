package com.example.demo.service;

import com.example.demo.entity.Member;
import com.example.demo.mapper.LoanMapper;
import com.example.demo.mapper.MemberMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    private final MemberMapper memberMapper;
    private final LoanMapper loanMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 비밀번호 규칙: 영문 + 숫자 + 특수문자, 8~16자 (회원가입 화면과 같음)
    private static final String PASSWORD_RULE =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,16}$";

    public MemberService(MemberMapper memberMapper, LoanMapper loanMapper) {
        this.memberMapper = memberMapper;
        this.loanMapper = loanMapper;
    }

    // 이메일 중복 체크
    public boolean isEmailExists(String email) {
        return memberMapper.findByEmail(email) != null;
    }

    // 닉네임 중복 체크
    public boolean isNicknameExists(String nickname) {
        return memberMapper.findByNickname(nickname) != null;
    }

    // 로그인: 입력한 비밀번호와 DB 해시를 matches로 비교
    // 에러는 문장 대신 "키"를 던짐 → Controller가 i18n.get()으로 번역
    public Member login(String email, String password) {
        Member member = memberMapper.findByEmail(email);
        if (member == null || !passwordEncoder.matches(password, member.getPasswordHash())) {
            throw new IllegalArgumentException("member.error.loginFail");
        }
        return member;
    }

    // 회원가입
    public void join(Member member) {
        if (memberMapper.findByEmail(member.getEmail()) != null) {
            throw new IllegalArgumentException("member.error.emailTaken");
        }
        if (memberMapper.findByNickname(member.getNickname()) != null) {
            throw new IllegalArgumentException("member.error.nicknameTaken");
        }
        if (member.getNickname().startsWith("탈퇴회원")) {
            throw new IllegalArgumentException("member.error.nicknameBanned");
        }
        // 비밀번호를 BCrypt로 섞어서 저장
        member.setPasswordHash(passwordEncoder.encode(member.getPasswordHash()));
        memberMapper.insert(member);
    }

    // 회원 1명 찾기
    public Member findById(Long id) {
        Member member = memberMapper.findById(id);
        if (member == null) {
            throw new IllegalArgumentException("member.error.notFound");
        }
        return member;
    }

    public List<Member> findAll() {
        return memberMapper.findAll();
    }

    // 정보 수정 (닉네임, 비밀번호)
    public void updateInfo(Long id, String nickname, String newPassword) {
        // ① 그 닉네임을 다른 사람이 쓰고 있으면 막기 (나면 괜찮음)
        Member sameNickname = memberMapper.findByNickname(nickname);
        if (sameNickname != null && !sameNickname.getId().equals(id)) {
            throw new IllegalArgumentException("member.error.nicknameTaken");
        }
        if (nickname.startsWith("탈퇴회원")) {
            throw new IllegalArgumentException("member.error.nicknameBanned");
        }

        // ② 내 정보 가져와서 닉네임 바꾸기
        Member member = findById(id);
        member.setNickname(nickname);

        // ③ 새 비밀번호를 입력했을 때만 바꾸기 (규칙 검사)
        if (newPassword != null && !newPassword.isBlank()) {
            if (!newPassword.matches(PASSWORD_RULE)) {
                throw new IllegalArgumentException("member.error.passwordRule");
            }
            member.setPasswordHash(passwordEncoder.encode(newPassword));
        }

        // ④ DB 저장
        memberMapper.update(member);
    }

    // 회원탈퇴
    public void withdraw(Long id, String password) {
        Member member = findById(id);

        // ① 비밀번호 확인 (BCrypt)
        if (!passwordEncoder.matches(password, member.getPasswordHash())) {
            throw new IllegalArgumentException("member.error.wrongPassword");
        }

        // ② 빌리고 있거나 신청 중인 책이 있으면 막기
        if (loanMapper.countActive(id) > 0) {
            throw new IllegalArgumentException("member.error.hasLoans");
        }

        // ③ 개인정보만 지우기 (익명화)
        memberMapper.withdraw(id);
    }

    // 관리자 회원 목록: 검색 + 한 페이지 10명 + 회원마다 대출·연체 수
    public List<Member> findPage(String keyword, int page) {
        int offset = (page - 1) * 10;
        List<Member> members = memberMapper.findPage(keyword, offset);   // ① 이번 페이지 회원 (최대 10명)

        // ② 그 회원들만 한 명씩 대출 수·연체 수 채우기
        for (Member m : members) {
            m.setLoanCount(loanMapper.countActive(m.getId()));
            m.setOverdueCount(loanMapper.countOverdue(m.getId()));
        }
        return members;
    }

    // 검색된 회원 수
    public int countAll(String keyword) {
        return memberMapper.countAll(keyword);
    }
}