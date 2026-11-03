package com.nczy.coalsales.constant;

/**
 * 磅单状态常量（对应 weigh_ticket.status）
 */
public class WeighTicketStatus {

    public static final Integer WAIT_TARE   = 0;  // 待称皮重
    public static final Integer TARED       = 1;  // 已称皮重
    public static final Integer FINISHED    = 2;  // 已完成
    public static final Integer CANCELLED   = 3;  // 已作废
}
