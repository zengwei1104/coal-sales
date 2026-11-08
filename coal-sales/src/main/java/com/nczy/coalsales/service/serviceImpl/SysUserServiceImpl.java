package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.annotation.Log;
import com.nczy.coalsales.common.BusinessException;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.dto.UpdatePasswordDTO;
import com.nczy.coalsales.dto.UpdateUserDTO;
import com.nczy.coalsales.dto.UserCreateDTO;
import com.nczy.coalsales.entity.SysUser;
import com.nczy.coalsales.mapper.SysUserMapper;
import com.nczy.coalsales.service.SysUserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordDTO dto) {
        // 1. 查用户
        SysUser user = getById(userId);
        if (user == null) {
            throw new BadCredentialsException("用户不存在");
        }

        // 2. 校验旧密码（BCrypt 必须用 matches，不能 equals）
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("旧密码错误");
        }

        // 3. 新旧不能相同（可选）
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("新密码不能与旧密码相同");
        }

        // 4. 加密新密码并更新（只更新 password 字段）
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        updateById(update);
    }

    @Override
    @Log("删除用户")
    public void deleteUser(Long id) {
        // 1. 查一下这个用户存不存在
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在或已被删除");
        }

        // 2. 保护内置管理员，不允许删除
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("系统内置管理员不可删除");
        }

        // 3. 逻辑删除（不是真的从数据库删，是把 deleted 置为 1）
        removeById(id);
    }

    @Override
    public PageResult<SysUser> pageUsers(Long current, Long size, String keyword) {
        // 按创建时间倒序
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getCreateTime);

        // 关键字：登录名 / 真实姓名 / 手机号 模糊匹配（外层加括号，避免 or 破坏其他条件）
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysUser::getUsername, kw)
                    .or().like(SysUser::getRealName, kw)
                    .or().like(SysUser::getPhone, kw));
        }

        Page<SysUser> page = page(new Page<>(current, size), wrapper);
        // 转成统一分页结构返回
        return PageResult.of(page);
    }

    @Override
    public void createUser(UserCreateDTO dto) {
        // 1. 校验用户名唯一
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        if (count(wrapper) > 0) {
            throw new BusinessException("登录名已存在");
        }

        // 2. 组装实体
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));  // 密码加密
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setRoleId(dto.getRoleId());
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());

        // 3. 保存
        save(user);
    }

    @Override
    public void updateUser(Long id, UpdateUserDTO dto) {
        // 1. 查用户是否存在
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在或已被删除");
        }

        // 2. 手机号唯一性校验（如果填了手机号，且排除自己）
        if (StringUtils.hasText(dto.getPhone())) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getPhone, dto.getPhone())
                    .ne(SysUser::getId, id);   // 排除自己
            if (count(wrapper) > 0) {
                throw new BusinessException("手机号已被使用");
            }
        }

        // 3. 组装要更新的字段（不含 username、password）
        SysUser update = new SysUser();
        update.setId(id);
        update.setRealName(dto.getRealName());
        update.setPhone(dto.getPhone());
        update.setRoleId(dto.getRoleId());
        update.setStatus(dto.getStatus());

        // 4. 更新（只更新非 null 字段）
        updateById(update);
    }


}