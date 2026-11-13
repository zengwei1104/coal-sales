package com.nczy.coalsales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nczy.coalsales.entity.WeighTicket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WeighTicketMapper extends BaseMapper<WeighTicket> {

    /**
     * 锁定并读取磅单。
     * 必须在事务中调用，所有会修改磅单状态的流程都必须先通过此方法加锁。
     */
    @Select("SELECT * FROM weigh_ticket WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    WeighTicket selectByIdForUpdate(@Param("id") Long id);

    /**
     * 完成过磅：仅允许从“已称皮重(1)”流转到“已完成(2)”。
     */
    @Update("UPDATE weigh_ticket " +
            "SET gross_weight = #{grossWeight}, gross_time = #{grossTime}, " +
            "    net_weight = #{netWeight}, status = 2, update_time = NOW() " +
            "WHERE id = #{id} AND status = 1 AND deleted = 0")
    int finishWeighing(@Param("id") Long id,
                       @Param("grossWeight") BigDecimal grossWeight,
                       @Param("netWeight") BigDecimal netWeight,
                       @Param("grossTime") LocalDateTime grossTime);

    /**
     * 作废磅单：只有当前状态仍与读取时一致才允许更新，防止重复作废。
     */
    @Update("UPDATE weigh_ticket " +
            "SET status = 3, cancel_reason = #{cancelReason}, update_time = NOW() " +
            "WHERE id = #{id} AND status = #{currentStatus} AND deleted = 0")
    int cancelIfStatus(@Param("id") Long id,
                       @Param("currentStatus") Integer currentStatus,
                       @Param("cancelReason") String cancelReason);

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

    /**
     * 取指定前缀下最大的磅单号（用于“最大号 + 1”生成新号，避免删除后重复）。
     */
    @Select("SELECT MAX(weight_no) FROM weigh_ticket WHERE weight_no LIKE CONCAT(#{prefix}, '%')")
    String selectMaxWeightNo(@Param("prefix") String prefix);
}
