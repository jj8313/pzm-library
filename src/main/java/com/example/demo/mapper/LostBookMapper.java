package com.example.demo.mapper;

import com.example.demo.entity.LostBook;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LostBookMapper {
    void insert(LostBook lostBook);   // 분실 기록 저장
    List<LostBook> findAll();         // 분실 목록 (최근 것부터)
    int countAll();                   // 분실 몇 권
}
