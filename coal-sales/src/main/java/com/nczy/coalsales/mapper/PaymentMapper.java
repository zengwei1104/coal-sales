package com.nczy.coalsales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nczy.coalsales.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    /**
     * 查询并锁定回款记录，防止同一笔回款被并发超额核销。
     */
    @Select("SELECT * FROM payment WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    Payment selectByIdForUpdate(@Param("id") Long id);

    /**
     * 取指定前缀下最大的回款单号（用于“最大号 + 1”生成新号，避免删除后重复）。
     */
    @Select("SELECT MAX(payment_no) FROM payment WHERE payment_no LIKE CONCAT(#{prefix}, '%')")
    String selectMaxPaymentNo(@Param("prefix") String prefix);
}
