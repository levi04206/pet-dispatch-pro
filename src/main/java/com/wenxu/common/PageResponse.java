package com.wenxu.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Data
public class PageResponse<T> implements Serializable {

    private long total;
    private long pages;
    private long pageNum;
    private long pageSize;
    private List<T> records;

    public static <T> PageResponse<T> of(IPage<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setTotal(page.getTotal());
        response.setPages(page.getPages());
        response.setPageNum(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setRecords(page.getRecords() == null ? Collections.emptyList() : page.getRecords());
        return response;
    }
}
