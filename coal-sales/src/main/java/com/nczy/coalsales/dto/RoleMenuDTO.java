package com.nczy.coalsales.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色分配菜单入参
 */
@Data
public class RoleMenuDTO {

    /** 菜单 id 列表 */
    private List<Long> menuIds;
}
