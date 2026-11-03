package com.nczy.coalsales.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 分页返回结构
 * <p>
 * 统一分页响应体，字段命名对齐 MyBatis-Plus 的 {@link Page}。
 */
@Data
public class PageResult<T> {

    /** 总记录数 */
    private Long total;

    /** 当前页数据 */
    private List<T> records;

    /** 当前页码 */
    private Long current;

    /** 每页条数 */
    private Long size;

    /**
     * 将 MyBatis-Plus 分页对象转换为统一分页结构
     */
    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.total = page.getTotal();
        result.records = page.getRecords();
        result.current = page.getCurrent();
        result.size = page.getSize();
        return result;
    }
}
