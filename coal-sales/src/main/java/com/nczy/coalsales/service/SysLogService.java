package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.entity.SysLog;

/**
 * 操作日志服务
 */
public interface SysLogService extends IService<SysLog> {

    /**
     * 分页查询操作日志，可按用户名/操作内容/时间范围筛选
     */
    PageResult<SysLog> pageLogs(Long current, Long size,
                                String username, String operation,
                                String startDate, String endDate);
}
