package com.nczy.coalsales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nczy.coalsales.entity.WeighTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WeighTicketMapper extends BaseMapper<WeighTicket> {

    // 需要自定义 SQL 时再补充，例如：
    // 统计某订单下已完成磅单的净重之和（用于累加订单 shippedQty）
    // @Select("SELECT IFNULL(SUM(net_weight),0) FROM weigh_ticket " +
    //         "WHERE order_id = #{orderId} AND status = 2 AND deleted = 0")
    // BigDecimal sumNetWeightByOrder(@Param("orderId") Long orderId);

    /**
     * 某合同下「已完成且未结算」的磅单（销售员结算用）
     * 未结算 = settlement_item 中不存在该 ticket_id
     */
    @Select("SELECT w.* FROM weigh_ticket w " +
            "LEFT JOIN settlement_item si ON si.ticket_id = w.id " +
            "WHERE w.status = 2 AND w.deleted = 0 AND si.id IS NULL " +
            "AND w.order_id IN (SELECT id FROM sale_order WHERE contract_id = #{contractId}) " +
            "ORDER BY w.create_time DESC")
    List<WeighTicket> settleable(@Param("contractId") Long contractId);
}
