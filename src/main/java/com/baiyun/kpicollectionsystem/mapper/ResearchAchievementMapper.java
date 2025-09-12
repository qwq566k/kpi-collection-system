package com.baiyun.kpicollectionsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baiyun.kpicollectionsystem.entity.ResearchAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ResearchAchievementMapper extends BaseMapper<ResearchAchievement> {

	@Select("select name as fieldName, score from assessment_fields  group by name")
	List<Map<String, Object>> selectAreaStats();

	@Select("SELECT u.name as submitterName, COALESCE(SUM(ra.score), 0) as score " +
			"FROM users u " +
			"LEFT JOIN research_achievements ra ON u.id = ra.submitter_id AND ra.status = 2 " +
			"GROUP BY u.id, u.name " +
			"ORDER BY score DESC, u.name")
	List<Map<String, Object>> selectSubmitterStats();
}


