package com.syi.project.common.entity;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class Criteria {

    /* 현재 페이지 번호 */
    private int pageNum;

    /* 페이지 표시 개수 */
    private int amount;

    /* 검색 타입 */
    private String type;

    /* 검색 키워드 */
    private String keyword;

    /* 카테고리 */
    private String category;

    /* Criteria 생성자 */
    public Criteria(int pageNum, int amount) {
        this.pageNum = pageNum;
        this.amount = amount;
    }

    /* Criteria 기본 생성자 */
    public Criteria() {
        this(1, 15);
    }

    @Override
    public String toString() {
        return "Criteria [pageNum=" + pageNum + ", amount=" + amount + ", type=" + type + ", keyword=" + keyword
                + "]";
    }

    // Pageable 객체 생성
    public Pageable getPageable() {
        return PageRequest.of(pageNum - 1, amount, Sort.by(Sort.Direction.DESC, "regDate"));
    }

}
