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

	@Select("select submitter_name as submitterName, coalesce(sum(score),0) as score from research_achievements where status=2 group by submitter_name")
	List<Map<String, Object>> selectSubmitterStats();
}


