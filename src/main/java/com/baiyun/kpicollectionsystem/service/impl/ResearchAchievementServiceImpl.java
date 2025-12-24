package com.baiyun.kpicollectionsystem.service.impl;

import com.baiyun.kpicollectionsystem.common.Result;
import com.baiyun.kpicollectionsystem.entity.AssessmentField;
import com.baiyun.kpicollectionsystem.entity.ScoreStandard;
import com.baiyun.kpicollectionsystem.entity.Users;
import com.baiyun.kpicollectionsystem.mapper.AssessmentFieldMapper;
import com.baiyun.kpicollectionsystem.mapper.ScoreStandardMapper;
import com.baiyun.kpicollectionsystem.mapper.UsersMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baiyun.kpicollectionsystem.entity.ResearchAchievement;
import com.baiyun.kpicollectionsystem.mapper.ResearchAchievementMapper;
import com.baiyun.kpicollectionsystem.service.ResearchAchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ResearchAchievementServiceImpl implements ResearchAchievementService {

	private final ResearchAchievementMapper mapper;
	private final ScoreStandardMapper standardMapper;
	private final AssessmentFieldMapper assessmentFieldMapper;

	@Autowired
	private UsersMapper usersMapper;
	public ResearchAchievementServiceImpl(ResearchAchievementMapper mapper, ScoreStandardMapper standardMapper, AssessmentFieldMapper assessmentFieldMapper) {
		this.mapper = mapper;
        this.standardMapper = standardMapper;
        this.assessmentFieldMapper = assessmentFieldMapper;
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
		List<String> authoritys = SecurityContextHolder.getContext()
				.getAuthentication()
				.getAuthorities()
				.stream()
				.map(GrantedAuthority::getAuthority)
				.toList();
		//判断用户
		if(authoritys.contains("ROLE_teacher")){
			if (!(db.getStatus() == 0 || db.getStatus() == 3)) {
				throw new IllegalArgumentException("仅草稿或已退回可修改");
			}
		}
		if(authoritys.contains("ROLE_admin")){
			if (!(db.getStatus() == 1 || db.getStatus() == 2)) {
				throw new IllegalArgumentException("仅审核或通过可修改");
			}
			//判断评价标准有没有改
			if (!Objects.equals(ra.getStandardId(), db.getStandardId())) {
				//更新为当前分数
				int curScore = standardMapper.selectById(ra.getStandardId()).getScore();
				ra.setScore(curScore);
				if (db.getStatus() == 2){
					//更新领域分数
					assessmentFieldMapper.minusScoreById(db.getFieldId(), db.getScore());
					assessmentFieldMapper.plusScoreById(ra.getFieldId(), curScore);
				}

			}
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

	@Override
	@Transactional
	public void deleteRecord(Integer id) {
		//查询出成果记录和考核领域
		ResearchAchievement researchAchievement = mapper.selectById(id);
		AssessmentField assessmentField = assessmentFieldMapper.selectById(researchAchievement.getFieldId());
		//更新成果记录状态为 4 ，4代表删除
		researchAchievement.setStatus(4);
		//更新考核领域存的分数
		assessmentField.setScore(assessmentField.getScore() - researchAchievement.getScore());
		//更新数据库
		assessmentFieldMapper.updateById(assessmentField);
		mapper.updateById(researchAchievement);
	}
}



