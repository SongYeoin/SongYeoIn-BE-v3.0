package com.syi.project.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageInfoDTO {
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private boolean hasNext;
    private boolean hasPrevious;
}
