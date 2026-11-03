package com.nczy.coalsales.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        PaginationInnerInterceptor paginationInterceptor =
                new PaginationInnerInterceptor(DbType.MYSQL);

        // 1. 防止恶意大分页，限制单页最多500条
        paginationInterceptor.setMaxLimit(500L);

        // 2. 页码溢出时返回第一页，避免空数据（按需开启）
        paginationInterceptor.setOverflow(true);

        // 3. 优化 COUNT 查询，自动移除不必要的 LEFT JOIN
        paginationInterceptor.setOptimizeJoin(true);

        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}
