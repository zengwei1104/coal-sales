package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.entity.SysLog;
import com.nczy.coalsales.mapper.SysLogMapper;
import com.nczy.coalsales.service.SysLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日志服务实现
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog>
        implements SysLogService {

    @Override
    public PageResult<SysLog> pageLogs(Long current, Long size,
                                       String username, String operation,
                                       String startDate, String endDate) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysLog::getUsername, username)
               .like(StringUtils.hasText(operation), SysLog::getOperation, operation)
               .ge(StringUtils.hasText(startDate), SysLog::getCreateTime, startDate + " 00:00:00")
               .le(StringUtils.hasText(endDate), SysLog::getCreateTime, endDate + " 23:59:59")
               .orderByDesc(SysLog::getCreateTime);

        Page<SysLog> page = page(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }
}
