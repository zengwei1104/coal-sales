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

}
