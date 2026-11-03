package com.nczy.coalsales.controller;

import com.nczy.coalsales.Util.AuthUtil;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.SysDictDTO;
import com.nczy.coalsales.entity.SysDict;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.SysDictService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典接口
 * <p>
 * 权限设计：<b>读放行、写限管理员</b>。
 * <ul>
 *   <li>GET /type/{type} 供前端下拉、业务参数展示，普通角色也要能访问，故不加管理员校验。</li>
 *   <li>POST/PUT/DELETE 为字典维护操作，仅系统管理员可执行。</li>
 * </ul>
 * 注意：业务代码读取参数（如净重区间、超发阈值）应直接调 SysDictService.getParam 系列方法走内存缓存，
 * 而非通过本 HTTP 接口，避免多余网络开销与认证拦截。
 */
@RestController
@RequestMapping("/api/dicts")
public class SysDictController {

    private final SysDictService sysDictService;

    public SysDictController(SysDictService sysDictService) {
        this.sysDictService = sysDictService;
    }

    /** 分页查询字典项（管理员） */
    @GetMapping
    public Result<PageResult<SysDict>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String dictType,
            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN);
        return Result.success(sysDictService.pageDict(current, size, dictType));
    }

    /** 按类型取全部键值（登录即可，前端下拉/参数展示用） */
    @GetMapping("/type/{type}")
    public Result<List<SysDict>> listByType(@PathVariable String type) {
        return Result.success(sysDictService.listByType(type));
    }

    /** 新增字典项（管理员） */
    @PostMapping
    public Result<Void> add(@RequestBody @Valid SysDictDTO dto,
                            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN);
        sysDictService.addDict(dto);
        return Result.success("添加成功", null);
    }

    /** 修改字典项（管理员） */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid SysDictDTO dto,
                               @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN);
        sysDictService.updateDict(id, dto);
        return Result.success("修改成功", null);
    }

    /** 删除字典项（管理员，逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN);
        sysDictService.deleteDict(id);
        return Result.success("删除成功", null);
    }
}
