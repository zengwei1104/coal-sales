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

    PageResult<SysUser> pageUsers(Long current, Long size);

    void createUser(@Valid UserCreateDTO dto);

    void updateUser(Long id, @Valid UpdateUserDTO dto);
}