package com.baiyun.kpicollectionsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baiyun.kpicollectionsystem.entity.ResearchAchievement;
import com.baiyun.kpicollectionsystem.mapper.ResearchAchievementMapper;
import com.baiyun.kpicollectionsystem.service.ResearchAchievementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResearchAchievementServiceImpl implements ResearchAchievementService {

	private final ResearchAchievementMapper mapper;

	public ResearchAchievementServiceImpl(ResearchAchievementMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	@Transactional
	public void submit(ResearchAchievement ra, boolean asDraft) {
		ra.setStatus(asDraft ? 0 : 1);
		if (ra.getId() == null) {
			mapper.insert(ra);
		} else {
			mapper.updateById(ra);
		}
	}

	@Override
	@Transactional
	public void updateRecord(ResearchAchievement ra) {
		ResearchAchievement db = mapper.selectById(ra.getId());
		if (db == null) throw new IllegalArgumentException("记录不存在");
		if (!(db.getStatus() == 0 || db.getStatus() == 3)) {
			throw new IllegalArgumentException("仅草稿或已退回可修改");
		}
		mapper.updateById(ra);
	}

	@Override
	public ResearchAchievement findById(Integer id) {
		return mapper.selectById(id);
	}

	@Override
	public IPage<ResearchAchievement> queryBySubmitter(Integer submitterId, int page, int size) {
		Page<ResearchAchievement> p = new Page<>(page, size);
		return mapper.selectPage(p, new LambdaQueryWrapper<ResearchAchievement>()
				.eq(ResearchAchievement::getSubmitterId, submitterId)
				.orderByDesc(ResearchAchievement::getCreatedAt));
	}
}



