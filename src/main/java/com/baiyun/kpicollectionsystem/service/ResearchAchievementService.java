package com.baiyun.kpicollectionsystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baiyun.kpicollectionsystem.entity.ResearchAchievement;

public interface ResearchAchievementService {
	void submit(ResearchAchievement ra, boolean asDraft);
	void updateRecord(ResearchAchievement ra);
	ResearchAchievement findById(Integer id);
	IPage<ResearchAchievement> queryBySubmitter(Integer submitterId, int page, int size);
}



