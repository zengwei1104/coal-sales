package com.nczy.coalsales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nczy.coalsales.entity.Contract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ContractMapper extends BaseMapper<Contract> {

    /**
     * 取指定前缀下最大的合同号（用于“最大号 + 1”生成新号，避免删除后重复）。
     */
    @Select("SELECT MAX(contract_no) FROM contract WHERE contract_no LIKE CONCAT(#{prefix}, '%')")
    String selectMaxContractNo(@Param("prefix") String prefix);
}
