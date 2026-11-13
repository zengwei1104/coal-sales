package com.nczy.coalsales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nczy.coalsales.entity.SaleOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface SaleOrderMapper extends BaseMapper<SaleOrder> {

    /**
     * 统计某合同下非取消订单的累计计划量（用于判断合同是否用满）
     * 注意：排除已取消（status=4）和逻辑删除（deleted=0 由 MyBatis-Plus 自动追加）
     */
    @Select("SELECT IFNULL(SUM(plan_qty), 0) FROM sale_order " +
            "WHERE contract_id = #{contractId} AND status <> 4 AND deleted = 0")
    BigDecimal sumPlanQtyByContract(@Param("contractId") Long contractId);

    /**
     * 锁定并读取订单，保证同一订单的并发发运/作废串行计算已发运量。
     */
    @Select("SELECT * FROM sale_order WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    SaleOrder selectByIdForUpdate(@Param("id") Long id);

    @Select("SELECT IFNULL(SUM(shipped_qty), 0) FROM sale_order " +
            "WHERE contract_id = #{contractId} AND status <> 4 AND deleted = 0")
    BigDecimal sumShippedQtyByContract(@Param("contractId") Long contractId);

    /**
     * 取指定前缀下最大的订单号（用于“最大号 + 1”生成新号，避免删除后重复）。
     * 不排除逻辑删除记录，因为唯一索引仍占用旧号，跳号是安全且可接受的。
     */
    @Select("SELECT MAX(order_no) FROM sale_order WHERE order_no LIKE CONCAT(#{prefix}, '%')")
    String selectMaxOrderNo(@Param("prefix") String prefix);
}
