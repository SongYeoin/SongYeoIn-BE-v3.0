package com.syi.project.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
        this(1, 20);
    }

    @Override
    public String toString() {
        return "Criteria [pageNum=" + pageNum + ", amount=" + amount + ", type=" + type + ", keyword=" + keyword
                + "]";
    }

    // Pageable 객체 생성 (pageNum이 음수가 되지않도록 코드 수정)
    // Pageable 관련 메서드만 swagger에서 숨김
    @Schema(hidden = true)
    public Pageable getPageable() {
        return PageRequest.of(Math.max(0, pageNum - 1), amount, Sort.by(Sort.Direction.DESC, "regDate"));
    }

    public void setKeywordFromTypeAndKeyword(String type, String keyword) {
        if ("C".equals(type)) {
            this.keyword = "대기".equals(keyword) ? "W" :
                    "승인".equals(keyword) ? "Y" :
                            "미승인".equals(keyword) ? "N" : "";
        } else {
            this.keyword = keyword;
        }
    }

}
