package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.entity.StockRecord;
import com.nczy.coalsales.mapper.StockRecordMapper;
import com.nczy.coalsales.service.StockRecordService;
import org.springframework.stereotype.Service;

@Service
public class StockRecordServiceImpl
        extends ServiceImpl<StockRecordMapper, StockRecord>
        implements StockRecordService {

    @Override
    public PageResult<StockRecord> pageStockRecord(
            Long current,
            Long size) {

        Page<StockRecord> page = baseMapper.pageStockRecord(
                new Page<>(current, size)
        );

        return PageResult.of(page);
    }
}
