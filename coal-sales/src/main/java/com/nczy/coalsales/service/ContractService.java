package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.dto.ContractDTO;
import com.nczy.coalsales.entity.Contract;
import com.nczy.coalsales.security.LoginUser;

public interface ContractService extends IService<Contract> {

    PageResult<Contract> pageContract(Long current, Long size,
                                      Integer status, String keyword);

    Contract getContract(Long id);

    void addContract(ContractDTO dto, LoginUser loginUser);

    void updateContract(Long id, ContractDTO dto, LoginUser loginUser);

    void deleteContract(Long id, LoginUser loginUser);

    void submit(Long id, LoginUser loginUser);

    void audit(Long id, boolean pass, String opinion, LoginUser loginUser);
}
