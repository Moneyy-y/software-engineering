package com.catering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catering.entity.UserBrowse;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserBrowseMapper extends BaseMapper<UserBrowse> {
    @Select("SELECT * FROM user_browse WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<UserBrowse> selectByUserIdOrderByTime(@Param("userId") Long userId);

    @Delete("DELETE FROM user_browse WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);
}