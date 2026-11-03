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
}
