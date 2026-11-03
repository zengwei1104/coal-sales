package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.entity.StockRecord;

public interface StockRecordService
        extends IService<StockRecord> {

    /**
     * 分页查询库存流水
     */
    PageResult<StockRecord> pageStockRecord(
            Long current,
            Long size
    );
}
