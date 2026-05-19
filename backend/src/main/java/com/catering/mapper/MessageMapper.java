package com.catering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catering.entity.Message;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    @Select("SELECT * FROM message WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Message> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM message WHERE user_id = #{userId} AND is_read = FALSE")
    int countUnread(@Param("userId") Long userId);

    @Update("UPDATE message SET is_read = TRUE WHERE user_id = #{userId}")
    void markAsRead(@Param("userId") Long userId);

    @Delete("DELETE FROM message WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);
}