package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.annotation.Log;
import com.nczy.coalsales.common.BusinessException;
import com.nczy.coalsales.dto.StockCheckDTO;
import com.nczy.coalsales.dto.StockInboundDTO;
import com.nczy.coalsales.entity.Stock;
import com.nczy.coalsales.entity.StockRecord;
import com.nczy.coalsales.mapper.StockMapper;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.StockRecordService;
import com.nczy.coalsales.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StockServiceImpl
        extends ServiceImpl<StockMapper, Stock>
        implements StockService {

    private final StockRecordService stockRecordService;

    public StockServiceImpl(StockRecordService stockRecordService) {
        this.stockRecordService = stockRecordService;
    }

    @Override
    public List<Stock> getStocks() {
        List<Stock> stocks = baseMapper.listStock();

        // 当前库存低于安全库存时，设置预警标记
        for (Stock stock : stocks) {
            boolean warning = stock.getTotalQty()
                    .compareTo(stock.getSafeStock()) < 0;

            stock.setWarning(warning);
        }

        return stocks;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inbound(StockInboundDTO dto, LoginUser loginUser) {
        // 1. 入库数量必须大于 0
        if (dto.getChangeQty() == null
                || dto.getChangeQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("入库数量必须大于 0");
        }

        // 2. 查询并锁定库存（防止并发修改）
        Stock stock = baseMapper.selectByProductIdForUpdate(dto.getProductId());

        // 3. 第一次入库时，先建立库存记录
        if (stock == null) {
            stock = new Stock();
            stock.setProductId(dto.getProductId());
            stock.setTotalQty(BigDecimal.ZERO);
            save(stock);

            stock = baseMapper.selectByProductIdForUpdate(dto.getProductId());
        }

        // 4. 保存变动前库存，计算变动后库存
        BigDecimal beforeQty = stock.getTotalQty();
        BigDecimal afterQty = beforeQty.add(dto.getChangeQty());

        // 5. 修改当前库存
        int rows = baseMapper.changeStockQty(
                dto.getProductId(),
                dto.getChangeQty()
        );
        if (rows != 1) {
            throw new BusinessException("库存更新失败");
        }

        // 6. 生成入库流水
        StockRecord record = new StockRecord();
        record.setProductId(dto.getProductId());
        record.setSourceType("INBOUND");
        record.setSourceId(null);
        record.setChangeQty(dto.getChangeQty());
        record.setBeforeQty(beforeQty);
        record.setAfterQty(afterQty);
        record.setRemark(dto.getRemark());
        record.setOperatorId(loginUser.getUserId());

        stockRecordService.save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Log("盘点调整")
    public void checkStock(StockCheckDTO dto, LoginUser loginUser) {
        // 1. 调整数量不能为 0（正数盘盈，负数盘亏）
        if (dto.getChangeQty() == null
                || dto.getChangeQty().compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException("盘点调整数量不能为 0");
        }

        // 2. 查询并锁定库存（防止并发修改）
        Stock stock = baseMapper.selectByProductIdForUpdate(dto.getProductId());

        // 3. 该煤种尚无库存记录时，无法盘点（需先入库）
        if (stock == null) {
            throw new BusinessException("该煤种暂无库存记录，无法盘点调整");
        }

        // 4. 保存变动前库存，计算变动后库存
        BigDecimal beforeQty = stock.getTotalQty();
        BigDecimal afterQty = beforeQty.add(dto.getChangeQty());

        // 5. 修改当前库存（盘亏导致为负时会更新失败）
        int rows = baseMapper.changeStockQty(
                dto.getProductId(),
                dto.getChangeQty()
        );
        if (rows != 1) {
            throw new BusinessException("库存更新失败");
        }

        // 6. 生成盘点流水
        StockRecord record = new StockRecord();
        record.setProductId(dto.getProductId());
        record.setSourceType("CHECK");
        record.setSourceId(null);
        record.setChangeQty(dto.getChangeQty());
        record.setBeforeQty(beforeQty);
        record.setAfterQty(afterQty);
        record.setRemark(dto.getRemark());
        record.setOperatorId(loginUser.getUserId());

        stockRecordService.save(record);
    }
}
