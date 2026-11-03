package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.entity.SysRole;
import com.nczy.coalsales.mapper.SysRoleMapper;
import com.nczy.coalsales.service.SysRoleService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {


    @Override
    public List<SysRole> getRoles() {
            return this.list();
    }
}
