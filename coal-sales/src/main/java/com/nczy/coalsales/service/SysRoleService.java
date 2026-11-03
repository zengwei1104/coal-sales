package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {
     List<SysRole> getRoles();
}
