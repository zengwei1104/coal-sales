package com.nczy.coalsales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nczy.coalsales.entity.Settlement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface SettlementMapper extends BaseMapper<Settlement> {

    /**
     * 查询并锁定结算单，保证核销与作废不会并发修改同一单据。
     */
    @Select("SELECT * FROM settlement WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    Settlement selectByIdForUpdate(@Param("id") Long id);

    /**
     * 客户应收 = 已审核结算金额 - 已核销金额，实时计算。
     */
    @Select("SELECT IFNULL(SUM(s.total_amount - IFNULL(w.received_amount, 0)), 0) " +
            "FROM settlement s " +
            "LEFT JOIN (SELECT settle_id, SUM(amount) AS received_amount " +
            "FROM write_off GROUP BY settle_id) w ON w.settle_id = s.id " +
            "WHERE s.customer_id = #{customerId} AND s.status = 1 AND s.deleted = 0")
    BigDecimal sumReceivableByCustomerId(@Param("customerId") Long customerId);

    /**
     * 某客户「已审核」结算单 + 每单已核销金额（receivedAmount 回填到实体的非表字段）
     * 剩余未收 = totalAmount - receivedAmount，由 Service 层计算
     */
    @Select("SELECT s.*, " +
            "IFNULL((SELECT SUM(w.amount) FROM write_off w WHERE w.settle_id = s.id), 0) AS received_amount " +
            "FROM settlement s " +
            "WHERE s.customer_id = #{customerId} AND s.status = 1 AND s.deleted = 0 " +
            "ORDER BY s.create_time DESC")
    List<Settlement> listWriteoffableByCustomer(@Param("customerId") Long customerId);
}
