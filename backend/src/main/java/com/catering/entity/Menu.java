package com.catering.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("menu")
public class Menu {
    @TableId(type = IdType.AUTO)
    private Long menuId;
    private String name;
    private String path;
    private String icon;
    private Long parentId;
    private Integer sortOrder;
    private String roles;
    private Integer status;
}