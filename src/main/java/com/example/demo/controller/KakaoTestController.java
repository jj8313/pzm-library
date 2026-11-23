//package com.example.demo.controller;
//
//import com.example.demo.dto.KakaoBook;
//import com.example.demo.dto.KakaoBookResponse;
//import com.example.demo.service.KakaoBookService;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.List;
//
//@RestController   // 화면 말고 글자(JSON) 그대로 돌려줌
//public class KakaoTestController {
//
//    private final KakaoBookService kakaoBookService;
//
//    public KakaoTestController(KakaoBookService kakaoBookService) {
//        this.kakaoBookService = kakaoBookService;
//    }
//
//    @GetMapping("/test/kakao")
//    public List<KakaoBook> test(@RequestParam String query) {
//        return kakaoBookService.search(query);
//    }
//}