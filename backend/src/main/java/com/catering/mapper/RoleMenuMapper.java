package com.catering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catering.entity.RoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
    @Select("SELECT menu_id FROM role_menu WHERE role = #{role}")
    List<Long> selectMenuIdsByRole(@Param("role") String role);

    @Delete("DELETE FROM role_menu WHERE role = #{role}")
    void deleteByRole(@Param("role") String role);
}