package com.catering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catering.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
    @Select("SELECT DISTINCT m.* FROM menu m " +
            "JOIN role_menu rm ON m.menu_id = rm.menu_id " +
            "WHERE rm.role = #{role} AND m.status = 1 ORDER BY m.sort_order")
    List<Menu> selectMenusByRole(@Param("role") String role);

    @Select("SELECT * FROM menu WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort_order")
    List<Menu> selectByParentId(@Param("parentId") Long parentId);
}