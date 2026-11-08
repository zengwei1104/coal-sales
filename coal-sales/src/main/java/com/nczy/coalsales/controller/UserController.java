package com.nczy.coalsales.controller;

import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.UpdatePasswordDTO;
import com.nczy.coalsales.dto.UpdateUserDTO;
import com.nczy.coalsales.dto.UserCreateDTO;
import com.nczy.coalsales.entity.SysUser;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.SysUserService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final SysUserService userService;

    private void checkAdmin(LoginUser loginUser) {
        if (loginUser == null || loginUser.getRoleId() == null
                || !RoleConstant.ADMIN.equals(loginUser.getRoleId())) {
            throw new AccessDeniedException("仅管理员可操作");
        }
    }


    public UserController(SysUserService userService) {
        this.userService = userService;
    }

    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(
            @PathVariable Long id,
            @RequestBody @Valid UpdatePasswordDTO dto,
            @AuthenticationPrincipal LoginUser loginUser) {

        // ① 身份校验：只能改自己（管理员改他人另开接口）
        if (!id.equals(loginUser.getUserId())) {
            return Result.error(403, "无权修改他人密码");
        }

        // ② 交给 Service 处理业务
        userService.updatePassword(loginUser.getUserId(), dto);

        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable("id") Long id,  @AuthenticationPrincipal LoginUser loginUser){

        // 管理员校验：只有 roleId == 1 才能删
        checkAdmin(loginUser);
        userService.deleteUser(id);
        return Result.success("删除成功", null);
    }
    @GetMapping
    public Result<PageResult<SysUser>> page(@RequestParam(defaultValue = "1") Long current,
                                            @RequestParam(defaultValue = "10") Long size,
                                            @RequestParam(required = false) String keyword,
                                            @AuthenticationPrincipal LoginUser loginUser) {
        checkAdmin(loginUser);
        return Result.success(userService.pageUsers(current, size, keyword));
    }
    @PostMapping
    public Result<Void> createUser(@RequestBody @Valid UserCreateDTO dto,@AuthenticationPrincipal LoginUser loginUser) {
        checkAdmin(loginUser);
        userService.createUser(dto);
        return Result.success("新增成功", null);   // data 为 null
    }
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id,
                                   @RequestBody @Valid UpdateUserDTO dto,
                                   @AuthenticationPrincipal LoginUser loginUser) {
        // 管理员校验
        checkAdmin(loginUser);
        userService.updateUser(id, dto);   // ← 这里补上 dto
        return Result.success("修改成功", null);
    }




}