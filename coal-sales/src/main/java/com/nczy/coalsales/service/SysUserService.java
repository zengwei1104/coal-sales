package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.dto.UpdatePasswordDTO;
import com.nczy.coalsales.dto.UpdateUserDTO;
import com.nczy.coalsales.dto.UserCreateDTO;
import com.nczy.coalsales.entity.SysUser;
import jakarta.validation.Valid;

public interface SysUserService extends IService<SysUser> {
    void updatePassword(Long userId,  @Valid UpdatePasswordDTO dto);

    void deleteUser(Long id);

    /**
     * 分页查询用户
     *
     * @param keyword 关键字，按登录名 / 真实姓名 / 手机号模糊匹配，可为空
     */
    PageResult<SysUser> pageUsers(Long current, Long size, String keyword);

    void createUser(@Valid UserCreateDTO dto);

    void updateUser(Long id, @Valid UpdateUserDTO dto);
}