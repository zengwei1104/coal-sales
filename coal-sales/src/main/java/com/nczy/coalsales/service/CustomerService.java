package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.dto.CustomerDTO;
import com.nczy.coalsales.entity.Customer;
import com.nczy.coalsales.security.LoginUser;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public interface CustomerService extends IService<Customer> {

    void addCustomer(CustomerDTO dto);

    PageResult<Customer> pageCustomer(Long current, Long size);

    void deleteCustomer( long id);

    Customer getCustomer(long id);

    void updateCustomer(long id, @Valid CustomerDTO dto, LoginUser loginUser);
}
