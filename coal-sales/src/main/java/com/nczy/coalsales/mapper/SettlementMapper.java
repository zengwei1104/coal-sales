package com.nczy.coalsales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nczy.coalsales.dto.CoalSalesRankDTO;
import com.nczy.coalsales.dto.CustomerRankDTO;
import com.nczy.coalsales.dto.PaymentRateDTO;
import com.nczy.coalsales.dto.SalesTrendDTO;
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

    /**
     * 销售趋势：按结算审核日聚合，统计每日已审核结算单的净重、金额、单数。
     * 结束日期边界由 Service 层补成「次日」，SQL 用 &lt; 次日以完整覆盖当天。
     */
    @Select("<script>" +
            "SELECT DATE_FORMAT(audit_time, '%Y-%m-%d') AS date, " +
            "       SUM(total_qty)    AS settleQty, " +
            "       SUM(total_amount) AS settleAmount, " +
            "       COUNT(*)          AS orderCount " +
            "FROM settlement " +
            "WHERE deleted = 0 AND status = 1 " +
            "<if test='startDate != null and startDate != \"\"'> AND audit_time &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND audit_time &lt; #{endDate} </if>" +
            "GROUP BY DATE_FORMAT(audit_time, '%Y-%m-%d') " +
            "ORDER BY date ASC" +
            "</script>")
    List<SalesTrendDTO> selectSalesTrend(@Param("startDate") String startDate,
                                         @Param("endDate") String endDate);

    /**
     * 煤炭销量排行：按煤种商品聚合销量（吨）与销售额（元）。
     * 只统计已审核结算单，支持按审核日区间过滤，按销量降序。
     */
    @Select("<script>" +
            "SELECT p.id AS productId, " +
            "       p.product_name AS productName, " +
            "       SUM(i.net_weight) AS totalQty, " +
            "       SUM(i.amount)     AS totalAmount " +
            "FROM settlement_item i " +
            "JOIN settlement s ON s.id = i.settle_id AND s.deleted = 0 AND s.status = 1 " +
            "JOIN coal_product p ON p.id = i.product_id AND p.deleted = 0 " +
            "<where>" +
            "<if test='startDate != null and startDate != \"\"'> AND s.audit_time &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND s.audit_time &lt; #{endDate} </if>" +
            "</where>" +
            "GROUP BY p.id, p.product_name " +
            "ORDER BY totalQty DESC" +
            "</script>")
    List<CoalSalesRankDTO> selectCoalSalesRank(@Param("startDate") String startDate,
                                               @Param("endDate") String endDate);


    @Select("<script>" +
            "SELECT c.id AS customerId, " +
            "       c.customer_name AS customerName, " +
            "       SUM(s.total_amount) AS Amount " +
            "FROM settlement s " +
            "JOIN customer c ON c.id = s.customer_id AND c.deleted = 0 " +
            "<where>" +
            "s.deleted = 0 AND s.status = 1 " +
            "<if test='startDate != null and startDate != \"\"'> AND s.audit_time &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND s.audit_time &lt; #{endDate} </if>" +
            "</where>" +
            "GROUP BY c.id, c.customer_name " +
            "ORDER BY Amount DESC" +
            "</script>")
    List<CustomerRankDTO> selectCustomerRank(@Param("startDate") String startDate,
                                             @Param("endDate") String endDate);

    /**
     * 回款率统计：应收 = 已审核结算总额（按审核日过滤），实收 = 回款总额（按付款时间过滤）。
     * 回款率 = 实收 / 应收，由 Service 层计算（避免 SQL 除零）。
     */
    @Select("<script>" +
            "SELECT " +
            "  IFNULL((SELECT SUM(total_amount) FROM settlement " +
            "          WHERE deleted = 0 AND status = 1 " +
            "          <if test='startDate != null and startDate != \"\"'> AND audit_time &gt;= #{startDate} </if>" +
            "          <if test='endDate != null and endDate != \"\"'> AND audit_time &lt; #{endDate} </if>), 0) AS receivable, " +
            "  IFNULL((SELECT SUM(amount) FROM payment " +
            "          WHERE deleted = 0 " +
            "          <if test='startDate != null and startDate != \"\"'> AND pay_time &gt;= #{startDate} </if>" +
            "          <if test='endDate != null and endDate != \"\"'> AND pay_time &lt; #{endDate} </if>), 0) AS received " +
            "</script>")
    PaymentRateDTO selectPaymentRate(@Param("startDate") String startDate,
                                     @Param("endDate") String endDate);
}
