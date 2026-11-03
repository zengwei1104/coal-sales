package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.dto.WeighTicketCancelDTO;
import com.nczy.coalsales.dto.WeighTicketGrossDTO;
import com.nczy.coalsales.dto.WeighTicketTareDTO;
import com.nczy.coalsales.entity.WeighTicket;
import com.nczy.coalsales.security.LoginUser;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface WeighTicketService extends IService<WeighTicket> {
    PageResult<WeighTicket> page(Long current, Long size, Integer status,Long orderId, String plateNo);

    void addWeighTicket(@Valid WeighTicketTareDTO dto, LoginUser loginUser);

    void addTareWeight(long id, @Valid WeighTicketTareDTO dto);

    void addGrossWeight(long id, @Valid WeighTicketGrossDTO dto);

    void cancelWeighTicket(Long id, WeighTicketCancelDTO dto);

    List<WeighTicket> available();

    /** 某合同下「已完成且未结算」的磅单（生成结算单时选磅单用） */
    List<WeighTicket> settleable(Long contractId);
}
