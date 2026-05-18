package com.catering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catering.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
