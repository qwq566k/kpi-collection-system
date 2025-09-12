package com.baiyun.kpicollectionsystem.service.impl;

import com.baiyun.kpicollectionsystem.common.Result;
import com.baiyun.kpicollectionsystem.entity.ScoreStandard;
import com.baiyun.kpicollectionsystem.entity.Users;
import com.baiyun.kpicollectionsystem.mapper.ScoreStandardMapper;
import com.baiyun.kpicollectionsystem.mapper.UsersMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baiyun.kpicollectionsystem.entity.ResearchAchievement;
import com.baiyun.kpicollectionsystem.mapper.ResearchAchievementMapper;
import com.baiyun.kpicollectionsystem.service.ResearchAchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResearchAchievementServiceImpl implements ResearchAchievementService {

	private final ResearchAchievementMapper mapper;
	private final ScoreStandardMapper standardMapper;

	@Autowired
	private UsersMapper usersMapper;
	public ResearchAchievementServiceImpl(ResearchAchievementMapper mapper, ScoreStandardMapper standardMapper) {
		this.mapper = mapper;
        this.standardMapper = standardMapper;
    }

	@Override
	@Transactional
	public void submit(ResearchAchievement ra, boolean asDraft) {
		Users users = usersMapper.selectById(ra.getSubmitterId());
		ra.setDepartment(users.getDepartment());
		//补充信息
		ScoreStandard standard = standardMapper.selectById(ra.getStandardId());
		String scoringInstruction = standardMapper.selectInstructionById(ra.getStandardId());
		String assessmentOrg = standardMapper.selectOrgById(ra.getStandardId());
		ra.setScoringInstruction(scoringInstruction);
		ra.setAssessmentOrg(assessmentOrg);
		ra.setScore(standard.getScore());

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



