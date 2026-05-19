package com.catering.controller;

import com.catering.common.Result;
import com.catering.entity.Menu;
import com.catering.entity.User;
import com.catering.service.MenuService;
import com.catering.service.RoleMenuService;
import com.catering.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {
    private final MenuService menuService;
    private final RoleMenuService roleMenuService;
    private final UserService userService;

    public PermissionController(MenuService menuService, RoleMenuService roleMenuService, UserService userService) {
        this.menuService = menuService;
        this.roleMenuService = roleMenuService;
        this.userService = userService;
    }

    @GetMapping("/menus")
    public Result<List<Menu>> getMenusByRole() {
        User user = userService.getCurrentUser();
        if (user == null) {
            return Result.fail(1004, "用户不存在，请重新登录");
        }
        String role = user.getRole();
        return Result.ok(menuService.getMenusByRole(role));
    }

    @GetMapping("/menus/all")
    public Result<List<Menu>> getAllMenus() {
        return Result.ok(menuService.getAllMenus());
    }

    @GetMapping("/menus/sub/{parentId}")
    public Result<List<Menu>> getSubMenus(@PathVariable Long parentId) {
        return Result.ok(menuService.getSubMenus(parentId));
    }

    @PostMapping("/menus")
    public Result<Void> addMenu(@RequestBody Menu menu) {
        menuService.addMenu(menu);
        return Result.ok();
    }

    @PutMapping("/menus")
    public Result<Void> updateMenu(@RequestBody Menu menu) {
        menuService.updateMenu(menu);
        return Result.ok();
    }

    @DeleteMapping("/menus/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.ok();
    }

    @GetMapping("/role/menus/{role}")
    public Result<List<Long>> getRoleMenuIds(@PathVariable String role) {
        return Result.ok(roleMenuService.getMenuIdsByRole(role));
    }

    @PostMapping("/role/menus/{role}")
    public Result<Void> assignMenusToRole(@PathVariable String role, @RequestBody List<Long> menuIds) {
        roleMenuService.assignMenusToRole(role, menuIds);
        return Result.ok();
    }

    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentPermission() {
        String role = userService.getCurrentUser().getRole();
        List<Menu> menus = menuService.getMenusByRole(role);
        
        Map<String, Object> result = new HashMap<>();
        result.put("role", role);
        result.put("menus", menus);
        return Result.ok(result);
    }
}