package com.nczy.coalsales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nczy.coalsales.entity.WriteOff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface WriteOffMapper extends BaseMapper<WriteOff> {

    /**
     * 核销事务内使用锁定当前读，确保统计到最新已提交的核销明细。
     */
    @Select("SELECT amount FROM write_off WHERE payment_id = #{paymentId} FOR UPDATE")
    List<BigDecimal> listAmountsByPaymentId(@Param("paymentId") Long paymentId);

    @Select("SELECT amount FROM write_off WHERE settle_id = #{settleId} FOR UPDATE")
    List<BigDecimal> listAmountsBySettleId(@Param("settleId") Long settleId);
}
