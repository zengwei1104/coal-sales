package com.nczy.coalsales.constant;

/**
 * 订单状态常量（对应 sale_order.status）
 */
public class SaleOrderStatus {
    public static final Integer WAIT_SHIP    = 1;  // 待发运
    public static final Integer PART_SHIPPED = 2;  // 部分发运
    public static final Integer FINISHED     = 3;  // 已完成
    public static final Integer CANCELLED    = 4;  // 已取消
}
