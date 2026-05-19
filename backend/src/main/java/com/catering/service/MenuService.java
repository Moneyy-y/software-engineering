package com.catering.service;

import com.catering.entity.Menu;
import com.catering.mapper.MenuMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {
    private final MenuMapper menuMapper;

    public MenuService(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    public List<Menu> getMenusByRole(String role) {
        return menuMapper.selectMenusByRole(role);
    }

    public List<Menu> getSubMenus(Long parentId) {
        return menuMapper.selectByParentId(parentId);
    }

    public List<Menu> getAllMenus() {
        return menuMapper.selectList(null);
    }

    public void addMenu(Menu menu) {
        menuMapper.insert(menu);
    }

    public void updateMenu(Menu menu) {
        menuMapper.updateById(menu);
    }

    public void deleteMenu(Long menuId) {
        menuMapper.deleteById(menuId);
    }
}