package com.example.demo.service;

import com.example.demo.dto.KakaoBook;
import com.example.demo.dto.KakaoBookResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class KakaoBookService {

    @Value("${kakao.rest-key}")
    private String restKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // query: 검색어, page: 몇 페이지 (1, 2, 3...)
    public List<KakaoBook> search(String query, int page) {
        if (query == null || query.isBlank()) return new ArrayList<>();

        // page: 몇 페이지 / size=10: 한 페이지에 10권
        String url = "https://dapi.kakao.com/v3/search/book?query={query}&page={page}&size=10";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + restKey);

        ResponseEntity<KakaoBookResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                KakaoBookResponse.class,
                query, page);           // {query}, {page} 자리에 순서대로

        return response.getBody().getDocuments();
    }
}