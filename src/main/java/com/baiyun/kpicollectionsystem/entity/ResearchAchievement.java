package com.baiyun.kpicollectionsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("research_achievements")
public class ResearchAchievement {
	@TableId(type = IdType.AUTO)
	private Integer id;
	private Integer year;
	private String department;
	private Integer submitterId;
	private String submitterName;
	private String achievementName;
	private Integer fieldId;
	private String fieldName;
	private Integer indicatorId;
	private String indicatorName;
	private Integer standardId;
	private String standardName;
	private String teamMembers;
	private LocalDate obtainDate;
	private String evidenceFile;
	private Integer score;
	private Integer status;
	private String rejectReason;
	private Integer reviewerId;
	private String reviewerName;
	private LocalDateTime reviewedAt;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}



