package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.entity.SysMenu;
import com.nczy.coalsales.mapper.SysMenuMapper;
import com.nczy.coalsales.service.SysMenuService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
        implements SysMenuService {

    @Override
    public List<SysMenu> tree() {
        // 1. 查全量启用且未删除的菜单，按 sortOrder 升序
        List<SysMenu> all = list(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSortOrder));

        // 2. 按 parentId 分组
        Map<Long, List<SysMenu>> byParent = all.stream()
                .collect(Collectors.groupingBy(m ->
                        m.getParentId() == null ? 0L : m.getParentId()));

        // 3. 递归组装树：顶级（parentId=0）为根
        return buildTree(0L, byParent);
    }

    /**
     * 递归组装：以 parentId 为根的子树
     */
    private List<SysMenu> buildTree(Long parentId, Map<Long, List<SysMenu>> byParent) {
        List<SysMenu> children = byParent.get(parentId);
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }
        for (SysMenu menu : children) {
            List<SysMenu> sub = buildTree(menu.getId(), byParent);
            menu.setChildren(sub.isEmpty() ? null : sub);
        }
        return children;
    }
}
