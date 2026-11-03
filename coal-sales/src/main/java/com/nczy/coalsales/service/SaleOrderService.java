package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.dto.SaleOrderDTO;
import com.nczy.coalsales.dto.SaleOrderUpdateDTO;
import com.nczy.coalsales.entity.SaleOrder;
import com.nczy.coalsales.security.LoginUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SaleOrderService extends IService<SaleOrder> {

    PageResult<SaleOrder> pageOrder(Long current, Long size,
                                    Integer status, Long customerId, Long contractId, String keyword);

    SaleOrder getOrder(Long id, LoginUser loginUser);

    void addOrder(SaleOrderDTO dto, LoginUser loginUser);

    void updateOrder(Long id, SaleOrderUpdateDTO dto, LoginUser loginUser);

    void deleteOrder(Long id, LoginUser loginUser);

    void cancelOrder(Long id, LoginUser loginUser);

    List<SaleOrder> available();
}
