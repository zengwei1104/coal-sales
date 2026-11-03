package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.dto.SettlementAuditDTO;
import com.nczy.coalsales.dto.SettlementCreateDTO;
import com.nczy.coalsales.entity.Settlement;
import com.nczy.coalsales.entity.SettlementItem;
import com.nczy.coalsales.security.LoginUser;

import java.util.List;

public interface SettlementService extends IService<Settlement> {

    PageResult<Settlement> pageSettlement(Long current, Long size,
                                          Integer status, Long customerId);

    Settlement getSettlement(Long id);

    List<SettlementItem> getItems(Long settleId);

    void createSettlement(SettlementCreateDTO dto, LoginUser loginUser);

    void audit(Long id, SettlementAuditDTO dto, LoginUser loginUser);

    void cancelSettlement(Long id, LoginUser loginUser);

    /** 某客户「已审核且未核销完」的结算单（含剩余未收金额），回款核销用 */
    List<Settlement> listWriteoffable(Long customerId);
}
