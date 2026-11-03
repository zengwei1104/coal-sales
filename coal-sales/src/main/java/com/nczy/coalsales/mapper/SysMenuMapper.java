package com.nczy.coalsales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nczy.coalsales.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询某角色可访问的菜单（启用状态，且未逻辑删除）
     * 按 sort_order 升序返回
     */
    @Select("SELECT m.path FROM sys_menu m " +
            "JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id = #{roleId} " +
            "AND m.status = 1 AND m.deleted = 0 " +
            "ORDER BY m.sort_order ASC")
    List<String> selectMenuPathsByRoleId(@Param("roleId") Long roleId);

}
