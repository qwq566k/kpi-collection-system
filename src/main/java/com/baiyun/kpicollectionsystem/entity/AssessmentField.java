package com.baiyun.kpicollectionsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assessment_fields")
public class AssessmentField {
	@TableId(type = IdType.AUTO)
	private Integer id;
	private String name;
	private Integer score;
	private Integer status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}



