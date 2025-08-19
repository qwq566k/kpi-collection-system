package com.baiyun.kpicollectionsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("assessment_relations")
public class AssessmentRelation {
	@TableId(type = IdType.AUTO)
	private Integer id;
	private Integer fieldId;
	private Integer indicatorId;
	private Integer standardId;
	private Integer orgId;
	private LocalDateTime createdAt;
}




