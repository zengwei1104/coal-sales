package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.dto.StockCheckDTO;
import com.nczy.coalsales.dto.StockInboundDTO;
import com.nczy.coalsales.entity.Stock;
import com.nczy.coalsales.security.LoginUser;

import java.util.List;

public interface StockService extends IService<Stock> {

    /**
     * 查询全部煤种的当前库存
     */
    List<Stock> getStocks();

    /**
     * 采购入库：增加库存并生成入库流水
     */
    void inbound(StockInboundDTO dto, LoginUser loginUser);

    /**
     * 盘点调整：按盘盈/盘亏修正库存并生成盘点流水
     */
    void checkStock(StockCheckDTO dto, LoginUser loginUser);
}
