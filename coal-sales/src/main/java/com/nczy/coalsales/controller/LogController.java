package com.nczy.coalsales.controller;

import com.nczy.coalsales.Util.AuthUtil;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.entity.SysLog;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.SysLogService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志接口
 */
@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final SysLogService sysLogService;

    public LogController(SysLogService sysLogService) {
        this.sysLogService = sysLogService;
    }

    /**
     * 操作日志分页（仅管理员）
     */
    @GetMapping
    public Result<PageResult<SysLog>> page(@RequestParam(defaultValue = "1") Long current,
                                           @RequestParam(defaultValue = "10") Long size,
                                           @RequestParam(required = false) String username,
                                           @RequestParam(required = false) String operation,
                                           @RequestParam(required = false) String startDate,
                                           @RequestParam(required = false) String endDate,
                                           @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN);
        return Result.success(sysLogService.pageLogs(current, size, username, operation, startDate, endDate));
    }
}
