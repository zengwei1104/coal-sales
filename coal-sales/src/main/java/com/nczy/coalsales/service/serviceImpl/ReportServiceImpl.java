package com.nczy.coalsales.service.serviceImpl;

import com.nczy.coalsales.dto.CoalSalesRankDTO;
import com.nczy.coalsales.dto.CustomerRankDTO;
import com.nczy.coalsales.dto.PaymentRateDTO;
import com.nczy.coalsales.dto.SalesTrendDTO;
import com.nczy.coalsales.dto.StockWarningDTO;
import com.nczy.coalsales.mapper.SettlementMapper;
import com.nczy.coalsales.mapper.StockMapper;
import com.nczy.coalsales.service.ReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    private final SettlementMapper settlementMapper;
    private final StockMapper stockMapper;

    public ReportServiceImpl(SettlementMapper settlementMapper, StockMapper stockMapper) {
        this.settlementMapper = settlementMapper;
        this.stockMapper = stockMapper;
    }

    // ============ 销售趋势（按结算审核日聚合） ============
    @Override
    public List<SalesTrendDTO> salesTrend(String startDate, String endDate) {
        endDate = normalizeEndDate(endDate);
        return settlementMapper.selectSalesTrend(startDate, endDate);
    }

    // ============ 煤炭销量排行（按煤种商品聚合） ============
    @Override
    public List<CoalSalesRankDTO> coalSalesRank(String startDate, String endDate) {
        endDate = normalizeEndDate(endDate);
        return settlementMapper.selectCoalSalesRank(startDate, endDate);
    }

    // ============ 客户销售额排行 ============
    @Override
    public List<CustomerRankDTO> customerRank(String startDate, String endDate) {
        endDate = normalizeEndDate(endDate);
        return settlementMapper.selectCustomerRank(startDate, endDate);
    }

    // ============ 回款率统计 ============
    @Override
    public PaymentRateDTO paymentRate(String startDate, String endDate) {
        endDate = normalizeEndDate(endDate);
        PaymentRateDTO dto = settlementMapper.selectPaymentRate(startDate, endDate);
        if (dto == null) {
            dto = new PaymentRateDTO();
            dto.setReceivable(BigDecimal.ZERO);
            dto.setReceived(BigDecimal.ZERO);
        }
        BigDecimal receivable = dto.getReceivable() == null ? BigDecimal.ZERO : dto.getReceivable();
        BigDecimal received = dto.getReceived() == null ? BigDecimal.ZERO : dto.getReceived();
        // 回款率 = 实收 / 应收 * 100，应收为 0 时回款率为 0（避免除零）
        BigDecimal rate = BigDecimal.ZERO;
        if (receivable.compareTo(BigDecimal.ZERO) > 0) {
            rate = received.multiply(BigDecimal.valueOf(100))
                    .divide(receivable, 2, RoundingMode.HALF_UP);
        }
        dto.setReceivable(receivable.setScale(2, RoundingMode.HALF_UP));
        dto.setReceived(received.setScale(2, RoundingMode.HALF_UP));
        dto.setRate(rate);
        return dto;
    }

    // ============ 库存预警 ============
    @Override
    public List<StockWarningDTO> stockWarning() {
        return stockMapper.selectStockWarning();
    }

    /**
     * 结束日期边界处理：endDate 传 "2026-11-06"，补成 "2026-11-07"，
     * SQL 用 &lt; 次日，即可完整覆盖当天 00:00:00 ~ 23:59:59。
     */
    private String normalizeEndDate(String endDate) {
        if (endDate != null && !endDate.isBlank()) {
            LocalDate end = LocalDate.parse(endDate);
            return end.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return null;
    }
}
