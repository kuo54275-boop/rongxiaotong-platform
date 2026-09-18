package com.rongxiaotong.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 分页返回格式：{ list 当前页数据, total 总条数, page 当前页, size 每页条数 }
 */
@Data
public class PageResult<T> {

    private List<T> list;
    private long total;
    private long page;
    private long size;

    public static <T> PageResult<T> of(Page<T> p) {
        return of(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize());
    }

    public static <T> PageResult<T> of(List<T> list, long total, long page, long size) {
        PageResult<T> r = new PageResult<>();
        r.setList(list);
        r.setTotal(total);
        r.setPage(page);
        r.setSize(size);
        return r;
    }
}
