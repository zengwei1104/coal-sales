package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.dto.PaymentCreateDTO;
import com.nczy.coalsales.dto.PaymentWriteOffDTO;
import com.nczy.coalsales.entity.Payment;

import java.math.BigDecimal;

public interface PaymentService extends IService<Payment> {

    PageResult<Payment> pagePayment(Long current, Long size, Long customerId);

    Long createPayment(PaymentCreateDTO dto);

    void writeOff(Long paymentId, PaymentWriteOffDTO dto);

    BigDecimal getReceivable(Long customerId);
}
