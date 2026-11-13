package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.annotation.Log;
import com.nczy.coalsales.common.BusinessException;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.constant.ContractStatus;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.ContractDTO;
import com.nczy.coalsales.entity.Contract;
import com.nczy.coalsales.mapper.ContractMapper;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.ContractService;
import com.nczy.coalsales.service.SysDictService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract>
        implements ContractService {

    private final SysDictService sysDictService;

    public ContractServiceImpl( SysDictService sysDictService) {

        this.sysDictService = sysDictService;
    }
    private String genContractNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "HT" + date;
        String maxNo = baseMapper.selectMaxContractNo(prefix);
        int nextSeq = nextSeq(maxNo, prefix);
        return prefix + String.format("%03d", nextSeq);
    }

    // 从当天最大编号解析流水号并 +1，无记录则从 1 开始
    private int nextSeq(String maxNo, String prefix) {
        if (maxNo != null && maxNo.startsWith(prefix) && maxNo.length() > prefix.length()) {
            try {
                return Integer.parseInt(maxNo.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                // 编号格式异常时回退为 1
            }
        }
        return 1;
    }
    // ============ 归属校验 ============
    private void checkOwner(Contract contract, LoginUser loginUser) {
        boolean isAdminOrManager = RoleConstant.ADMIN.equals(loginUser.getRoleId())
                || RoleConstant.MANAGER.equals(loginUser.getRoleId());
        if (!isAdminOrManager) {
            if (contract.getCreateBy() == null
                    || !contract.getCreateBy().equals(loginUser.getUserId())) {
                throw new BusinessException("无权操作该合同：这不是您创建的合同");
            }
        }
    }

    // ============ 查询 ============
    @Override
    public PageResult<Contract> pageContract(Long current, Long size,
                                             Integer status, String keyword) {
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Contract::getStatus, status)
               .orderByDesc(Contract::getCreateTime);
        // keyword 可按合同编号模糊匹配，客户/煤种需要 join，这里先做编号
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Contract::getContractNo, keyword);
        }
        Page<Contract> page = page(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    public Contract getContract(Long id) {
        Contract contract = getById(id);
        if (contract == null) {
            throw new BusinessException("合同不存在或已被删除");
        }
        return contract;
    }

    // ============ 新增 ============
    @Override
    public void addContract(ContractDTO dto, LoginUser loginUser) {
        Contract contract = new Contract();
        contract.setCustomerId(dto.getCustomerId());
        contract.setProductId(dto.getProductId());
        contract.setSignDate(dto.getSignDate());
        contract.setStartDate(dto.getStartDate());
        contract.setEndDate(dto.getEndDate());
        contract.setTotalQty(dto.getTotalQty());
        contract.setPrice(dto.getPrice());
        contract.setQnetAr(dto.getQnetAr());
        contract.setShortRate(dto.getShortRate() == null ? new BigDecimal("5.00") : dto.getShortRate());
        contract.setPayType(dto.getPayType() == null ? 3 : dto.getPayType());
        contract.setCreditDays(dto.getCreditDays() == null ? 0 : dto.getCreditDays());
        contract.setTransportBear(dto.getTransportBear() == null ? 1 : dto.getTransportBear());
        // 税率：没传就走字典默认值，字典也没配就用 13.00
        contract.setTaxRate(dto.getTaxRate() == null
                ? sysDictService.getParamAsDecimal("default_tax_rate", new BigDecimal("13.00"))
                : dto.getTaxRate());
        // 新建即草稿
        contract.setStatus(ContractStatus.DRAFT);
        // 编号：HT + 年月日 + 3 位流水
        contract.setContractNo(genContractNo());

        save(contract);
    }



    // ============ 修改（仅草稿） ============
    @Override
    public void updateContract(Long id, ContractDTO dto, LoginUser loginUser) {
        Contract contract = getContract(id);
        // 状态机：仅草稿可改
        if (!ContractStatus.DRAFT.equals(contract.getStatus())) {
            throw new BusinessException("只有草稿状态的合同才能修改");
        }
        // 归属校验：销售员只能改自己的合同
        checkOwner(contract, loginUser);

        Contract update = new Contract();
        update.setId(id);
        update.setCustomerId(dto.getCustomerId());
        update.setProductId(dto.getProductId());
        update.setSignDate(dto.getSignDate());
        update.setStartDate(dto.getStartDate());
        update.setEndDate(dto.getEndDate());
        update.setTotalQty(dto.getTotalQty());
        update.setPrice(dto.getPrice());
        update.setTaxRate(dto.getTaxRate());
        update.setQnetAr(dto.getQnetAr());
        update.setShortRate(dto.getShortRate());
        update.setPayType(dto.getPayType());
        update.setCreditDays(dto.getCreditDays());
        update.setTransportBear(dto.getTransportBear());

        updateById(update);
    }

    // ============ 删除（仅草稿） ============
    @Override
    public void deleteContract(Long id, LoginUser loginUser) {
        Contract contract = getContract(id);
        if (!ContractStatus.DRAFT.equals(contract.getStatus())) {
            throw new BusinessException("只有草稿状态的合同才能删除");
        }
        checkOwner(contract, loginUser);
        removeById(id);
    }

    // ============ 提交审核（草稿 → 待审核） ============
    @Override
    public void submit(Long id, LoginUser loginUser) {
        Contract contract = getContract(id);
        if (!ContractStatus.DRAFT.equals(contract.getStatus())) {
            throw new BusinessException("只有草稿状态的合同才能提交审核");
        }
        checkOwner(contract, loginUser);

        Contract update = new Contract();
        update.setId(id);
        update.setStatus(ContractStatus.PENDING);
        updateById(update);
    }

    // ============ 审核（待审核 → 执行中 / 草稿） ============
    @Override
    @Log("审核合同")
    public void audit(Long id, boolean pass, String opinion, LoginUser loginUser) {
        Contract contract = getContract(id);
        if (!ContractStatus.PENDING.equals(contract.getStatus())) {
            throw new BusinessException("只有待审核状态的合同才能审核");
        }
        // 谁创建谁不能审核（需求 R33）
        if (contract.getCreateBy() != null
                && contract.getCreateBy().equals(loginUser.getUserId())) {
            throw new BusinessException("不能审核自己创建的合同");
        }

        Contract update = new Contract();
        update.setId(id);
        update.setAuditBy(loginUser.getUserId());
        update.setAuditTime(LocalDateTime.now());
        update.setStatus(pass ? ContractStatus.EXECUTING : ContractStatus.DRAFT);
        updateById(update);
    }


}
