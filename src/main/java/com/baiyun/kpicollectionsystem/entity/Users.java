package com.baiyun.kpicollectionsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class Users {
	@TableId(type = IdType.AUTO)
	private Integer id;
	private String username;
	private String password;
	private String employeeId;
	private String name;
	private String phone;
	private String department;
	private Integer status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}



