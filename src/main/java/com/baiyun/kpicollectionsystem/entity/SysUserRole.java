package com.baiyun.kpicollectionsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class SysUserRole {
	@TableId(type = IdType.AUTO)
	private Integer id;
	private Integer userId;
	private Integer roleId;
}



