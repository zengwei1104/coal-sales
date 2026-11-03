package com.nczy.coalsales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nczy.coalsales.entity.StockRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StockRecordMapper extends BaseMapper<StockRecord> {

    /**
     * 分页查询库存流水，联表带出煤种名称。
     * 用 Page 参数可让 MyBatis-Plus 分页插件自动拦截生成 count / limit。
     */
    @Select("SELECT r.*, p.product_name AS productName " +
            "FROM stock_record r " +
            "LEFT JOIN coal_product p ON p.id = r.product_id " +
            "WHERE r.deleted = 0 " +
            "ORDER BY r.create_time DESC")
    Page<StockRecord> pageStockRecord(Page<StockRecord> page);
}
