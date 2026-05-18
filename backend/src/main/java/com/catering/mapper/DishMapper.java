package com.catering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catering.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
