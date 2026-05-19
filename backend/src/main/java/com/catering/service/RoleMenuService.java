package com.catering.service;

import com.catering.entity.RoleMenu;
import com.catering.mapper.RoleMenuMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleMenuService {
    private final RoleMenuMapper roleMenuMapper;

    public RoleMenuService(RoleMenuMapper roleMenuMapper) {
        this.roleMenuMapper = roleMenuMapper;
    }

    public List<Long> getMenuIdsByRole(String role) {
        return roleMenuMapper.selectMenuIdsByRole(role);
    }

    public void assignMenusToRole(String role, List<Long> menuIds) {
        roleMenuMapper.deleteByRole(role);
        for (Long menuId : menuIds) {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setRole(role);
            roleMenu.setMenuId(menuId);
            roleMenuMapper.insert(roleMenu);
        }
    }
}