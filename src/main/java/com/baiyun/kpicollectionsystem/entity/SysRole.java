package com.baiyun.kpicollectionsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_role")
public class SysRole {
	@TableId(type = IdType.AUTO)
	private Integer id;
	private String code;
	private String name;
	private String description;
}



