package com.nczy.coalsales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nczy.coalsales.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    /**
     * 查询并锁定指定煤种的库存记录。
     * 必须在事务中调用，防止过磅和入库同时修改库存。
     */
    @Select("SELECT * FROM stock " +
            "WHERE product_id = #{productId} AND deleted = 0 " +
            "FOR UPDATE")
    Stock selectByProductIdForUpdate(@Param("productId") Long productId);

    /**
     * 增减库存。
     * changeQty 为正数表示入库，为负数表示出库。
     * 库存不足时不会更新，返回影响行数 0。
     */
    @Update("UPDATE stock " +
            "SET total_qty = total_qty + #{changeQty}, " +
            "    update_time = NOW() " +
            "WHERE product_id = #{productId} " +
            "  AND deleted = 0 " +
            "  AND total_qty + #{changeQty} >= 0")
    int changeStockQty(@Param("productId") Long productId,
                       @Param("changeQty") BigDecimal changeQty);

    /**
     * 查询全部煤种的当前库存。
     * 商品没有库存记录时，当前库存显示为 0。
     */
    @Select("SELECT " +
            "s.id, " +
            "p.id AS product_id, " +
            "p.product_name, " +
            "IFNULL(s.total_qty, 0) AS total_qty, " +
            "p.safe_stock, " +
            "s.create_time, " +
            "s.update_time " +
            "FROM coal_product p " +
            "LEFT JOIN stock s " +
            "ON s.product_id = p.id AND s.deleted = 0 " +
            "WHERE p.deleted = 0 " +
            "ORDER BY p.id DESC")
    List<Stock> listStock();


}
